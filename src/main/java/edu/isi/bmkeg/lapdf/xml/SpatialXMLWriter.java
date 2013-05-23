package edu.isi.bmkeg.lapdf.xml;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import edu.isi.bmkeg.lapdf.model.ChunkBlock;
import edu.isi.bmkeg.lapdf.model.LapdfDocument;
import edu.isi.bmkeg.lapdf.model.PageBlock;
import edu.isi.bmkeg.lapdf.model.WordBlock;
import edu.isi.bmkeg.lapdf.model.ordering.SpatialOrdering;
import edu.isi.bmkeg.lapdf.model.spatial.SpatialEntity;

public class SpatialXMLWriter implements XMLWriter {
	
	private static final String NEWLINE = System.getProperty("line.separator");
	private static final String ENCODING = "UTF-8";
	private static final String ELEMENT_NAME_DOCUMENT = "Document";
	private static final String ELEMENT_NAME_PAGE = "Page";
	private static final String ELEMENT_NAME_CHUNK = "Chunk";
	private static final String ELEMENT_NAME_WORD = "Word";
	private static final String BLOCK_ATTRIBUTE_X1 = "x1";
	private static final String BLOCK_ATTRIBUTE_X2 = "x2";
	private static final String BLOCK_ATTRIBUTE_Y1 = "y1";
	private static final String BLOCK_ATTRIBUTE_Y2 = "y2";
	private static final String BLOCK_ATTRIBUTE_TYPE = "type";
	private static final String PAGE_ATTRIBUTE_PAGENUMBER = "pageNumber";
	private static final String PAGE_ATTRIBUTE_CHUNK_COUNT = "chunkCount";
	private static final String PAGE_ATTRIBUTE_WORD_COUNT = "wordCount";
	private static final String WORD_ATTRIBUTE_WORD_FONT = "font";
	private static final String WORD_ATTRIBUTE_WORD_STYLE = "style";
	private static final String ELEMENT_NAME_TYPE = "type";

	public void write(LapdfDocument document, String outputFilename) {

		try {
			FileOutputStream XMLOutputFileStream;
			XMLOutputFileStream = new FileOutputStream(outputFilename);

			OutputFormat XMLOutputFormat = new OutputFormat("XML", ENCODING,
					true);
			XMLSerializer serializer = new XMLSerializer(XMLOutputFileStream,
					XMLOutputFormat);
			ContentHandler documentContentHandler = serializer
					.asContentHandler();
			documentContentHandler.startDocument();
			AttributesImpl documentAttribute = new AttributesImpl();
			AttributesImpl chunkAttribute = new AttributesImpl();
			AttributesImpl wordAttribute = new AttributesImpl();
			AttributesImpl pageAttribute = new AttributesImpl();
			PageBlock page;
			List<ChunkBlock> chunks;
			List<SpatialEntity> words;
			WordBlock word;
			documentContentHandler.startElement("", "", ELEMENT_NAME_DOCUMENT,
					documentAttribute);
			int totalNumberOfPages = document.getTotalNumberOfPages();
			for (int i = 1; i <= totalNumberOfPages; i++) {
				page = document.getPage(i);
				pageAttribute.clear();
				pageAttribute.addAttribute("", "", BLOCK_ATTRIBUTE_X1, "CDATA",
						page.getMargin()[0] + "");
				pageAttribute.addAttribute("", "", BLOCK_ATTRIBUTE_Y1, "CDATA",
						page.getMargin()[1] + "");
				pageAttribute.addAttribute("", "", BLOCK_ATTRIBUTE_X2, "CDATA",
						page.getMargin()[2] + "");
				pageAttribute.addAttribute("", "", BLOCK_ATTRIBUTE_Y2, "CDATA",
						page.getMargin()[3] + "");

				pageAttribute.addAttribute("", "", BLOCK_ATTRIBUTE_TYPE,
						"CDATA", page.getType() + "");

				pageAttribute.addAttribute("", "", PAGE_ATTRIBUTE_CHUNK_COUNT,
						"CDATA", page.getAllChunkBlocks(null).size() + "");
				pageAttribute.addAttribute("", "", PAGE_ATTRIBUTE_PAGENUMBER,
						"CDATA", page.getPageNumber() + "");
				pageAttribute.addAttribute("", "", PAGE_ATTRIBUTE_WORD_COUNT,
						"CDATA", page.getAllWordBlocks(null).size() + "");
				documentContentHandler.startElement("", "", ELEMENT_NAME_PAGE,
						pageAttribute);
				chunks = page
						.getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE);
				for (ChunkBlock chunk : chunks) {
					chunkAttribute.clear();
					chunkAttribute.addAttribute("", "", BLOCK_ATTRIBUTE_X1,
							"CDATA", chunk.getX1() + "");
					chunkAttribute.addAttribute("", "", BLOCK_ATTRIBUTE_Y1,
							"CDATA", chunk.getY1() + "");
					chunkAttribute.addAttribute("", "", BLOCK_ATTRIBUTE_X2,
							"CDATA", chunk.getX2() + "");
					chunkAttribute.addAttribute("", "", BLOCK_ATTRIBUTE_Y2,
							"CDATA", chunk.getY2() + "");
					chunkAttribute.addAttribute("", "", BLOCK_ATTRIBUTE_TYPE,
							"CDATA", chunk.getType() + "");
					documentContentHandler.startElement("", "",
							ELEMENT_NAME_CHUNK, chunkAttribute);
					words = page.containsByType(chunk,
							SpatialOrdering.MIXED_MODE, WordBlock.class);
					for (SpatialEntity entity : words) {
						word = (WordBlock) entity;
						wordAttribute.clear();
						wordAttribute.addAttribute("", "", BLOCK_ATTRIBUTE_X1,
								"CDATA", word.getX1() + "");
						wordAttribute.addAttribute("", "", BLOCK_ATTRIBUTE_Y1,
								"CDATA", word.getY1() + "");
						wordAttribute.addAttribute("", "", BLOCK_ATTRIBUTE_X2,
								"CDATA", word.getX2() + "");
						wordAttribute.addAttribute("", "", BLOCK_ATTRIBUTE_Y2,
								"CDATA", word.getY2() + "");
						wordAttribute.addAttribute("", "",
								WORD_ATTRIBUTE_WORD_FONT, "CDATA",
								word.getFont() + "");
						wordAttribute.addAttribute("", "",
								WORD_ATTRIBUTE_WORD_STYLE, "CDATA",
								word.getFontStyle() + "");
						documentContentHandler.startElement("", "",
								ELEMENT_NAME_WORD, wordAttribute);
						documentContentHandler.characters(word.getWord()
								.toCharArray(), 0,
								word.getWord().toCharArray().length);
						documentContentHandler.endElement("", "",
								ELEMENT_NAME_WORD);
					}
					documentContentHandler.endElement("", "",
							ELEMENT_NAME_CHUNK);
				}
				documentContentHandler.endElement("", "", ELEMENT_NAME_PAGE);
			}
			documentContentHandler.endElement("", "", ELEMENT_NAME_DOCUMENT);
			documentContentHandler.endDocument();
			XMLOutputFileStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
