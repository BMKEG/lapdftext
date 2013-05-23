package edu.isi.bmkeg.lapdf.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jpedal.exception.PdfException;

import edu.isi.bmkeg.lapdf.classification.ruleBased.RuleBasedChunkClassifier;
import edu.isi.bmkeg.lapdf.extraction.exceptions.AccessException;
import edu.isi.bmkeg.lapdf.extraction.exceptions.ClassificationException;
import edu.isi.bmkeg.lapdf.extraction.exceptions.EncryptionException;
import edu.isi.bmkeg.lapdf.model.Block;
import edu.isi.bmkeg.lapdf.model.ChunkBlock;
import edu.isi.bmkeg.lapdf.model.LapdfDocument;
import edu.isi.bmkeg.lapdf.model.PageBlock;
import edu.isi.bmkeg.lapdf.model.RTree.RTModelFactory;
import edu.isi.bmkeg.lapdf.model.ordering.SpatialOrdering;
import edu.isi.bmkeg.lapdf.parser.RuleBasedParser;
import edu.isi.bmkeg.lapdf.text.SectionsTextWriter;
import edu.isi.bmkeg.lapdf.text.SpatialLayoutFeaturesReportGenerator;
import edu.isi.bmkeg.lapdf.text.SpatiallyOrderedChunkTextWriter;
import edu.isi.bmkeg.lapdf.text.SpatiallyOrderedChunkTypeFilteredTextWriter;
import edu.isi.bmkeg.lapdf.utils.JPedalPDFRenderer;
import edu.isi.bmkeg.lapdf.utils.PageImageOutlineRenderer;
import edu.isi.bmkeg.lapdf.xml.OpenAccessXMLWriter;
import edu.isi.bmkeg.lapdf.xml.SpatialXMLWriter;
import edu.isi.bmkeg.utils.Converters;


/**
 * Basic Java API to high-level LAPDFText functionality, including:
 *
 * 1) Gathering layout statistics for the PDF file
 * 2) Running Block-based spatial chunker on PDF.
 * 3) Classifying texts of blocks in the file to categories based on a rule file.
 * 4) Outputting text or XML to file
 * 5) Rendering pages images of text layout or the original PDF file as PNG files
 * 6) Serializing LAPDFText object to a VPDMf database record.
 * 
 * @author burns
 *
 */
public class LapdfEngine  {

	private static Logger logger = Logger.getLogger(LapdfEngine.class);

	private RuleBasedParser parser;

	private File ruleFile;

	private boolean imgFlag = false;
	
	private JPedalPDFRenderer imagifier = new JPedalPDFRenderer();
	

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public LapdfEngine() 
			throws Exception {

		this.parser = new RuleBasedParser(new RTModelFactory());
		URL u = this.getClass().getClassLoader().getResource("rules/general.drl");
		this.setRuleFile(new File(u.getPath()));

	}

	public LapdfEngine(File ruleFile) 
			throws Exception {

		this.parser = new RuleBasedParser(new RTModelFactory());
		this.setRuleFile(ruleFile);
	
	}
	
	public LapdfEngine(boolean imgFlag) 
			throws Exception  {

		this.parser = new RuleBasedParser(new RTModelFactory());
		URL u = this.getClass().getClassLoader().getResource("rules/general.drl");
		this.setRuleFile(new File(u.getPath()));
		this.setImgFlag(imgFlag);

	}
	
	public LapdfEngine(File ruleFile, boolean imgFlag) throws Exception {

		this.parser = new RuleBasedParser(new RTModelFactory());
		this.setRuleFile(ruleFile);
		this.setImgFlag(imgFlag);

	}	
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public RuleBasedParser getParser() {
		return parser;
	}

	public void setParser(RuleBasedParser parser) {
		this.parser = parser;
	}
	
	public boolean isImgFlag() {
		return imgFlag;
	}

	public void setImgFlag(boolean imgFlag) {
		this.imgFlag = imgFlag;
	}
	
	public File getRuleFile() {
		return ruleFile;
	}

	public void setRuleFile(File ruleFile) {
		this.ruleFile = ruleFile;
	}
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public void processBlocks(File inFile, File outDir,
			boolean reportBlocks, boolean extractUnclassified) throws Exception {

		String stem = inFile.getName();
		stem = stem.substring(0, stem.lastIndexOf("."));
		
		this.parser.setPath(outDir.getPath());
		
		LapdfDocument doc = blockifyPdfFile(inFile);
		
		if (doc == null) {
			logger.info("Error encountered while performing block detection." +
					" Skipping " + inFile.getPath() + " because doc is null");
			return;
		}

		logger.info("Writing spatial block xml to " + outDir.getPath() + "/"
				+ stem + "_spatial.xml");

		if( this.isImgFlag() )
			this.renderImageOutlines(doc, outDir, stem, LapdfMode.BLOCK_ONLY);
		
		SpatialXMLWriter sxw = new SpatialXMLWriter();
		sxw.write(doc, outDir.getPath() + "/" + stem + "_spatial.xml");

		if (reportBlocks) {

			logger.info("Running block feature reporter on " + inFile.getPath());

			SpatialLayoutFeaturesReportGenerator slfrg = new SpatialLayoutFeaturesReportGenerator();
			slfrg.write(doc, outDir.getPath() + "/" + stem + "_spatialFeatures.dat");

		}

		if (extractUnclassified) {

			SpatiallyOrderedChunkTextWriter soctw = new SpatiallyOrderedChunkTextWriter();
			soctw.write(doc, outDir.getPath() + "/" + stem + "_unclassifiedFlowAwareText.dat");

		}

	}
	
	public void processClassify(File inFile, File outDir,
			boolean reportBlocks, boolean extractUnclassified) 
					throws Exception {

		String stem = inFile.getName();
		stem = stem.substring(0, stem.lastIndexOf("."));
		
		this.parser.setPath(outDir.getPath());
		
		LapdfDocument doc = blockifyPdfFile(inFile);
		if (doc == null) {

			logger.info("Error encountered while performing block detection. Skipping "
					+ inFile.getPath() + " because doc is null");

			return;

		}

		logger.info("Writing spatial block xml to " + outDir.getPath() + "/"
				+ stem + "_spatial.xml");

		SpatialXMLWriter sxw = new SpatialXMLWriter();
		sxw.write(doc, outDir.getPath() + "/" + stem + "_spatial.xml");

		logger.info("Running block classification on "
				+ inFile.getPath());
		classifyDocument(doc, this.getRuleFile());
		
		if( this.isImgFlag() )
			this.renderImageOutlines(doc, outDir, stem, LapdfMode.CLASSIFY);
		
		logger.info("Writing block classified XML in OpenAccess format "
						+ outDir.getPath() + "/" + stem + "_rhetorical.xml");
		
		OpenAccessXMLWriter oaxw = new OpenAccessXMLWriter();
		oaxw.write(doc, outDir.getPath() + "/" + stem + "_rhetorical.xml");
		
		if (reportBlocks) {
			
			logger.info("Running block feature reporter on "
					+ inFile.getPath());
			
			SpatialLayoutFeaturesReportGenerator slfrg = 
					new SpatialLayoutFeaturesReportGenerator();
			
			slfrg.write(doc, outDir.getPath() + "/" + stem + "_spatialFeatures.dat");
		
		}
		
		if (extractUnclassified) {
		
			SpatiallyOrderedChunkTextWriter soctw = new SpatiallyOrderedChunkTextWriter();
			soctw.write(doc, outDir.getPath() + "/" + stem + "_unclassifiedFlowAwareText.dat");
		
		}

	}
	
	public void processSectionFilter(File inFile, File outDir,
			boolean reportBlocks, boolean extractUnclassified) 
					throws Exception {

		String stem = inFile.getName();
		stem = stem.substring(0, stem.lastIndexOf("."));

		this.parser.setPath(outDir.getPath());
		
		logger.info("Running block detection on " + inFile.getPath());
							
		LapdfDocument doc = blockifyPdfFile(inFile);
			
		if (doc == null) {
			logger.info("Error encountered while performing block detection. Skipping "
						+ inFile.getPath() + " because doc is null");
			return;
		}

		logger.info("Running block classification on " + inFile.getPath());

		classifyDocument(doc, this.getRuleFile());
		
		if( this.isImgFlag() )
			this.renderImageOutlines(doc, outDir, stem, LapdfMode.SECTION_FILTER);

		
		SpatiallyOrderedChunkTypeFilteredTextWriter soctftw = 
				new SpatiallyOrderedChunkTypeFilteredTextWriter(true, true);
		soctftw.write(doc, outDir.getPath() + "/" + stem + "_spatialFiltered.txt");
		
		logger.info("Writing block classified XML in OpenAccess format "
						+ outDir.getPath() + "/" + stem + "_rhetorical.xml");


		if (reportBlocks) {
			logger.info("Running block feature reporter on "
					+ inFile.getPath());
			SpatialLayoutFeaturesReportGenerator slfrg =
					new SpatialLayoutFeaturesReportGenerator();
			slfrg.write(doc, outDir.getPath() + "/" + stem + "_spatialFeatures.dat");
		}
		
		if (extractUnclassified) {
			SpatiallyOrderedChunkTextWriter soctw = new SpatiallyOrderedChunkTextWriter();
			soctw.write(doc, outDir.getPath() + "/" + stem + "_unclassifiedFlowAwareText.dat");
		}

	}
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// File Processing functions
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Extracts the blocks within file to generate a LapdfDocument Object
	 * @param file - input file
	 * @return
	 * @throws PdfException 
	 * @throws AccessException
	 * @throws EncryptionException
	 * @throws IOException 
	 */
	public LapdfDocument blockifyPdfFile(File pdf) throws Exception {
		
		LapdfDocument doc = null;
		
		doc = parser.parse(pdf);
		doc.setPdfFile( pdf );
		
		if (doc.hasjPedalDecodeFailed()) {
			return null;
		}
		return doc;
	
	}

	public void classifyDocumentWithBaselineRules(LapdfDocument document) 
					throws ClassificationException, 
					IOException, URISyntaxException {
		
		File f = Converters
				.extractFileFromJarClasspath("rules/general.drl");
		
		this.classifyDocument(document, f);
		
		if( this.isImgFlag() )
			this.renderImageOutlines(document, new File("."), "debug", LapdfMode.CLASSIFY);
		
	}
	
	/**
	 * Classifies the chunks in a file based on the rule file
	 * @param document - an instantiated LapdfDocument
	 * @param ruleFile - a rule file on disk
	 * @throws IOException 
	 */
	public void classifyDocument(LapdfDocument document,
			File ruleFile) 
					throws ClassificationException, 
					IOException {
		
		RuleBasedChunkClassifier classfier = new RuleBasedChunkClassifier(
				ruleFile.getPath(), new RTModelFactory());
		
		for (int i = 1; i <= document.getTotalNumberOfPages(); i++) {
			
			PageBlock page = document.getPage(i);
			
			List<ChunkBlock> chunkList = page.getAllChunkBlocks(
					SpatialOrdering.COLUMN_AWARE_MIXED_MODE);

			classfier.classify(chunkList);

		}

	}

	public String readBasicText(LapdfDocument document) 
			throws IOException,FileNotFoundException {

		List<Set<String>> stack = new ArrayList<Set<String>>();
		Set<String> sections = new HashSet<String>();		
		sections.add(Block.TYPE_BODY);
		sections.add(Block.TYPE_HEADING);
		stack.add(sections);
		sections = new HashSet<String>();		
		sections.add(Block.TYPE_FIGURE_LEGEND);
		stack.add(sections);
				
		return this.readClassifiedText(document, stack);
		
	}

	

	public String readClassifiedText(LapdfDocument document, List<Set<String>> stack) 
			throws IOException,FileNotFoundException {

		StringBuilder text = new StringBuilder();

		Iterator<Set<String>> it = stack.iterator();
		while( it.hasNext() ) {
			Set<String> sections = it.next();
			
			text.append( this.readClassifiedText(document, sections) );
			
		}
		
		return text.toString();

	}

		
	public String readClassifiedText(LapdfDocument document, Set<String> sections) 
			throws IOException,FileNotFoundException {

		StringBuilder sb = new StringBuilder();
		
		int n = document.getTotalNumberOfPages();
		for (int i = 1; i <= n; i++)	{
			PageBlock page = document.getPage(i);
			
			List<ChunkBlock> chunksPerPage = page.getAllChunkBlocks(
					SpatialOrdering.PAGE_COLUMN_AWARE_MIXED_MODE
					);
			
			for(ChunkBlock chunkBlock:chunksPerPage){
				if( sections.contains( chunkBlock.getType() ) ) {
					sb.append(chunkBlock.readChunkText() + "\n");
				} 
			}
		}
		
		return sb.toString();

	}
	
	
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Output functions
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Write out the blocked LapdfDocument object to XML
	 * @param doc
	 * @param out
	 */
	public void writeSpatialXmlToFile(LapdfDocument doc, File out) {
	
		logger.info("Writing spatial block XML to " + out.getPath() );
		SpatialXMLWriter sxw = new SpatialXMLWriter();
		sxw.write(doc, out.getPath() );
	
	}
	
	/** 
	 * Write an LapdfDocument out to an OpenAccess-compatible XML format
	 * @param doc
	 * @param out
	 */
	public void writeSectionsToOpenAccessXmlFile(LapdfDocument doc, File out) {

		logger.info("Writing block-classified XML in OpenAccess format " + out.getPath() );
		OpenAccessXMLWriter oaxw = new OpenAccessXMLWriter();
		oaxw.write(doc, out.getPath() );

	}

	/** 
	 * Write an LapdfDocument out to an OpenAccess-compatible XML format
	 * @param doc
	 * @param out
	 * @throws IOException 
	 */
	public void writeBlockStatisticsReport(LapdfDocument doc, File out) throws IOException {

		logger.info("Writing spatial features report to " + out.getPath() );
		SpatialLayoutFeaturesReportGenerator slfrg = new SpatialLayoutFeaturesReportGenerator();
		slfrg.write(doc, out.getPath());

	}
	
	/**
	 * Render images of the pages of the PDF file
	 * @param pdfFile
	 * @param outputDir
	 * @throws Exception
	 */
	public void renderPageImages(File pdfFile, File outputDir) throws Exception {

		this.imagifier.generateImages(pdfFile, outputDir);
		
	}
	
	/**
	 * Render images of the positions of words on each page of pdf, color coded by section
	 * @param doc
	 * @param dir
	 * @param stem
	 * @param mode
	 * @throws IOException
	 */
	public void renderImageOutlines(LapdfDocument doc, File dir, String stem, int lapdfMode) 
			throws IOException {
		
		for (int i = 1; i <= doc.getTotalNumberOfPages(); i++) {
			PageBlock page = doc.getPage(i);
			File imgFile = new File(dir.getPath() + "/" + stem + "_" + page.getPageNumber() + ".png");
			PageImageOutlineRenderer.createPageImage(page, 
					imgFile, 
					stem + "_" + page.getPageNumber(), 
					lapdfMode);
		}
		
	}
	
	/**
	 * Writing text based report of spatial features of the PDF file
	 * @param doc
	 * @param out
	 * @throws IOException
	 */
	public void writeSpatialFeaturesReport(LapdfDocument doc, File out) 
			throws IOException {
		
		logger.info("Writing block feature report of " + 
				doc.getPdfFile().getPath() + " to " + out.getPath());
		SpatialLayoutFeaturesReportGenerator slfrg = new SpatialLayoutFeaturesReportGenerator();
		slfrg.write(doc, out.getPath());
	
	}

	public void writeTextToFile(LapdfDocument doc, Set<String> sections, File out)
			throws Exception {

		logger.info("Writing text of  "+ doc.getPdfFile().getPath() + " to " + out.getPath());
		SectionsTextWriter stw = new SectionsTextWriter();
		stw.addToStack(sections);
		stw.write(doc, out.getPath() );
	
	}
	
	public void writeTextToFile(LapdfDocument doc, List<Set<String>> stack, File out)
			throws Exception {

		logger.info("Writing text of  "+ doc.getPdfFile().getPath() + " to " + out.getPath());
		SectionsTextWriter stw = new SectionsTextWriter();
		Iterator<Set<String>> it = stack.iterator();
		while( it.hasNext() ) {
			Set<String> sections = it.next();
			stw.addToStack(sections);
		}
		stw.write(doc, out.getPath() );
	
	}

}
