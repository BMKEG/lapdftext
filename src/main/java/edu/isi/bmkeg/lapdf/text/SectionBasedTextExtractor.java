package edu.isi.bmkeg.lapdf.text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SectionBasedTextExtractor {

	private static boolean isFactoryCreated = false;
	private static Result result;
	private static SAXSource xmlSource;
	private static Source xsltSource;
	private static String OPEN_ACCESS_XSL = "src/main/resources/xsl/openAccess.xsl";
	private static TransformerFactory transFact;
	private static SAXParserFactory saxParserfactory;
	private static SAXParser parser;
	private static Transformer trans;
	private static Logger logger = Logger.getLogger("sys");
	public static final String ELEMENT_INTRODUCTION = "introduction";
	public static final String ELEMENT_MATERIALS_METHODS = "materials|methods";
	public static final String ELEMENT_DISCUSSION = "discussion";
	public static final String ELEMENT_CONCLUSIONS = "conclusions";
	public static final String ELEMENT_RESULTS = "results";
	public static final String ELEMENT_ABSTRACT = "abstract";
	public static final String ELEMENT_REFERENCES = "ref";

	/*
	 * This method runs the XSLT over the given XML file.
	 */
	public static void parse(String xslName, File xmlFile, File outputFile,
			String type) throws IOException {

		if (!isFactoryCreated) {
			logger.addHandler(new FileHandler("javaExecutionReport.xml"));
			xsltSource = new javax.xml.transform.stream.StreamSource(
					new FileInputStream(xslName));

			// create an instance of TransformerFactory
			transFact = TransformerFactory.newInstance();
			saxParserfactory = SAXParserFactory.newInstance();

			try {
				xmlSource = new SAXSource();
				parser = saxParserfactory.newSAXParser();

				xmlSource.setXMLReader(parser.getXMLReader());
				trans = transFact.newTransformer(xsltSource);

			} catch (TransformerConfigurationException e) {

				e.printStackTrace();
			} catch (ParserConfigurationException e) {

				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
			isFactoryCreated = true;

		}
		try {

			FileWriter fileWriter = new FileWriter(outputFile);
			result = new javax.xml.transform.stream.StreamResult(fileWriter);
			trans.setParameter("type", type);
			xmlSource.setInputSource(new InputSource(new FileReader(xmlFile)));

			trans.transform(xmlSource, result);
			trans.clearParameters();
			fileWriter.close();

			result = null;
		}

		catch (TransformerConfigurationException e) {

		} catch (TransformerException e) {

		} catch (FileNotFoundException e) {
			logger.severe("Error file " + xmlFile.getName() + " not found");
		}

	}

	public static void parse(String xslName, File xmlFile, OutputStream os,
			String type) throws IOException {
		
		if (!isFactoryCreated) {
			logger.addHandler(new FileHandler("javaExecutionReport.xml"));
			xsltSource = new javax.xml.transform.stream.StreamSource(
					new FileInputStream(xslName));

			// create an instance of TransformerFactory
			transFact = TransformerFactory.newInstance();
			saxParserfactory = SAXParserFactory.newInstance();

			try {
				xmlSource = new SAXSource();
				parser = saxParserfactory.newSAXParser();

				xmlSource.setXMLReader(parser.getXMLReader());
				trans = transFact.newTransformer(xsltSource);

			} catch (TransformerConfigurationException e) {

				e.printStackTrace();
			} catch (ParserConfigurationException e) {

				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
			isFactoryCreated = true;

		}
		try {

			//FileWriter fileWriter = new FileWriter(outputFile);
			result = new javax.xml.transform.stream.StreamResult(os);
			trans.setParameter("type", type);
			xmlSource.setInputSource(new InputSource(new FileReader(xmlFile)));

			trans.transform(xmlSource, result);
			trans.clearParameters();
			//fileWriter.close();

			result = null;
		}

		catch (TransformerConfigurationException e) {

		} catch (TransformerException e) {

		} catch (FileNotFoundException e) {
			logger.severe("Error file " + xmlFile.getName() + " not found");
		}

	}
	public static void getSection(File inputFile, File outputFile, String type) throws IOException {
		if (ELEMENT_ABSTRACT.equals(type) || ELEMENT_INTRODUCTION.equals(type)
				|| ELEMENT_MATERIALS_METHODS.equals(type)
				|| ELEMENT_DISCUSSION.equals(type)
				|| ELEMENT_RESULTS.equals(type)
				|| ELEMENT_CONCLUSIONS.equals(type)
				|| ELEMENT_REFERENCES.equals(type)) {
				parse(OPEN_ACCESS_XSL, inputFile, outputFile, type);
			

		
		} else {
			System.out.println("Please use one of the following section type:");
			System.out.println("1. " + ELEMENT_ABSTRACT);
			System.out.println("2. " + ELEMENT_INTRODUCTION);
			System.out.println("3. " + ELEMENT_MATERIALS_METHODS);
			System.out.println("4. " + ELEMENT_DISCUSSION);
			System.out.println("5. " + ELEMENT_RESULTS);
			System.out.println("6. " + ELEMENT_CONCLUSIONS);
			System.out.println("7. " + ELEMENT_REFERENCES);
		}
	}

	public static String getSectionString(File inputFile, String type) throws IOException {
		OutputStream bos = new ByteArrayOutputStream();
		if (ELEMENT_ABSTRACT.equals(type) || ELEMENT_INTRODUCTION.equals(type)
				|| ELEMENT_MATERIALS_METHODS.equals(type)
				|| ELEMENT_DISCUSSION.equals(type)
				|| ELEMENT_RESULTS.equals(type)
				|| ELEMENT_CONCLUSIONS.equals(type)
				|| ELEMENT_REFERENCES.equals(type)) {
		
				parse(OPEN_ACCESS_XSL, inputFile, bos, type);
			

		
		} else {
			System.out.println("Please use one of the following section type:");
			System.out.println("1. " + ELEMENT_ABSTRACT);
			System.out.println("2. " + ELEMENT_INTRODUCTION);
			System.out.println("3. " + ELEMENT_MATERIALS_METHODS);
			System.out.println("4. " + ELEMENT_DISCUSSION);
			System.out.println("5. " + ELEMENT_RESULTS);
			System.out.println("6. " + ELEMENT_CONCLUSIONS);
			System.out.println("7. " + ELEMENT_REFERENCES);
		}
		return bos.toString();
	}
	
	public static void main(String args[]) {
		String types[] = {ELEMENT_ABSTRACT,ELEMENT_INTRODUCTION,ELEMENT_RESULTS,ELEMENT_DISCUSSION,ELEMENT_MATERIALS_METHODS,ELEMENT_CONCLUSIONS};
		//String types[] = {ELEMENT_RESULTS};
		StringBuilder sb = new StringBuilder();
		for (String type: types)
		{
			File input = new File("src/test/resources/sampleData/plos/8_8_OUTPUT/pbio.1000441.pdf_rhetorical.xml");
			//File output = new File("/Users/cartic/Documents/workspace/pdfExtraction-v0.3/sampleData/testData/JCN/epoch204_490/" + type + ".txt");
			try
			{
				sb.append(getSectionString(input, type));
				sb.append("\n");
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println(sb.toString().trim());

	}

}
