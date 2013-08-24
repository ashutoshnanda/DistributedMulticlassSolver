package mover;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import peersim.config.*;
import peersim.core.*;
import Jama.Matrix;

public class LearnInitializer implements Control {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String PAR_TYPE = "type";

    /**
     * The protocol to operate on.
     * 
     * @config
     */
    private static final String PAR_PROT = "protocol";
    
    private static final String PAR_FILE = "file";

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------
    
    private final String type;

    private final int pid;
    
    public static int numClasses;
    
    public static int n;

	public static double lambda = Math.pow(10, -4);
	
	public static Matrix optimal;
	
	public static String file;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Creates a new instance and read parameters from the config file.
     */
    public LearnInitializer(String prefix) {
        type = Configuration.getString(prefix + "." + PAR_TYPE);
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
        file = Configuration.getString(prefix + "." + PAR_FILE);
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    public boolean execute() {
    	ArrayList<ArrayList> trainset = getSet();
    	this.optimal = getClassifier(trainset);
    	System.out.printf("Optimal Accuracy: %f\n", getAccuracy(this.optimal));
        for (int i = 0; i < Network.size(); i++) {
            Move nodeValue = (Move) Network.get(i).getProtocol(pid);
            nodeValue.resetExamples();
            nodeValue.counts = new int[numClasses];
            Matrix val = null;
            if(type.equals("uniform")) {
            	val = getClassifier(trainset);
            	nodeValue.setNumTrained(trainset.size());
            	for(ArrayList example : trainset) {
            		nodeValue.counts[(int) example.get(1)]++;
            	}
            } else if(type.equals("kth")) {
            	ArrayList<ArrayList> smallset = new ArrayList<ArrayList>();
            	for(int j = 0; j < trainset.size(); j++) {
            		if(j % Network.size() == i) {
            			//System.out.printf("Example %d for Node %d\n", j, i);
            			smallset.add(trainset.get(j));
                		nodeValue.counts[(int) trainset.get(j).get(1)]++;
            		}
            	}
            	val = getClassifier(smallset);
            	nodeValue.setNumTrained(smallset.size());
            } else {
            	System.err.printf("LearnInitializer: UNABLE TO PARSE LEARN TYPE \"%s\"!\n", type);
            	System.exit(1);
            }
            nodeValue.setMatrix(val);
        }
        return false;
    }
    
    public static ArrayList<ArrayList> getSet() {
    	ArrayList trainingset = new ArrayList();
		File inputData = new File(file);
		Scanner scnr = null;
		try {
			scnr = new Scanner(inputData);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.printf("LearnInitializer: UNABLE TO LOAD FILE %s!\n", file);
			System.exit(1);
		}
		if(file.contains("letter")) {
			numClasses = 26;
			while (scnr.hasNextLine()) {
				String in = scnr.nextLine();
				if (in.trim().length() != 0) {
					String[] split = in.split(" ").length > in.split(",").length ? in.split(" ") : in.split(",");
					double[] vector = new double[split.length - 1];
					int type = ((int) split[0].charAt(0)) - 65;
					boolean allpresent = true;
					for (int i = 1; i < split.length; i++) {
						String splitpart = split[i];
						if (splitpart.equals("?")) {
							allpresent &= false;
						} else {
							vector[i - 1] = Double.parseDouble(splitpart);
							allpresent &= true;
						}
					}
					if (allpresent) {
						double[][] doublematrix = new double[vector.length][1];
						for (int i = 0; i < vector.length; i++) {
							doublematrix[i][0] = vector[i];
						}
						Matrix train = new Matrix(doublematrix);
						ArrayList example = new ArrayList();
						example.add(train);
						example.add(type);
						trainingset.add(example);
					}
				}
			}
		} else {
			numClasses = 0;
			if (file.toUpperCase().contains("IRIS")) {
				numClasses = 3;
			} else {
				numClasses = 5;
			}
			while (scnr.hasNextLine()) {
				String in = scnr.nextLine();
				if (in.trim().length() != 0) {
					String[] split = in.split(" ").length > in.split(",").length ? in.split(" ") : in.split(",");
					double[] vector = new double[split.length - 1];
					int type = -1;
					if (file.toUpperCase().contains("IRIS")) {
						if (split[split.length - 1].contains("setosa")) {
							type = 0;
						} else if (split[split.length - 1].contains("versicolor")) {
							type = 1;
						} else {
							type = 2;
						}
					} else {
						type = Integer.parseInt(split[split.length - 1]);
					}
					boolean allpresent = true;
					for (int i = 0; i < split.length; i++) {
						String splitpart = split[i];
						if (splitpart.equals("?")) {
							allpresent &= false;
						} else {
							if (i != split.length - 1) {
								vector[i] = Double.parseDouble(splitpart);
							}
							allpresent &= true;
						}
					}
					if (allpresent) {
						double[][] doublematrix = new double[vector.length][1];
						for (int i = 0; i < vector.length; i++) {
							doublematrix[i][0] = vector[i];
						}
						Matrix train = new Matrix(doublematrix);
						ArrayList example = new ArrayList();
						example.add(train);
						example.add(type);
						trainingset.add(example);
					}
				}
			}
		}
		return trainingset;
    }
    
    public static Matrix getClassifier(ArrayList set) {
		n = getNumberOfFeatures(set);
		Matrix classifier = new Matrix(numClasses, n);
		for (int i = 0; i < numClasses; i++) {
			Matrix initialVector = Matrix.random(1, n);
			initialVector.timesEquals(Math.pow(lambda, -0.5)
					/ initialVector.normF());
			// initialVector.timesEquals(averageNorm / initialVector.normF());
			// System.out.println(initialVector.normF());
			classifier.setMatrix((new int[] { i }), 0, n - 1, initialVector);
		}
		return trainClassifier(classifier, set);
	}
    
    public static Matrix trainClassifier(Matrix classifier, ArrayList trainingset) {
    	int[] counts = new int[numClasses];
		classifier = new Matrix(numClasses, n);
		for(int i = 0; i < trainingset.size(); i++) {
			int y = (int) ((ArrayList) trainingset.get(i)).get(1);
			Matrix row = classifier.getMatrix(new int[] {y}, 0, n - 1);
			Matrix example = (Matrix) ((ArrayList) trainingset.get(i)).get(0);
			example = example.transpose();
			row.plusEquals(example);
			classifier.setMatrix(new int[] {y}, 0, n - 1, row);
			counts[y]++;
		}
		//System.out.println(Arrays.toString(counts));
		//printClassifier();
		for(int i = 0; i < numClasses; i++) {
			Matrix row = classifier.getMatrix(new int[] {i}, 0, n - 1);
			row.timesEquals(1.0 / counts[i]);
			//row.timesEquals(1.0 / row.normF());
			classifier.setMatrix(new int[] {i}, 0, n - 1, row);
		}
		return classifier;
    }
    
    public static int getNumberOfFeatures(ArrayList set) {
		ArrayList firstexample = (ArrayList) set.get(0);
		return ((Matrix) (firstexample.get(0))).getRowDimension();
	}
    
    public static double getAccuracy(Matrix classifier) {
		int numPredicted = 0;
		ArrayList<ArrayList> trainingset = getSet();
		for (int i = 0; i < trainingset.size(); i++) {
			ArrayList example = (ArrayList) trainingset.get(i);
			Matrix test = new Matrix(classifier.getRowDimension(), classifier.getColumnDimension());
    		for(int j = 0; j < classifier.getRowDimension(); j++) {
        		Matrix current = classifier.getMatrix(new int[] {j}, 0, LearnInitializer.n - 1);
        		current.timesEquals(1 / current.normF());
        		test.setMatrix(new int[] {j}, 0, LearnInitializer.n - 1, current);
    		}
			Matrix dot = test.times((Matrix) example.get(0));
			// dot.print(2, 2);
			// ((Matrix) example.get(0)).transpose().print(2, 4);
			// System.out.println("Should be " + example.get(1));
			int correct = (int) example.get(1);
			double max = 0;
			int predicted = -1;
			for (int j = 0; j < numClasses; j++) {
				if (dot.get(j, 0) > max) {
					max = dot.get(j, 0);
					predicted = j;
				}
			}
			if (correct == predicted) {
				numPredicted++;
			}
		}
		return ((double) numPredicted) / trainingset.size();
	}
    
}
