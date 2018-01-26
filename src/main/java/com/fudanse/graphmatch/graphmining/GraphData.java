package com.fudanse.graphmatch.graphmining;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

/**
 * Created by Administrator on 2017-12-21.
 */
public class GraphData {
	public static final double sup = 0.6;
	public final static Driver driver = GraphDatabase.driver("bolt://10.141.221.72:7687",
			AuthTokens.basic("neo4j", "fdse"));
	public static Session session = null;
	public static Transaction tx = null;

	// public static StatementResult result=null;
	public static void databaseConnection() {
		GraphData.session = GraphData.driver.session();
		GraphData.tx = GraphData.session.beginTransaction();
		System.out.println("Connect graphdatabase successfully!");
	}

	public static StatementResult graphMatch(String str) {
		StatementResult result = GraphData.tx.run(str);
		return result;
	}

	public static void main(String[] args) {
		try {
			GraphData.databaseConnection();
			String str = "match p=(n:Project)-[:Parent*0..]->(m:MethodDeclaration{name:'onFling'})-[:Parent*1..]->(b) return p";
			StatementResult result = GraphData.graphMatch(str);
			AuthoritativePaths auPaths = SubGraphMining.miningProcess(result);
			// System.out.println(auPaths.visualizationPaths());
			// auPaths.outputPath();
			System.out.println(auPaths.visualizationPaths2());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
