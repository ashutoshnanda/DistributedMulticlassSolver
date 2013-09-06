package mover;

import peersim.config.Configuration;
import peersim.core.Control;

public class Recorder implements Control{

	public static final String PAR_PROTID = "protocol";

	private final String name;

	private final int pid;

	public Recorder(String name) {
		this.name = name;
		pid = Configuration.getPid(name + "." + PAR_PROTID);
	}
	
	
	public boolean execute() {
		System.out.println("RECORDER IS RECORDING!");
		return false;
	}

}
