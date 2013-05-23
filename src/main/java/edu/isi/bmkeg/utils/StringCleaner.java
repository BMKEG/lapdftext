package edu.isi.bmkeg.utils;

public interface StringCleaner {

	/**
	 * Clean string passed as argument according to rules implemented the filter
	 * @param dirtyString
	 * @return clean string
	 * @throws StringCleanerException 
	 */
	public String cleanItUp(String dirtyString) throws StringCleanerException;
	
}
