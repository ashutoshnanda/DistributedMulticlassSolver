package mover;

import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Control;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;

public class MatCalc implements Control {
	
	public static final String PAR_PROTID = "protocol";
	
	private final String name;
	
	private final int pid;
	
	public MatCalc(String name) {
		this.name = name;
		pid = Configuration.getPid(name + "." + PAR_PROTID);
	}

	public boolean execute() {
		long time = peersim.core.CommonState.getTime();
		//System.out.printf("MatCalc: Observing time = %d\n", time);
		for (int i = 0; i < Network.size(); i++) {
			if (Network.get(i).isUp()) {
				MatrixHolder node = (MatrixHolder) Network.get(i).getProtocol(pid);
				//System.out.println("MatCalc: Conversion Worked for node " + i + "!"); 
				int linkableID = FastConfig.getLinkable(pid);
		        Linkable linkable = (Linkable) Network.get(i).getProtocol(linkableID);
		        if(linkable.degree() > 0) {
		        	//System.out.println("Degree: " + linkable.degree());
		        	
		        	//CALCULATE NEW MATRIX BASED ON SUBGRADIENT AND PUT IT IN THE RIGHT PLACES

					node.value.plusEquals(node.subgradient);
					node.value.timesEquals(1 / (Math.pow(GradCalc.lambda, 0.5) * node.value.normF()));
					
					for(int j = 0; j < linkable.degree(); j++) {
						Node other = linkable.getNeighbor(j);
		            	MatrixHolder m = (MatrixHolder) other.getProtocol(pid);
		            	m.classifiers.add(node.value);
					}
					node.classifiers.add(node.value);
		        }
			}
		}
		return false;
	}

}
