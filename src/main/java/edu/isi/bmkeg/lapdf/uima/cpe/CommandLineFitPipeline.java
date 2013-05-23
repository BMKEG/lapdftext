package edu.isi.bmkeg.lapdf.uima.cpe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;

import edu.isi.bmkeg.lapdf.bin.CommandLineTool;
import edu.isi.bmkeg.lapdf.uima.ae.ParserRuleBasedClassfierAE;
import edu.isi.bmkeg.lapdf.uima.cr.DirectoryCollectionReader;
import edu.isi.bmkeg.utils.PipelineLauncher;

/**
 * The primary execution engine of the LAPDFText system.
 *
 */
public class CommandLineFitPipeline {

	private static Logger logger = Logger.getLogger(CommandLineFitPipeline.class);
	
	private List<AnalysisEngine> aeList = new ArrayList<AnalysisEngine>();
	
	private CollectionReader documentCollectionReader;
	
	private static String PDF_SUFFIX = ".pdf";
	
	private String modeNumber;

	/**
	 * Start time of the processing - used to compute elapsed time.
	 */
	private long mStartTime;

	public CommandLineFitPipeline(
			String inputDocumentsLocation,
			String ruleFileLocation, 
			boolean reportBlocks,
			boolean extractUnclassified, 
			String outputDocumentsLocation) throws ResourceInitializationException {
		
		TypeSystemDescription typeSystem = 
				TypeSystemDescriptionFactory.createTypeSystemDescription(
						"desc.typeSystem.LAPDFTextTypeSystemDescriptor"
						);
		
		logger.info("Loaded the type system...");

		documentCollectionReader = CollectionReaderFactory.createCollectionReader(
				DirectoryCollectionReader.class,
				DirectoryCollectionReader.DIRECTORY, inputDocumentsLocation,
				DirectoryCollectionReader.FILE_SUFFIX, PDF_SUFFIX,
				DirectoryCollectionReader.DIR_RECURSION, true, 
				DirectoryCollectionReader.ITEMS_TO_SKIP, -1, 
				DirectoryCollectionReader.END_INDEX, -1);
		
		AnalysisEngine pdfParserClassifier = null;
		
		if (reportBlocks) {
			pdfParserClassifier = ParserRuleBasedClassfierAE.createAnalysisEngine(
					typeSystem, 
					2, 
					reportBlocks,
					extractUnclassified, 
					outputDocumentsLocation,
					ruleFileLocation);
		} else {
			pdfParserClassifier = ParserRuleBasedClassfierAE.createAnalysisEngine(
					typeSystem, 
					3,
					reportBlocks,
					extractUnclassified, 
					outputDocumentsLocation,
					ruleFileLocation);
		}

		aeList.add(pdfParserClassifier);
		
	}

	public CommandLineFitPipeline(
			String inputDocumentsLocation,
			String ruleFileLocation, 
			boolean reportBlocks,
			boolean extractUnclassified, 
			String outputDocumentsLocation,
			int itemsToSkip, 
			int endIndex) throws ResourceInitializationException {
		
		TypeSystemDescription typeSystem = TypeSystemDescriptionFactory
				.createTypeSystemDescription("desc.typeSystem.LAPDFTextTypeSystemDescriptor");

		logger.info("Loaded the type system...");

		documentCollectionReader = CollectionReaderFactory.createCollectionReader(
						DirectoryCollectionReader.class, 
						DirectoryCollectionReader.DIRECTORY, inputDocumentsLocation, 
						DirectoryCollectionReader.FILE_SUFFIX, PDF_SUFFIX,
						DirectoryCollectionReader.DIR_RECURSION, true, 
						DirectoryCollectionReader.ITEMS_TO_SKIP, itemsToSkip, 
						DirectoryCollectionReader.END_INDEX, endIndex);
		
		AnalysisEngine pdfParserClassifier = null;
		
		if (reportBlocks) {
			pdfParserClassifier = ParserRuleBasedClassfierAE
					.createAnalysisEngine(typeSystem, 
							2, 
							reportBlocks,
							extractUnclassified, 
							outputDocumentsLocation,
							ruleFileLocation);
		} else {
			pdfParserClassifier = ParserRuleBasedClassfierAE
					.createAnalysisEngine(typeSystem, 
							3, 
							reportBlocks,
							extractUnclassified, 
							outputDocumentsLocation,
							ruleFileLocation);
		}

		aeList.add(pdfParserClassifier);
	
	}

	public CommandLineFitPipeline(
			String inputDocumentsLocation,
			boolean reportBlocks, 
			boolean extractUnclassified,
			String outputDocumentsLocation)
			throws ResourceInitializationException {

		TypeSystemDescription typeSystem = TypeSystemDescriptionFactory
				.createTypeSystemDescription("desc.typeSystem.LAPDFTextTypeSystemDescriptor");
		
		logger.info("Loaded the type system...");

		documentCollectionReader = CollectionReaderFactory
				.createCollectionReader(
						DirectoryCollectionReader.class,
						DirectoryCollectionReader.DIRECTORY, inputDocumentsLocation,
						DirectoryCollectionReader.FILE_SUFFIX, PDF_SUFFIX,
						DirectoryCollectionReader.DIR_RECURSION, true, 
						DirectoryCollectionReader.ITEMS_TO_SKIP, -1,
						DirectoryCollectionReader.END_INDEX, -1);

		AnalysisEngine pdfParserClassifier = ParserRuleBasedClassfierAE
				.createAnalysisEngine(typeSystem, 
						1, 
						reportBlocks,
						extractUnclassified, 
						outputDocumentsLocation);

		aeList.add(pdfParserClassifier);
	}

	public CommandLineFitPipeline(
			String inputDocumentsLocation,
			boolean reportBlocks, 
			boolean extractUnclassified,
			String outputDocumentsLocation, 
			int itemsToSkip,
			int endIndex) throws ResourceInitializationException {
		
		TypeSystemDescription typeSystem = TypeSystemDescriptionFactory
				.createTypeSystemDescription("desc.typeSystem.LAPDFTextTypeSystemDescriptor");
		logger.info("Loaded the type system...");

		documentCollectionReader = CollectionReaderFactory.createCollectionReader( 
				DirectoryCollectionReader.class,
				DirectoryCollectionReader.DIRECTORY, inputDocumentsLocation,
				DirectoryCollectionReader.FILE_SUFFIX, PDF_SUFFIX,
				DirectoryCollectionReader.DIR_RECURSION, true, 
				DirectoryCollectionReader.ITEMS_TO_SKIP, itemsToSkip, 
				DirectoryCollectionReader.END_INDEX, endIndex);

		AnalysisEngine pdfParserClassifier = ParserRuleBasedClassfierAE.createAnalysisEngine(
				typeSystem, 
				1, 
				reportBlocks,
				extractUnclassified, 
				outputDocumentsLocation);

		aeList.add(pdfParserClassifier);
	
	}

	public void run() {
		
		logger.info("Running Pipeline...");

		try {
			
			AnalysisEngine[] aeArray = aeList.toArray(new AnalysisEngine[0]);
			PipelineLauncher.runPipeline(documentCollectionReader, aeArray);
			// SimplePipeline.runPipeline(articleCollectionReader, aeArray);
		
		} catch (ResourceInitializationException e) {

			e.printStackTrace();

		} catch (UIMAException e) {

			e.printStackTrace();
		
		} catch (IOException e) {
		
			e.printStackTrace();

		}
	
	}

}
