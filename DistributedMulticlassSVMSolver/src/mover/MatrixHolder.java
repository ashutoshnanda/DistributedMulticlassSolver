package mover;

import java.util.ArrayList;

import peersim.core.Protocol;
import Jama.Matrix;

public class MatrixHolder implements Protocol {

	protected Matrix value;
	
	protected Matrix subgradient;

	protected ArrayList<Matrix> subgradients;

	protected ArrayList<Matrix> classifiers;
	
	protected ArrayList<ArrayList> examples;
	
	protected ArrayList<Double> accuracies;

	public MatrixHolder(String prefix) {
	}

	public Object clone() {
		MatrixHolder mh = null;
		try {
			mh = (MatrixHolder) super.clone();
		} catch (CloneNotSupportedException e) {
		} // never happens
		return mh;
	}

	public String toString() {
		return "" + value.toString();
	}
	// Maybe implement own version of MatrixObject.toString()
	
}