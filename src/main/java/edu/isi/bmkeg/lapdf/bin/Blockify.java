package edu.isi.bmkeg.lapdf.bin;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import edu.isi.bmkeg.lapdf.controller.LapdfEngine;
import edu.isi.bmkeg.lapdf.controller.LapdfMode;
import edu.isi.bmkeg.lapdf.model.LapdfDocument;
import edu.isi.bmkeg.utils.Converters;

public class Blockify {

	private static String USAGE = "usage: <input-dir-or-file> [<output-dir>]\n\n"
			+ "<input-dir-or-file> - the full path to the PDF file or directory to be extracted \n"
			+ "<output-dir> (optional or '-') - the full path to the output directory \n\n"
			+ "Running this command on a PDF file or directory will attempt to generate \n"
			+ "one XML document per file with unnannotated text chunks .\n";

	public static void main(String args[]) throws Exception	{

		LapdfEngine engine = new LapdfEngine();

		if (args.length < 1 ) {
			System.err.println(USAGE);
			System.exit(1);
		}
		
		String inputFileOrDirPath = args[0];
		String outputDirPath = "";
							
		File inputFileOrDir = new File( inputFileOrDirPath ); 
		if( !inputFileOrDir.exists() ) {
			System.err.println(USAGE);
			System.err.println("Input file / dir '" + inputFileOrDirPath + "' does not exist.");
			System.err.println("Please include full path");
			System.exit(1);
		}
		
		// output folder is set.
		if ( args.length > 1 ) {	
			outputDirPath = args[1];
		} else {
			outputDirPath = "-";
		}
		
		if( outputDirPath.equals( "-") ) {
			if( inputFileOrDir.isDirectory() ) {
				outputDirPath = inputFileOrDirPath;
			} else {
				outputDirPath = inputFileOrDir.getParent();				
			}
		}
		
		File outDir = new File( outputDirPath ); 
		if( !outDir.exists() ) {
			outDir.mkdir();
		}  
		
		if( inputFileOrDir.isDirectory() ){

			Pattern patt = Pattern.compile("\\.pdf$");
			Map<String, File> inputFiles = Converters.recursivelyListFiles(inputFileOrDir, patt);
			Iterator<String> it = inputFiles.keySet().iterator();
			while( it.hasNext() ) {
				String key = it.next();
				File pdf = inputFiles.get(key);
				String pdfStem = pdf.getName();
				pdfStem = pdfStem.replaceAll("\\.pdf", "");
	
				String outXmlPath = Converters.mimicDirectoryStructure(inputFileOrDir, outDir, pdf).getPath();
				outXmlPath = outXmlPath.replaceAll("\\.pdf",  "_spatial.xml");
	
				File outFile = new File( outXmlPath );
				
				try {
	
					LapdfDocument lapdf = engine.blockifyPdfFile(pdf);
					engine.writeSpatialXmlToFile(lapdf, outFile);
					
				} catch (Exception e) {
				
					e.printStackTrace();
				
				}
				
			} 
			
		} else {
			
			String pdfStem = inputFileOrDir.getName();
			pdfStem = pdfStem.replaceAll("\\.pdf", "");

			String outPath = outDir + "/" + pdfStem + "_spatial.xml";
			File outFile = new File(outPath);
			
			LapdfDocument lapdf = engine.blockifyPdfFile(inputFileOrDir);
			engine.writeSpatialXmlToFile(lapdf, outFile);
			
		}
			
	}

}
