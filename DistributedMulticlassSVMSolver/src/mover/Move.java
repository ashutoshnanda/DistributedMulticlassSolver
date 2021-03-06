package mover;

import java.util.ArrayList;

import peersim.cdsim.CDProtocol;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import Jama.Matrix;

public class Move extends MatrixHolder implements CDProtocol {
    /**
     * Creates a new {@link example.aggregation.AverageFunction} protocol
     * instance.
     * 
     * @param prefix
     *            the component prefix declared in the configuration file.
     */
    public Move(String prefix) {
    	super(prefix);
    }

    public void nextCycle(Node node, int protocolID) {
        int linkableID = FastConfig.getLinkable(protocolID);
        Linkable linkable = (Linkable) node.getProtocol(linkableID);
        if (linkable.degree() > 0) {
            System.out.println("Move says hai from node " + node.getID());
            Node peer = linkable.getNeighbor(CommonState.r.nextInt(linkable.degree()));
            double bestvalue = 0;
            int bestNode = 0;
            for(int i = 0; i < linkable.degree(); i++) {
            	Node other = linkable.getNeighbor(i);
            	MatrixHolder m = (MatrixHolder) other.getProtocol(protocolID);
            	//if(LearnInitializer.acc(m.value, m.train) > bestvalue) {
            	//	bestvalue = LearnInitializer.acc(m.value, m.train);
            	//	bestNode = i;
            	//}
            }
            //peer = linkable.getNeighbor(bestNode);
            // Failure handling
            if (!peer.isUp())
                return;
            
            Move self = (Move) node.getProtocol(protocolID);

            Move neighbor = (Move) peer.getProtocol(protocolID);
            
            int classToSend = CommonState.r.nextInt(LearnInitializer.numClasses);
            
            //System.out.printf("Gonna Send Class %d From Node %d To Node %d!\n", classToSend, node.getIndex(), peer.getIndex());            
            
            Matrix send = this.value.getMatrix(new int[] {classToSend}, 0, LearnInitializer.n - 1);
            
            ArrayList toSend = new ArrayList();

            toSend.add(send);
            
            //toSend.add(self.counts[classToSend]);
            
            //neighbor.addTrainExample(toSend);
        }
    }

}

