package edu.isi.bmkeg.lapdf.controller;


/**
 * Basic Java API to high-level LAPDFText functionality, including:
 *
 * 1) Gathering layout statistics for the PDF file
 * 2) Running Block-based spatial chunker on PDF.
 * 3) Classifying texts of blocks in the file to categories based on a rule file.
 * 4) Outputting text or XML to file
 * 5) Rendering pages images of text layout or the original PDF file as PNG files
 * 6) Serializing LAPDFText object to a VPDMf database record.
 * 
 * @author burns
 *
 */
public class LapdfMode  {

	public static int BLOCK_ONLY = 1;
	public static int CLASSIFY = 2;
	public static int SECTION_FILTER = 3;
	
}
