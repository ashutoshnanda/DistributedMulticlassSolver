package mover;

import java.util.ArrayList;
import java.util.Random;

import Jama.Matrix;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Control;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;

public class GradCalc implements Control {

	public static final String PAR_PROTID = "protocol";

	private static final String PAR_T = "T";

	private static final String PAR_K = "k";

	public static double lambda = Start.lambda;

	public static int n = Start.n;

	public static int numClasses = Start.numClasses;

	public final String name;

	public static int pid;

	public static int T = Start.T;

	public static int k = Start.k;

	public GradCalc(String name) {
		this.name = name;
		pid = Configuration.getPid(name + "." + PAR_PROTID);
	}

	public boolean execute() {
		long time = peersim.core.CommonState.getTime() + 1; //Pegasos uses +1 numbering scheme
		//System.out.printf("GradCalc: Observing time = %d\n", time);
		for (int z = 0; z < Network.size(); z++) {
			if (Network.get(z).isUp()) {
				System.out.println(time);
				MatrixHolder node = (MatrixHolder) Network.get(z).getProtocol(pid);
				//System.out.println("GradCalc: Conversion Worked for node " + z + "!");
				// System.out.printf("T: %d; k: %d\n", T, k);
				// System.out.printf("lambda: %f\n", lambda);
				// System.out.printf("m: %d; n: %d\n", numClasses, n);
				int linkableID = FastConfig.getLinkable(pid);
				Linkable linkable = (Linkable) Network.get(z).getProtocol(linkableID);



				//CALCULATE SUBGRADIENTS AND GO AROUND PUTTING IT IN PLACES HERE


				if (linkable.degree() > 0) {				
					double curlyn = 1 / (lambda * time);
					Matrix avg = new Matrix(node.value.getRowDimension(), node.value.getColumnDimension());
					ArrayList examples = (ArrayList) getRandomSetExamples(node);
					for(Object o : examples) {
						ArrayList example = (ArrayList) o;
						int correct = (int) example.get(1);
						int r = 0;
						double maxr = 0;
						Matrix dot = node.value.times((Matrix) example.get(0));
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
							MoveDumper.numSVs++;
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
					//System.out.println("Degree: " + linkable.degree());
					for(int i = 0; i < linkable.degree(); i++) {
						Node other = linkable.getNeighbor(i);
		            	MatrixHolder m = (MatrixHolder) other.getProtocol(pid);
		            	m.subgradients.add(avg);
					}
					node.subgradients.add(avg);
				}
			}
		}
		return false;
	}

	public static ArrayList getRandomSetExamples(MatrixHolder mh) {
		ArrayList trainingset = mh.examples;
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
		return randomSet;
	}

}
