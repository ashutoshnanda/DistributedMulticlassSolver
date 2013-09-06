package mover;

import java.util.ArrayList;

import peersim.cdsim.CDProtocol;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import Jama.Matrix;

public class MoveDummy extends MatrixHolder implements CDProtocol {
    /**
     * Creates a new {@link example.aggregation.AverageFunction} protocol
     * instance.
     * 
     * @param prefix
     *            the component prefix declared in the configuration file.
     */
    public MoveDummy(String prefix) {
    	super(prefix);
    }

    public void nextCycle(Node node, int protocolID) {
        int linkableID = FastConfig.getLinkable(protocolID);
        Linkable linkable = (Linkable) node.getProtocol(linkableID);
        long time = CommonState.getTime();
        if (linkable.degree() > 0) {
        	//System.out.println("MoveDummy: Hi from node " + node.getID() + " at time " + time);
        }
    }

}

