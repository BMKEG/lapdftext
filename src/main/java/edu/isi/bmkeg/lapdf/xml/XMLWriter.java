package edu.isi.bmkeg.lapdf.xml;

import edu.isi.bmkeg.lapdf.model.LapdfDocument;

public interface XMLWriter {
	public  void write(LapdfDocument document, String outputFilename);
}
