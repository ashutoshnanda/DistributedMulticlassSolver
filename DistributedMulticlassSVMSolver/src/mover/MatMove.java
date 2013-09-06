package mover;

import java.util.ArrayList;

import Jama.Matrix;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Control;
import peersim.core.Linkable;
import peersim.core.Network;

public class MatMove implements Control {
	
	public static final String PAR_PROTID = "protocol";
	
	private final String name;
	
	private final int pid;
	
	public MatMove(String name) {
		this.name = name;
		pid = Configuration.getPid(name + "." + PAR_PROTID);
	}

	public boolean execute() {
		long time = peersim.core.CommonState.getTime();
		//System.out.printf("MatMove: Observing time = %d\n", time);
		for (int i = 0; i < Network.size(); i++) {
			if (Network.get(i).isUp()) {
				MatrixHolder node = (MatrixHolder) Network.get(i).getProtocol(pid);
				//System.out.println("MatMove: Conversion Worked for node " + i + "!");
				
				//PROCESS ALL MATRICES AND PUT IT INTO VAL
				
				//System.out.println("MatMove: Number of classifiers: " + node.classifiers.size());
				
				Matrix totalclassifier = new Matrix(node.classifiers.get(0).getRowDimension(), node.classifiers.get(0).getColumnDimension());
				
				for(int a = 0; a < node.classifiers.size(); a++) {
					totalclassifier.plusEquals(node.classifiers.get(a));
				}
				
				totalclassifier.timesEquals(1.0 / node.classifiers.size());
				
				Matrix oldclassifier = Matrix.constructWithCopy(node.value.getArray());
				
				double oldacc = Start.acc(oldclassifier, Start.trainset);
				double newacc= Start.acc(totalclassifier, Start.trainset);
				
				System.out.println(oldacc + " " + Start.acc(totalclassifier, Start.trainset));
				
				//node.value = Start.acc(totalclassifier, Start.trainset) > Start.acc(oldclassifier, Start.trainset) ? totalclassifier : oldclassifier;
				
				node.value = totalclassifier;
				
				node.classifiers = new ArrayList<Matrix>();
				
				double accuracy = Start.acc(node.value, Start.trainset);
				
				//System.out.println(accuracy + " " + (Math.max(oldacc, newacc)  == accuracy));
				
				//System.out.println("Accuracy for node " + i + " is " + Start.acc(node.value, Start.trainset));
				
				MoveDumper.machineOut(String.format("Node: %d; Time: %d; Accuracy: %f; Norm Diff Matrix: %f; Norm Diff Percent: %f", i, time, accuracy, Start.optimal.minus(node.value).normF(), Start.optimal.minus(node.value).normF() / Start.optimal.normF()));
				
				node.accuracies.add(accuracy);
				
				if(node.accuracies.size() > 5) {
					//System.out.println(node.accuracies.get(node.accuracies.size() - 2) + " " + node.accuracies.get(node.accuracies.size() - 1));
				}
			}
		}
		return false;
	}

}
