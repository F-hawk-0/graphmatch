package com.fudanse.graphmatch.util;

import com.fudanse.graphmatch.enums.EnumNeoNodeRelation;
import com.fudanse.graphmatch.model.Edge;
import com.fudanse.graphmatch.model.NeoNode;

public class CypherStatment {
	public final static Edge PARNET = new Edge(EnumNeoNodeRelation.PARENT.getValue(),
			EnumNeoNodeRelation.PARENT.getValue());
	public final static Edge TRUE = new Edge(EnumNeoNodeRelation.CDEPENDENCY.getValue(),
			EnumNeoNodeRelation.TRUE.getValue());
	public final static Edge FALSE = new Edge(EnumNeoNodeRelation.CDEPENDENCY.getValue(),
			EnumNeoNodeRelation.FALSE.getValue());
	public final static Edge EQUALS = new Edge(EnumNeoNodeRelation.CDEPENDENCY.getValue(),
			EnumNeoNodeRelation.EQUALS.getValue());
	public final static Edge IN = new Edge(EnumNeoNodeRelation.CDEPENDENCY.getValue(),
			EnumNeoNodeRelation.IN.getValue());
	public final static Edge CDEPENDENCY = new Edge(EnumNeoNodeRelation.CDEPENDENCY.getValue(),
			EnumNeoNodeRelation.CDEPENDENCY.getValue());
	public final static Edge ORDER = new Edge(EnumNeoNodeRelation.ORDER.getValue(),
			EnumNeoNodeRelation.ORDER.getValue());

	public static String getInsertCypher(NeoNode node) {
		String insertCypher = "create(n:" + node.getLabel() + "{name:'" + node.getName() + "'}) return n";
		return insertCypher;
	}

	public static String getInsertCypher(NeoNode left, NeoNode right, Edge e) {
		String insertCypher = "Start a=node(" + left.getId() + "),b=node(" + right.getId() + ") create (a)-[r:"
				+ e.getLabel() + "{name:'" + e.getName() + "'}]->(b)";
		return insertCypher;
	}

	public static String getAddLabelCypher(Integer id, String label) {
		String addLabelCypher = "Start n=node(" + id + ") set n:" + label + " return n";
		return addLabelCypher;
	}

}
