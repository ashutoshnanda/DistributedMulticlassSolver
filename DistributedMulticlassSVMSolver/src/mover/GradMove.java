package mover;

import java.util.ArrayList;

import Jama.Matrix;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;

public class GradMove implements Control {
	
	public static final String PAR_PROTID = "protocol";
	
	private final String name;
	
	private final int pid;
	
	public GradMove(String name) {
		this.name = name;
		pid = Configuration.getPid(name + "." + PAR_PROTID);
	}

	public boolean execute() {
		long time = peersim.core.CommonState.getTime();
		//System.out.printf("GradMove: Observing time = %d\n", time);
		for (int i = 0; i < Network.size(); i++) {
			if (Network.get(i).isUp()) {
				MatrixHolder node = (MatrixHolder) Network.get(i).getProtocol(pid);
				//System.out.println("GradMove: Conversion Worked for node " + i + "!");
				
				//CALCULATE THE SUBGRADIENT ESTIMATE AT EACH POINT AND STORE IT SOMEPLACE
				//System.out.println("GradMove: Number of subgradients: " + node.subgradients.size());
				
				Matrix totalSubgradient = new Matrix(node.subgradients.get(0).getRowDimension(), node.subgradients.get(0).getColumnDimension());
				
				for(int a = 0; a < node.subgradients.size(); a++) {
					totalSubgradient.plusEquals(node.subgradients.get(a));
				}
				
				totalSubgradient.timesEquals(1.0 / node.subgradients.size());
				
				node.subgradient = totalSubgradient;
				
				node.subgradients = new ArrayList<Matrix>();
				
			}
		}
		return false;
	}

}
