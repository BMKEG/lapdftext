package edu.isi.bmkeg;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.junit.Test;


import edu.isi.bmkeg.lapdf.bin.CommandLineTool;
import edu.isi.bmkeg.utils.Converters;

public class CommandLineToolTest extends TestCase
{

	private File plos8_8_dir;
	private File plos8_8_dir_out;
	private File epoch_7Jun_8_drl;
	private File epoch_7Jun_8_csv;
	
	protected void setUp() throws Exception
	{ 
		super.setUp();

		URL url = this.getClass().getClassLoader().getResource(
				"sampleData/plos/8_8"
				);
		plos8_8_dir = new File( url.getPath() );

		url = this.getClass().getClassLoader().getResource(
				"sampleData/plos/8_8_OUTPUT"
				);
		plos8_8_dir_out = new File( url.getPath() );
		
		// empty output directory.
		Converters.recursivelyDeleteFiles(plos8_8_dir_out);
		plos8_8_dir_out.mkdir();
		
		url = this.getClass().getClassLoader().getResource(
				"rules/plosbiology/epoch_7Jun_8.drl"
				);
		epoch_7Jun_8_drl = new File( url.getPath() );
		
		url = this.getClass().getClassLoader().getResource(
				"rules/plosbiology/epoch_7Jun_8.csv"
				);
		epoch_7Jun_8_csv = new File( url.getPath() );
		
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
		
		Converters.cleanContentsFiles(plos8_8_dir, "pdf");
		
	}

	/**
	 * This test is designed to demonstrate the capability of LA-PDFText to 
	 * - Extract contiguous blocks from an input PDF file 
	 * - generate a report file for each input PDF containing page-wise statistics 
	 *   of each block detected. This is meant as a guide for developers to use in 
	 *   the process of developing rules for block classification and evantual
	 *   section-wise text extraction. 
	 */
	@Test
	public void testBlockStats()
	{
		String args[] = {
				"blockStatistics",
				plos8_8_dir.getPath()
				};
		CommandLineTool.main(args);

	}
	/**
	 * This test is designed to demonstrate the capability of LA-PDFText to 
	 * - Extract contiguous blocks from an input PDF file. In this test the output location
	 *   is unspecified and therefore output is written to the input folder. 
	 * 
	 */
	@Test
	public void test1()
	{
		String args[] = {
				"blockify",
				plos8_8_dir.getPath()
				};
		CommandLineTool.main(args);

	}

	/**
	 * This test is designed to demonstrate the capability of LA-PDFText to 
	 * - Extract contiguous blocks from an input PDF file. In this test the output location
	 *   is specified. 
	 * 
	 */
	@Test
	public void test2()
	{
		String args[] = {
				"blockify",
				plos8_8_dir.getPath(),
				plos8_8_dir_out.getPath()
				};
		CommandLineTool.main(args);

	}

	/**
	 * This test is designed to demonstrate the capability of LA-PDFText to 
	 * - Extract contiguous blocks from an input PDF file. In this test the output location
	 *   is unspecified and therefore output is written to the input folder.
	 * - Classify the extracted blocks into their corresponding sections. 
	 *   The types of sections that are supported are listed in the 
	 *   java interface edu.isi.bmkeg.pdf.model.Block 
	 */
	@Test
	public void test3()
	{
		String args[] = {
				"blockifyClassify",
				plos8_8_dir.getPath(),
				epoch_7Jun_8_drl.getPath(),
				plos8_8_dir_out.getPath()
				
				};
		CommandLineTool.main(args);

	}
	
	/**
	 * This test is designed to demonstrate the capability of LA-PDFText to 
	 * - Extract contiguous blocks from an input PDF file. In this test the output location
	 *   is unspecified and therefore output is written to the input folder.
	 * - Classify the extracted blocks into their corresponding sections. 
	 *   The types of sections that are supported are listed in the 
	 *   java interface edu.isi.bmkeg.pdf.model.Block 
	 * - The ability of LA-PDFText to accept classification rules written as an excel spreadsheet
	 */
	@Test
	public void test3Excel()
	{
		String args[] = {
				"blockifyClassify",
				plos8_8_dir.getPath(),
				epoch_7Jun_8_csv.getPath(),
				plos8_8_dir_out.getPath()
				};
		CommandLineTool.main(args);

	}
	
	/**
	 * This test is designed to demonstrate the capability of LA-PDFText to 
	 * - Extract contiguous blocks from an input PDF file. In this test the output location
	 *   is unspecified and therefore output is written to the input folder.
	 * - Classify the extracted blocks into their corresponding sections. 
	 *   The types of sections that are supported are listed in the 
	 *   java interface edu.isi.bmkeg.pdf.model.Block 
	 * - The ability of LA-PDFText to extract the text in plain text from by using the 
	 *   section classifications to filter out those sections that are not a part of the 
	 *   main narrative of the input article. 
	 */
	@Test
	public void test4()
	{
		String args[] = {
				"extractFullText",
				plos8_8_dir.getPath(),
				epoch_7Jun_8_csv.getPath(),
				plos8_8_dir_out.getPath()
				};
		CommandLineTool.main(args);

	}

	/**
	 * This test is designed to demonstrate the capability of LA-PDFText to 
	 * error check the input modes of operation. Current version does not 
	 * support the extraction of individual sections. 
	 *
	@Test
	public void test5()
	{
		String args[] = {"extractSection",
				"src/test/resources/sampleData/plos/8_8_OUTPUT/pbio.1000441.pdf_rhetorical.xml",
				"src/test/resources/sampleData/plos/8_8_OUTPUT/pbio.1000441.pdf_rhetorical.methods",
				"materials|methods"};
		CommandLineTool.main(args);

	}*/
	
	/**
	 * This test is designed to demonstrate the capability of LA-PDFText to 
	 * - Extract contiguous blocks from an input PDF file. In this test the output location
	 *   is unspecified and therefore output is written to the input folder.
	 * - Classify the extracted blocks into their corresponding sections. 
	 *   The types of sections that are supported are listed in the 
	 *   java interface edu.isi.bmkeg.pdf.model.Block 
	 * - The ability of LA-PDFText to extract the text in plain text from by using the 
	 *   section classifications to filter out those sections that are not a part of the 
	 *   main narrative of the input article. 
	 * - The rule file used here is a generic journal and publisher format-agnostic 
	 *   rule file which identifies the page footers and headers only. Subsequently,
	 *   the class edu.isi.bmkeg.pdf.text.SpatiallyOrderedChunkTextWriter is used to 
	 *   filter out the header and footer to write text that is not interrupted by 
	 *   their formatting embellishments. 
	 *
	@Test
	public void testGeneral()
	{
		String args[] = {"blockifyClassify",
				"/Users/cartic/Desktop/temp/bloodOriginal/new",
				"src/main/resources/rules/plosbiology/general.drl"};
		CommandLineTool.main(args);

	}*/


}
