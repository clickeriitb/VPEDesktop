package com.iitb.vpeub;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.iitb.vpeconfig.Config;
import com.iitb.vpeconfig.Target;


/**
 * Class to upload using avrdude, mostly copied from Uploader.java and ArvdudeUploader.java
 * from Arduino's source
 * @author vighnesh
 *
 */
public class Uploader {
	
	List<String> commandUploader;

	PrintStream out;
	PrintStream error;
	String base;
	private Target target;
	public Uploader(String base,String board,String port,OutputStream out,OutputStream error){
		
		this.out = new PrintStream(out);
		this.base = base;
		this.error = new PrintStream(error);
		target = null;
		if(board.equals("uno")) {
			target = Config.uno;
		}
		if(board.equals("atmega328")) {
			target = Config.atmega328;
		}
		
		if(target == null) {
			this.error.println("Board not Found");
			return;
		}
		
		commandUploader = new ArrayList<String>();

		String avrdudePath = base +File.separator+ "tools" + File.separator + "avrdude";
		commandUploader.add(avrdudePath);
		
		commandUploader.add("-C" + base + File.separator + "tools" + File.separator + "avrdude.conf" );
		commandUploader.add("-v");
		commandUploader.add("-v");
		commandUploader.add("-v");
		commandUploader.add("-v");
		commandUploader.add("-p" + target.getMCU());
		
		String protocol = target.getProtocol();
	    if (protocol.equals("stk500"))
	        protocol = "stk500v1"; 
		commandUploader.add("-V");
		commandUploader.add("-c" + protocol);
		commandUploader.add("-P" + port);
		commandUploader.add("-b" + target.getSpeed());
	    commandUploader.add("-D"); // don't erase
	    commandUploader.add("-Uflash:w:" + base + File.separator + "build" +File.separator  + "final" + ".hex:i");
	    
	    
	}
	
	public void upload() throws InterruptedException {
		exec(commandUploader);
	}
	void exec(List<String> commandList) throws InterruptedException {
		ProcessBuilder pb = new ProcessBuilder(commandList);
		pb.redirectError(new File(base + File.separator + "dude.log"));
		//pb.redirectError(new File("upload.log"));
		try {
			pb.start().waitFor();
		} catch (IOException e) {
			error.println(e.getMessage());
		}
		
	}
}
