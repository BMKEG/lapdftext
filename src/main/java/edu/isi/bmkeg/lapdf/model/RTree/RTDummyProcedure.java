package edu.isi.bmkeg.lapdf.model.RTree;

import edu.isi.bmkeg.lapdf.model.spatial.SpatialEntity;
import edu.isi.bmkeg.lapdf.model.spatial.SpatialRepresentation;
import gnu.trove.TIntProcedure;

public class RTDummyProcedure implements TIntProcedure {
	
	SpatialRepresentation tree;

	public RTDummyProcedure(SpatialRepresentation tree) {
		this.tree = tree;
	}

	@Override
	public boolean execute(int arg0) {
		SpatialEntity entity = this.tree.getEntity(arg0);
		System.out.println(arg0);
		return true;
	}

}
