package edu.isi.bmkeg.lapdf.bin;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import edu.isi.bmkeg.lapdf.controller.LapdfEngine;
import edu.isi.bmkeg.lapdf.controller.LapdfMode;
import edu.isi.bmkeg.lapdf.model.Block;
import edu.isi.bmkeg.lapdf.model.LapdfDocument;
import edu.isi.bmkeg.lapdf.uima.cpe.CommandLineFitPipeline;
import edu.isi.bmkeg.lapdf.utils.PdfDirWatcher;
import edu.isi.bmkeg.utils.Converters;

public class WatchDirectory {
	
	public static String USAGE = "usage: <COMMAND> <dir-to-be-watched> <output-dir> [<rule-file>]\n\n"
			+ "<COMMAND> - the command to be executed: \n" 
			+ " - " + PdfDirWatcher.IMAGIFY_BLOCKS + "\n" 
			+ " - " + PdfDirWatcher.IMAGIFY_SECTIONS + "\n" 
			+ " - " + PdfDirWatcher.BLOCKIFY + "\n" 
			+ " - " + PdfDirWatcher.BLOCKIFY_CLASSIFY + "\n" 
			+ " - " + PdfDirWatcher.READ_SECTION_TEXT + "\n" 
			+ "<dir-to-be-watched> - the full path to the directory to be watched \n" 
			+ "<output-dir> (optional or '-') - the full path to the output directory \n" 
			+ "<rule-file> (optional or '-') - the full path to the rule file \n\n" 
			+ "This program maintains a watcher on this directory to execute the \n" 
			+ "denoted command on any PDF files added to the directory. \n"
			+ "The system will then delete the appropriate files and folders\n"
			+ "when the originating PDF file is removed.\n";
	
	public static void main(String args[]) throws Exception {

		if (args.length < 3 ) {
			System.err.println(USAGE);
			System.exit(1);
		}
		
		String type = args[0];
		
		String inputDirPath = args[1];
		String outputDirPath = "";
		String ruleFilePath = "";
							
		File inputDir = new File( inputDirPath ); 
		if( !inputDir.exists() ) {
			System.err.println(USAGE);
			System.err.println("Input file / dir '" + inputDirPath + "' does not exist.");
			System.err.println("Please include full path");
			System.exit(1);
		}
		
		// output folder must not be contained in the input set.
		outputDirPath = args[2];
				
		File outDir = new File( outputDirPath ); 
		if( !outDir.exists() ) {
			outDir.mkdir();
		}  

		File temp = new File(outDir.getPath());
		while( temp.getParentFile() != null ) {
			if( temp.equals(inputDir) ) {
				System.err.println(USAGE);
				System.err.println(outputDirPath + " cannot be inside " + inputDirPath);
				System.exit(1);
			}
			temp = temp.getParentFile();
		}
		
		// output folder is set.
		File ruleFile =  null;
		if ( args.length > 3 ) {	
			ruleFilePath = args[3];
		} else {
			ruleFilePath = "-";
		}
		
		if( ruleFilePath.equals( "-" ) ) {
			ruleFile = Converters.extractFileFromJarClasspath("rules/general.drl");
		} else {
			ruleFile = new File( ruleFilePath );
		}
		
		if( !ruleFile.exists() ) {
			System.err.println(USAGE);
			System.err.println(ruleFilePath + " does not exist.");
			System.err.println("Please include full path");
			System.exit(1);
		}  
		
		// then run the watcher to sit around and wait for changes to the directory. 
		PdfDirWatcher p = new PdfDirWatcher(type, inputDir, outDir, ruleFile);
		
		//
		// start by running the text extraction pipeline over the folders.
		//
		Pattern patt = Pattern.compile("\\.pdf$");
		Map<String, File> inputFiles = Converters.recursivelyListFiles(
				inputDir, patt);
		Iterator<String> it = inputFiles.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			File pdf = inputFiles.get(key);
		
			p.execute(pdf);
		
		}
		
		p.setUpLiveFolder();

		p.run();
			
	}


}
