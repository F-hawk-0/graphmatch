package com.fudanse.graphmatch;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fudanse.graphmatch.enums.EnumNeoNodeLabelType;
import com.fudanse.graphmatch.model.NeoNode;
import com.fudanse.graphmatch.service.INeoNodeService;
import com.fudanse.graphmatch.service.NeoNodeService;
import com.fudanse.graphmatch.util.CypherStatment;
import com.fudanse.graphmatch.util.FileUtil;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.Statement;

public class ProjectToGraph {

	private INeoNodeService service;
	// private CompilationUnit cu;
	private Map<String, Integer> pgNIdPair = new HashMap<>();// package和对应的node的id
	// private Integer rootId;

	public ProjectToGraph() {
		this.service = new NeoNodeService();
	}

	public ProjectToGraph(CompilationUnit cu) {
		this.service = new NeoNodeService();
	}

	public void analyzePj(String filePath) {
		File file = new File(filePath);
		analyzePj(file);
	}

	public void analyzePj(File file) {
		if (file == null)
			return;
		NeoNode pjNode = new NeoNode(EnumNeoNodeLabelType.PROJECT.getValue(), file.getName());
		pjNode = service.saveNode(pjNode);
		// this.rootId = pjNode.getId();
		List<File> javaFiles = getJavaFiles(file);
		javaFiles.forEach((n) -> System.out.println(n.getName()));
		analyzeJavaFile(pjNode, javaFiles);
	}

	private void analyzeJavaFile(NeoNode pjNode, List<File> javaFiles) {
		for (File javaFile : javaFiles) {
			CompilationUnit cu = FileUtil.openCU(javaFile);
			Integer pgId = createPackage(pjNode, cu); // package
			Integer classId = createClass(cu);// class
			service.saveEdge(new NeoNode(pgId), new NeoNode(classId), CypherStatment.PARNET);
			List<BodyDeclaration> body = getBodyList(cu);
			List<MethodDeclaration> methods = getMethodList(body);
			for (MethodDeclaration method : methods) {
				Integer methodId = createMethod(method);
				service.saveEdge(new NeoNode(classId), new NeoNode(methodId), CypherStatment.PARNET);
				createMethodBody(method, methodId);
			}
			// createMethod(methods,classId);
		}
	}

	private void createMethodBody(MethodDeclaration method, Integer methodId) {
		BlockStmt blockStmt = method.getBody();// 有可能是抽象类或接口声明的方法，此时body为空
		if (blockStmt == null)
			return;
		List<Statement> stmts = blockStmt.getStmts();
		NeoNode preNode = null;
		for (Statement stmt : stmts) {
			// System.out.println("-----------------------------------------");
			NeoNode node = create(stmt, methodId);
			if (preNode != null)
				service.saveEdge(preNode, node, CypherStatment.ORDER);
			preNode = node;
		}
	}

	private NeoNode create(Statement stmt, Integer methodId) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 创建method节点
	 * 
	 * @param methodDeclaration
	 * @return
	 */
	private Integer createMethod(MethodDeclaration methodDeclaration) {
		NeoNode methodNode = new NeoNode(EnumNeoNodeLabelType.METHODDECLARATION.getValue(),
				methodDeclaration.getName());
		methodNode = service.saveNode(methodNode);
		return methodNode.getId();
	}

	/**
	 * 从类中获取方法的bodylist
	 * 
	 * @param body
	 * @return
	 */
	private List<MethodDeclaration> getMethodList(List<BodyDeclaration> body) {
		List<MethodDeclaration> methods = null;
		methods = body.stream().filter((n) -> (n instanceof MethodDeclaration)).map((n) -> ((MethodDeclaration) n))
				.collect(Collectors.toList());
		return methods;
	}

	/**
	 * 创建类节点
	 * 
	 * @param cu
	 * @return
	 */
	private Integer createClass(CompilationUnit cu) {
		TypeDeclaration td = getClass(cu);// td中包含类的信息
		String className = td.getName();
		NeoNode classNode = new NeoNode(EnumNeoNodeLabelType.CLASSORINTERFACE.getValue(), className);
		classNode = service.saveNode(classNode);

		return classNode.getId();
	}

	/**
	 * 从类中获取classorinteface信息
	 * 
	 * @param cu
	 * @return
	 */
	private TypeDeclaration getClass(CompilationUnit cu) {
		if (cu.getTypes() == null)
			return null;
		return cu.getTypes().stream().filter((n) -> (n instanceof ClassOrInterfaceDeclaration)).findFirst().get();
	}

	/**
	 * 根据CompilationUnit得到类成员
	 * 
	 * @param cu
	 * @return 类成员
	 */
	private List<BodyDeclaration> getBodyList(CompilationUnit cu) {
		TypeDeclaration td = getClass(cu);
		return td.getMembers();
	}

	/**
	 * 创建package，存入map中
	 * 
	 * @param pjNode
	 * @param cu
	 * @return package's id
	 */
	private Integer createPackage(NeoNode pjNode, CompilationUnit cu) {
		String pgName = cu.getPackage().getName().getName();
		if (!pgNIdPair.containsKey(pgName)) {
			NeoNode pgNode = new NeoNode(EnumNeoNodeLabelType.PACKAGE.getValue(), pgName);
			pgNode = service.saveNode(pgNode);
			service.saveEdge(pjNode, pgNode, CypherStatment.PARNET);
			pgNIdPair.put(pgName, pgNode.getId());
		}
		return pgNIdPair.get(pgName);
	}

	public static List<File> getJavaFiles(File file) {
		List<File> javaFiles = new ArrayList<>();
		if(!file.exists())
			return null;
		if(!file.isDirectory())
			javaFiles.add(file);
		else{
			File[] files = file.listFiles();
			for(File f : files){
				if(f.isDirectory())
					javaFiles.addAll(getJavaFiles(f));
				else{
					if(f.getName().length() > 5 && f.getName().substring(f.getName().length()-5).equals(".java")){
						javaFiles.add(f);
					}
				}	
			}
		}
		return javaFiles;
	}

	public INeoNodeService getService() {
		return service;
	}

	public void setService(INeoNodeService service) {
		this.service = service;
	}
	
	public static void main(String[] args) {
		List<File> javaFiles = ProjectToGraph.getJavaFiles(new File("/Users/xiyaoguo/Desktop/test"));
		javaFiles.forEach((n)->System.out.println(n.getAbsolutePath()));
	}

}
