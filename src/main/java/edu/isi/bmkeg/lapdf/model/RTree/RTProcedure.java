package edu.isi.bmkeg.lapdf.model.RTree;

import edu.isi.bmkeg.lapdf.model.ChunkBlock;
import edu.isi.bmkeg.lapdf.model.WordBlock;
import edu.isi.bmkeg.lapdf.model.ordering.SpatialOrdering;
import edu.isi.bmkeg.lapdf.model.spatial.SpatialEntity;
import gnu.trove.TIntProcedure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

public class RTProcedure implements TIntProcedure {

	private Collection<SpatialEntity> foundEntities;
	private RTSpatialRepresentation tree;
	private Class type = null;
	private RTSpatialEntity sourceEnity;
	private boolean isContainmentQuery;

	public RTProcedure(RTSpatialRepresentation tree, String ordering,
			RTSpatialEntity sourceEntity, Class type, boolean isContainmentQuery) {
		this.tree = tree;
		if (ordering != null) {
			foundEntities = new TreeSet<SpatialEntity>(new SpatialOrdering(
					ordering));
		} else {
			foundEntities = new ArrayList<SpatialEntity>();
		}
		this.type = type;
		this.sourceEnity = sourceEntity;
		this.isContainmentQuery = isContainmentQuery;

	}

	@Override
	public boolean execute(int id) {
		SpatialEntity entity = this.tree.getEntity(id);

		if (type != null) {

			if (checkType(entity) && !isSameInstance(sourceEnity, entity)) {
				this.foundEntities.add(entity);
			}
			return true;
		} else if (!sourceEnity.equals(entity)) {
			this.foundEntities.add(entity);
			return true;
		}

		return true;

	}

	public List getIntersectionList() {
		return new ArrayList(foundEntities);
	}

	private boolean checkType(SpatialEntity entity) {
		Class[] interfaces = entity.getClass().getInterfaces();
		for (int i = 0; i < interfaces.length; i++)
			if (type.getSimpleName().equals(interfaces[i].getSimpleName()))
				return true;

		return false;
	}

	private boolean isSameInstance(SpatialEntity entity1, SpatialEntity entity2) {
		if (!this.isContainmentQuery)
			return false;
		if (entity1 instanceof WordBlock && entity2 instanceof WordBlock) {

			return entity1.equals(entity2);
		} else if (entity1 instanceof ChunkBlock
				&& entity2 instanceof ChunkBlock) {
			return entity1.equals(entity2);

		} else {
			return false;
		}
	}

}
