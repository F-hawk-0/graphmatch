package com.fudanse.graphmatch.persistence;

import com.fudanse.graphmatch.model.Edge;
import com.fudanse.graphmatch.model.NeoNode;

public interface INeoDAO {
	
	public NeoNode saveNeoNode(NeoNode node);
	
	public NeoNode addLabel(Integer id,String label);
	
	public void saveEdge(Integer left,Integer right,Edge e);
	

}
