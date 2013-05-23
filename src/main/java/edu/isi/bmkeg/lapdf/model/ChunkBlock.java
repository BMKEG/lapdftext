package edu.isi.bmkeg.lapdf.model;

import edu.isi.bmkeg.lapdf.model.spatial.SpatialEntity;

public interface ChunkBlock extends Block, SpatialEntity {

	public int getMostPopularWordHeight();

	public int getMostPopularWordSpaceWidth();

	public String getMostPopularWordFont();

	public String getMostPopularWordStyle();

	public void setMostPopularWordHeight(int height);

	public void setMostPopularWordSpaceWidth(int spaceWidth);

	public void setMostPopularWordStyle(String style);

	public void setMostPopularWordFont(String font);

	public int readNumberOfLine();

	public String readChunkText();

	public Boolean isHeaderOrFooter();

	public void setHeaderOrFooter(boolean headerOrFooter);

	public ChunkBlock readLastChunkBlock();

	boolean isMatchingRegularExpression(String regex);

	boolean isUnderOneLineFlushNeighboursOfType(String type);

	boolean hasNeighboursOfType(String type, int nsew);
	
}
