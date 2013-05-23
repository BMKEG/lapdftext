package edu.isi.bmkeg.ladpdf.bin;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.junit.Test;

import edu.isi.bmkeg.lapdf.bin.ImagifyBlocks;
import edu.isi.bmkeg.lapdf.bin.ImagifySections;
import edu.isi.bmkeg.utils.Converters;
import edu.isi.bmkeg.utils.springContext.BmkegProperties;

public class ImagifySectionsTest extends TestCase
{

	BmkegProperties prop;
	String login, password, dbUrl;

	File inputDir, outputDir, ruleFile;
	File f1, f2, f3;
	
	protected void setUp() throws Exception
	{ 
				
		URL u = this.getClass().getClassLoader().getResource("sampleData/plos/8_8");
		inputDir = new File( u.getPath() );
		outputDir = new File( inputDir.getParentFile().getPath() + "/temp/output" );
		
		u = this.getClass().getClassLoader().getResource("rules/plosbiology/epoch_5_7May.drl");
		ruleFile = new File( u.getPath() );
				
	}

	protected void tearDown() throws Exception	{
		Converters.cleanContentsFiles(inputDir, "pdf");
		if( outputDir.exists() ) {
			Converters.recursivelyDeleteFiles(outputDir.getParentFile());
		}
	}

	@Test
	public void testInputFileOnly() throws Exception
	{		
		String[] args = {
				inputDir.getPath(), "-", "-"
			};
		ImagifySections.main(args);
	}

	@Test
	public void testInputOutputFiles() throws Exception
	{		
		String[] args = {
				inputDir.getPath(), outputDir.getPath(), "-"
			};
		ImagifySections.main(args);
	}

	@Test
	public void testInputOutputRuleFiles() throws Exception
	{		
		String[] args = {
				inputDir.getPath(), outputDir.getPath(), ruleFile.getPath()
			};
		ImagifySections.main(args);
	}
	
}
