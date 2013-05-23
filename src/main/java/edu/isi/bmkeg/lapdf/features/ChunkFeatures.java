package edu.isi.bmkeg.lapdf.features;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.uimafit.component.xwriter.XWriterFileNamer;

import edu.isi.bmkeg.lapdf.extraction.exceptions.InvalidPopularSpaceValueException;
import edu.isi.bmkeg.lapdf.model.Block;
import edu.isi.bmkeg.lapdf.model.ChunkBlock;
import edu.isi.bmkeg.lapdf.model.PageBlock;
import edu.isi.bmkeg.lapdf.model.WordBlock;
import edu.isi.bmkeg.lapdf.model.factory.AbstractModelFactory;
import edu.isi.bmkeg.lapdf.model.spatial.SpatialEntity;

public class ChunkFeatures {

	private ChunkBlock chunk;
	private PageBlock parent;
	private static Pattern patternLowerCase = Pattern.compile("[a-z]");
	private static Pattern patternUpperCase = Pattern.compile("[A-Z]");
	
	private static AbstractModelFactory modelFactory;

	public ChunkFeatures(ChunkBlock chunk, AbstractModelFactory modelFactory) {
		this.chunk = chunk;
		this.parent = (PageBlock) chunk.getContainer();
		this.modelFactory = modelFactory;
	}
	
	public boolean isMostPopularFontInDocument() {
		
		String ds = parent.getDocument().getMostPopularFontStyle();
		
		String s = chunk.getMostPopularWordFont() 
				+ ";" + chunk.getMostPopularWordStyle();
		
		if( s.equals(ds) )
			return true;
		
		return false;
		
	}	
	
	/**
	 * Note that we screen out the most popular font on the last page 
	 * from this calculation since we expect that to be the font of the 
	 * references.
	 * @return
	 */
	public boolean isNextMostPopularFontInDocument() {
		
		String ds = parent.getDocument().getNextMostPopularFontStyle();
		
		String s = chunk.getMostPopularWordFont() 
				+ ";" + chunk.getMostPopularWordStyle();
		
		if( s.equals(ds) )
			return true;
		
		return false;
		
	}
	
	/**
	 * returns the difference between the most popular font size in the in the current chunk 
	 * and the most popular font size in the document.
	 * @return
	 */
	public int getHeightDifferenceBetweenChunkWordAndDocumentWord() {
		
		int i = chunk.getMostPopularWordHeight();
		int j = parent.getDocument().readMostPopularWordHeight();
		
		return (i-j);
	}
	
	/**
	 * returns true if chunk block is left aligned
	 * @return
	 */
	public boolean isAlignedLeft() {
		if (Block.LEFT.equalsIgnoreCase(chunk.readLeftRightMedLine()))
			return true;
		return false;
	}
	
	/**
	 * returns true if chunk block starts in the top half of the page
	 * @return
	 */
	public boolean isInTopHalf() {
		
		// x1, y1, x2, y2
		int top = parent.getMargin()[1];
		int bottom = parent.getMargin()[3];
		double middle = (top + bottom) / 2.0;
		
		if( chunk.getY1() < middle )
			return true;
	
		return false;
	
	}	
	
	/**
	 * returns the most popular font size in the chunk block
	 * @return
	 */
	public int getMostPopularFontSize() {
		
		String fontStyle = chunk.getMostPopularWordStyle();
		if(fontStyle==null)
			return chunk.getMostPopularWordHeight();
		int fontSizeIndex = fontStyle.indexOf("font-size");
		int colonIndex = fontStyle.indexOf(":", fontSizeIndex);
		int ptIndex = fontStyle.indexOf("pt", colonIndex);
		
		return Integer.parseInt(fontStyle.substring(colonIndex + 1, ptIndex));
	
	}
	
	/**
	 * returns true if chunk block is right aligned
	 * @return
	 */
	public boolean isAlignedRight() {
	
		if (Block.RIGHT.equalsIgnoreCase(chunk.readLeftRightMedLine()))
			return true;
		
		return false;
	
	}
	
	/**
	 * returns true if chunk block is center aligned
	 * @return
	 */
	public boolean isAlignedMiddle() {
		if (Block.MIDLINE.equalsIgnoreCase(chunk.readLeftRightMedLine()))
			return true;
		return false;
	}
	
	/**
	 * returns true if chunk block contains mostly capitalized text
	 * @return
	 */
	public boolean isAllCapitals() {
		String chunkText = chunk.readChunkText();
		Matcher matcher = patternLowerCase.matcher(chunkText);
		if (matcher.find()) {

			return false;
		} else {
			matcher = patternUpperCase.matcher(chunkText);
			if (matcher.find()) {
				return true;
			} else {
				return false;
			}

		}
	}
	
	/**
	 * returns true if chunk block contains mostly bold face text
	 * @return
	 */
	public boolean isMostPopularFontModifierBold() {

		if ((chunk.getMostPopularWordStyle() != null && chunk
				.getMostPopularWordStyle().indexOf("Bold") != -1)
				|| (chunk.getMostPopularWordFont() != null && (chunk
						.getMostPopularWordFont().indexOf("Bold") != -1 || chunk
						.getMostPopularWordFont().indexOf("-B") != -1))) {
			return true;
		}
		return false;
	}
	
	/**
	 * returns true if chunk block contains mostly italicized  text
	 * @return
	 */
	public boolean isMostPopularFontModifierItalic() {
		if ((chunk.getMostPopularWordStyle() != null && chunk
				.getMostPopularWordStyle().indexOf("Italic") != -1)
				|| (chunk.getMostPopularWordFont() != null && chunk
						.getMostPopularWordFont().indexOf("Italic") != -1)) {
			return true;
		}
		return false;
	}
	
	/**
	 * returns true if chunk block contains the first line of a page's text
	 * @return
	 */
	public boolean isContainingFirstLineOfPage() {

		if (Math.abs(chunk.getY1() - parent.getMargin()[1]) < parent
				.getDocument().readMostPopularWordHeight())
			return true;
		else
			return false;
	}

	/**
	 * returns true if chunk block contains the last line of a page's text
	 * @return
	 */
	public boolean isContainingLastLineOfPage() {
		if (Math.abs(chunk.getY2() - parent.getMargin()[3]) < parent
				.getDocument().readMostPopularWordHeight())
			return true;
		else
			return false;
	}

	/**
	 * returns true if chunk block is an outlier or stray block
	 * @return
	 */
	public boolean isOutlier() {

		// TODO - UseReflections
		ChunkBlock block = modelFactory.createChunkBlock(
				chunk.getX1(), 
				chunk.getY1() - 30, 
				chunk.getX2(), 
				chunk.getY2() + 60);

		int neighbouringChunksCount = parent.intersectsByType(block, null,
				ChunkBlock.class).size();
		
		int wordBlockCount = parent.containsByType(chunk, null, WordBlock.class).size();
		
		int sizeAfterTrunc = chunk.readChunkText().
				replaceAll("[A-Za-z0-9]", "").length();
		
		if ( (wordBlockCount < 10 && neighbouringChunksCount < 10)
				|| (sizeAfterTrunc < 10 && neighbouringChunksCount < 10)
				|| chunk.getMostPopularWordHeight() > 50)
			return true;
		
		return false;
	
	}

	public int getChunkTextLength() {
		return chunk.readChunkText().length();
	}

	/**
	 * returns the word block density in a chunk block
	 * @return
	 */
	public double getDensity() {
		List<SpatialEntity> wordBlockList = parent.containsByType(chunk, null,
				WordBlock.class);
		double areaCoveredByWordBlocks = 0;
		for (SpatialEntity entity : wordBlockList)
			areaCoveredByWordBlocks = areaCoveredByWordBlocks
			+ (entity.getHeight() * entity.getWidth());
		return areaCoveredByWordBlocks / (chunk.getHeight() * chunk.getWidth());
	}

	/**
	 * returns true if the chunk block is aligned with column boundaries
	 * @return
	 */
	public boolean isAlignedWithColumnBoundaries() {
		
		String lrm = chunk.readLeftRightMedLine();
		int columnLeft = 0;
		int columnRight = 0;
//		double threshold = chunk.getMostPopularWordHeight() * 1.5;
		double threshold = chunk.getMostPopularWordHeight() * 3;
		
		int l = parent.getDocument().getBodyTextFrame().getX1();
		int r = parent.getDocument().getBodyTextFrame().getX2();
		int m = (int) Math.round( (l+r)/2.0);
		
		if (Block.MIDLINE.equalsIgnoreCase(lrm)) {
		
			return false;
		
		} else if (Block.LEFT.equalsIgnoreCase(lrm)) {
		
			columnLeft = l;
			columnRight = m;

		} else if (Block.RIGHT.equalsIgnoreCase(lrm)) {
		
			columnLeft = m;
//			columnRight = parent.getMargin()[2];
			columnRight = r;
			
		}
		
		int leftDiff = Math.abs(chunk.getX1() - columnLeft);
		int rightDiff = Math.abs(chunk.getX2() - columnRight);

		if (chunk.readNumberOfLine() > 1
				&& leftDiff < threshold
				&& rightDiff < threshold) {

			return true;
		
		} else if (chunk.readNumberOfLine() == 1
				&& leftDiff < threshold) {
		
			return true;
		
		}
		
		return false;
	
	}

	/**
	 * returns the classification assigned to previous chunk block
	 * @return
	 */
	public String getlastClassification() {

		ChunkBlock lastBlock = chunk.readLastChunkBlock();

		return (lastBlock == null) ? null : lastBlock.getType();

	}

	/**
	 * returns the section label of chunk
	 * @return
	 * @throws InvalidPopularSpaceValueException
	 */
	public String getSection() throws InvalidPopularSpaceValueException {
		
		ChunkBlock lastBlock = null;
		lastBlock = parent.getDocument().getLastChunkBlock(chunk);

		/*String section = (lastBlock == null) ? null : (lastBlock.getType()
				.contains(".")) ? lastBlock.getType().substring(0,
				lastBlock.getType().indexOf(".")) : lastBlock.getType();*/
		String section;
		if(lastBlock==null){ 
			section=null;
		}else if(lastBlock.getType().contains(".")){
			section= lastBlock.getType().substring(0,lastBlock.getType().indexOf("."));
		}else{
			section=lastBlock.getType();
		}
		if (section == null)
			return null;
		else if (isMainSection(section))
			return section;

		ChunkBlock prev = null;
		while (section != null) {

			/**
			 * introducing a special check to see if the call to getLastChunkBlock returns
			 * the same block i.e. lastBlock if so we break the loop and exit with section = lastBlock.getType()
			 */
			prev = lastBlock;
			lastBlock = parent.getDocument().getLastChunkBlock(lastBlock);
			/*if (lastBlock!=null)
			{
				System.out.println(prev.getchunkText());
				System.out.println(lastBlock.getchunkText());
				System.out.println("---------------");
			}
			section = (lastBlock == null) ? null : (lastBlock.getType()
					.contains(".")) ? lastBlock.getType().substring(0,
							lastBlock.getType().indexOf(".")) : lastBlock.getType();*/
			if(lastBlock==null){
				section=null;
			}else if(lastBlock.getType().contains(".")){
				section= lastBlock.getType().substring(0,lastBlock.getType().indexOf("."));
				if(lastBlock.equals(prev)){
					break;
				}
			}else{
				section=lastBlock.getType();
				if(lastBlock.equals(prev)){
					break;
				}
			}
			if (isMainSection(section))
				return section;

		}

		return section;
	}

	private boolean isMainSection(String section) {
		boolean result = !(chunk.TYPE_AFFLIATION.equals(section)
				|| chunk.TYPE_CITATION.equals(section)
				|| chunk.TYPE_FIGURE_LEGEND.equals(section)
				|| chunk.TYPE_FOOTER.equals(section)
				|| chunk.TYPE_HEADER.equals(section)
				|| chunk.TYPE_KEYWORDS.equals(section)
				|| chunk.TYPE_TABLE.equals(section) || chunk.TYPE_UNCLASSIFIED
				.equals(section));

		return result;
	}

    
	/**
	 * returns the page number where the block is located
	 * @return
	 */
	public int getPageNumber() {
		return this.parent.getPageNumber();
	}

	/**
	 * returns true if the chunk is a single column centered on the page else returns false
	 * @return
	 */
	public boolean isColumnCentered() {

		int chunkMedian = chunk.getX1() + chunk.getWidth() / 2;
		int pageMedian = parent.getMedian();
		String lrm = chunk.readLeftRightMedLine();

		if (chunk.MIDLINE.equalsIgnoreCase(lrm)) {
			if (Math.abs(pageMedian - chunkMedian) < parent.getDocument()
					.readMostPopularWordHeight() * 2)
				return true;
			return false;
		}
		
		int pageMedianLeftRight = 0;
		
		if (chunk.LEFT.equalsIgnoreCase(lrm)) {
		
			pageMedianLeftRight = parent.getMargin()[0]
			                                         + (pageMedian - parent.getMargin()[0]) / 2;
		} else if (chunk.RIGHT.equalsIgnoreCase(lrm)) {
			
			pageMedianLeftRight = pageMedian
			+ (parent.getMargin()[2] - pageMedian) / 2;
		
		}

		if (Math.abs(chunkMedian - pageMedianLeftRight) < parent.getDocument()
				.readMostPopularWordHeight() * 2)
			return true;
		
		return false;
	}
	
	public boolean isWithinBodyTextFrame() {
		
		SpatialEntity btf = parent.getDocument().getBodyTextFrame();
		double threshold = chunk.getMostPopularWordHeight() * 3;

		if( chunk.getX1() + threshold > btf.getX1() &&
				chunk.getX2() - threshold < btf.getX2() &&
				chunk.getY1() + threshold > btf.getY1() &&
				chunk.getY2() - threshold < btf.getY2() ) {
			return true;
		} else {
			return false;
		}
		
	}

}
