package edu.isi.bmkeg.lapdf.xml;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import edu.isi.bmkeg.lapdf.model.Block;
import edu.isi.bmkeg.lapdf.model.ChunkBlock;
import edu.isi.bmkeg.lapdf.model.LapdfDocument;
import edu.isi.bmkeg.lapdf.model.PageBlock;
import edu.isi.bmkeg.lapdf.model.ordering.SpatialOrdering;

public class OpenAccessXMLWriter implements XMLWriter {

	public static final String ENCODING = "UTF-8";
	public static final String ELEMENT_NAME_SECTION = "sec";
	public static final String SECTION_ATTRIBUTE_SEC_TYPE = "sec-type";
	public static final String ELEMENT_NAME_TITLE = "title";
	public static final String ELEMENT_NAME_PARAGRAPH = "p";
	public static final String ELEMENT_NAME_ARTICLE = "article";
	public static final String ELEMENT_NAME_BODY = "body";
	public static final String ELEMENT_NAME_ABSTRACT = "abstract";
	public static final String ELEMENT_NAME_FRONT = "front";
	public static final String ELEMENT_NAME_ARTICLE_META = "article-meta";
	public static final String ELEMENT_NAME_ACKNOWLEDGEMENT = "ack";
	public static final String ELEMENT_NAME_BACK = "back";
	public static final String ELEMENT_NAME_REFERENCES = "ref";
	public static final AttributesImpl emptyAttribute = new AttributesImpl();
	public HashMap<String, ArrayList<ChunkBlock>> mappedDocument = new HashMap<String, ArrayList<ChunkBlock>>();

	@Override
	public void write(LapdfDocument document, String outputFilename) {
		try {
			FileOutputStream XMLOutputFileStream;
			XMLOutputFileStream = new FileOutputStream(outputFilename);

			OutputFormat XMLOutputFormat = new OutputFormat("XML", ENCODING,
					false);
			XMLSerializer serializer = new XMLSerializer(XMLOutputFileStream,
					XMLOutputFormat);
			ContentHandler documentContentHandler = serializer
					.asContentHandler();
			populateMap(document);

			documentContentHandler.startDocument();
			documentContentHandler.startElement("", "", ELEMENT_NAME_ARTICLE,
					emptyAttribute);

			documentContentHandler.startElement("", "", ELEMENT_NAME_FRONT,
					emptyAttribute);
			documentContentHandler.startElement("", "",
					ELEMENT_NAME_ARTICLE_META, emptyAttribute);
			doAbstractAndAcknowledgment(Block.TYPE_ABSTRACT,
					documentContentHandler);
			documentContentHandler
					.endElement("", "", ELEMENT_NAME_ARTICLE_META);
			documentContentHandler.endElement("", "", ELEMENT_NAME_FRONT);

			documentContentHandler.startElement("", "", ELEMENT_NAME_BODY,
					emptyAttribute);
			doSection(Block.TYPE_INTRODUCTION, documentContentHandler, document);
			doSection(Block.TYPE_METHODS, documentContentHandler, document);
			doSection(Block.TYPE_DISCUSSION, documentContentHandler, document);

			doSection(Block.TYPE_RESULTS, documentContentHandler, document);
			doSection(Block.TYPE_CONCLUSIONS, documentContentHandler, document);

			documentContentHandler.endElement("", "", ELEMENT_NAME_BODY);
			documentContentHandler.startElement("", "", ELEMENT_NAME_BACK,
					emptyAttribute);
			doAbstractAndAcknowledgment(Block.TYPE_ACKNOWLEDGEMENTS,
					documentContentHandler);
			doReferences(documentContentHandler);
			documentContentHandler.endElement("", "", ELEMENT_NAME_BACK);
			documentContentHandler.endElement("", "", ELEMENT_NAME_ARTICLE);
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

	private void doSection(String type, ContentHandler documentContentHandler,
			LapdfDocument doc) {

		ArrayList<ChunkBlock> list = mappedDocument.get(type);
		if (list != null && list.size() > 0)
			writeSection(documentContentHandler, list);
	
	}

	private void writeSection(ContentHandler documentContentHandler, 
			ArrayList<ChunkBlock> chunkList) {

		Collections.sort(chunkList, new SpatialOrdering(
				SpatialOrdering.PAGE_COLUMN_AWARE_MIXED_MODE));
		ArrayList<ChunkBlock> headingList = new ArrayList<ChunkBlock>();
		String lastEncounteredType = "";

		String chunkText = null;
		boolean hasSectionStarted = false;
		try {

			for (ChunkBlock chunk : chunkList) {
				if (chunk.getType().contains(Block.META_TYPE_HEADING)) {

					headingList.add(chunk);
				}

				if (headingList.size() > 0) {
					chunkText = createStringFromChunk(headingList);

					String openAccessType = createSectionType(headingList
							.get(0));
					AttributesImpl sectionAttribute = new AttributesImpl();
					sectionAttribute
							.addAttribute("", "", SECTION_ATTRIBUTE_SEC_TYPE,
									"CDATA", openAccessType);

					documentContentHandler.startElement("", "",
							ELEMENT_NAME_SECTION, sectionAttribute);
					documentContentHandler.startElement("", "",
							ELEMENT_NAME_TITLE, null);
					documentContentHandler.characters(chunkText.toCharArray(),
							0, chunkText.toCharArray().length);
					documentContentHandler.endElement("", "",
							ELEMENT_NAME_TITLE);
					chunkList.removeAll(headingList);
					headingList.clear();
				} else {
					documentContentHandler.startElement("", "",
							ELEMENT_NAME_SECTION, emptyAttribute);
				}

				break;
			}

			StringBuilder builder=new StringBuilder();
			for (ChunkBlock chunk : chunkList) {
				if (chunk.getType().contains(Block.META_TYPE_SUBTITLE)) {

					headingList.add(chunk);
				} else {
					if (headingList.size() > 0) {
						if (hasSectionStarted)
							documentContentHandler.endElement("", "",
									ELEMENT_NAME_SECTION);
						else
							hasSectionStarted = true;
						chunkText = createStringFromChunk(headingList);
						headingList.clear();
						documentContentHandler.startElement("", "",
								ELEMENT_NAME_SECTION, emptyAttribute);
						documentContentHandler.startElement("", "",
								ELEMENT_NAME_TITLE, emptyAttribute);

						documentContentHandler.characters(chunkText
								.toCharArray(), 0,
								chunkText.toCharArray().length);
						documentContentHandler.endElement("", "",
								ELEMENT_NAME_TITLE);

					}
					chunkText = chunk.readChunkText();
					if(chunkText.indexOf("-")==chunkText.length()-1){
						builder.append(chunkText.substring(0,chunkText.length()-1));
					}else{
						builder.append(chunkText.substring(0,chunkText.length()));
						documentContentHandler.startElement("", "",
								ELEMENT_NAME_PARAGRAPH, emptyAttribute);

						documentContentHandler.characters(builder.toString().toCharArray(),
								0, builder.toString().toCharArray().length);
						documentContentHandler.endElement("", "",
								ELEMENT_NAME_PARAGRAPH);
						builder.delete(0, builder.length());
					}
					

				}
			}
			if (hasSectionStarted)
				documentContentHandler.endElement("", "", ELEMENT_NAME_SECTION);
			documentContentHandler.endElement("", "", ELEMENT_NAME_SECTION);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String createSectionType(ChunkBlock chunk) {
		if (chunk.getType().contains(Block.TYPE_INTRODUCTION)) {
			return "intro";
		} else if (chunk.getType().contains(Block.TYPE_METHODS)) {
			return "materials|methods";
		} else if (chunk.getType().contains(Block.TYPE_DISCUSSION)) {
			return "discussion";
		} else if (chunk.getType().contains(Block.TYPE_CONCLUSIONS)) {
			return "conclusions";
		} else if (chunk.getType().contains(Block.TYPE_RESULTS)) {
			return "results";
		}
		return null;
	}

	private String createStringFromChunk(ArrayList<ChunkBlock> chunkList) {
		String returnString = "";
		for (ChunkBlock chunk : chunkList)
			returnString = returnString + chunk.readChunkText().trim();
		return returnString.trim();
	}

	private void doAbstractAndAcknowledgment(String type,
			ContentHandler documentContentHandler) {
		ArrayList<ChunkBlock> list;
		list = mappedDocument.get(type);
		if(list==null ||list.size()==0)
        	return;
		ArrayList<ChunkBlock> headingList = new ArrayList<ChunkBlock>();

		String chunkText = null;
		boolean hasSectionStarted = false;
		String element;
		if (Block.TYPE_ABSTRACT.equals(type)) {
			element = ELEMENT_NAME_ABSTRACT;
		} else {
			element = ELEMENT_NAME_ACKNOWLEDGEMENT;

		}
		if (list != null && list.size() > 0) {
			Collections.sort(list, new SpatialOrdering(
					SpatialOrdering.PAGE_COLUMN_AWARE_MIXED_MODE));
			try {
				documentContentHandler.startElement("", "", element,
						emptyAttribute);
				documentContentHandler.startElement("", "",
						ELEMENT_NAME_SECTION, emptyAttribute);
				StringBuilder builder=new StringBuilder();
				for (ChunkBlock chunk : list) {
					if (chunk.getType().contains(Block.META_TYPE_SUBTITLE)) {

						headingList.add(chunk);
					} else {
						if (headingList.size() > 0) {
							if (hasSectionStarted)
								documentContentHandler.endElement("", "",
										ELEMENT_NAME_SECTION);
							else
								hasSectionStarted = true;
							chunkText = createStringFromChunk(headingList);
							headingList.clear();
							documentContentHandler.startElement("", "",
									ELEMENT_NAME_SECTION, emptyAttribute);
							documentContentHandler.startElement("", "",
									ELEMENT_NAME_TITLE, emptyAttribute);

							documentContentHandler.characters(chunkText
									.toCharArray(), 0,
									chunkText.toCharArray().length);
							documentContentHandler.endElement("", "",
									ELEMENT_NAME_TITLE);

						}
						chunkText = chunk.readChunkText();
						if(chunkText.indexOf("-")==chunkText.length()-1){
							builder.append(chunkText.substring(0,chunkText.length()-1));
						}else{
							builder.append(chunkText.substring(0,chunkText.length()));
							documentContentHandler.startElement("", "",
									ELEMENT_NAME_PARAGRAPH, emptyAttribute);

							documentContentHandler.characters(builder.toString().toCharArray(),
									0, builder.toString().toCharArray().length);
							documentContentHandler.endElement("", "",
									ELEMENT_NAME_PARAGRAPH);
							builder.delete(0, builder.length());
						}

					}
				}
				if (hasSectionStarted)
					documentContentHandler.endElement("", "",
							ELEMENT_NAME_SECTION);
				documentContentHandler.endElement("", "", ELEMENT_NAME_SECTION);
				documentContentHandler.endElement("", "", element);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private void populateMap(LapdfDocument document) {
		int totalNumberOfPages = document.getTotalNumberOfPages();
		PageBlock page;
		List<ChunkBlock> chunks;
		ArrayList<ChunkBlock> chunkList;
		for (int i = 1; i <= totalNumberOfPages; i++) {
			page = document.getPage(i);

			chunks = page.getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE);
			for (ChunkBlock chunk : chunks) {
				String type = (chunk.getType().contains(".")) ? chunk.getType()
						.substring(0, chunk.getType().indexOf(".")) : chunk
						.getType();
				chunkList = mappedDocument.get(type);
				if (chunkList == null) {
					chunkList = new ArrayList<ChunkBlock>();
					chunkList.add(chunk);
					mappedDocument.put(type, chunkList);
				} else {
					chunkList.add(chunk);
				}
			}
		}
	}

	private void doReferences(ContentHandler documentContentHandler) {
		StringBuilder builder = new StringBuilder();
		ArrayList<ChunkBlock> list;
		list = mappedDocument.get(Block.TYPE_REFERENCES);
        if(list==null ||list.size()==0)
        	return;
		String chunkText;
		for (ChunkBlock chunk : list) {

			if (!chunk.getType().contains(Block.META_TYPE_HEADING)) {
				builder.append(chunk.readChunkText());
			}
		}

		try {
			documentContentHandler.startElement("", "",
					ELEMENT_NAME_REFERENCES, emptyAttribute);
			documentContentHandler.startElement("", "",
					ELEMENT_NAME_SECTION, emptyAttribute);
			documentContentHandler.startElement("", "",
					ELEMENT_NAME_PARAGRAPH, emptyAttribute);
			documentContentHandler.characters(builder.toString().toCharArray(),
					0, builder.toString().toCharArray().length);
			documentContentHandler.endElement("", "", ELEMENT_NAME_PARAGRAPH);
			documentContentHandler.endElement("", "", ELEMENT_NAME_SECTION);
			documentContentHandler.endElement("", "", ELEMENT_NAME_REFERENCES);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
