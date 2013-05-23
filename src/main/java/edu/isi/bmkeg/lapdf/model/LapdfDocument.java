package edu.isi.bmkeg.lapdf.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.isi.bmkeg.lapdf.extraction.exceptions.InvalidPopularSpaceValueException;
import edu.isi.bmkeg.lapdf.model.RTree.RTPageBlock;
import edu.isi.bmkeg.lapdf.model.RTree.RTSpatialEntity;
import edu.isi.bmkeg.lapdf.model.ordering.SpatialOrdering;
import edu.isi.bmkeg.lapdf.model.spatial.SpatialEntity;
import edu.isi.bmkeg.utils.FrequencyCounter;
import edu.isi.bmkeg.utils.IntegerFrequencyCounter;

public class LapdfDocument implements Serializable {

	private File pdfFile;
	
	private ArrayList<PageBlock> pageList;
	
	private IntegerFrequencyCounter avgHeightFrequencyCounter;
	private FrequencyCounter fontFrequencyCounter;
	
	private int mostPopularWordHeight = -1;
	private String mostPopularFontStyle = "";
	private String nextMostPopularFontStyle = "";
	private String mostPopularFontStyleOnLastPage = "";
	
	// This the rectangle that holds the text of the main 'panel' 
	// across the whole document (excluding footers and headers)
	private SpatialEntity bodyTextFrame;

	private boolean jPedalDecodeFailed;

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public LapdfDocument(File pdfFile) {
		this.setPdfFile(pdfFile);
		this.setAvgHeightFrequencyCounter(new IntegerFrequencyCounter(1));
		this.setFontFrequencyCounter(new FrequencyCounter());
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public boolean hasjPedalDecodeFailed() {
		return jPedalDecodeFailed;
	}

	public void setjPedalDecodeFailed(boolean jPedalDecodeFailed) {
		this.jPedalDecodeFailed = jPedalDecodeFailed;
	}

	public int getTotalNumberOfPages() {
		return this.pageList.size();
	}

	public File getPdfFile() {
		return pdfFile;
	}

	public void setPdfFile(File pdfFile) {
		this.pdfFile = pdfFile;
	}

	public IntegerFrequencyCounter getAvgHeightFrequencyCounter() {
		return avgHeightFrequencyCounter;
	}

	public void setAvgHeightFrequencyCounter(IntegerFrequencyCounter avgHeightFrequencyCounter) {
		this.avgHeightFrequencyCounter = avgHeightFrequencyCounter;
	}
	
	public FrequencyCounter getFontFrequencyCounter() {
		return fontFrequencyCounter;
	}

	public void setFontFrequencyCounter(FrequencyCounter fontFrequencyCounter) {
		this.fontFrequencyCounter = fontFrequencyCounter;
	}

	public SpatialEntity getBodyTextFrame() {
		return bodyTextFrame;
	}

	public void setBodyTextFrame(SpatialEntity bodyTextFrame) {
		this.bodyTextFrame = bodyTextFrame;
	}

	public String getMostPopularFontStyle() {
		return mostPopularFontStyle;
	}

	public void setMostPopularFontStyle(String mostPopularFontStyle) {
		this.mostPopularFontStyle = mostPopularFontStyle;
	}

	public String getNextMostPopularFontStyle() {
		return nextMostPopularFontStyle;
	}

	public void setNextMostPopularFontStyle(String nextMostPopularFontStyle) {
		this.nextMostPopularFontStyle = nextMostPopularFontStyle;
	}

	public String getMostPopularFontStyleOnLastPage() {
		return mostPopularFontStyleOnLastPage;
	}

	public void setMostPopularFontStyleOnLastPage(
			String mostPopularFontStyleOnLastPage) {
		this.mostPopularFontStyleOnLastPage = mostPopularFontStyleOnLastPage;
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public void addPages(List<PageBlock> pageList) {
		this.pageList = new ArrayList<PageBlock>(pageList);
	}

	public PageBlock getPage(int pageNumber) {

		return pageList.get(pageNumber - 1);
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public ChunkBlock getLastChunkBlock(ChunkBlock chunk)
			throws InvalidPopularSpaceValueException {
		
		int pageNumber = ((PageBlock) chunk.getContainer()).getPageNumber();
		PageBlock page = this.getPage(pageNumber);
		
		if (page.getMostPopularVerticalSpaceBetweenWordsPage() < 0
				&& page.getMostPopularWordHeightPage() > page
						.getMostPopularWordWidthPage() * 2) {
			// page.getMostPopularWordHeightPage()>page.getMostPopularWordWidthPage()*2
			
			System.err.println(
					"Possible page with vertical text flow at page number +"
					+ pageNumber);

			// throw new
			// InvalidPopularSpaceValueException("Possible page with vertical text flow at page number +"+pageNumber);
		}

		if (chunk.readLastChunkBlock() != null) {
			// System.out.println("Same page");
			return chunk.readLastChunkBlock();
		} else {
			pageNumber = ((PageBlock) chunk.getContainer()).getPageNumber() - 1;

			if (pageNumber == 0) {
				return null;
			}

			page = this.getPage(pageNumber);
			List<ChunkBlock> sortedChunkBlockList = page
					.getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE);
			// System.out.println("Page:"+ pageNumber);
			return sortedChunkBlockList.get(sortedChunkBlockList.size() - 1);
		}

	}

	public int readMostPopularWordHeight() {
		
		if( this.mostPopularWordHeight != -1 )
			return this.mostPopularWordHeight;
		
		int mp = this.avgHeightFrequencyCounter.getMostPopular();
		double mpCount = this.avgHeightFrequencyCounter.getCount(mp);
		int nmp = this.avgHeightFrequencyCounter.getNextMostPopular();
		double nmpCount = this.avgHeightFrequencyCounter.getCount(nmp);
		double ratio = nmpCount / mpCount;

		// Sneaky check for long reference sections
		if (nmp > mp && ratio > 0.8) {
			mostPopularWordHeight = nmp;
		} else {
			mostPopularWordHeight = mp;
		}

		return mostPopularWordHeight;
	}	
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public void packForSerialization() {

		this.setBodyTextFrame(null);
		for (int i = 1; i <= this.getTotalNumberOfPages(); i++) {	
			RTPageBlock page = (RTPageBlock) this.getPage(i);
			page.packForSerialization();
		}	
	
	}
	
	public void unpackFromSerialization() {
	
		for (int i = 1; i <= this.getTotalNumberOfPages(); i++) {	
			RTPageBlock page = (RTPageBlock) this.getPage(i);
			page.unpackFromSerialization();
		}	
		this.calculateBodyTextFrame();
		
	}

	public void calculateBodyTextFrame() {

		String mp = (String) this.fontFrequencyCounter.getMostPopular();
		String[] mpArray = mp.split(";");
		
		int x_min = 10000;
		int y_min = 10000;
		int x_max = -1;
		int y_max = -1;
		
		Iterator<PageBlock> pgIt = this.pageList.iterator();
		while( pgIt.hasNext() ) {
			PageBlock pg = pgIt.next();
			
			Iterator<WordBlock> wdIt = pg.getAllWordBlocks(SpatialOrdering.MIXED_MODE).iterator();
			while( wdIt.hasNext() ) {
				WordBlock wd = wdIt.next();

				if( wd.getFont() == null || wd.getFontStyle() == null) 
					continue;
				
				if( wd.getFont().equals(mpArray[0]) && 
						wd.getFontStyle().equals(mpArray[1]) ) {
				
					if( wd.getX1() < x_min )
						 x_min = wd.getX1();
					if( wd.getX2() > x_max )
						 x_max = wd.getX2();
					if( wd.getY1() < y_min )
						 y_min = wd.getY1();
					if( wd.getY2() > y_max )
						 y_max = wd.getY2();
					
				}
			
			}
		
		}
		
		this.setBodyTextFrame(new RTSpatialEntity(
				(float) x_min, (float) y_min, (float) x_max, (float) y_max
				));
		
	}

	public void calculateMostPopularFontStyles() {

		String lastPage = this.readMostPopularFontStyleOnLastPage();

		String mp = (String) this.fontFrequencyCounter.getMostPopular();
		String nmp = (String) this.fontFrequencyCounter.getNextMostPopular();
		String nnmp = (String) this.fontFrequencyCounter.getThirdMostPopular();

		if( mp.equals( lastPage ) ) {
			
			this.setMostPopularFontStyle(nmp);
			this.setNextMostPopularFontStyle(nnmp);
			
		} else if( nmp.equals( lastPage ) ) {

			this.setMostPopularFontStyle(mp);
			this.setNextMostPopularFontStyle(nnmp);
			
		} else {

			this.setMostPopularFontStyle(mp);
			this.setNextMostPopularFontStyle(nmp);

		}
		
	}

	public String readMostPopularFontStyleOnLastPage() {

		if( this.getMostPopularFontStyleOnLastPage() != null &&
				this.getMostPopularFontStyleOnLastPage().length() > 0 ) {

			return this.getMostPopularFontStyleOnLastPage();
		
		}
		
		this.setMostPopularFontStyle((String) this.fontFrequencyCounter.getMostPopular() );
	
		FrequencyCounter freq = new FrequencyCounter ();
		
		Iterator<PageBlock> pgIt = this.pageList.iterator();
		while( pgIt.hasNext() ) {
			PageBlock pg = pgIt.next();
			
			if( pg.getPageNumber() < this.pageList.size() ) {
				continue;
			}

			
			Iterator<WordBlock> wdIt = pg.getAllWordBlocks(SpatialOrdering.MIXED_MODE).iterator();
			while( wdIt.hasNext() ) {
				WordBlock wd = wdIt.next();

				if( wd.getFont() == null || wd.getFontStyle() == null) 
					continue;
				
				freq.add( wd.getFont() + ";" + wd.getFontStyle() );
								
			}
		
		}
		
		this.setMostPopularFontStyleOnLastPage((String) freq.getMostPopular() );
		
		return this.getMostPopularFontStyleOnLastPage();
				
	}
	
	
}
