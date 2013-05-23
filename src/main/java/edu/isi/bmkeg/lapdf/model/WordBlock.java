package edu.isi.bmkeg.lapdf.model;

import edu.isi.bmkeg.lapdf.model.spatial.SpatialEntity;

public interface WordBlock extends Block, SpatialEntity {

	public String getWord();

	public String getFont();

	public String getFontStyle();

	public int getSpaceWidth();

}
