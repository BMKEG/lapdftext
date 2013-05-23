package edu.isi.bmkeg.lapdf.model.RTree;

import java.util.List;

import edu.isi.bmkeg.lapdf.model.Block;
import edu.isi.bmkeg.lapdf.model.ChunkBlock;
import edu.isi.bmkeg.lapdf.model.LapdfDocument;
import edu.isi.bmkeg.lapdf.model.PageBlock;
import edu.isi.bmkeg.lapdf.model.WordBlock;
import edu.isi.bmkeg.lapdf.model.ordering.SpatialOrdering;
import edu.isi.bmkeg.lapdf.model.spatial.SpatialEntity;

public class RTPageBlock extends RTSpatialRepresentation implements PageBlock {
	
	private int pageNumber;
	private int boxHeight;
	private int boxWidth;
	private LapdfDocument document;

	public RTPageBlock(int pageNumber,
			int pageBoxWidth, int pageBoxHeight,
			LapdfDocument document) {
		
		super();
		
		this.pageNumber = pageNumber;
		this.boxHeight = pageBoxHeight;
		this.boxWidth = pageBoxWidth;
		this.document = document;

	}
	
	public int getHeight() {
		return this.getX2()-this.getX1();
	}

	public int getWidth() {
		return this.getY2()-this.getY1();
	}

	public int getX1() {
		return (int) this.getMargin()[0];
	}

	public int getX2() {
		return (int) this.getMargin()[2];
	}

	public int getY1() {
		return (int) this.getMargin()[1];
	}

	public int getY2() {
		return (int) this.getMargin()[3];
	}

	public int getPageNumber() {
		return pageNumber;
	}

	@Override
	public String readLeftRightMedLine() {
		return null;
	}

	@Override
	public boolean isFlush(String condition, int value) {		
		return false;
	}

	@Override
	public Block getContainer() {
		return null;
	}

	@Override
	public void setContainer(Block block) {
	}

	@Override
	public int getPageBoxHeight() {		
		return boxHeight;
	}

	@Override
	public int getPageBoxWidth() {
		return boxWidth;
	}

	@Override
	public String getType() {
		return Block.TYPE_PAGE;
	}

	@Override
	
	public void setType(String type) {
	}

	@Override
	public LapdfDocument getDocument() {
		return document;
	}

	@Override
	public int initialize(List<WordBlock> list, int startId) {

		for(WordBlock block:list){
			block.setContainer(this);
			super.add(block, startId++);
		}
		
		return startId;
	
	}	

}
