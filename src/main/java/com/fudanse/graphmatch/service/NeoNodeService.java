package com.fudanse.graphmatch.service;

import com.fudanse.graphmatch.model.Edge;
import com.fudanse.graphmatch.model.NeoNode;
import com.fudanse.graphmatch.persistence.INeoDAO;
import com.fudanse.graphmatch.persistence.JDBCNeoDAO;

public class NeoNodeService implements INeoNodeService {

	private INeoDAO neoDAO = new JDBCNeoDAO();

	@Override
	public NeoNode saveNode(NeoNode node) {
		NeoNode returnNode = null;
		if (node != null) {
			returnNode = neoDAO.saveNeoNode(node);
			neoDAO.addLabel(returnNode.getId(), "test1");
		}
		return node;
	}

	@Override
	public boolean saveEdge(Integer left, Integer right, Edge e) {
		if (left != null && right != null)
			neoDAO.saveEdge(left, right, e);
		return true;
	}

}
