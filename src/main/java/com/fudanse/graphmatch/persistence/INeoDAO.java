package com.fudanse.graphmatch.persistence;

import com.fudanse.graphmatch.model.Edge;
import com.fudanse.graphmatch.model.NeoNode;

public interface INeoDAO {
	
	public NeoNode saveNeoNode(NeoNode node);
	
	public NeoNode addLabel(Integer id,String label);
	
	public void saveEdge(NeoNode left,NeoNode right,Edge e);
	

}
