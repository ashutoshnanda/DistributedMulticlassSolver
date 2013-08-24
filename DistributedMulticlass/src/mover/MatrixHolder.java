package mover;

import java.util.ArrayList;

import peersim.core.Protocol;
import Jama.Matrix;

/**
 * The task of this protocol is to store a single double value and make it
 * available through the {@link SingleValue} interface.
 *
 * @author Alberto Montresor
 * @version $Revision: 1.6 $
 */
public class MatrixHolder implements Protocol
{

//--------------------------------------------------------------------------
//Fields
//--------------------------------------------------------------------------
	
/** Value held by this protocol */
protected Matrix value;

protected int numTrained;

protected ArrayList<ArrayList> examples;

protected int[] counts;
	

//--------------------------------------------------------------------------
//Initialization
//--------------------------------------------------------------------------

/**
 * Does nothing.
 */
public MatrixHolder(String prefix)
{
	this.numTrained = 0;
}

//--------------------------------------------------------------------------

/**
 * Clones the value holder.
 */
public Object clone()
{
	MatrixHolder mh=null;
	try { mh=(MatrixHolder)super.clone(); }
	catch( CloneNotSupportedException e ) {} // never happens
	return mh;
}

//--------------------------------------------------------------------------
//methods
//--------------------------------------------------------------------------

/**
 * @inheritDoc
 */
public Matrix getMatrix()
{
	return value;
}

//--------------------------------------------------------------------------

/**
 * @inheritDoc
 */
public void setMatrix(Matrix value)
{
	this.value = value;
}

//--------------------------------------------------------------------------

public int getNumTrained() {
	return numTrained;
}

public void setNumTrained(int numTrained) {
	this.numTrained = numTrained;
}

public void addTrainExample(ArrayList example) {
	this.examples.add(example);
}

/**
 * Returns the value as a string.
 */
public String toString() { return ""+value.toString(); }
//Maybe implement own version of MatrixObject.toString()

public ArrayList<ArrayList> getTrainExamples() {
	return this.examples;
}

public void resetExamples() {
	this.examples = new ArrayList<ArrayList>();
}

public int numExamplesForClass(int classValue) {
	return counts[classValue];
}

}