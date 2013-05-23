package edu.isi.bmkeg.lapdf.uima.ae;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.jpedal.exception.PdfException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;

import edu.isi.bmkeg.lapdf.controller.LapdfEngine;
import edu.isi.bmkeg.lapdf.controller.LapdfMode;
import edu.isi.bmkeg.lapdf.extraction.exceptions.AccessException;
import edu.isi.bmkeg.lapdf.extraction.exceptions.ClassificationException;
import edu.isi.bmkeg.lapdf.extraction.exceptions.EncryptionException;
import edu.isi.bmkeg.lapdf.model.LapdfDocument;
import edu.isi.bmkeg.utils.ISI_UIMA_PDFUtils;

public class ParserRuleBasedClassfierAE extends JCasAnnotator_ImplBase {

	private static Logger logger = Logger.getLogger(ParserRuleBasedClassfierAE.class);
		
	public static final String MODE = ConfigurationParameterFactory
			.createConfigurationParameterName(ParserRuleBasedClassfierAE.class, "mode");
	@ConfigurationParameter(mandatory = true, 
			description = "This is the mode of operation.")
	private int mode;

	public static final String RULE_FILE = ConfigurationParameterFactory
			.createConfigurationParameterName(ParserRuleBasedClassfierAE.class, "ruleFile");
	@ConfigurationParameter(mandatory = false, 
			description = "This is the rule file used for block classification.")
	protected String ruleFile;

	public static final String REPORT_BLOCKS = ConfigurationParameterFactory
			.createConfigurationParameterName(ParserRuleBasedClassfierAE.class, "reportBlocks");
	@ConfigurationParameter(mandatory = true, 
			description = "This is the flag used to trigger debug reporting.")
	protected Boolean reportBlocks;

	public static final String EXTRACT_UNCLASSIFIED = ConfigurationParameterFactory
			.createConfigurationParameterName(ParserRuleBasedClassfierAE.class, "extractUnclassified");
	@ConfigurationParameter(mandatory = true, 
			description = "this flag is used to decide whether unclassified flow aware output " + 
			"text is required.")
	protected boolean extractUnclassified;

	public static final String OUTPUT_FOLDER = ConfigurationParameterFactory
			.createConfigurationParameterName(ParserRuleBasedClassfierAE.class, "outputFolder");
	@ConfigurationParameter(mandatory = true, 
			description = "This is the location of the output for debug and results.")
	private String outputFolder;

	protected File outputFolderFileDescriptor;

	private LapdfEngine pdfEng;

	protected LapdfDocument doc;

	public void initialize(UimaContext uimaContext)
			throws ResourceInitializationException {

		try {
			
			super.initialize(uimaContext);

			mode = (Integer) uimaContext.getConfigParameterValue(MODE);
			
			ruleFile = (String) uimaContext.getConfigParameterValue(RULE_FILE);
			
			if (ruleFile != null) {
				logger.info("Using rulefile " + ruleFile);
				this.pdfEng = new LapdfEngine(new File(ruleFile));
			} else {
				this.pdfEng = new LapdfEngine();
			}
			
			extractUnclassified = (Boolean) uimaContext
					.getConfigParameterValue(EXTRACT_UNCLASSIFIED);
			
			reportBlocks = (Boolean) uimaContext
					.getConfigParameterValue(REPORT_BLOCKS);
			
			outputFolder = (String) uimaContext
					.getConfigParameterValue(OUTPUT_FOLDER);
			
			outputFolderFileDescriptor = new File(outputFolder);
			
			if (!outputFolderFileDescriptor.exists()) {
				logger.info(outputFolderFileDescriptor.getAbsolutePath()
						+ " does not exist! Creating it!!");
				outputFolderFileDescriptor.mkdir();
			}
			
		} catch (Exception e) {

			throw new ResourceInitializationException(e);

		}
	
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {

		String inputPDFFilePath = ISI_UIMA_PDFUtils
				.getDocumentSecondaryID(jcas);
		
		File inFile = new File(inputPDFFilePath);
		String inputPDFFileName = inFile.getName();
		String stem = inputPDFFileName.substring(0, inputPDFFileName.lastIndexOf("."));
		
		try {
			
			File outDir = new File(outputFolderFileDescriptor.getPath() + "/" + stem); 
			if( outDir.exists() ) {
				return; 
			} else {
				outDir.mkdir();
			}
			
			if( mode == LapdfMode.BLOCK_ONLY ) {
				
				logger.info("Running block detection on " + inputPDFFilePath);
				
				this.pdfEng.processBlocks(inFile, outDir, reportBlocks, extractUnclassified);				

			} else if( mode == LapdfMode.CLASSIFY ) {
				
				this.pdfEng.processClassify(inFile, outDir, reportBlocks, extractUnclassified);
				
			} else if( mode == LapdfMode.SECTION_FILTER ) {
				
				this.pdfEng.processSectionFilter(inFile, outDir, reportBlocks, extractUnclassified);	

			}

		} catch (Exception e) {
			
			throw new AnalysisEngineProcessException(e);

		} 
		
	}



	public static AnalysisEngine createAnalysisEngine(
			TypeSystemDescription typeSystem, 
			int mode, 
			boolean reportBlocks,
			boolean extractUnclassified, 
			String outputFolderName,
			String ruleFileName) throws ResourceInitializationException {

		AnalysisEngineDescription aed = AnalysisEngineFactory.createPrimitiveDescription(
						ParserRuleBasedClassfierAE.class,
						typeSystem,
						// name, value
						ParserRuleBasedClassfierAE.OUTPUT_FOLDER, outputFolderName,
						ParserRuleBasedClassfierAE.RULE_FILE, ruleFileName,
						ParserRuleBasedClassfierAE.MODE, mode,
						ParserRuleBasedClassfierAE.REPORT_BLOCKS, reportBlocks,
						ParserRuleBasedClassfierAE.EXTRACT_UNCLASSIFIED, extractUnclassified);

		return AnalysisEngineFactory.createPrimitive(aed);
	}

	public static AnalysisEngine createAnalysisEngine(
			TypeSystemDescription typeSystem, 
			int mode, 
			boolean reportBlocks,
			boolean extractUnclassified, 
			String outputFolderName)
			throws ResourceInitializationException {

		AnalysisEngineDescription aed = AnalysisEngineFactory.createPrimitiveDescription(
						ParserRuleBasedClassfierAE.class,
						typeSystem,
						// name, value
						ParserRuleBasedClassfierAE.OUTPUT_FOLDER, outputFolderName,
						ParserRuleBasedClassfierAE.RULE_FILE, null,
						ParserRuleBasedClassfierAE.MODE, mode,
						ParserRuleBasedClassfierAE.REPORT_BLOCKS, reportBlocks,
						ParserRuleBasedClassfierAE.EXTRACT_UNCLASSIFIED, extractUnclassified);

		return AnalysisEngineFactory.createPrimitive(aed);
	}
}
