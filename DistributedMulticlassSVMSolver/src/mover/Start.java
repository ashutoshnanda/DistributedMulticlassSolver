package mover;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import peersim.config.*;
import peersim.core.*;
import peersim.graph.SubGraphEdges;
import Jama.Matrix;

public class Start implements Control {
	
    private static final String PAR_PROT = "protocol";
    
    private static final String PAR_FILE = "file";
    
    private static final String PAR_T = "T";
    
    private static final String PAR_K = "k";

    private final int pid;
    
    public static int numClasses;
    
    public static int n;

	public static double lambda = Math.pow(10, -2);
	
	public static Matrix optimal;
	
	public static String file;
	
	public static int T;
	
	public static int k;
	
	public static long start = 0;
	
	public static ArrayList<ArrayList> trainset = null;

    public Start(String prefix) {
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
        file = Configuration.getString(prefix + "." + PAR_FILE);
        T = Configuration.getInt(prefix + "." + PAR_T);
        k = Configuration.getInt(prefix + "." + PAR_K);
    }
    
    public boolean execute() {
    	start = System.nanoTime();
    	trainset = getSet();
    	this.optimal = getClassifier(trainset);
    	System.out.printf("Optimal Accuracy: %f\n", acc(this.optimal, trainset));
        for (int i = 0; i < Network.size(); i++) {
            MatrixHolder nodeValue = (MatrixHolder) Network.get(i).getProtocol(pid);
            nodeValue.classifiers = new ArrayList<Matrix>();
            nodeValue.subgradients = new ArrayList<Matrix>();
            Matrix val = null;
        	ArrayList<ArrayList> smallset = new ArrayList<ArrayList>();
        	for(int j = 0; j < trainset.size(); j++) {
        		if(j % Network.size() == i) {
        			smallset.add(trainset.get(j));
        		}
        	}
        	nodeValue.examples = smallset;
            val = new Matrix(numClasses, n);
            for(int j = 0; j < numClasses; j++) {
            	for(int k = 0; k < n; k++) {
            		val.set(j, k, Math.random());
            	}
            }
            val.timesEquals(Math.pow(lambda,  -0.5) / val.normF());
            //System.out.println(val.normF() + " " + (Math.pow(lambda, -0.5)));
        	nodeValue.value = val;
        	nodeValue.subgradient = new Matrix(val.getRowDimension(), val.getColumnDimension());
        	nodeValue.accuracies = new ArrayList<Double>();
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
    
    /*public static Matrix trainClassifier(Matrix classifier, ArrayList trainingset) {
    	for(int t = 1; t <= T; t++) {
			Matrix newclassifier = Matrix.constructWithCopy(classifier.getArray());
			double curlyn = 1 / (lambda * t);
			Matrix del = classifier.times(lambda);
			Matrix avg = new Matrix(del.getRowDimension(), del.getColumnDimension());
			ArrayList examples = (ArrayList) getRandomSetExamples();
			for(Object o : examples) {
				ArrayList example = (ArrayList) o;
				int correct = (int) example.get(1);
				int r = 0;
				double maxr = 0;
				Matrix dot = classifier.times((Matrix) example.get(0));
				for(int i = 0; i < numClasses; i++) {
					if(correct != i) {
						double currentr = dot.get(i, 0);
						if(currentr > maxr) {
							r = i;
							maxr = currentr;
						}
					}
				}
				double hingeloss = Math.max(0, 1 + maxr - dot.get(correct, 0));
				//dot.print(5, 4);
				//System.out.println("Should be " + correct);
				if(hingeloss > 0) {
					//System.out.println("We have loss");
					Matrix xt = ((Matrix) example.get(0)).transpose();
					Matrix yt = avg.getMatrix(new int[] {correct}, 0, n - 1).minus(xt);
					avg.setMatrix(new int[] {correct}, 0, n - 1, yt);
					Matrix rt = avg.getMatrix(new int[] {r}, 0, n - 1).plus(xt);
					avg.setMatrix(new int[] {r}, 0, n - 1, rt);
				}
			}
			for(int i = 0; i < avg.getRowDimension(); i++) {
				avg.setMatrix(new int[] {i}, 0, n-1, avg.getMatrix(new int[] {i}, 0, n-1).times(1));
			}
			del.plusEquals(avg);
			classifier.minusEquals(del.times(curlyn));
			classifier.timesEquals(1 / (Math.pow(lambda, 0.5) * classifier.normF()));
		}
    	return classifier;
    }*/
    
    public static Matrix trainClassifier(Matrix in, ArrayList trainingset) {
    	Matrix classifier = Matrix.constructWithCopy(in.getArray());
		for(int t = 1; t <= T; t++) {
			double curlyn = 1 / (lambda * t);
			Matrix avg = new Matrix(classifier.getRowDimension(), classifier.getColumnDimension());
			ArrayList examples = (ArrayList) getRandomSetExamples();
			for(Object o : examples) {
				ArrayList example = (ArrayList) o;
				int correct = (int) example.get(1);
				int r = 0;
				double maxr = 0;
				Matrix dot = classifier.times((Matrix) example.get(0));
				for(int i = 0; i < numClasses; i++) {
					if(correct != i) {
						double currentr = dot.get(i, 0);
						if(currentr > maxr) {
							r = i;
							maxr = currentr;
						}
					}
				}
				double hingeloss = Math.max(0, 1 + maxr - dot.get(correct, 0));
				if(hingeloss > 0) {
					Matrix xt = ((Matrix) example.get(0)).transpose();
					Matrix yt = avg.getMatrix(new int[] {correct}, 0, n - 1).plus(xt);
					avg.setMatrix(new int[] {correct}, 0, n - 1, yt);
					Matrix rt = avg.getMatrix(new int[] {r}, 0, n - 1).minus(xt);
					avg.setMatrix(new int[] {r}, 0, n - 1, rt);
				}
			}
			for(int i = 0; i < avg.getRowDimension(); i++) {
				avg.setMatrix(new int[] {i}, 0, n-1, avg.getMatrix(new int[] {i}, 0, n-1).times(1.0 / k * curlyn));
			}
			classifier.plusEquals(avg);
			classifier.timesEquals(1 / (Math.pow(lambda, 0.5) * classifier.normF()));
		}
		return classifier;
    }
	
    public static int getNumberOfFeatures(ArrayList set) {
		ArrayList firstexample = (ArrayList) set.get(0);
		return ((Matrix) (firstexample.get(0))).getRowDimension();
	}
    
    public static double acc(Matrix classifier, ArrayList trainingset) {
    	long start = System.nanoTime();
		int correct = 0;
		for(int i = 0; i < trainingset.size(); i++) {
			Matrix example = (Matrix) ((ArrayList) trainingset.get(i)).get(0);
			Matrix dot = classifier.times(example);
			int predict = -1;
			double predictvalue = 0;
			for(int z = 0; z < numClasses; z++) {
				if(predictvalue < dot.get(z, 0)) {
					predict = z;
					predictvalue = dot.get(z, 0);
				}
			}
			if((int) ((ArrayList) trainingset.get(i)).get(1) == predict) {
				correct++;
			}
		}
		long end = System.nanoTime();
		double timeinsecs = (end - start) / Math.pow(10, 9);
		MoveDumper.numcalls++;
		MoveDumper.totalTime += timeinsecs;
		return (double) correct / trainingset.size();
	}
    
    public static ArrayList getRandomSetExamples() {
    	ArrayList trainingset = getSet();
		ArrayList randomSet = new ArrayList();
		Random generator = new Random();
		ArrayList<Integer> randomindices = new ArrayList<Integer>();
		for (int i = 0; i < k; i++) {
			boolean shouldAdd = false;
			int currentRandom = -1;
			while (!shouldAdd) {
				currentRandom = generator.nextInt(trainingset.size());
				shouldAdd = randomindices.indexOf(currentRandom) == -1;
			}
			randomindices.add(currentRandom);
		}
		for (int randomindex : randomindices) {
			randomSet.add(trainingset.get(randomindex));
		}
		// System.out.println(randomindices);
		return randomSet;
	}
    
}
