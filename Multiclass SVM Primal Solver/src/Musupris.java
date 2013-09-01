import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import javax.swing.JApplet;

import Jama.Matrix;

//MUlticlass SUpport vector machine PRimal Solver

//Consider calling it marsupial...

// Dealing with multiple vectors at one iteration, weight the average based on confidence assigned to each vector
// ^^ Done and Done :D

//What is the deal with smartget....?

public class Musupris {

	public static ArrayList trainingset = new ArrayList();

	public static int T = 400;

	public static double lambda = Math.pow(10, -2);

	public static int numClasses;

	public static int k = 1;

	public static int n = 0;

	public static Matrix classifier = null;

	public static double weightN = 0;

	public static Integer[] examplecounts = null;

	public static double averageNorm = 0;

	public static double avgsmall = 0;

	public static double avglarge = 0;
	
	public static void main(String[] args) throws FileNotFoundException {
		File out = new File("out.txt");
		PrintStream p = new PrintStream(out);
		int num = 100;
		p.println(num/2);
		int localT = T;
		int firstk = 1, secondk = 150;
		for(int i = 0; i < num; i++) {
			if(i <= num/2 - 1) {
				k = 1;
				//T = secondk * localT; //"num examples looked at" equality
				T = localT;
			} else {	
				if (i == num/2) {
					System.out.println("BREAK");
				}
				k = secondk;
				T = localT;
			}
			double acc = call(i <= num/2 - 1);
			System.out.println(acc);
			p.print(acc);
			if(i != num - 1) {
				p.println();
			}
		}
		avgsmall /= num;
		avglarge /= num;
		System.out.println("Small Average: " + avgsmall);
		System.out.println("Large Average: " + avglarge);
	}
	
	public static double call(boolean small) {
		long initTime = System.nanoTime();
		setup();
		setClassifier();
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
				//dot.print(5, 4);
				//System.out.println("Should be " + correct);
				if(hingeloss > 0) {
					//System.out.println("We have loss");
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
			//System.out.println("New Accuracy: " + Musupris.acc());
		}
		long donetime = System.nanoTime();
		double dub = (((float) (donetime - initTime)) * Math.pow(10, -9));
		//System.out.printf("Done in %f seconds.\n", (((float) (donetime - initTime)) * Math.pow(10, -9)));
		if(small) {
			avgsmall += dub;
		} else {
			avglarge += dub;
		}
		return Musupris.acc();
	}

	public static void setup() {
		String name = "iris.data";
		File inputData = new File(name);
		Scanner scnr = null;
		try {
			scnr = new Scanner(inputData);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		}
		if(name.contains("letter")) {
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
			if (name.toUpperCase().contains("IRIS")) {
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
					if (name.toUpperCase().contains("IRIS")) {
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
			averageNorm /= trainingset.size();
			examplecounts = new Integer[trainingset.size()];
			for (int i = 0; i < examplecounts.length; i++) {
				examplecounts[i] = 0;
			}
		}
	}

	public static void printTrainingSet(ArrayList set) {
		for (Object o : set) {
			ArrayList example = (ArrayList) o;
			System.out.println("Classified as type " + example.get(1));
			((Matrix) (example.get(0))).print(5, 1);
		}
	}

	public static int getNumberOfFeatures() {
		ArrayList firstexample = (ArrayList) trainingset.get(0);
		return ((Matrix) (firstexample.get(0))).getRowDimension();
	}

	public static void printInfo() {
		System.out.printf("T = %d\n", T);
		System.out.printf("lambda = %f\n", lambda);
		System.out.printf("k = %d\n", k);
		System.out.printf("numclasses = %d\n", numClasses);
		System.out.printf("n = %d\n", n);
	}

	public static void setInfo() {
		Scanner in = new Scanner(System.in);
		System.out.print("Please enter the value of T: ");
		String TString = in.nextLine();
		if (TString.trim().length() > 0) {
			T = Integer.parseInt(TString);
		}
		System.out.print("Please enter the value of lambda: ");
		String lambdaString = in.nextLine();
		if (lambdaString.trim().length() > 0) {
			lambda = Double.parseDouble(lambdaString);
		}
		System.out.print("Please enter the value of k: ");
		String kString = in.nextLine();
		if (kString.trim().length() > 0) {
			k = Integer.parseInt(kString);
		}
	}

	public static void printClassifier() {
		classifier.print(6, 3);
	}

	public static void setClassifier() {
		n = getNumberOfFeatures();
		classifier = new Matrix(numClasses, n);
		for (int i = 0; i < numClasses; i++) {
			Matrix initialVector = Matrix.random(1, n);
			initialVector.timesEquals(Math.pow(lambda, -0.5)
					/ initialVector.normF());
			// initialVector.timesEquals(averageNorm / initialVector.normF());
			// System.out.println(initialVector.normF());
			classifier.setMatrix((new int[] { i }), 0, n - 1, initialVector);
		}
	}

	public static ArrayList getRandomSetExamples() {
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

	public static ArrayList smartGetOfRandomSetExamples() {
		List<Integer> countslist = Arrays.asList(examplecounts.clone());
		int currentmin = Collections.min(countslist);
		int maxever = Collections.max(countslist);
		int[] distributionofuses = new int[maxever - currentmin + 1];
		int totalexampleswithcurrentvalue = 0;
		int maxneedtouse;
		for (maxneedtouse = currentmin; maxneedtouse <= maxever; maxneedtouse++) {
			totalexampleswithcurrentvalue += Collections.frequency(countslist,
					maxneedtouse);
			if (totalexampleswithcurrentvalue > k) {
				break;
			}
		}
		ArrayList<Integer> topickfrom = new ArrayList<Integer>();
		for (int i = 0; i < trainingset.size(); i++) {
			if (examplecounts[i] <= maxneedtouse) {
				topickfrom.add(i);
			}
		}
		ArrayList randomSet = new ArrayList();
		Random generator = new Random();
		ArrayList<Integer> randomindices = new ArrayList<Integer>();
		for (int i = 0; i < k; i++) {
			boolean shouldAdd = false;
			int currentRandom = -1;
			while (!shouldAdd) {
				currentRandom = generator.nextInt(topickfrom.size());
				shouldAdd = randomindices.indexOf(currentRandom) == -1;
			}
			randomindices.add(currentRandom);
		}
		for (int randomindex : randomindices) {
			randomSet.add(trainingset.get(topickfrom.get(randomindex)));
			examplecounts[topickfrom.get(randomindex)]++;
		}
		return randomSet;
	}

	public static double loss(ArrayList example) {
		Matrix dot = classifier.times((Matrix) example.get(0));
		int correct = (int) example.get(1);
		double maxnotcorrect = 0;
		for (int i = 0; i < numClasses; i++) {
			if (dot.get(i, 0) > maxnotcorrect && i != correct) {
				maxnotcorrect = dot.get(i, 0);
			}
		}
		// System.out.println(1 + maxnotcorrect - dot.get(correct, 0) + " " +
		// dot.get(correct, 0));
		return Math.max(0, 1 + maxnotcorrect - dot.get(correct, 0));
	}

	public static double getAccuracy() {
		int numPredicted = 0;
		for (int i = 0; i < trainingset.size(); i++) {
			ArrayList example = (ArrayList) trainingset.get(i);
			Matrix dot = classifier.times((Matrix) example.get(0));
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
	
	public static double getAlternativeAccuracy() {
		int numPredicted = 0;
		for (int i = 0; i < trainingset.size(); i++) {
			ArrayList example = (ArrayList) trainingset.get(i);
			Matrix exampleVector = (Matrix) example.get(0);
			Matrix results = classifier.copy();
			double min = exampleVector.normF();
			int predicted = -1;
			for(int z = 0; z < results.getRowDimension(); z++) {
				Matrix row = results.getMatrix(new int[] {z}, 0, n - 1);
				row.timesEquals(exampleVector.normF());
				row.minusEquals(exampleVector.transpose());
				if(row.normF() < min) {
					min = row.normF();
					predicted = z;
				}
				results.setMatrix(new int[] {z}, 0, n - 1, row);
			}
			int correct = (int) example.get(1);
			if(correct == predicted) {
				numPredicted++;
			}
			// dot.print(2, 2);
			// ((Matrix) example.get(0)).transpose().print(2, 4);
			// System.out.println("Should be " + example.get(1));
		}
		return ((double) numPredicted) / trainingset.size();
	}

	public static double normW() {
		return classifier.normF();
	}

	public static double objectiveForSet(ArrayList set) {
		double first = lambda / 2.0 * normW();
		double second = 0;
		for (Object o : set) {
			second += loss((ArrayList) set);
		}
		second /= set.size();
		return first + second;
	}
	
	public static double acc() {
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
		return (double) correct / trainingset.size();
	}
}









/* GOOD EXAMPLE */

/*
double oldacc = acc();
Matrix oldclassifier = Matrix.constructWithCopy(classifier.getArray());
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
System.out.println("New Accuracy: " + Musupris.acc());
*/

/* End Good Example */






/*
 * for(int t = 1; t <= T; t++) { double currentAcc = getAccuracy(); Matrix
 * oldClassifier = classifier.copy(); printClassifier(); ArrayList randomSet =
 * getRandomSetExamples(); ArrayList randomSetThatCreatesError = new
 * ArrayList(); weightN = 1 / (lambda * t); double trueweight = 1 - weightN *
 * lambda; Matrix firstterm = classifier.times(trueweight); System.out.println(1
 * - weightN * lambda); Matrix secondterm = new Matrix(numClasses, n); int[]
 * counts = new int[numClasses]; for(Object o : randomSet) { ArrayList example =
 * (ArrayList) o; double loss = loss(example); //System.out.println("Loss: " +
 * loss); if(Math.abs(loss) > 0.001) { randomSetThatCreatesError.add(example);
 * Matrix rowvector = (Matrix) example.get(0); Matrix dot =
 * classifier.times((Matrix) example.get(0)); dot.timesEquals(1/dot.normF());
 * //dot.print(0, 6); for(int i = 0; i < numClasses; i++) { Matrix row =
 * secondterm.getMatrix(new int[] {i}, 0, n - 1);
 * row.plusEquals(rowvector.transpose().times(dot.get(i, 0)));
 * secondterm.setMatrix(new int[] {i}, 0, n - 1, row); } Matrix row =
 * secondterm.getMatrix(new int[] {(int) example.get(1)}, 0, n - 1);
 * row.plusEquals(rowvector.transpose());//.times(dot.get((int) example.get(1),
 * 0))); secondterm.setMatrix(new int[] {(int) example.get(1)}, 0, n - 1, row);
 * //row.print(3, 3); //System.out.println("FOR CLASS " + (int) example.get(1));
 * counts[(int) example.get(1)]++; } } //secondterm.timesEquals(1.0/k); for(int
 * i = 0; i < numClasses; i++) { Matrix row = secondterm.getMatrix(new int[]
 * {i}, 0, n - 1); //row.print(3, 3); if(row.normF() > 0) {
 * row.timesEquals(1.0/counts[i]); secondterm.setMatrix(new int[] {i}, 0, n - 1,
 * row); } else { Matrix newrow = firstterm.getMatrix(new int[] {i}, 0, n - 1);
 * secondterm.setMatrix(new int[] {i}, 0, n - 1, newrow); } }
 * secondterm.timesEquals(1 - trueweight); firstterm.print(0, 8);
 * secondterm.print(0, 8); classifier = firstterm.plus(secondterm);
 * System.out.printf("Had %d in random set\n",
 * randomSetThatCreatesError.size()); for(int i = 0; i < numClasses; i++) {
 * Matrix row = classifier.getMatrix(new int[] {i}, 0, n - 1);
 * //row.timesEquals(Math.min(1, Math.pow(lambda, -0.5) / row.normF()));
 * classifier.setMatrix(new int[] {i}, 0, n - 1, row); } double newAcc =
 * getAccuracy(); if(newAcc < currentAcc) { classifier = oldClassifier; }
 * System.out.println("I GOT " + getAccuracy() * 100 + "% ACCURACY!!!");
 * System.out.println("MOVING ON TO NEXT ITERATION!"); }
 */

/*
 * 
 * classifier.print(2, 4); ArrayList randomset = getRandomSetExamples(); double
 * rate = 1 / (lambda * t); Matrix nabla = new
 * Matrix(classifier.getRowDimension(), classifier.getColumnDimension());
 * for(int z = 0; z < numClasses; z++) { Matrix before =
 * classifier.getMatrix(new int[] {z}, 0, classifier.getColumnDimension() -
 * 1).times(lambda); Matrix added = new Matrix(1,
 * classifier.getColumnDimension()); for(Object o : randomset) { ArrayList
 * example = (ArrayList) o; if(Math.abs(loss(example)) > 0.001) { Matrix dot =
 * classifier.times((Matrix) example.get(0)); int correct = (int)
 * example.get(1); double maxnotcorrect = 0; int notcorrect = 0; for(int i = 0;
 * i < numClasses; i++) { if(dot.get(i, 0) > maxnotcorrect && i != correct) {
 * maxnotcorrect = dot.get(i, 0); notcorrect = i; } }
 * System.out.println("Should be " + example.get(1));
 * dot.transpose().print(4,4); System.out.println("Predicted = " + notcorrect +
 * "\n\n"); if(maxnotcorrect > dot.get(correct, 0)) { //ri
 * added.plusEquals(((Matrix) example.get(0)).transpose()); } else { //yi
 * added.minusEquals(((Matrix) example.get(0)).transpose()); } } }
 * added.timesEquals(1.0 / randomset.size()); before.plusEquals(added);
 * System.out.println("nabla"); before.print(4, 4); nabla.setMatrix(new int[]
 * {z}, 0, n - 1, added); } classifier.minusEquals(nabla.times(rate));
 * classifier.timesEquals(Math.min(1, 1 / (Math.pow(lambda, 0.5) *
 * classifier.normF()))); System.out.printf("New Accuracy: %f\n",
 * getAccuracy());
 */

/*
 * Matrix old = classifier.copy(); double oldAcc = getAccuracy(); double rate =
 * 1 / (lambda * t); Random rand = new Random(); int randomindex =
 * rand.nextInt(trainingset.size()); Matrix nabla = classifier.times(lambda);
 * Matrix updateToNabla = new Matrix(nabla.getRowDimension(),
 * nabla.getColumnDimension()); int[] numupdates = new
 * int[classifier.getRowDimension()]; double[] weights = new
 * double[classifier.getRowDimension()]; for(Object o :
 * smartGetOfRandomSetExamples()) { ArrayList randomExample = (ArrayList) o;
 * if(loss(randomExample) > 0.001) { int correct = (int) randomExample.get(1);
 * int nextcorrect = 0; Matrix example = (Matrix) randomExample.get(0); Matrix
 * dot = classifier.times(example); double maxnextcorrect = 0; for(int j = 0; j
 * < numClasses; j++) { if(dot.get(j, 0) > maxnextcorrect && j != correct) {
 * maxnextcorrect = dot.get(j, 0); nextcorrect = j; } } System.out.println("Y: "
 * + correct); Matrix dotnormalized = dot.times(1 / dot.normF());
 * numupdates[correct]++; //numupdates[nextcorrect]++; weights[correct] +=
 * dotnormalized.get(correct, 0); //weights[nextcorrect] +=
 * dotnormalized.get(nextcorrect, 0); Matrix nablay =
 * updateToNabla.getMatrix(new int[] {correct}, 0, n - 1);
 * nablay.minusEquals(example.transpose().times(dotnormalized.get(correct, 0)));
 * //Matrix nablar = updateToNabla.getMatrix(new int[] {nextcorrect}, 0, n - 1);
 * //nablar.plusEquals(example.transpose().times(dotnormalized.get(nextcorrect,
 * 0))); updateToNabla.setMatrix(new int[] {correct}, 0, n - 1, nablay);
 * //updateToNabla.setMatrix(new int[] {nextcorrect}, 0, n - 1, nablar); } }
 * //System.out.println(Arrays.toString(numupdates)); for(int i = 0; i <
 * classifier.getRowDimension(); i++) { Matrix currentRow =
 * updateToNabla.getMatrix(new int[] {i}, 0, n - 1); if(numupdates[i] != 0) {
 * currentRow.timesEquals(1.0 / numupdates[i]); } if(weights[i] != 0) {
 * currentRow.timesEquals(1.0 / weights[i]); } updateToNabla.setMatrix(new int[]
 * {i}, 0 , n - 1, currentRow); Matrix currentNablaRow = nabla.getMatrix(new
 * int[] {i}, 0, n - 1); currentNablaRow.plusEquals(updateToNabla.getMatrix(new
 * int[] {i}, 0, n - 1)); nabla.setMatrix(new int [] {i}, 0, n - 1,
 * currentNablaRow); } updateToNabla.print(3,4);
 * classifier.minusEquals(nabla.times(rate));
 * //classifier.timesEquals(Math.min(1, 1 / (Math.pow(lambda, 0.5) *
 * classifier.normF()))); for(int i = 0; i < numClasses; i++) { Matrix row =
 * classifier.getMatrix(new int[] {i}, 0, n - 1); row.timesEquals(averageNorm /
 * row.normF()); classifier.setMatrix(new int[] {i}, 0, n - 1, row); } if(oldAcc
 * >= getAccuracy()) { classifier = old; } printClassifier();
 * System.out.println("I GOT " + getAccuracy() * 100 + "% ACCURACY!!!");
 * 
 * } long donetime = System.nanoTime();
 * System.out.printf("Done in %f seconds.\n", (((float) (donetime - initTime)) *
 * Math.pow(10, -9)));
 */





/*
// printTrainingSet(trainingset);
		// setInfo();
		// printInfo();
		setClassifier();
		// printClassifier();
		int t = 1;
		/*while((((float) (System.nanoTime() - initTime)) * Math.pow(10, -9)) < 550) {
			Matrix old = classifier.copy();
			double oldAcc = getAccuracy();
			double rate = 1 / (lambda * t);
			Random rand = new Random();
			int randomindex = rand.nextInt(trainingset.size());
			Matrix nabla = classifier.times(lambda);
			Matrix updateToNabla = new Matrix(nabla.getRowDimension(), nabla.getColumnDimension());
			ArrayList randomExample = (ArrayList) trainingset.get(randomindex);
			if (loss(randomExample) > 0.001) {
				int correct = (int) randomExample.get(1);
				int nextcorrect = 0;
				Matrix example = (Matrix) randomExample.get(0);
				Matrix dot = classifier.times(example);
				double maxnextcorrect = 0;
				for (int j = 0; j < numClasses; j++) {
					if (dot.get(j, 0) > maxnextcorrect && j != correct) {
						maxnextcorrect = dot.get(j, 0);
						nextcorrect = j;
					}
				}
				//System.out.printf("Correct: %d; Predicted: %d", correct, nextcorrect);
				//dot.transpose().print(4, 4);
				Matrix nablay = updateToNabla.getMatrix(new int[] { correct }, 0, n - 1);
				nablay.minusEquals(example.transpose());
				Matrix nablar = updateToNabla.getMatrix(new int[] { nextcorrect }, 0, n - 1);
				nablar.plusEquals(example.transpose());
				updateToNabla.setMatrix(new int[] { correct }, 0, n - 1, nablay);
				updateToNabla.setMatrix(new int[] { nextcorrect }, 0, n - 1, nablar);;
			}
			for (int i = 0; i < classifier.getRowDimension(); i++) {
				Matrix currentNablaRow = nabla.getMatrix(new int[] { i }, 0, n - 1);
				currentNablaRow.plusEquals(updateToNabla.getMatrix(new int[] { i }, 0, n - 1));
				nabla.setMatrix(new int[] { i }, 0, n - 1, currentNablaRow);
			}
			//updateToNabla.print(3, 4);
			classifier.minusEquals(nabla.times(rate));
			classifier.timesEquals(Math.min(1, 1 / (Math.pow(lambda, 0.5) * classifier.normF())));
			if (oldAcc >= getAccuracy()) {
				//classifier = old;
			}
			//printClassifier();
			System.out.println("I GOT " + getAccuracy() * 100 + "% ACCURACY!!!");
			t += 1;
		}
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
			row.timesEquals(1.0 / row.normF());
			classifier.setMatrix(new int[] {i}, 0, n - 1, row);
		}
		//printClassifier();
		System.out.println("I GOT " + getAccuracy() * 100 + "% ACCURACY!!!");
		System.out.println("I GOT ALTERNATE ACCURACY OF " + getAlternativeAccuracy() * 100 + "%!!!!");
*/























//GOOOOOOOOOOOOOOOOOD VERSION

/*
long initTime = System.nanoTime();
		setup();
		setClassifier();
		for(int t = 1; t <= T; t++) {
			double oldacc = acc();
			Matrix oldclassifier = Matrix.constructWithCopy(classifier.getArray());
			Matrix newclassifier = Matrix.constructWithCopy(classifier.getArray());
			double curlyn = 1 / (lambda * t);
			Matrix del = classifier.times(lambda);
			ArrayList example = (ArrayList) trainingset.get((new Random()).nextInt(trainingset.size()));
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
				Matrix yt = del.getMatrix(new int[] {correct}, 0, n - 1).minus(xt);
				del.setMatrix(new int[] {correct}, 0, n - 1, yt);
				Matrix rt = del.getMatrix(new int[] {r}, 0, n - 1).plus(xt);
				del.setMatrix(new int[] {r}, 0, n - 1, rt);
			}
			classifier.minusEquals(del.times(curlyn));
			classifier.timesEquals(1 / (Math.pow(lambda, 0.5) * classifier.normF()));
			System.out.println("New Accuracy: " + Musupris.acc());
		}
		long donetime = System.nanoTime();
		System.out.printf("Done in %f seconds.\n", (((float) (donetime - initTime)) * Math.pow(10, -9)));
*/