package edu.isi.bmkeg.ladpdf.bin;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.junit.Test;

import edu.isi.bmkeg.lapdf.bin.Blockify;
import edu.isi.bmkeg.lapdf.bin.ImagifyBlocks;
import edu.isi.bmkeg.utils.Converters;
import edu.isi.bmkeg.utils.springContext.BmkegProperties;

public class ImagifyBlocksTest extends TestCase
{

	BmkegProperties prop;
	String login, password, dbUrl;

	File inputDir, outputDir;
	File f1, f2, f3;
	
	protected void setUp() throws Exception
	{ 
				
		URL u = this.getClass().getClassLoader().getResource("sampleData/plos/8_8");
		inputDir = new File( u.getPath() );
		outputDir = new File( inputDir.getParentFile().getPath() + "/temp/output" );
				
	}

	protected void tearDown() throws Exception	{
		Converters.cleanContentsFiles(inputDir, "pdf");
		if( outputDir.exists() )
			Converters.recursivelyDeleteFiles(outputDir.getParentFile());
	}

	@Test
	public void testInputFileOnly() throws Exception
	{		
		String[] args = {
				inputDir.getPath(), 
			};
		ImagifyBlocks.main(args);
	}

	@Test
	public void testInputOutputFiles() throws Exception
	{		
		String[] args = {
				inputDir.getPath(), outputDir.getPath()
			};
		ImagifyBlocks.main(args);
	}

	
}
