package edu.isi.bmkeg.lapdf.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.isi.bmkeg.lapdf.bin.CommandLineTool;
import edu.isi.bmkeg.lapdf.bin.WatchDirectory;
import edu.isi.bmkeg.lapdf.controller.LapdfEngine;
import edu.isi.bmkeg.lapdf.controller.LapdfMode;
import edu.isi.bmkeg.lapdf.model.Block;
import edu.isi.bmkeg.lapdf.model.LapdfDocument;
import edu.isi.bmkeg.lapdf.uima.cpe.CommandLineFitPipeline;
import edu.isi.bmkeg.utils.Converters;
import edu.isi.bmkeg.utils.parser.DirWatcher;

public class PdfDirWatcher extends DirWatcher {

	private static Logger logger = Logger.getLogger(PdfDirWatcher.class);

	int state = 0;
	static int WAITING = 0;
	static int WORKING = 1;
	static int ERROR = 2;

	private String type;
	public static String IMAGIFY_BLOCKS = "ImagifyBlock";
	public static String IMAGIFY_SECTIONS = "ImagifySection";
	public static String BLOCKIFY = "Blockify";
	public static String BLOCKIFY_CLASSIFY = "BlockifyClassify";
	public static String READ_SECTION_TEXT = "ReadText";

	private Timer timer;

	private File output;
	private File ruleFile;
	private LapdfEngine engine;

	private Set<File> files = new HashSet<File>();

	public PdfDirWatcher(String type, File input, File output, File ruleFile)
			throws Exception {
		super(input, ".pdf");

		this.type = type;
		this.output = output;
		this.ruleFile = ruleFile;

		this.engine = new LapdfEngine();

	}

	public void setUpLiveFolder() {
		this.timer = new Timer();
		timer.schedule(this, new Date(), 1000);
	}

	public void cancelLiveFolder() {
		this.timer.cancel();
	}

	protected void onChange(File file, String action) {

		try {

			state = WORKING;

			if (action.equals("added")  ) {

				logger.info("File " + file.getName() + " action: " + action);

				this.execute(file);

			} else if (action.equals("deleted")) {


				logger.info("File " + file.getName() + " action: " + action);

				String stem = file.getName();
				
				if (type.equals(PdfDirWatcher.IMAGIFY_BLOCKS)) {

					stem = stem.replaceAll("\\.pdf", "_blockImgs");
					File toDelete = new File(output.getPath() + "/" + stem);
					Converters.recursivelyDeleteFiles(toDelete);
					
				} else if (type.equals(PdfDirWatcher.IMAGIFY_SECTIONS)) {

					stem = stem.replaceAll("\\.pdf", "_secImgs");
					File toDelete = new File(output.getPath() + "/" + stem);
					Converters.recursivelyDeleteFiles(toDelete);

				} else if (type.equals(PdfDirWatcher.BLOCKIFY)) {

					stem = stem.replaceAll("\\.pdf", "_spatial.xml");
					File toDelete = new File(output.getPath() + "/" + stem);
					toDelete.delete();
				
				} else if (type.equals(PdfDirWatcher.BLOCKIFY_CLASSIFY)) {

					stem = stem.replaceAll("\\.pdf", "_openAccess.xml");
					File toDelete = new File(output.getPath() + "/" + stem);
					toDelete.delete();

				} else if (type.equals(PdfDirWatcher.READ_SECTION_TEXT)) {
				
					stem = stem.replaceAll("\\.pdf", "_fullText.txt");
					File toDelete = new File(output.getPath() + "/" + stem);
					toDelete.delete();

				}

			}

			state = WAITING;

		} catch (Exception e) {

			state = ERROR;
			e.printStackTrace();

		}

	}

	public void execute(File pdf) throws Exception {

		String pdfStem = pdf.getName();
		pdfStem = pdfStem.replaceAll("\\.pdf", "");

		String outPath = Converters.mimicDirectoryStructure(this.getInput(),
				output, pdf).getPath();
		File out = null;

		if (type.equals(PdfDirWatcher.IMAGIFY_BLOCKS)) {

			outPath = outPath.replaceAll("\\.pdf", "_blockImgs");
			out = new File(outPath);
			if (!out.exists())
				out.mkdir();

			try {
				LapdfDocument lapdf = engine.blockifyPdfFile(pdf);
				engine.renderImageOutlines(lapdf, out, pdfStem,
						LapdfMode.BLOCK_ONLY);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (type.equals(PdfDirWatcher.IMAGIFY_SECTIONS)) {

			outPath = outPath.replaceAll("\\.pdf", "_secImgs");
			out = new File(outPath);
			if (!out.exists())
				out.mkdir();

			try {
				LapdfDocument lapdf = engine.blockifyPdfFile(pdf);
				engine.classifyDocument(lapdf, ruleFile);
				engine.renderImageOutlines(lapdf, out, pdfStem,
						LapdfMode.CLASSIFY);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (type.equals(PdfDirWatcher.BLOCKIFY)) {

			outPath = outPath.replaceAll("\\.pdf", "_spatial.xml");
			out = new File(outPath);

			try {
				LapdfDocument lapdf = engine.blockifyPdfFile(pdf);
				engine.writeSpatialXmlToFile(lapdf, out);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (type.equals(PdfDirWatcher.BLOCKIFY_CLASSIFY)) {

			outPath = outPath.replaceAll("\\.pdf", "_openAccess.xml");
			out = new File(outPath);

			try {
				LapdfDocument lapdf = engine.blockifyPdfFile(pdf);
				engine.classifyDocument(lapdf, ruleFile);
				engine.writeSectionsToOpenAccessXmlFile(lapdf, out);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (type.equals(PdfDirWatcher.READ_SECTION_TEXT)) {

			List<Set<String>> stack = new ArrayList<Set<String>>();
			Set<String> sections = new HashSet<String>();
			sections.add(Block.TYPE_BODY);
			sections.add(Block.TYPE_HEADING);
			stack.add(sections);
			sections = new HashSet<String>();
			sections.add(Block.TYPE_FIGURE_LEGEND);
			stack.add(sections);

			outPath = outPath.replaceAll("\\.pdf$", "") + "_fullText.txt";
			File outFile = new File(outPath);

			try {
				LapdfDocument lapdf = engine.blockifyPdfFile(pdf);
				engine.classifyDocument(lapdf, ruleFile);
				engine.writeTextToFile(lapdf, stack, outFile);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			throw new Exception(WatchDirectory.USAGE + type
					+ " is not a prescribed exectution command.");
		}

	}

}
