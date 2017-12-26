package com.fudanse.graphmatch.model;

public class Edge {

	private String label;
	private String name;
	
	public Edge(){
		
	}
	
	public Edge(String label, String name) {
		this.label = label;
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
