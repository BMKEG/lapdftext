package edu.isi.bmkeg.lapdf.extraction;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;

import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.jpedal.fonts.PdfFont;
import org.jpedal.grouping.PdfGroupingAlgorithms;

import edu.isi.bmkeg.lapdf.extraction.exceptions.AccessException;
import edu.isi.bmkeg.lapdf.extraction.exceptions.EncryptionException;

public class JPedalPageImageExtractor implements Iterator<BufferedImage>
{
	private PdfDecoder decoder = null;
	int currentPage = 1;
	int pageCount;
	private List<BufferedImage> pageImages;

	public JPedalPageImageExtractor()
	{
		this.decoder = new PdfDecoder(true);
		//PdfDecoder.s
		//PdfDecoder.setTextPrint(PdfDecoder.);
		//decoder.setExtractionMode(PdfDecoder.TEXT); // extract just text
		decoder.init(true);
		PdfGroupingAlgorithms.useUnrotatedCoords = true;
		// if you do not require XML content, pure text extraction is much
		// faster.
		decoder.useXMLExtraction();
	}

	public void init(String fileName) throws PdfException, AccessException,
	EncryptionException {
		if (decoder.isOpen()) {
			decoder.flushObjectValues(true);
			decoder.closePdfFile();

		}
		decoder.openPdfFile(fileName);
		currentPage = 1;
		pageCount = decoder.getPageCount();
		if (!decoder.isExtractionAllowed()) {
			throw new AccessException(fileName);
		} else if (decoder.isEncrypted()) {
			throw new EncryptionException(fileName);
		}

	}

    public void close(){
    	if (decoder.isOpen()) {
			decoder.flushObjectValues(true);
			decoder.closePdfFile();
		}
    }

	@Override
	public boolean hasNext()
	{
		if(currentPage<=pageCount)
			return true;
		return false;
	}

	@Override
	public BufferedImage next()
	{
		try
		{
			BufferedImage img = decoder.getPageAsImage(currentPage);
			currentPage++;
			return img;
		} catch (PdfException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void remove()
	{
	

	}

}
