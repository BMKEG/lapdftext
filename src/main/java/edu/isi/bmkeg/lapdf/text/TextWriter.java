package edu.isi.bmkeg.lapdf.text;

import java.io.FileNotFoundException;
import java.io.IOException;

import edu.isi.bmkeg.lapdf.model.LapdfDocument;

public interface TextWriter {

	public void write(LapdfDocument document, String outputFilename) 
			throws FileNotFoundException,IOException;
	
	public static String UTF_8 = "UTF-8";

}
