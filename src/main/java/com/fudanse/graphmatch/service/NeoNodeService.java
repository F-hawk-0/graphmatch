package com.fudanse.graphmatch.service;

import com.fudanse.graphmatch.model.Edge;
import com.fudanse.graphmatch.model.NeoNode;
import com.fudanse.graphmatch.persistence.INeoDAO;
import com.fudanse.graphmatch.persistence.NeoDAO;

public class NeoNodeService implements INeoNodeService {

	private INeoDAO neoDAO = new NeoDAO();

	@Override
	public NeoNode saveNode(NeoNode node) {
		NeoNode returnNode = null;
		if (node != null){
			returnNode = neoDAO.saveNeoNode(node);
			neoDAO.addLabel(returnNode.getId(), "test1");
		}
		node = returnNode;
		return node;
	}

	@Override
	public boolean saveEdge(NeoNode left, NeoNode right, Edge e) {
		if (left != null && right != null)
			neoDAO.saveEdge(left, right, e);
		return true;
	}

}
