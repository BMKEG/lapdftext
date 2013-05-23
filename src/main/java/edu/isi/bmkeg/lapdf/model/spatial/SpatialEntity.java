package edu.isi.bmkeg.lapdf.model.spatial;


public interface SpatialEntity {

	public SpatialEntity union(SpatialEntity entity);

	public void resize(int X1,int Y1,int width,int height);

	public int getY1();

	public int getY2();

	public int getX1();

	public int getX2();

	public int getHeight();

	public int getWidth();

	public double getRelativeOverlap(SpatialEntity entity);
	
	public SpatialEntity getIntersectingRectangle(SpatialEntity entity);
	
	public int getId();
	
	public void setId(int id);

}
