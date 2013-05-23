package edu.isi.bmkeg.lapdf.uima.cr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;

import edu.isi.bmkeg.utils.ISI_UIMA_PDFUtils;

public class DirectoryCollectionReader extends CollectionReader_ImplBase {

	private static Logger logger = Logger.getLogger(DirectoryCollectionReader.class);
	
	private final boolean DEBUG = false;

	public static final String ITEMS_TO_SKIP = 
			ConfigurationParameterFactory.createConfigurationParameterName(
					DirectoryCollectionReader.class, "itemsToSkip");
	@ConfigurationParameter(mandatory=false, 
			description="this number indicates the number of items to skip before beginning processing")
	private int itemsToSkip;

	public static final String END_INDEX = 
			ConfigurationParameterFactory.createConfigurationParameterName(
					DirectoryCollectionReader.class, "endIndex");
	@ConfigurationParameter(mandatory=false, 
			description="this number indicates the termination point")
	private int endIndex;

	/**
	 * The directory that files will be read from. If a file is used as the
	 * input parameter, then only that file will be processed.
	 */
	public static final String DIRECTORY = 
			ConfigurationParameterFactory.createConfigurationParameterName(
					DirectoryCollectionReader.class,"directory");
	@ConfigurationParameter(mandatory=false, 
			description="This is the input directory.")
	private String directory;
	
	/**
	 * A boolean flag which determines if the files from subdirectories are
	 * processed, or if only the files in the top-level directory are used.
	 */
	public static final String DIR_RECURSION = 
			ConfigurationParameterFactory.createConfigurationParameterName(
					DirectoryCollectionReader.class,"dirRecursion"
					);
	@ConfigurationParameter(mandatory=false, 
			description="This is a flag to decide whether to recurse into subdirs.")
	private Boolean dirRecursion;
	
	/**
	 * Enables the user to filter based on file suffix. The user must include
	 * the "." as part of the suffix.
	 */
	public static final String FILE_SUFFIX = 
			ConfigurationParameterFactory.createConfigurationParameterName(
					DirectoryCollectionReader.class,"fileSuffix"
					);
	@ConfigurationParameter(mandatory=false, 
			description="This is the file suffix to match files agains and process.")
	private String fileSuffix;

	private int numberOfFilesProcessed;
	private List<String> fileSuffixesToProcess;

	protected List<File> filesToProcess;
	protected Iterator<File> fileIterator;

	/**
	 * In the initialization of this collection reader, the file names for all
	 * files to be processed are collected
	 */
	@Override
	public void initialize() throws ResourceInitializationException {
		
		fileSuffixesToProcess = new ArrayList<String>();
		numberOfFilesProcessed = 0;
		
		/* get input parameters from descriptor file */
		directory = (String) getConfigParameterValue(DIRECTORY);
		itemsToSkip = (Integer) getConfigParameterValue(ITEMS_TO_SKIP);
		endIndex = (Integer) getConfigParameterValue(END_INDEX);
		dirRecursion = 
				((Boolean) getConfigParameterValue(DIR_RECURSION))
				.booleanValue();

		fileSuffix = (String) getConfigParameterValue(FILE_SUFFIX);
		fileSuffixesToProcess.add(fileSuffix);

		/* initialize list to hold files to process */
		filesToProcess = new ArrayList<File>();

		/* Recurse through directories to get files to process */
		System.err.println("Initializing DirectoryOfFilesCollectionReader on directory: "
				+ directory);
		File root = new File(directory);
		if (root.isFile()) {
		
			filesToProcess.add(root);

		} else if (root.isDirectory()) {
			
			processDirectory(root, filesToProcess);

		} else {
			
			error("Invalid input detected. Document collection root is neither a file nor a directory.");

		}
		
		fileIterator = filesToProcess.iterator();
		if(itemsToSkip>0){
			int i = itemsToSkip;
			while(i>0){
				fileIterator.next();
				i--;
			}
			logger.info("Skipping "+itemsToSkip+" PDF files");
		}
		System.err.println("CR initialization complete. # files to process: "
				+ filesToProcess.size());
		//filesToProcess = null;

	}

	/* recurse through a directory tree, adding files to the filesToProcess list */
	protected void processDirectory(File dir, List<File> fileList) {
		String[] files = dir.list();

		for (String file : files) {
			File f = new File(dir.getAbsolutePath() + "/" + file);

			/*
			 * If it is an html file, then add it to the filesToProcess list, if
			 * it is a directory, then process the directory.
			 */
			if (f.isFile()) {
				if (checkForValidSuffix(f)) {
					fileList.add(f);
				}
			} else if (f.isDirectory() && dirRecursion) {
				processDirectory(f, fileList);
			} else if (f.isDirectory()) {
				// do nothing.. the recurseIntoDirectoryStructure flag is set to
				// false
			} else {
				System.err
				.println("Error. Expecting a file or directory, but encountered something else...  "
						+ f.getPath());
			}
		}
	}

	protected boolean checkForValidSuffix(File f) {
		if (fileSuffixesToProcess.size() == 0) {
			/* if no suffixes were specified, then we process every file type */
			return true;
		} else {
			for (String suffix : fileSuffixesToProcess) {
				if (f.getName().endsWith(suffix)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @see com.ibm.uima.collection.CollectionReader#getNext(com.ibm.uima.cas.CAS)
	 */
	public void getNext(CAS aCAS) throws IOException, CollectionException {
		JCas jcas;
		try {
			jcas = aCAS.getJCas();
		} catch (CASException e) {
			throw new CollectionException(e);
		}
		String name=fileIterator.next().getAbsolutePath();
		numberOfFilesProcessed++;
		ISI_UIMA_PDFUtils.setDocumentSecondaryIDs(jcas, name);


	}

	/**
	 * @see com.ibm.uima.arg0collection.base_cpm.BaseCollectionReader#hasNext()
	 */
	public boolean hasNext() throws IOException, CollectionException {
		if(endIndex>0){
			if(numberOfFilesProcessed < filesToProcess.size() && numberOfFilesProcessed+itemsToSkip<endIndex){
				return true;
			}else{
				return false;
			}
		}else{
			if(numberOfFilesProcessed < filesToProcess.size()){
				return true;
			}
		}
		return false;
	}


	public void close() throws IOException {
	}

	private void error(String message) {
		System.err.println("ERROR -- DirectoryOfFilesCollectionReader: "
				+ message);
	}

	@SuppressWarnings("unused")
	private void warn(String message) {
		System.err.println("WARNING -- DirectoryOfFilesCollectionReader: "
				+ message);
	}

	@SuppressWarnings("unused")
	private void debug(String message) {
		if (DEBUG) {
			System.err.println("DEBUG -- DirectoryOfFilesCollectionReader: "
					+ message);
		}
	}

	@Override
	public Progress[] getProgress() {
		// TODO Auto-generated method stub
		return null;
	}

}
