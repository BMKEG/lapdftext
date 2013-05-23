package edu.isi.bmkeg.ladpdf.bin.dirWatchers;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.junit.Test;

import edu.isi.bmkeg.lapdf.bin.WatchDirectory;
import edu.isi.bmkeg.lapdf.utils.PdfDirWatcher;
import edu.isi.bmkeg.utils.Converters;

public class WatchDirectory_BLOCKIFY_CLASSIFY_Test extends TestCase
{
	File inputFile, outputFile;
	File f1, f2, f3;
	
	protected void setUp() throws Exception	{ 

		super.setUp();
		
		URL u = this.getClass().getClassLoader().getResource("sampleData/plos/8_8");
		inputFile = new File( u.getPath() );

		outputFile = new File( inputFile.getParent() + "/temp");
		Converters.recursivelyDeleteFiles(outputFile);
		outputFile.mkdir();
		
		u = this.getClass().getClassLoader().getResource("sampleData/plos/8_8/pbio.1000441.pdf");
		f1 = new File(u.getPath());
		
		f2 = new File(f1.getParent() + "/temp.pdf");
		f2.delete();
		f3 = new File(f1.getParent() + "/temp2.pdf");
		f3.delete();
		
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
		f2.delete();
		f3.delete();
		Converters.recursivelyDeleteFiles(outputFile);
	}

	@Test
	public void test() throws Exception {
		
		String[] args = {
				PdfDirWatcher.BLOCKIFY_CLASSIFY, inputFile.getPath(), outputFile.getPath()
			};

		WatchDirectory.main(args);

		// Listen for changes over 60 seconds 
		for( int i=0; i<60; i++ ) {
			 Thread.sleep(1000);
			 System.out.print(i + " secs ... ");
			 
			 if( i == 5 ) {
				 Converters.copyFile(f1, f2);
				 Converters.copyFile(f1, f3);
			 }
			 
			 if( i == 45 ) {
				 f3.delete();
			 }
			 
		}
		
	}
	
}
