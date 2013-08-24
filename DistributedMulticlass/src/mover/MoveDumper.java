package mover;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;

public class MoveDumper implements Control {
	
	public static String filename;
	
	public static File output;
	
	public static PrintStream mout;
	
	private static final String PAR_NAME = "file";
	
	public MoveDumper(String prefix) {
		filename = Configuration.getString(prefix + "." + PAR_NAME);
		output = new File(filename);
		try {
			mout = new PrintStream(output);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public boolean execute() {
		if(CommonState.getPhase() == CommonState.POST_SIMULATION) {
			MoveDumper.mout.close();
			System.out.println("Done with file output!");
		}
		return false;
	}
	
	public static void machineOut(String s) {
		mout.println(s);
	}

}
