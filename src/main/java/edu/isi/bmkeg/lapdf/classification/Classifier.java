package edu.isi.bmkeg.lapdf.classification;

import java.util.List;

import edu.isi.bmkeg.lapdf.extraction.exceptions.ClassificationException;
import edu.isi.bmkeg.lapdf.model.Block;
/**
 * Implementations of this interface will contain 
 * different ways of classifying blocks.
 * @author cartic
 *
 * @param <T>
 */
public interface Classifier<T extends Block> {
	
	public void classify(List<T> blockList) throws ClassificationException;

}
