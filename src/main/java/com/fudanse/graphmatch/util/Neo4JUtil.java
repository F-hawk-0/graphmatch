package com.fudanse.graphmatch.util;

import static org.neo4j.driver.v1.Values.parameters;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

public class Neo4JUtil {

	private final static String uri = "bolt://localhost:7687";
	private final static String username = "neo4j";
	private final static String password = "fdse";

	public static Driver getDriver() {
		Driver driver = null;
		try {
			driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return driver;
	}

	public static void closeDriver(Driver driver) {
		try {
			if (driver != null)
				driver.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Session getSession(Driver driver) {
		Session session = null;
		try {
			if (driver != null)
				session = driver.session();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return session;
	}

	public static void closeSession(Session session) {
		try {
			if (session != null)
				session.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Driver driver = Neo4JUtil.getDriver();
		Session session = Neo4JUtil.getSession(driver);
		final String message = "123";
		String greeting = session.writeTransaction(new TransactionWork<String>() {
			public String execute(Transaction tx) {
				StatementResult result = tx.run("CREATE (a:Greeting) " + "SET a.message = $message "
						+ "RETURN id(a)", parameters("message", message));
				return result.single().get(0).toString();
				//return result.single().get(0).asInt();
				//return result.single().get(0).asString();
			}
		});
		System.out.println(greeting);

	}

}
