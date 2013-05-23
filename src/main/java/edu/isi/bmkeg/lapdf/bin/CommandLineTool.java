package edu.isi.bmkeg.lapdf.bin;

import java.io.File;
import java.net.URL;

import edu.isi.bmkeg.lapdf.uima.cpe.CommandLineFitPipeline;

@Deprecated
public class CommandLineTool
{
	private static final String OPERATION_PDFEX_BLOCKIFY = "blockify";
	private static final String OPERATION_PDFEX_BLOCK_STATISTICS = "blockStatistics";
	private static final String OPERATION_PDFEX_BLOCKIFY_CLASSIFY = "blockifyClassify";
	private static final String OPERATION_FILTERED_LAYOUTAWARE_FULLTEXT_EXTRACTION = "extractFullText";

	public static void main(String args[])
	{
		CommandLineFitPipeline pipeline = null;

		if (args.length == 0) {
			printUsage();
			System.exit(1);
		}
		
		String operationType = args[0];
		String outputFolder = null;
		String inputFolder = null;
		String ruleFileLocation = null;
		
		if (operationType.equals(OPERATION_PDFEX_BLOCKIFY)) {

			if (args.length == 2) {
				
				inputFolder = args[1];
				outputFolder = args[1];
			
			} else if(args.length==3) {
			
				inputFolder = args[1];
				outputFolder = args[2];
			
			} else {
				printUsage();
				System.exit(1);
			}
			
			try {
				
				pipeline = new CommandLineFitPipeline(inputFolder, true, false, outputFolder);

			} catch (Exception e) {
				e.printStackTrace();
				printUsage();
			}

		} else if (operationType.equals(OPERATION_PDFEX_BLOCK_STATISTICS)) {
			
			if (args.length == 2) {

				inputFolder = args[1];
				outputFolder = args[1];
			
			} else if(args.length==3) {
				
				inputFolder = args[1];
				outputFolder = args[2];
			
			} else {
				
				printUsage();
				System.exit(1);
			
			}
			
			try {
				
				pipeline = new CommandLineFitPipeline(inputFolder, true, true, outputFolder);
	
			} catch (Exception e) {
				e.printStackTrace();
				printUsage();
			}
			
		} else if (operationType.equals(OPERATION_PDFEX_BLOCKIFY_CLASSIFY)) {

			if (args.length == 3) {

				inputFolder = args[1];
				outputFolder = args[1];
				ruleFileLocation = args[2];
				
			} else if(args.length == 4) {

				inputFolder = args[1];
				ruleFileLocation = args[2];
				outputFolder = args[3];

			} else {
				printUsage();
				System.exit(1);
			}
			try {
				
				pipeline = new CommandLineFitPipeline(inputFolder, ruleFileLocation, false, true, outputFolder);
			
			} catch (Exception e) {
				e.printStackTrace();
				printUsage();
			}

		} else if (operationType.equals(OPERATION_FILTERED_LAYOUTAWARE_FULLTEXT_EXTRACTION)) {

			if ( args.length > 4 || args.length < 2 ) {
				
				printUsage();
				System.exit(1);
			
			} else if(args.length == 4) {

				inputFolder = args[1];
				ruleFileLocation = args[2];
				outputFolder = args[3];
				
			} else if( args.length == 3) {
				
				inputFolder = args[1];
				outputFolder = args[1];
				ruleFileLocation = args[2];
			
			} else if( args.length == 2 ) {

				inputFolder = args[1];
				outputFolder = args[1];
				
				URL u = CommandLineFitPipeline.class.getClassLoader().getResource(
						"rules/general.drl");
				File ruleFile = new File( u.getPath() );
				ruleFileLocation = ruleFile.getPath();

			}
			
			try {
				
				//in this mode since we have a rule file we set extractUnclassified to false and report blocks to false
				pipeline = new CommandLineFitPipeline(inputFolder, ruleFileLocation, false, false, outputFolder);

			} catch (Exception e) {
				e.printStackTrace();
				printUsage();
			}
			
		} else {
			printUsage();
		}
		
		if( pipeline != null ){
			pipeline.run();
		}
	
	}

	public static void printUsage() {
		System.out.println("Usage Guidelines");
		System.out.println("1. Blockifying PDF: Use this option if you want to blockify the PDF and output the blocks XML");
		System.out.println("Usage\nArgument 1:" + OPERATION_PDFEX_BLOCKIFY + "\nArgument 2: The directory path where the PDFs are located \nArgument 3[Optional]: The directory path where output of blockify will be placed");
		System.out.println("\n2. Blockifying PDF and reporting Features: Use this option if you want to blockify the PDF and output the blocks XML and generate a report file that serves as a guide in crafting a rule file for the sectionify step.");
		System.out.println("Usage\nArgument 1:" + OPERATION_PDFEX_BLOCK_STATISTICS + "\nArgument 2: The directory path where the PDFs are located \nArgument 3[Optional]: The directory path where output of blockify and the feature reports will be placed");
		System.out.println("\n3. Blockifying and sectionifying PDF: Use this option if you want to blockify the PDF and do rhetorical classification of the blocks.It will output an openAccess based XML");
		System.out.println("Usage\nArgument 1:" + OPERATION_PDFEX_BLOCKIFY_CLASSIFY + "\nArgument 2: The directory path where the PDFs are located\nArgument 3: The path of the rule file for Drools \nArgument 4[Optional]: The directory path where output of blockify and sectionify will be placed");
		System.out.println("\n4. Extracting full text from PDF: Use this argument if you want to extract particular section from the openAccess based XML");
		System.out.println("Usage\nArgument 1:" + OPERATION_FILTERED_LAYOUTAWARE_FULLTEXT_EXTRACTION + "\nArgument 2: The directory path where the PDFs are located\nArgument 3: The path of the rule file for Drools");

		/*System.out.println("4. Extracting sections from openAccess based XML: Use this argument if you want to extract particular section from the openAccess based XML");
		System.out.println("Usage\nArgument 1:" + OPERATION_SECTION_EXTRACTION + "\nArgument 2: The path of the openAccess based XML\nArgument 3: The location where the output file should be created\nArgument 4: Type of the section");
		System.out.println("Please use one of the following section type:");
		System.out.println("1. " + SectionBasedTextExtractor.ELEMENT_ABSTRACT);
		System.out.println("2. " + SectionBasedTextExtractor.ELEMENT_INTRODUCTION);
		System.out.println("3. " + SectionBasedTextExtractor.ELEMENT_MATERIALS_METHODS);
		System.out.println("4. " + SectionBasedTextExtractor.ELEMENT_DISCUSSION);
		System.out.println("5. " + SectionBasedTextExtractor.ELEMENT_RESULTS);
		System.out.println("6. " + SectionBasedTextExtractor.ELEMENT_CONCLUSIONS);
		 */
	}

}
