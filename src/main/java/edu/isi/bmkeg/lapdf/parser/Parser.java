package edu.isi.bmkeg.lapdf.parser;

import java.io.File;
import java.io.IOException;

import org.jpedal.exception.PdfException;

import edu.isi.bmkeg.lapdf.extraction.exceptions.AccessException;
import edu.isi.bmkeg.lapdf.extraction.exceptions.EncryptionException;
import edu.isi.bmkeg.lapdf.model.LapdfDocument;

public interface Parser {
	
	public LapdfDocument parse(File file) throws Exception;

}
