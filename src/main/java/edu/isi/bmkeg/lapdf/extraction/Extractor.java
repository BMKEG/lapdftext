package edu.isi.bmkeg.lapdf.extraction;

import java.util.Iterator;
import java.util.List;

import edu.isi.bmkeg.lapdf.model.WordBlock;

public interface Extractor extends Iterator<List<WordBlock>>{

	public int getCurrentPageBoxHeight();
	public int getCurrentPageBoxWidth();

}
