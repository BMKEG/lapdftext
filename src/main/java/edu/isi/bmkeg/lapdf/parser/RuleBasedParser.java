package edu.isi.bmkeg.lapdf.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.isi.bmkeg.lapdf.extraction.JPedalExtractor;
import edu.isi.bmkeg.lapdf.extraction.exceptions.InvalidPopularSpaceValueException;
import edu.isi.bmkeg.lapdf.features.HorizontalSplitFeature;
import edu.isi.bmkeg.lapdf.model.Block;
import edu.isi.bmkeg.lapdf.model.ChunkBlock;
import edu.isi.bmkeg.lapdf.model.LapdfDocument;
import edu.isi.bmkeg.lapdf.model.PageBlock;
import edu.isi.bmkeg.lapdf.model.WordBlock;
import edu.isi.bmkeg.lapdf.model.factory.AbstractModelFactory;
import edu.isi.bmkeg.lapdf.model.ordering.SpatialOrdering;
import edu.isi.bmkeg.lapdf.model.spatial.SpatialEntity;
import edu.isi.bmkeg.lapdf.utils.PageImageOutlineRenderer;
import edu.isi.bmkeg.utils.FrequencyCounter;
import edu.isi.bmkeg.utils.IntegerFrequencyCounter;

public class RuleBasedParser implements Parser {

	private static Logger logger = Logger.getLogger(RuleBasedParser.class);

	private boolean debugImages = false;

	private ArrayList<PageBlock> pageList;
	
	private JPedalExtractor extractor;
	
	private int idGenerator;
	
	private IntegerFrequencyCounter avgHeightFrequencyCounter;

	private FrequencyCounter fontFrequencyCounter;
	
	private int northSouthSpacing;
	
	private int eastWestSpacing;
	
	protected AbstractModelFactory modelFactory;
	
	protected String path;

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	private boolean isDebugImages() {
		return debugImages;
	}

	private void setDebugImages(boolean debugImages) {
		this.debugImages = debugImages;
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public RuleBasedParser(AbstractModelFactory modelFactory)
			throws Exception {

		pageList = new ArrayList<PageBlock>();
		extractor = new JPedalExtractor(modelFactory);

		idGenerator = 1;
		this.avgHeightFrequencyCounter = new IntegerFrequencyCounter(1);
		this.fontFrequencyCounter = new FrequencyCounter();

		this.modelFactory = modelFactory;

	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public LapdfDocument parse(File file) 
			throws Exception {

		LapdfDocument document = null;

		init(file);
		List<WordBlock> pageWordBlockList = null;
		PageBlock pageBlock = null;
		int pageCounter = 1;

		document = new LapdfDocument(file);
		document.setjPedalDecodeFailed(true);

		while (extractor.hasNext()) {
			
			document.setjPedalDecodeFailed(false);
			
			pageBlock = modelFactory.createPageBlock(
					pageCounter++,
					extractor.getCurrentPageBoxWidth(),
					extractor.getCurrentPageBoxHeight(), 
					document);
			
			pageList.add(pageBlock);
			
			pageWordBlockList = extractor.next();

			idGenerator = pageBlock.initialize(pageWordBlockList, idGenerator);

			this.eastWestSpacing = pageBlock.getMostPopularWordHeightPage()
					+ pageBlock.getMostPopularHorizontalSpaceBetweenWordsPage();
			
			logger.debug(this.eastWestSpacing);
			
			this.northSouthSpacing = pageBlock.getMostPopularWordHeightPage()
					+ pageBlock.getMostPopularVerticalSpaceBetweenWordsPage();

			buildChunkBlocks(pageWordBlockList, pageBlock);

			if (isDebugImages()) {
				PageImageOutlineRenderer.createPageImage(
						pageBlock,
						file,
						file.getName() + "afterBuildBlocks"
								+ pageBlock.getPageNumber() + ".png", 0);
			}

		}

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		
		if (!document.hasjPedalDecodeFailed()) {

			// initial parse is commplete. 
			String s = file.getName().replaceAll("\\.pdf", "");
			Pattern p = Pattern.compile("(\\d+)");
			Matcher m = p.matcher(file.getName());
			if( m.find() ) {
				s = m.group(1);
			}

			for (PageBlock page : pageList) {

				if (isDebugImages()) {
					PageImageOutlineRenderer.createPageImage(page, file,
							file.getName()
									+ "beforeBuildBlocksOverlapDeletion_"
									+ s + "_" + page.getPageNumber()
									+ ".png", 0);
				}

				this.deleteHighlyOverlappedChunkBlocks(page);

				if (isDebugImages()) {
					PageImageOutlineRenderer.createPageImage(page, file,
							file.getName() + "afterBuildBlocksOverlapDeletion_"
									+ s + "_" + page.getPageNumber()
									+ ".png", 0);
				}

				this.divideBlocksVertically(page);

				if (isDebugImages()) {

					PageImageOutlineRenderer.createPageImage(page, file,
							file.getName() + "afterVerticalDivide_" + s
									+ "_" + page.getPageNumber() + ".png", 0);
				}

				this.joinLines(page);

				if (isDebugImages()) {
					PageImageOutlineRenderer.createPageImage(page, file,
							file.getName() + "afterJoinLines_" + s + "_"
									+ page.getPageNumber() + ".png", 0);
				}

				this.divideBlocksHorizontally(page);

				if (isDebugImages()) {
					PageImageOutlineRenderer.createPageImage(page, file,
							file.getName() + "afterHorizontalDivide_" + s
									+ "_" + page.getPageNumber() + ".png", 0);
				}

				this.deleteHighlyOverlappedChunkBlocks(page);

				if (isDebugImages()) {
					PageImageOutlineRenderer.createPageImage(page, file,
							file.getName() + "/afterOverlapDeletion_" + s
									+ "_" + page.getPageNumber() + ".png", 0);
				}

			}

			document.addPages(pageList);

			document.calculateBodyTextFrame();
			document.calculateMostPopularFontStyles();

		}

		return document;
	}

	private void init(File file) throws Exception {

		extractor.init(file);
		idGenerator = 1;
		
		this.avgHeightFrequencyCounter.reset();
		this.fontFrequencyCounter.reset();

		pageList.clear();

	}
	
	private void buildChunkBlocks(List<WordBlock> pageWordBlockList,
			PageBlock pageBlock) {

		ChunkBlock chunkBlock = null;

		LinkedBlockingQueue<WordBlock> wordBlockList = new LinkedBlockingQueue<WordBlock>();
		ArrayList<WordBlock> seenList = new ArrayList<WordBlock>();
		List tempList;
		int counter;
		ArrayList<ChunkBlock> chunkBlockList = new ArrayList<ChunkBlock>();

		while (pageWordBlockList.size() > 0) {
			wordBlockList.clear();
			wordBlockList.add(pageWordBlockList.get(0));

			counter = 0;
			int extra;
			seenList.clear();

			while (wordBlockList.size() != 0) {

				WordBlock wordBlock = wordBlockList.peek();

				pageBlock.getDocument().getAvgHeightFrequencyCounter().add(
						wordBlock.getHeight()
						);
				pageBlock.getDocument().getFontFrequencyCounter().add(
						wordBlock.getFont() + ";" + wordBlock.getFontStyle() 
						);
				
				pageWordBlockList.remove(wordBlock);
				tempList = this.getOverlappingNeighbors(pageBlock, wordBlock,
						pageWordBlockList);

				tempList.removeAll(wordBlockList);
				tempList.removeAll(seenList);
				wordBlockList.addAll(tempList);

				seenList.add(wordBlockList.poll());
			}

			pageWordBlockList.removeAll(seenList);

			chunkBlock = buildChunkBlock(seenList, pageBlock);

			chunkBlockList.add(chunkBlock);

		}

		idGenerator = pageBlock.addAll(new ArrayList<SpatialEntity>(
				chunkBlockList), idGenerator);

	}

	private List<WordBlock> getOverlappingNeighbors(
			PageBlock pageBlock,
			WordBlock wordBlock, 
			List<WordBlock> pageWordList) {

		// expand the current word.
		int topX = wordBlock.getX1() - this.eastWestSpacing;
		int topY = wordBlock.getY1() - this.northSouthSpacing;
		int bottomX = wordBlock.getX2() + this.eastWestSpacing;
		int bottomY = wordBlock.getY2() + this.northSouthSpacing;

		SpatialEntity expandedWord = modelFactory.createWordBlock(
				topX, topY, bottomX, bottomY, 
				0, null, null, null);
		
		// find all overlapping words
		TreeSet listOfInteresectingBlock = new TreeSet<SpatialEntity>(
				new SpatialOrdering(SpatialOrdering.MIXED_MODE)
				);
		listOfInteresectingBlock.addAll(pageBlock.intersects(expandedWord, null));
		listOfInteresectingBlock.retainAll(pageWordList);

		List<WordBlock> overlappingNeighbors = 
				new ArrayList<WordBlock>(listOfInteresectingBlock);
		
		return overlappingNeighbors;

	}

	private void divideBlocksVertically(PageBlock page)
			throws InvalidPopularSpaceValueException {

		List<ChunkBlock> chunkBlockList;
		String leftRightMidline;
		boolean leftFlush;
		boolean rightFlush;

		chunkBlockList = new ArrayList<ChunkBlock>(page.getAllChunkBlocks(null));

		for (ChunkBlock chunky : chunkBlockList) {
			leftRightMidline = chunky.readLeftRightMedLine();
			leftFlush = chunky.isFlush(chunky.LEFT,
					chunky.getMostPopularWordHeight() * 2);
			rightFlush = chunky.isFlush(chunky.RIGHT,
					chunky.getMostPopularWordHeight() * 2);
			int deltaH = chunky.getMostPopularWordHeight()
					- page.getDocument().readMostPopularWordHeight();
			if (chunky.MIDLINE.equalsIgnoreCase(leftRightMidline)
					&& (leftFlush || rightFlush) && deltaH < 3) {
				if (verticalSplitCandidate(chunky))
					this.splitBlockDownTheMiddle(chunky);
			}
		}

	}

	private boolean verticalSplitCandidate(ChunkBlock block)
			throws InvalidPopularSpaceValueException {

		// 0:x,1:width
		ArrayList<Integer[]> spaceList = new ArrayList<Integer[]>();
		int previousX = 0;

		int previousWidth = 0;
		int currentX = 0;
		int currentY = 0;
		int currentWidth = 0;
		Integer[] currentSpace = new Integer[] { -1, -1 };
		Integer[] currentWidestSpace = new Integer[] { -1, -1 };

		PageBlock parent = (PageBlock) block.getContainer();
		List<SpatialEntity> wordBlockList = parent.containsByType(block,
				SpatialOrdering.MIXED_MODE, WordBlock.class);
		int pageWidth = parent.getMargin()[2] - parent.getMargin()[0];
		int marginHeight = parent.getMargin()[3] - parent.getMargin()[1];
		int averageWidth = 0;
		float spaceWidthToPageWidth = 0;

		for (int i = 0; i < wordBlockList.size(); i++) {
			WordBlock wordBlock = (WordBlock) wordBlockList.get(i);

			// New line started
			if (i == 0
					|| Math.abs(((double) (wordBlock.getY1() - currentY) / (double) marginHeight)) > 0.01) {

				currentY = wordBlock.getY1();
				currentX = wordBlock.getX1();
				currentWidth = wordBlock.getWidth();
				if (currentWidestSpace[1] > 0) {
					spaceList.add(new Integer[] { currentWidestSpace[0],
							currentWidestSpace[1] });
				}
				currentWidestSpace[0] = -1;
				currentWidestSpace[1] = -1;
				continue;
			}

			// Continuing current line
			previousX = currentX;
			previousWidth = currentWidth;
			currentY = wordBlock.getY1();
			currentX = wordBlock.getX1();
			currentWidth = wordBlock.getWidth();
			currentSpace[1] = currentX - (previousX + previousWidth);
			currentSpace[0] = currentX + currentWidth;

			if (currentWidestSpace[1] == -1
					|| currentSpace[1] > currentWidestSpace[1]) {
				currentWidestSpace[0] = currentSpace[0];
				currentWidestSpace[1] = currentSpace[1];
			}
		}

		// Criterium for whether the widest spaces are properly lined up:
		// At least 20% of them have an x position within that differ with less
		// than 1% to the x position of the previous space.
		// The average x position doesn't matter!
		if (spaceList.size() <= 0)
			return false;

		// Find average width of the widest spaces and make sure it's at least
		// as wide as 2.5% of the page width.
		for (int i = 0; i < spaceList.size(); i++)
			averageWidth += spaceList.get(i)[1];
		averageWidth = averageWidth / spaceList.size();
		// spaceWidthToPageWidth = (float) averageWidth / (float) pageWidth;

		/*
		 * if (spaceWidthToPageWidth > 0.015) return true; else return false;
		 */
		if (averageWidth > parent
				.getMostPopularHorizontalSpaceBetweenWordsPage())
			return true;
		else
			return false;
	}

	private void splitBlockDownTheMiddle(ChunkBlock block) {

		PageBlock parent = (PageBlock) block.getContainer();
		int median = parent.getMedian();
		ArrayList<WordBlock> leftBlocks = new ArrayList<WordBlock>();
		ArrayList<WordBlock> rigthBlocks = new ArrayList<WordBlock>();
		List<SpatialEntity> wordBlockList = parent.containsByType(block,
				SpatialOrdering.MIXED_MODE, WordBlock.class);
		String wordBlockLeftRightMidLine;
		for (int i = 0; i < wordBlockList.size(); i++) {
			WordBlock wordBlock = (WordBlock) wordBlockList.get(i);
			wordBlockLeftRightMidLine = wordBlock.readLeftRightMedLine();

			if (wordBlockLeftRightMidLine.equals(Block.LEFT))
				leftBlocks.add(wordBlock);
			else if (wordBlockLeftRightMidLine.equals(Block.RIGHT))
				rigthBlocks.add(wordBlock);
			else if (wordBlockLeftRightMidLine.equals(Block.MIDLINE)) {
				// Assign the current word to the left or right side depending
				// upon
				// whether most of the word is on the left or right side of the
				// median.

				if (Math.abs(median - wordBlock.getX1()) > Math.abs(wordBlock
						.getX2() - median)) {
					wordBlock.resize(wordBlock.getX1(), wordBlock.getY1(),
							median - wordBlock.getX1(), wordBlock.getHeight());

				} else {
					wordBlock.resize(median, wordBlock.getY1(),
							wordBlock.getX2() - median, wordBlock.getHeight());
					rigthBlocks.add(wordBlock);

				}

			}
		}// END for

		if (leftBlocks.size() == 0 || rigthBlocks.size() == 0)
			return;
		
		ChunkBlock leftChunkBlock = buildChunkBlock(leftBlocks, parent);
		ChunkBlock rightChunkBlock = buildChunkBlock(rigthBlocks, parent);

		SpatialEntity entity = modelFactory.createWordBlock(
				leftChunkBlock.getX2() + 1, leftChunkBlock.getY1(),
				rightChunkBlock.getX1() - 1, rightChunkBlock.getY2(), 0, null,
				null, null);
		if (parent.intersectsByType(entity, null, WordBlock.class).size() >= 1) {
			if (block == null) {
				logger.info("null null");
			}
			for (SpatialEntity wordBlockEntity : wordBlockList)
				((Block) wordBlockEntity).setContainer(block);

			return;
		}

		double relative_overlap = leftChunkBlock
				.getRelativeOverlap(rightChunkBlock);
		if (relative_overlap < 0.1) {
			parent.delete(block, block.getId());
			parent.add(leftChunkBlock, idGenerator++);
			parent.add(rightChunkBlock, idGenerator++);
		}

	}

	private ChunkBlock buildChunkBlock(List<WordBlock> wordBlockList,
			PageBlock pageBlock) {

		ChunkBlock chunkBlock = null;
		
		IntegerFrequencyCounter lineHeightFrequencyCounter = new IntegerFrequencyCounter(1);
		IntegerFrequencyCounter spaceFrequencyCounter = new IntegerFrequencyCounter(0);
		FrequencyCounter fontFrequencyCounter = new FrequencyCounter();
		FrequencyCounter styleFrequencyCounter = new FrequencyCounter();

		for (WordBlock wordBlock : wordBlockList) {
			
			lineHeightFrequencyCounter.add(wordBlock.getHeight());
			spaceFrequencyCounter.add(wordBlock.getSpaceWidth());

			avgHeightFrequencyCounter.add(wordBlock.getHeight());
			fontFrequencyCounter.add(wordBlock.getFont());
			styleFrequencyCounter.add(wordBlock.getFontStyle());
			
			if (chunkBlock == null) {

				chunkBlock = modelFactory
						.createChunkBlock(wordBlock.getX1(), wordBlock.getY1(),
								wordBlock.getX2(), wordBlock.getY2());

			} else {
			
				SpatialEntity spatialEntity = chunkBlock.union(wordBlock);
				chunkBlock.resize(spatialEntity.getX1(), spatialEntity.getY1(),
						spatialEntity.getWidth(), spatialEntity.getHeight());

			}

			wordBlock.setContainer(chunkBlock);

		}
		
		chunkBlock.setMostPopularWordFont(
				(String) fontFrequencyCounter.getMostPopular()
				);

		chunkBlock.setMostPopularWordStyle(
				(String) styleFrequencyCounter.getMostPopular()
				);
		
		chunkBlock.setMostPopularWordHeight(
				lineHeightFrequencyCounter.getMostPopular()
				);
		
		chunkBlock.setMostPopularWordSpaceWidth(
				spaceFrequencyCounter.getMostPopular()
				);
		
		chunkBlock.setContainer(pageBlock);
		
		return chunkBlock;

	}

	private void divideBlocksHorizontally(PageBlock page) {

		List<ChunkBlock> chunkBlockList;
		ArrayList<Integer> breaks;

		chunkBlockList = page.getAllChunkBlocks(SpatialOrdering.MIXED_MODE);
		for (ChunkBlock chunky : chunkBlockList) {
			breaks = this.getBreaks(chunky);
			if (breaks.size() > 0)
				this.splitBlockByBreaks(chunky, breaks);
		}

	}

	private ArrayList<Integer> getBreaks(ChunkBlock block) {
		ArrayList<Integer> breaks = new ArrayList<Integer>();
		PageBlock parent = (PageBlock) block.getContainer();

		int mostPopulareWordHeightOverCorpora = parent.getDocument()
				.readMostPopularWordHeight();

		List<SpatialEntity> wordBlockList = parent.containsByType(block,
				SpatialOrdering.MIXED_MODE, WordBlock.class);

		WordBlock firstWordOnLine = (WordBlock) wordBlockList.get(0);
		WordBlock lastWordOnLine = firstWordOnLine;

		int lastY = firstWordOnLine.getY1() + firstWordOnLine.getHeight() / 2;
		int currentY = lastY;

		String chunkBlockString = "";

		ArrayList<Integer> breakCandidates = new ArrayList<Integer>();

		ArrayList<HorizontalSplitFeature> featureList = new ArrayList<HorizontalSplitFeature>();
		HorizontalSplitFeature feature = new HorizontalSplitFeature();
		for (SpatialEntity entity : wordBlockList) {
			lastY = currentY;
			WordBlock wordBlock = (WordBlock) entity;
			currentY = wordBlock.getY1() + wordBlock.getHeight() / 2;

			if (currentY > lastY + wordBlock.getHeight() / 2) {
				feature.calculateFeatures(block, firstWordOnLine,
						lastWordOnLine, chunkBlockString);
				featureList.add(feature);
				feature = new HorizontalSplitFeature();
				breakCandidates
						.add((lastWordOnLine.getY2() + wordBlock.getY1()) / 2);

				firstWordOnLine = wordBlock;
				lastWordOnLine = wordBlock;
				chunkBlockString = "";

			}
			feature.addToFrequencyCounters(wordBlock.getFont(),
					wordBlock.getFontStyle());
			chunkBlockString = chunkBlockString + " " + wordBlock.getWord();
			lastWordOnLine = wordBlock;
		}
		feature.calculateFeatures(block, firstWordOnLine, lastWordOnLine,
				chunkBlockString);
		featureList.add(feature);
		feature = null;
		HorizontalSplitFeature featureMinusOne;

		// What kind of column is this?
		//
		// a. Titles and large-font blocks
		// b. centered titles
		// c. centered blocks
		// d. text & titles in left or right columns
		// e. references
		// f. figure legends

		for (int i = 1; i < featureList.size(); i++) {
			featureMinusOne = featureList.get(i - 1);
			feature = featureList.get(i);

			if (featureMinusOne.isAllCapitals() && !feature.isAllCapitals()) {
				breaks.add(breakCandidates.get(i - 1));
			} else if (!featureMinusOne.isAllCapitals()
					&& feature.isAllCapitals()) {
				breaks.add(breakCandidates.get(i - 1));
			} else if (featureMinusOne.getMostPopularFont() != null
					&& feature.getMostPopularFont() == null) {
				breaks.add(breakCandidates.get(i - 1));
			} else if (featureMinusOne.getMostPopularFont() == null
					&& feature.getMostPopularFont() != null) {
				breaks.add(breakCandidates.get(i - 1));
			} else if (!featureMinusOne.getMostPopularFont().equals(
					feature.getMostPopularFont())
					&& !feature.isMixedFont() && !featureMinusOne.isMixedFont()) {
				breaks.add(breakCandidates.get(i - 1));
			} else if (Math.abs(feature.getFirstWordOnLineHeight()
					- featureMinusOne.getFirstWordOnLineHeight()) > 2) {
				breaks.add(breakCandidates.get(i - 1));

			} else if (Math.abs(feature.getMidYOfLastWordOnLine()
					- featureMinusOne.getMidYOfLastWordOnLine()) > (feature
					.getFirstWordOnLineHeight() + featureMinusOne
					.getFirstWordOnLineHeight()) * 0.75) {
				breaks.add(breakCandidates.get(i - 1));
			} else if (Math.abs(featureMinusOne.getFirstWordOnLineHeight()
					- mostPopulareWordHeightOverCorpora) <= 2
					&& Math.abs(feature.getFirstWordOnLineHeight()
							- mostPopulareWordHeightOverCorpora) <= 2
					&& Math.abs(featureMinusOne.getMidOffset()) < 10
					&& Math.abs(featureMinusOne.getExtremLeftOffset()) > 10
					&& Math.abs(featureMinusOne.getExtremeRightOffset()) > 10
					&& Math.abs(feature.getExtremLeftOffset()) < 20
					&& Math.abs(feature.getExtremeRightOffset()) < 10) {
				breaks.add(breakCandidates.get(i - 1));
			} else if (Math.abs(feature.getFirstWordOnLineHeight()
					- mostPopulareWordHeightOverCorpora) <= 2
					&& Math.abs(feature.getMidOffset()) < 10
					&& Math.abs(feature.getExtremLeftOffset()) > 10
					&& Math.abs(feature.getExtremeRightOffset()) > 10
					&& Math.abs(featureMinusOne.getExtremLeftOffset()) < 10) {
				breaks.add(breakCandidates.get(i - 1));
			} else if (featureMinusOne.isEndOFLine()
					&& Math.abs(featureMinusOne.getFirstWordOnLineHeight()
							- mostPopulareWordHeightOverCorpora) <= 2
					&& (Math.abs(featureMinusOne.getExtremeRightOffset()) > 10 || Math
							.abs(feature.getExtremLeftOffset()) > 10)) {
				breaks.add(breakCandidates.get(i - 1));
			}

		}

		return breaks;
	}

	private void splitBlockByBreaks(ChunkBlock block, ArrayList<Integer> breaks) {

		Collections.sort(breaks);
		PageBlock parent = (PageBlock) block.getContainer();

		List<SpatialEntity> wordBlockList = parent.containsByType(block,
				SpatialOrdering.MIXED_MODE, WordBlock.class);

		int y;
		int breakIndex;
		ArrayList<ArrayList<WordBlock>> bigBlockList = new ArrayList<ArrayList<WordBlock>>();
		for (int j = 0; j < breaks.size() + 1; j++) {
			ArrayList<WordBlock> littleBlockList = new ArrayList<WordBlock>();
			bigBlockList.add(littleBlockList);
		}

		for (SpatialEntity entity : wordBlockList) {
			WordBlock wordBlock = (WordBlock) entity;
			y = wordBlock.getY1() + wordBlock.getHeight() / 2;
			breakIndex = Collections.binarySearch(breaks, y);
			if (breakIndex < 0) {
				breakIndex = -1 * breakIndex - 1;
				bigBlockList.get(breakIndex).add(wordBlock);

			} else {
				bigBlockList.get(breakIndex).add(wordBlock);
			}
		}
		ChunkBlock chunky;
		TreeSet<ChunkBlock> chunkBlockList = new TreeSet<ChunkBlock>(
				new SpatialOrdering(SpatialOrdering.MIXED_MODE));
		for (ArrayList<WordBlock> list : bigBlockList) {
			if (list.size() == 0)
				continue;
			chunky = this.buildChunkBlock(list, parent);
			chunkBlockList.add(chunky);

		}
		parent.delete(block, block.getId());
		idGenerator = parent.addAll(
				new ArrayList<SpatialEntity>(chunkBlockList), idGenerator);
	}

	private void joinLines(PageBlock page) {

		LinkedBlockingQueue<ChunkBlock> chunkBlockList = new LinkedBlockingQueue<ChunkBlock>(
				page.getAllChunkBlocks(SpatialOrdering.MIXED_MODE));
		List wordBlockList;

		int midY;
		ChunkBlock chunky = null;
		List<SpatialEntity> neighbouringChunkBlockList;
		ChunkBlock neighbouringChunkBlock;
		ArrayList<SpatialEntity> removalList = new ArrayList<SpatialEntity>();
		while (chunkBlockList.size() > 0) {

			chunky = chunkBlockList.peek();

			wordBlockList = page.containsByType(chunky, null, WordBlock.class);
			if (wordBlockList.size() < 4 && chunky.readNumberOfLine() == 1) {

				neighbouringChunkBlockList = page.intersectsByType(
						calculateBoundariesForJoin(chunky, page),
						SpatialOrdering.MIXED_MODE, ChunkBlock.class);

				if (neighbouringChunkBlockList.size() <= 1) {
					chunkBlockList.poll();
					continue;
				}
				for (SpatialEntity entity : neighbouringChunkBlockList) {
					neighbouringChunkBlock = (ChunkBlock) entity;
					if (neighbouringChunkBlock.equals(chunky))
						continue;
					midY = chunky.getY1() + chunky.getHeight() / 2;
					if (neighbouringChunkBlock.getY1() < midY
							&& neighbouringChunkBlock.getY2() > midY
							&& ((neighbouringChunkBlock.getX2() < chunky
									.getX1() && neighbouringChunkBlock
									.readNumberOfLine() < 3) || (neighbouringChunkBlock
									.getX1() > chunky.getX2() && neighbouringChunkBlock
									.readNumberOfLine() == 1))) {
						removalList.add(neighbouringChunkBlock);
						wordBlockList.addAll(page.containsByType(
								neighbouringChunkBlock, null, WordBlock.class));

					}
				}

				if (removalList.size() > 0) {

					ChunkBlock newChunkBlock = this.buildChunkBlock(
							wordBlockList, page);
					page.add(newChunkBlock, idGenerator++);
					page.delete(chunky, chunky.getId());
					chunkBlockList.removeAll(removalList);
					for (SpatialEntity forDeleteEntity : removalList) {
						page.delete(forDeleteEntity, forDeleteEntity.getId());
					}
				}

			}
			removalList.clear();

			chunkBlockList.poll();

		}

	}

	private SpatialEntity calculateBoundariesForJoin(ChunkBlock chunk,
			PageBlock parent) {

		SpatialEntity entity = null;
		int x1 = 0, x2 = 0, y1 = 0, y2 = 0;
		int width = parent.getMargin()[2] - parent.getMargin()[0];
		int height = parent.getMargin()[3] - parent.getMargin()[1];
		String lrm = chunk.readLeftRightMedLine();

		width = (int) (width * 0.25);
		y1 = chunk.getY1();
		y2 = chunk.getY2();
		if (Block.LEFT.equalsIgnoreCase(lrm)) {
			// TODO:Use reflection

			x1 = (chunk.getX1() - width <= 0) ? parent.getMargin()[0] : chunk
					.getX1() - width;
			x2 = (chunk.getX2() + width >= parent.getMedian()) ? parent
					.getMedian() : chunk.getX2() + width;

			entity = modelFactory.createChunkBlock(x1, y1, x2, y2);

		} else if (Block.RIGHT.equalsIgnoreCase(lrm)) {

			x1 = (chunk.getX1() - width <= parent.getMedian()) ? parent
					.getMedian() : chunk.getX1() - width;
			x2 = (chunk.getX2() + width >= parent.getMargin()[2]) ? parent
					.getMargin()[2] : chunk.getX2() + width;

			entity = modelFactory.createChunkBlock(x1, y1, x2, y2);

		} else {

			x1 = (chunk.getX1() - width <= 0) ? parent.getMargin()[0] : chunk
					.getX1() - width;
			x2 = (chunk.getX2() + width >= parent.getMargin()[2]) ? parent
					.getMargin()[2] : chunk.getX2() + width;
			entity = modelFactory.createChunkBlock(x1, y1, x2, y2);
		}

		return entity;

	}

	private void deleteHighlyOverlappedChunkBlocks(PageBlock page) {
		
		List<ChunkBlock> chunkBlockList = page.getAllChunkBlocks(
				SpatialOrdering.MIXED_MODE
				);
		
		ChunkBlock chunky;
		ChunkBlock neighbourChunk;
		List<SpatialEntity> neighbouringChunkBlockList;
		List<SpatialEntity> wordList;
		SpatialEntity intersectingRectangle;
		double property1, property2;

		for (SpatialEntity entity : chunkBlockList) {

			chunky = (ChunkBlock) entity;

			neighbouringChunkBlockList = page.intersectsByType(chunky,
					SpatialOrdering.MIXED_MODE, ChunkBlock.class);

			for (SpatialEntity neighbourEntity : neighbouringChunkBlockList) {

				neighbourChunk = (ChunkBlock) neighbourEntity;

				intersectingRectangle = chunky
						.getIntersectingRectangle(neighbourChunk);

				property1 = (intersectingRectangle.getHeight() * intersectingRectangle
						.getWidth())
						/ (double) (chunky.getWidth() * chunky.getHeight());

				property2 = (double) (intersectingRectangle.getHeight() * intersectingRectangle
						.getWidth())
						/ (double) (neighbourChunk.getWidth() * neighbourChunk
								.getHeight());

				if (property1 > property2 && property1 > 0.9) {
					wordList = page.containsByType(chunky, null,
							WordBlock.class);

					for (SpatialEntity wordEntity : wordList)
						((Block) wordEntity).setContainer(neighbourChunk);
					page.delete(chunky, chunky.getId());

				}

				if (property2 > property1 && property2 > 0.9) {
					wordList = page.containsByType(neighbourChunk, null,
							WordBlock.class);

					for (SpatialEntity wordEntity : wordList)
						((Block) wordEntity).setContainer(chunky);
					page.delete(neighbourChunk, neighbourChunk.getId());

				}

			}

		}

	}

}
