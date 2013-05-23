package edu.isi.bmkeg.lapdf.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.jpedal.objects.PdfFileInformation;

// 72 DPI ==>  Width:  595 pixels, Height: 738 pixels for page 1 (page 2 higher)
// 144 DPI ==> Width:  1190 pixels, Height: 1476 pixels

/**
 * Writes a JPEG image for each page of a given list of PDF files.
 * 
 * @author tommying
 */
public class JPedalPDFRenderer {

	private static final String DIRECTORY_SEPARATOR = System
			.getProperty("file.separator");
	private static final String PDF_SUFFIX = ".pdf";

	private String ocr[] = { "TeleForm" }; // creators that produce OCR PDFs??
	private static final int DPI = 144; // 72 dpi is default - use 144
										// production

	private String outputDirectory;
	private ArrayList<String> inputFilenames;
	private RenderingHints hints;

	public JPedalPDFRenderer() {
		
		hints = new RenderingHints(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		hints.put(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		hints.put(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, // tommy
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}

	/**
	 * Generates one JPEG image per page of the input PDFs. The images are named
	 * according to the leaf filename and page number of the input. E.g. for
	 * page 2 of "/tmp/Heidinger_1997_767_279.pdf", the output file would be
	 * "/path/to/output/Heidinger_1997_767_279_2.jpg".
	 * 
	 * @return A list of all generated files.
	 */
	public List<File> generateImages(File pdf, File outputDir) 
			throws IOException, PdfException {

		int startPage = 1;
		int endPage;
		double h = -0.01;
		double w = -0.01;
		List<File> generatedFiles = new ArrayList<File>();

		String pdfName = pdf.getName();
		String pdfStem = pdfName.substring(0, pdfName.lastIndexOf("."));

		PdfDecoder decoder = new PdfDecoder(true);
	
		// decoder.setExtractionMode(0, DPI, DPI/72);
		decoder.setExtractionMode(PdfDecoder.TEXT + PdfDecoder.RAWIMAGES
					+ PdfDecoder.FINALIMAGES, DPI, DPI / 72);
		decoder.openPdfFile(pdf.getPath());

		endPage = decoder.getPageCount();
		for (int pageNumber = startPage; pageNumber <= endPage; pageNumber++) {
			String outputImageName = pdfStem + "_" + pageNumber + ".png";
			File outputImageFile = new File( outputImageName );
			generatedFiles.add(outputImageFile);

			PdfFileInformation currentFileInformation = decoder
					.getFileInformationData();
			
			String values[] = currentFileInformation.getFieldValues();
			String fields[] = PdfFileInformation.getFieldNames();
			
			for (int i = 0; i < fields.length; i++) {
				if (fields[i].equals("Creator")) {
					for (int j = 0; j < ocr.length; j++) {
						if (values[i].equals(ocr[j])) {
							decoder.setRenderMode(PdfDecoder.RENDERIMAGES);
							//decoder.setsetEnableLegacyJPEGConversion(true);
						}
					}
				}
			}

			BufferedImage image_to_save = decoder
						.getPageAsImage(pageNumber);
			h = image_to_save.getHeight();
			w = image_to_save.getWidth();

			ImageIO.write(image_to_save, "png", outputImageFile);


		}

		return generatedFiles;
	
	}

	//
	// Not sure if this works. Seems to be a PNG -> JPG converter.
	//
	private void writeImage(File outputFile, BufferedImage img)
			throws FileNotFoundException, IOException {
		
		Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName("jpeg");
		ImageWriter iw = it.next();
		ImageWriteParam iwp = iw.getDefaultWriteParam();
		iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		iwp.setCompressionQuality(1);

		FileImageOutputStream outs = new FileImageOutputStream(outputFile);
		iw.setOutput(outs);
		IIOImage ioimage = new IIOImage(img, null, null);
		iw.write(null, ioimage, iwp);
		iw.dispose();
	
	}

}
