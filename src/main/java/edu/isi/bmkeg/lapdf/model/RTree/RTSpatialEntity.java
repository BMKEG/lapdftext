package edu.isi.bmkeg.lapdf.model.RTree;

import java.awt.geom.Rectangle2D;

import com.infomatiq.jsi.Rectangle;

import edu.isi.bmkeg.lapdf.model.spatial.SpatialEntity;

public class RTSpatialEntity extends Rectangle implements SpatialEntity {
	
	private int id;
   
	public RTSpatialEntity(float x1, float y1, float x2, float y2) {
	
		super(x1, y1, x2, y2);
	
	}

	private RTSpatialEntity(Rectangle rectangle) {
		
		super(rectangle.minX, rectangle.minY, rectangle.maxX,
				rectangle.maxY);
		
	}

	public SpatialEntity union(SpatialEntity entity) {

		RTSpatialEntity rtSpatialEntity = (RTSpatialEntity) entity;
		Rectangle rectangle = super.union(rtSpatialEntity);
		return new RTSpatialEntity(rectangle);

	}

	@Override
	public int getId() {

		return id;
	
	}

	@Override
	public void setId(int id) {

		this.id = id;
	
	}

	@Override
	public void resize( int X1, int Y1, int width, int height) {		

		super.set(X1, Y1, X1+width, Y1+height);
	
	}

	@Override
	public int getHeight() {
        int y2 = (int) super.maxY;
        int y1 = (int) super.minY;
		return Math.abs(y2-y1);
	}

	@Override
	public int getWidth() {
		int x2=(int) super.maxX;
        int x1=(int) super.minX;
		return Math.abs(x2 -x1);
	}

	@Override
	public int getX1() {
	
		return (int)super.minX;
	}

	@Override
	public int getX2() {
		
		return (int)super.maxX;
	}

	@Override
	public int getY1() {
		return (int)super.minY;
	}

	@Override
	public int getY2() {
		
		return (int)super.maxY;
	}

	@Override
	public double getRelativeOverlap(SpatialEntity entity) {
		   
		RTSpatialEntity intersection=(RTSpatialEntity) this.getIntersectingRectangle(entity);
		RTSpatialEntity union=(RTSpatialEntity) this.union(entity);
	       
		double relOverlap = (intersection.getWidth() * intersection.getHeight()) 
				/ (union.getWidth() * union.getHeight());
		
		return relOverlap;
		
	}

	@Override
	public SpatialEntity getIntersectingRectangle(SpatialEntity entity) {
		
		java.awt.Rectangle AWTRectangle1 = new java.awt.Rectangle(
				this.getX1(), this.getY1(), this.getWidth(), this.getHeight()
				);
		
		java.awt.Rectangle AWTRectangle2 = new java.awt.Rectangle(
				entity.getX1(), entity.getY1(), entity.getWidth(), entity.getHeight()
				);
		
		Rectangle2D intersectingAWTRectangle2 = AWTRectangle1
				.createIntersection(AWTRectangle2);
		
		SpatialEntity se = new RTSpatialEntity( 
				(int) intersectingAWTRectangle2.getMinX(),
				(int) intersectingAWTRectangle2.getMinY(),
				(int) intersectingAWTRectangle2.getMaxX(),
				(int) intersectingAWTRectangle2.getMaxY()
				);
		
		return se;

	}
	
	// TODO: want to enrich the feature set of this. Need this for Figures. 
	public boolean hasNeighbors(SpatialEntity entity, String direction) {
						
		return false;

	}
	
}
