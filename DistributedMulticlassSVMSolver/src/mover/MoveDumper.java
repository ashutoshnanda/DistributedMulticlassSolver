package mover;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import Jama.Matrix;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;

public class MoveDumper implements Control {
	
	public static String filename;
	
	public static File output;
	
	public static PrintStream mout;
	
	private static final String PAR_NAME = "file";
	
	public static int numSVs = 0;
	
	public static int numcalls = 0;
	
	public static double totalTime = 0;
	
	public static long end = 0;
	
	public MoveDumper(String prefix) {
		filename = Configuration.getString(prefix + "." + PAR_NAME);
		output = new File(filename);
		try {
			mout = new PrintStream(output);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		MoveDumper.machineOut(String.format("Cycles: %d; Nodes: %d", Configuration.getInt("simulation.cycles"), Configuration.getInt("network.size"))); 
	}

	public boolean execute() {
		if(CommonState.getPhase() == CommonState.POST_SIMULATION) {
			end = System.nanoTime();
			MoveDumper.mout.close();
			System.out.println("Done with file output!");
			for (int i = 0; i < Network.size(); i++) {
				if (Network.get(i).isUp()) {
					MatrixHolder node = (MatrixHolder) Network.get(i).getProtocol(GradCalc.pid);
					System.out.println(node.accuracies);
				}
			}
			System.out.println("Number of Support Vectors: " + numSVs);
			System.out.println("Out of " + GradCalc.k * GradCalc.T * Network.size());
			System.out.println("Classification Time (Average): " + totalTime/numcalls);
			System.out.println("Training Time (Average): " + (end - Start.start) / Math.pow(10, 9));
		}
		return false;
	}
	
	public static void machineOut(String s) {
		mout.println(s);
	}

}
