package com.umeng.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;

import com.umeng.editor.decode.AXMLDoc;

public class Main {
	/**
	 * In:AndroidManifest.xml channel1,channel2,channel3...
	 * Out:channel1.xml,channel2.xml,channel3.xml...
	 * @param args
	 */
	public static void main(String[] args) {
		try{
//			Args arg = Args.parseArgs(args);
//
//			if(arg == null){
//				System.err.println("Usage:");
//				System.err.println("AndroidManifest.xml dir  xxx [xxx] [xxx]");
//				System.exit(0);
//			}

			Options options = new Options();
			options.addOption("f", true, "input AndroidManifest.xml");
			options.addOption("k", true, "meta-data key");
			options.addOption("v", true, "meta-data key ==> value");
			options.addOption("o", true, "out file name");

			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse( options, args);

			String xml = cmd.getOptionValue("f");
			String key = cmd.getOptionValue("k");
			String value = cmd.getOptionValue("v");
			String out = cmd.getOptionValue("o");
			if (xml == null || key == null || value == null || out == null ) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "java -jar axmleditor.jar ", options );
			}

			cloneAXML(new File(xml), key, value, out);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private static void cloneAXML(File axml, String key, String value, String out) throws Exception{
		
		AXMLDoc doc = new AXMLDoc();
		doc.parse(new FileInputStream(axml));
		
		ChannelEditor editor = new ChannelEditor(doc);
		editor.setChannelName(key);
		editor.setChannel(value);
		editor.commit();
		doc.build(new FileOutputStream(new File(out)));
	}
	
	static class Args {
		public String mAXML;
		public String mDir;
		public String[] mChannels;
		
		public static Args parseArgs(String[] args){
			Args arg = new Args();
			
			if(args == null || args.length < 2){
				return null;
			}
			
			arg.mAXML = args[0];
			int chanIndex = 1;
			if(args[1].equals("-dir")){
				if(args.length < 4){
					return null;
				}
				arg.mDir = args[2];
				chanIndex = 3;
			}
			
			String[] chans = new String[args.length-chanIndex];
			for(int i= chanIndex ; i< args.length; i++){
				chans[i-chanIndex] = args[i];
			}
			
			arg.mChannels = chans;
			return arg;
		}
	}
}
