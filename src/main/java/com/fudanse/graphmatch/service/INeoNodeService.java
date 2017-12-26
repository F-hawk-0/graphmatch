package com.fudanse.graphmatch.service;

import com.fudanse.graphmatch.model.Edge;
import com.fudanse.graphmatch.model.NeoNode;

public interface INeoNodeService {
	
	public NeoNode saveNode(NeoNode node);
	
	public boolean saveEdge(NeoNode left,NeoNode right,Edge e);

}
