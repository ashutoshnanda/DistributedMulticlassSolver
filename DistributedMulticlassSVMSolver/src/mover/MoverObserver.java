package mover;

import java.util.ArrayList;
import java.util.Arrays;

import Jama.Matrix;
import peersim.core.*;
import peersim.config.*;

/**
 * Class DummyObserver
 * Implements a {@link Control} interface necessary to run the simulation.
 * This does nothing except returning false, so that the simulation keeps running.
 * @author Deepak Nayak
 *
 */
public class MoverObserver implements Control {

//--------------------------------------------------------------------------
// Constants
//--------------------------------------------------------------------------

/** 
 * String name of the parameter used to select the protocol to operate on
 */
public static final String PAR_PROTID = "protocol";


//--------------------------------------------------------------------------
// Fields
//--------------------------------------------------------------------------

/** The name of this object in the configuration file */
private final String name;

/** Protocol identifier */
private final int pid;

// iterator counter
private static int i = 0;


//--------------------------------------------------------------------------
// Constructor
//--------------------------------------------------------------------------

/**
 * Creates a new observer and initializes the configuration parameter.
 */
public MoverObserver(String name) {
	this.name = name;
	pid = Configuration.getPid(name + "." + PAR_PROTID);
}


//--------------------------------------------------------------------------
// Methods
//--------------------------------------------------------------------------

// Comment inherited from interface
// Do nothing, just for test
public boolean execute() {
	long time = peersim.core.CommonState.getTime();
	System.out.printf("Observing time: %d\n", time);
	for (int i = 0; i < Network.size(); i++) {
        if(Network.get(i).isUp()) {
        	Move nodeValue = (Move) Network.get(i).getProtocol(pid);
        	System.out.printf("Hi from node %d! (which has %d training examples and %d examples waiting!)\n", i, nodeValue.getNumTrained(), nodeValue.getTrainExamples().size());
        	//System.out.printf("Counts: %s\n", Arrays.toString(nodeValue.counts));
        	for(ArrayList trainExample : nodeValue.getTrainExamples()) {
        		Matrix train = (Matrix) trainExample.get(0);
        		Matrix test = new Matrix(nodeValue.getMatrix().getRowDimension(), nodeValue.getMatrix().getColumnDimension());
        		for(int j = 0; j < nodeValue.getMatrix().getRowDimension(); j++) {
            		Matrix current = nodeValue.getMatrix().getMatrix(new int[] {j}, 0, LearnInitializer.n - 1);
            		current.timesEquals(1 / current.normF());
            		test.setMatrix(new int[] {j}, 0, LearnInitializer.n - 1, current);
        		}
        		Matrix dot = test.times(train.transpose());
        		int max = 0;
        		double maxVal = 0;
        		for(int j = 0; j < dot.getRowDimension(); j++) {
        			if(dot.get(j, 0) > maxVal) {
        				max = j;
        				maxVal = dot.get(j, 0);
        			}
        		}
        		//System.out.printf("Setting to value %d in Node %d!\n", max, i);
        		Matrix current = nodeValue.getMatrix().getMatrix(new int[] {max}, 0, LearnInitializer.n - 1);
        		current.timesEquals(nodeValue.counts[max]);
        		//current.print(6, 4);
        		//train.times((int) trainExample.get(1)).print(6, 4);
        		current.plusEquals(train.times((int) trainExample.get(1)));
        		current.timesEquals(1.0 / (nodeValue.counts[max] + (int) trainExample.get(1)));
        		nodeValue.getMatrix().setMatrix(new int[] {max}, 0, LearnInitializer.n - 1, current);
        	    nodeValue.setNumTrained(nodeValue.getNumTrained() + (int) trainExample.get(1));
        	    nodeValue.counts[max] += (int) trainExample.get(1);
        	}
        	nodeValue.resetExamples();
        	nodeValue.getMatrix().print(5, 4);
        	System.out.printf("Norm of Diff Matrix: %f\n", nodeValue.value.minus(LearnInitializer.optimal).normF());
        	System.out.printf("Percent Norm: %f\n", nodeValue.value.minus(LearnInitializer.optimal).normF() / LearnInitializer.optimal.normF() * 100);
        	System.out.printf("Percent Accuracy: %f\n", LearnInitializer.acc(nodeValue.value, LearnInitializer.getSet()));
        	MoveDumper.machineOut(String.format("Node: %d;Timepoint: %d;NormDiff: %f", i, CommonState.getTime(), nodeValue.value.minus(LearnInitializer.optimal).normF()));
        	MoveDumper.machineOut(String.format("Node: %d;Timepoint: %d;NormDiffNormal: %f", i, CommonState.getTime(), nodeValue.value.minus(LearnInitializer.optimal).normF() / LearnInitializer.optimal.normF() * 100));
        	MoveDumper.machineOut(String.format("Node: %d;Timepoint: %d;Accuracy: %f", i, CommonState.getTime(), LearnInitializer.acc(nodeValue.value, LearnInitializer.getSet())));
        }
}
	return false;
}

//--------------------------------------------------------------------------

}
