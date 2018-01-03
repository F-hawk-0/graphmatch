package com.fudanse.graphmatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fudanse.graphmatch.model.NeoNode;

/**
 * Hello world!
 *
 */
public class Main {
	
	public void sss(Map<String,String> map){
		map.put("aaa", "bbb");
	}
	
	public void s(NeoNode node){
		node.setBelongto(5);
	}
	
	public static void main(String[] args) {
		/*SemanticGraph sg = new SemanticGraph(FileUtil.openCU("/Users/xiyaoguo/Desktop/XListViewer.java"));
		sg.analyzeMethod("onTouchEvent");*/
		/*Map<NeoNode,String> map = new HashMap<>();
		NeoNode n1 = new NeoNode("ss","ss");
		NeoNode n2 = new NeoNode("ss","ss");
		map.put(n1, "aaa");
		System.out.println(map.get(n2));*/
//		INeoNodeService service = new NeoNodeService();
//		NeoNode node1 = new NeoNode("aaa","sss");
//		service.saveNode(node1);
		//node1.setId(1);
		//test t = new tests();
		//t.modify(node1);
		//node1.setId(1);
		//service.saveNode(node1);
		//System.out.println(node1.getId());
		List<String> strs = new ArrayList<>();
		for(String str : strs) {
			System.out.println("str");
		}
	}
}
