package com.fudanse.graphmatch;

import java.util.HashMap;
import java.util.Map;

import com.fudanse.graphmatch.model.NeoNode;

/**
 * Hello world!
 *
 */
public class Main {
	public static void main(String[] args) {
		/*SemanticGraph sg = new SemanticGraph(FileUtil.openCU("/Users/xiyaoguo/Desktop/XListViewer.java"));
		sg.analyzeMethod("onTouchEvent");*/
		Map<NeoNode,String> map = new HashMap<>();
		NeoNode n1 = new NeoNode("ss","ss");
		NeoNode n2 = new NeoNode("ss","ss");
		map.put(n1, "aaa");
		System.out.println(map.get(n2));
	}
}
