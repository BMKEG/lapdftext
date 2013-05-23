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
import edu.isi.bmkeg.lapdf.model.Block;
import edu.isi.bmkeg.lapdf.model.LapdfDocument;
import edu.isi.bmkeg.utils.Converters;

public class ReadSectionText {

	private static String USAGE = "usage: <input-dir-or-file> [<output-dir>] [<rule-file>] [<sec1> ... <secN>]\n\n"
			+ "<input-dir-or-file> - the full path to the PDF file or directory to be extracted \n"
			+ "<output-dir> (optional or '-') - the full path to the output directory \n"
			+ "<rule-file> (optional or '-') - the full path to the rule file \n"
			+ "<sec1> ... <secN> (optional) - a list of section names to be included in the dump \n\n"
			+ "Running this command on a PDF file or directory will attempt to extract uninterrupted\n"
			+ "two-column- formatted text of the main narrative section of the paper with one \n"
			+ "font change (i.e. for papers that use a smaller font for methods sections).\n"
			+ "Figure legends are moved to the end of the paper (but included), and \n"
			+ "tables are dropped.\n\n"
			+ "Please send failure examples where this basic behavior fails to \n"
			+ "'gully@usc.edu' for troubleshooting.\n";

	public static void main(String args[]) throws Exception {

		LapdfEngine engine = new LapdfEngine();

		if (args.length < 1) {
			System.err.println(USAGE);
			System.exit(1);
		}

		String inputFileOrDirPath = args[0];
		String outputDirPath = "";
		String ruleFilePath = "";

		File inputFileOrDir = new File(inputFileOrDirPath);
		if (!inputFileOrDir.exists()) {
			System.err.println(USAGE);
			System.err.println("Input file / dir '" + inputFileOrDirPath
					+ "' does not exist.");
			System.err.println("Please include full path");
			System.exit(1);
		}

		// output folder is set.
		if (args.length > 1) {
			outputDirPath = args[1];
		} else {
			outputDirPath = "-";
		}

		if (outputDirPath.equals("-")) {
			if (inputFileOrDir.isDirectory()) {
				outputDirPath = inputFileOrDirPath;
			} else {
				outputDirPath = inputFileOrDir.getParent();
			}
		}

		File outDir = new File(outputDirPath);
		if (!outDir.exists()) {
			outDir.mkdir();
		}

		// output folder is set.
		File ruleFile = null;
		if (args.length > 2) {
			ruleFilePath = args[2];
		} else {
			ruleFilePath = "-";
		}

		if (ruleFilePath.equals("-")) {
			ruleFile = Converters
					.extractFileFromJarClasspath("rules/general.drl");
		} else {
			ruleFile = new File(ruleFilePath);
		}

		if (!ruleFile.exists()) {
			System.err.println(USAGE);
			System.err.println(ruleFilePath + " does not exist.");
			System.err.println("Please include full path");
			System.exit(1);
		}

		// section name stack is set.
		List<Set<String>> stack = new ArrayList<Set<String>>();
		String sec1 = "";
		if (args.length > 3) {
			sec1 = args[3];
		} else {
			sec1 = "-";
		}

		// default sections to include are
		// * headings and body interwoven in the text
		// * figure legends at the end
		if (sec1.equals("-")) {

			Set<String> sections = new HashSet<String>();
			sections.add(Block.TYPE_BODY);
			sections.add(Block.TYPE_HEADING);
			stack.add(sections);
			sections = new HashSet<String>();
			sections.add(Block.TYPE_FIGURE_LEGEND);
			stack.add(sections);

		} else {
			for (int i = 3; i < args.length; i++) {
				Set<String> sections = new HashSet<String>();
				sections.add(args[i]);
				stack.add(sections);
			}
		}

		if( inputFileOrDir.isDirectory() ){

			Pattern patt = Pattern.compile("\\.pdf$");
			Map<String, File> inputFiles = Converters.recursivelyListFiles(
					inputFileOrDir, patt);
			Iterator<String> it = inputFiles.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				File pdf = inputFiles.get(key);
				String pdfStem = pdf.getName();
				pdfStem = pdfStem.replaceAll("\\.pdf$", "");
	
				String outPath = Converters.mimicDirectoryStructure(inputFileOrDir,
						outDir, pdf).getPath();
				outPath = outPath.replaceAll("\\.pdf$", "") + "_fullText.txt";
				File outFile = new File(outPath);
	
				try {
	
					LapdfDocument lapdf = engine.blockifyPdfFile(pdf);
					engine.classifyDocument(lapdf, ruleFile);
					engine.writeTextToFile(lapdf, stack, outFile);
	
				} catch (Exception e) {
	
					e.printStackTrace();
	
				}
			
			}
		
		} else { 
			
			String pdfStem = inputFileOrDir.getName();
			pdfStem = pdfStem.replaceAll("\\.pdf$", "");

			String outPath = outDir + "/" + pdfStem + "_fullText" + ".txt";
			File outFile = new File(outPath);

			LapdfDocument lapdf = engine.blockifyPdfFile(inputFileOrDir);
			engine.classifyDocument(lapdf, ruleFile);
			engine.writeTextToFile(lapdf, stack, outFile);
			
		}
	
	}

}