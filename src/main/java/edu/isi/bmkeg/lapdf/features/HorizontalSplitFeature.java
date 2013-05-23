package edu.isi.bmkeg.lapdf.features;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.isi.bmkeg.lapdf.model.ChunkBlock;
import edu.isi.bmkeg.lapdf.model.WordBlock;
import edu.isi.bmkeg.utils.FrequencyCounter;

public class HorizontalSplitFeature {

	private static Pattern eop = Pattern.compile("[\\.\\!\\?]\\n*$");

	private boolean mixedFont = false;
	private boolean endOFLine = false;
	private boolean allCapitals = false;
	private String mostPopularFont = null;
	private int midYOfLastWordOnLine;
	private int firstWordOnLineHeight;
	private int wordCount = 0;
	private int extremLeftOffset;
	private int extremeRightOffset;
	private int midOffset;
	private static FrequencyCounter fontFrequencyCounter = new FrequencyCounter();
	private static FrequencyCounter fontStyleFrequencyCounter = new FrequencyCounter();

	private static Pattern patternLowerCase = Pattern.compile("[a-z]");
	private static Pattern patternUpperCase = Pattern.compile("[A-Z]");

	public HorizontalSplitFeature() {

	}

	public void calculateFeatures(ChunkBlock chunky, WordBlock firstWordBlock,
			WordBlock lastWordBlock, String completeString) {

		calculateIntegerFeatures(chunky, firstWordBlock, lastWordBlock);
		calculateStringFeatures(completeString);
		fontFrequencyCounter.reset();
		fontStyleFrequencyCounter.reset();

	}

	public void addToFrequencyCounters(String font, String style) {
		fontFrequencyCounter.add(font);
		fontStyleFrequencyCounter.add(style);
		this.wordCount++;
	}

	private void calculateIntegerFeatures(ChunkBlock chunky,
			WordBlock firstWordBlock, WordBlock lastWordBlock) {
		firstWordOnLineHeight = firstWordBlock.getHeight();
		extremLeftOffset = firstWordBlock.getX1() - chunky.getX1();
		extremeRightOffset = chunky.getX2() - lastWordBlock.getX2();
		midYOfLastWordOnLine = lastWordBlock.getY1()
				+ lastWordBlock.getHeight() / 2;
		int chunkBlockMidLine = chunky.getX1() + chunky.getWidth() / 2;
		int median = (lastWordBlock.getX2() - firstWordBlock.getX1()) / 2;

		midOffset = chunkBlockMidLine - median;
	}

	private void calculateStringFeatures(String completeString) {
		endOFLine = (eop.matcher(completeString).find()) ? true : false;
		Matcher matcher = patternLowerCase.matcher(completeString);
		if (matcher.find()) {

			allCapitals = false;
		} else {
			matcher = patternUpperCase.matcher(completeString);
			if (matcher.find()) {
				allCapitals = true;
			}else{
				allCapitals=false;
			}

		}

		// allCapitals=(completeString.matches(regex))
		mostPopularFont = (String) fontFrequencyCounter.getMostPopular();
		mixedFont = (fontStyleFrequencyCounter.countOptions() > 1) ? true
				: false;
	}

	public boolean isMixedFont() {
		return mixedFont;
	}

	public boolean isEndOFLine() {
		return false;
	}

	public boolean isAllCapitals() {
		return allCapitals;
	}

	public String getMostPopularFont() {
		return mostPopularFont;
	}

	public int getMidYOfLastWordOnLine() {
		return midYOfLastWordOnLine;
	}

	public int getFirstWordOnLineHeight() {
		return firstWordOnLineHeight;
	}

	public int getWordCount() {
		return wordCount;
	}

	public int getExtremLeftOffset() {
		return extremLeftOffset;
	}

	public int getExtremeRightOffset() {
		return extremeRightOffset;
	}

	public int getMidOffset() {
		return midOffset;
	}
	
	
	
}