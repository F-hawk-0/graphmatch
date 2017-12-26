package com.fudanse.graphmatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fudanse.graphmatch.enums.EnumNeoNodeLabelType;
import com.fudanse.graphmatch.model.NeoNode;
import com.fudanse.graphmatch.service.INeoNodeService;
import com.fudanse.graphmatch.service.NeoNodeService;
import com.fudanse.graphmatch.util.ConvertEnumUtil;
import com.fudanse.graphmatch.util.CypherStatment;
import com.fudanse.graphmatch.util.FileUtil;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.Node;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.EnclosedExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.DoStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.ForStmt;
import japa.parser.ast.stmt.ForeachStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.ReturnStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.stmt.SwitchEntryStmt;
import japa.parser.ast.stmt.SwitchStmt;
import japa.parser.ast.stmt.WhileStmt;

/**
 * 生成图
 * 
 * @author xiyaoguo
 *
 */
public class GenerateGraph {

	private INeoNodeService service;
	private CompilationUnit cu;
	private Map<Node, NeoNode> nodeNeoNodePair = new HashMap<>();	//node和NeoNode的keyvaluepair
	private Map<String, NeoNode> varNodePair = new HashMap<>();	//变量和它的跟节点

	public GenerateGraph() {
		this.service = new NeoNodeService();
	}

	public GenerateGraph(CompilationUnit cu) {
		this.service = new NeoNodeService();
		this.cu = cu;
	}

	public void analyzeMethod(String methodName) {
		List<Statement> stmts = getMethodBodyByName(methodName).getBody().getStmts();
		create(stmts.get(0));
		NeoNode preNode = null;
		/*for (Statement stmt : stmts) {
			// System.out.println("-----------------------------------------");
			NeoNode node = create(stmt);
			if (preNode != null)
				service.saveEdge(preNode, node, CypherStatment.ORDER);
			preNode = node;
		}*/

	}

	/**
	 * 创建neo4j的图
	 * 
	 * @param node
	 * @return neo4j对应的节点
	 */
	public NeoNode create(Node node) {
		NeoNode nn = null;
		if (node == null)
			return null;
		if (node instanceof EnclosedExpr) { // 括号()
			EnclosedExpr enclosedExpr = (EnclosedExpr) node;
			nn = create(enclosedExpr.getInner());
		} else if (node instanceof VariableDeclarationExpr) { // 变量声明语句 类似于int i,j=0
			VariableDeclarationExpr vdexpr = (VariableDeclarationExpr) node;
			nn = new NeoNode(EnumNeoNodeLabelType.VARIBLEDECLARATIONEXPR.getValue(),
					EnumNeoNodeLabelType.VARIBLEDECLARATIONEXPR.getValue());
			nn = service.saveNode(nn);
			nodeNeoNodePair.put(node, nn);
			NeoNode type = new NeoNode(EnumNeoNodeLabelType.TYPE.getValue(), vdexpr.getType().toString());
			type = service.saveNode(type);
			nodeNeoNodePair.put(vdexpr.getType(), type);
			service.saveEdge(nn, type, CypherStatment.PARNET);
			List<VariableDeclarator> vds = vdexpr.getVars();
			for (VariableDeclarator vd : vds) {
				NeoNode vdNN = create(vd);
				service.saveEdge(nn, vdNN, CypherStatment.PARNET);
			}
		} else if (node instanceof VariableDeclarator) { // 初始化语句 int x = 14
			VariableDeclarator vd = (VariableDeclarator) node;
			nn = new NeoNode(EnumNeoNodeLabelType.VARIBLEDECLARATOR.getValue(), "=");
			nn = service.saveNode(nn);
			nodeNeoNodePair.put(node, nn);
			NeoNode left = new NeoNode(EnumNeoNodeLabelType.ATOM.getValue(), vd.getId().getName());
			left = service.saveNode(left);
			nodeNeoNodePair.put(vd.getId(), left);
			service.saveEdge(nn, left, CypherStatment.PARNET);
			if (vd.getInit() != null) {
				NeoNode right = create(vd.getInit());
				service.saveEdge(nn, right, CypherStatment.PARNET);
			}
		} else if (node instanceof BlockStmt) { // BlockStmt {}
			nn = new NeoNode(EnumNeoNodeLabelType.BLOCKSTMT.getValue(), EnumNeoNodeLabelType.BLOCKSTMT.getValue());
			nn = service.saveNode(nn);
			nodeNeoNodePair.put(node, nn);
			pcBlockStmt(nn, node);
		} else if (node instanceof BinaryExpr) { // 二元表达式
			BinaryExpr binExpr = (BinaryExpr) node;
			nn = new NeoNode(EnumNeoNodeLabelType.BINARYEXPR.getValue(),
					ConvertEnumUtil.getBinaryOperator(binExpr.getOperator()));
			nn = service.saveNode(nn);
			nodeNeoNodePair.put(node, nn);
			NeoNode left = create(binExpr.getLeft());
			NeoNode right = create(binExpr.getRight());
			service.saveEdge(nn, left, CypherStatment.PARNET);
			service.saveEdge(nn, right, CypherStatment.PARNET);
		} else if (node instanceof AssignExpr) { // 赋值语句
			AssignExpr assExpr = (AssignExpr) node;
			nn = new NeoNode(EnumNeoNodeLabelType.ASSIGNEXPR.getValue(),
					ConvertEnumUtil.getAssignOperator(assExpr.getOperator()));
			nn = service.saveNode(nn);
			nodeNeoNodePair.put(node, nn);
			NeoNode target = create(assExpr.getTarget());
			NeoNode value = create(assExpr.getValue());
			service.saveEdge(nn, target, CypherStatment.PARNET);
			service.saveEdge(nn, value, CypherStatment.PARNET);
		} else if (node instanceof ExpressionStmt) { // 表达式
			nn = create(((ExpressionStmt) node).getExpression());
		} else if (node instanceof MethodCallExpr) { // 方法调用
			nn = new NeoNode(EnumNeoNodeLabelType.METHODCALLEXPR.getValue(), node.toString());
			nn = service.saveNode(nn);
			nodeNeoNodePair.put(node, nn);
		} else if (node instanceof ReturnStmt) { // Return语句，不往下细分？？？
			String nodeString = node.toString();
			nn = new NeoNode(EnumNeoNodeLabelType.RETURNSTMT.getValue(),
					nodeString.substring(0, nodeString.length() - 1));
			nn = service.saveNode(nn);
			nodeNeoNodePair.put(node, nn);
		} else if (node instanceof StringLiteralExpr) {
			StringLiteralExpr sle = (StringLiteralExpr) node;
			nn = new NeoNode(EnumNeoNodeLabelType.ATOM.getValue(), sle.getValue());
			nn = service.saveNode(nn);
			nodeNeoNodePair.put(node, nn);
		} else if (node instanceof IfStmt) { // if语句
			IfStmt ifStmt = (IfStmt) node;
			nn = new NeoNode(EnumNeoNodeLabelType.IFSTMT.getValue(), EnumNeoNodeLabelType.IFSTMT.getValue());
			service.saveNode(nn);
			nodeNeoNodePair.put(node, nn);
			Expression condition = ifStmt.getCondition();
			NeoNode conditionNN = create(condition);
			service.saveEdge(nn, conditionNN, CypherStatment.PARNET);
			Node thenNode = ifStmt.getThenStmt();
			NeoNode thenNN = create(thenNode);
			service.saveEdge(nn, thenNN, CypherStatment.PARNET);
			service.saveEdge(conditionNN, thenNN, CypherStatment.TRUE); // 添加控制依赖
			Node elseNode = ifStmt.getElseStmt();
			NeoNode elseNN = null;
			if (elseNode instanceof IfStmt) { // 处理else if语句
				elseNN = create(elseNode);
				service.saveEdge(nn, elseNN, CypherStatment.PARNET);
				service.saveEdge(conditionNN, elseNN, CypherStatment.FALSE);
			} else { // 处理else语句(带block和不带block)
				elseNN = new NeoNode(EnumNeoNodeLabelType.ELSE.getValue(), EnumNeoNodeLabelType.ELSE.getValue());
				elseNN = service.saveNode(elseNN);
				nodeNeoNodePair.put(elseNode, elseNN);
				service.saveEdge(nn, elseNN, CypherStatment.PARNET);
				service.saveEdge(conditionNN, elseNN, CypherStatment.FALSE);
				pcBlockStmt(elseNN, elseNode);
			}
		} else if (node instanceof SwitchStmt) { // switch语句
			SwitchStmt switchStmt = (SwitchStmt) node;
			nn = new NeoNode(EnumNeoNodeLabelType.SWITCHSTMT.getValue(), EnumNeoNodeLabelType.SWITCHSTMT.getValue());
			nn = service.saveNode(nn);
			nodeNeoNodePair.put(node, nn);
			NeoNode condition = create(switchStmt.getSelector());
			service.saveEdge(nn, condition, CypherStatment.PARNET);
			List<SwitchEntryStmt> seStmts = ((SwitchStmt) node).getEntries();
			for (SwitchEntryStmt seStmt : seStmts) {
				NeoNode entry = create(seStmt);
				service.saveEdge(nn, entry, CypherStatment.PARNET);
				if (!entry.getName().equals("default"))
					service.saveEdge(condition, entry, CypherStatment.EQUALS);
			}
		} else if (node instanceof SwitchEntryStmt) { // switch语句的case
			SwitchEntryStmt seStmt = (SwitchEntryStmt) node;
			nn = new NeoNode(EnumNeoNodeLabelType.SWITCHENTRY.getValue(),
					seStmt.getLabel() == null ? "default" : seStmt.getLabel().toString());
			nn = service.saveNode(nn);
			nodeNeoNodePair.put(node, nn);
			List<Statement> stmts = seStmt.getStmts();
			for (Statement stmt : stmts) {
				NeoNode stmtNN = create(stmt);
				service.saveEdge(nn, stmtNN, CypherStatment.PARNET);
			}
		} else if (node instanceof DoStmt) { // do-while语句
			DoStmt doStmt = (DoStmt) node;
			nn = new NeoNode(EnumNeoNodeLabelType.DOWHILESTMT.getValue(), EnumNeoNodeLabelType.DOWHILESTMT.getValue());
			nn = service.saveNode(nn);
			nodeNeoNodePair.put(node, nn);
			NeoNode condition = create(doStmt.getCondition());
			service.saveEdge(nn, condition, CypherStatment.PARNET);
			NeoNode body = create(doStmt.getBody());
			service.saveEdge(nn, body, CypherStatment.PARNET);
			service.saveEdge(condition, body, CypherStatment.TRUE);
			service.saveEdge(body, condition, CypherStatment.CDEPENDENCY);
		} else if (node instanceof WhileStmt) { // while语句
			WhileStmt whileStmt = (WhileStmt) node;
			nn = new NeoNode(EnumNeoNodeLabelType.WHILESTMT.getValue(), EnumNeoNodeLabelType.WHILESTMT.getValue());
			nn = service.saveNode(nn);
			nodeNeoNodePair.put(node, nn);
			NeoNode condition = create(whileStmt.getCondition());
			service.saveEdge(nn, condition, CypherStatment.PARNET);
			NeoNode body = create(whileStmt.getBody());
			service.saveEdge(nn, body, CypherStatment.PARNET);
			service.saveEdge(condition, body, CypherStatment.TRUE);
			service.saveEdge(body, condition, CypherStatment.CDEPENDENCY);
		} else if (node instanceof ForStmt) { // for语句
			ForStmt forStmt = (ForStmt) node;
			nn = new NeoNode(EnumNeoNodeLabelType.FORSTMT.getValue(), EnumNeoNodeLabelType.FORSTMT.getValue());
			nn = service.saveNode(nn);
			nodeNeoNodePair.put(node, nn);
			NeoNode init = new NeoNode(EnumNeoNodeLabelType.FORINIT.getValue(),
					EnumNeoNodeLabelType.FORINIT.getValue()); // init
			init = service.saveNode(init);
			for (Node initStmt : forStmt.getInit()) { // init的语句块
				NeoNode initNN = create(initStmt);
				service.saveEdge(init, initNN, CypherStatment.PARNET);
				if (initStmt instanceof AssignExpr){
					AssignExpr ae = (AssignExpr) initStmt;
					varNodePair.put(ae.getTarget().toString(), initNN);
				}else if(initStmt instanceof VariableDeclarationExpr){
					VariableDeclarationExpr vde = (VariableDeclarationExpr) initStmt;
					List<VariableDeclarator> vds = vde.getVars();
					for(VariableDeclarator vd : vds){
						varNodePair.put(vd.getId().getName(), initNN);
					}
				}
			}
			service.saveEdge(nn, init, CypherStatment.PARNET);

			NeoNode cmp = create(forStmt.getCompare()); // compare
			service.saveEdge(nn, cmp, CypherStatment.PARNET);
			service.saveEdge(init, cmp, CypherStatment.CDEPENDENCY);

			NeoNode body = create(forStmt.getBody()); // body
			service.saveEdge(nn, body, CypherStatment.PARNET);
			service.saveEdge(cmp, body, CypherStatment.TRUE);

			NeoNode update = new NeoNode(EnumNeoNodeLabelType.FORUPDATE.getValue(),
					EnumNeoNodeLabelType.FORUPDATE.getValue()); // update
			update = service.saveNode(update);
			for (Node updateStmt : forStmt.getUpdate()) {
				NeoNode updateNN = create(updateStmt);
				service.saveEdge(update, updateNN, CypherStatment.PARNET);
			}
			service.saveEdge(nn, update, CypherStatment.PARNET);
			service.saveEdge(body, update, CypherStatment.CDEPENDENCY);
			service.saveEdge(update, cmp, CypherStatment.CDEPENDENCY);
		} else if (node instanceof ForeachStmt) { // foreach语句
			ForeachStmt foreachStmt = (ForeachStmt) node;
			nn = new NeoNode(EnumNeoNodeLabelType.FOREACHSTMT.getValue(), EnumNeoNodeLabelType.FOREACHSTMT.getValue());
			nn = service.saveNode(nn);
			nodeNeoNodePair.put(node, nn);
			NeoNode vdNN = create(foreachStmt.getVariable());
			service.saveEdge(nn, vdNN, CypherStatment.PARNET);
			NeoNode iterable = create(foreachStmt.getIterable());
			service.saveEdge(nn, iterable, CypherStatment.PARNET);
			NeoNode body = create(foreachStmt.getBody());
			service.saveEdge(nn, body, CypherStatment.PARNET);
			service.saveEdge(vdNN, iterable, CypherStatment.IN);
			service.saveEdge(vdNN, body, CypherStatment.CDEPENDENCY);
			service.saveEdge(body, vdNN, CypherStatment.CDEPENDENCY);
		} else {
			nn = new NeoNode(EnumNeoNodeLabelType.ATOM.getValue(), node.toString());
			nn = service.saveNode(nn);
			nodeNeoNodePair.put(node, nn);
		}

		return nn;
	}

	/**
	 * 处理if/for等语句的语句块，blockstmt或者expression
	 * 
	 * @param NeoNode
	 *            nn
	 * @param Node
	 *            node
	 */
	private void pcBlockStmt(NeoNode nn, Node node) {
		if (node instanceof BlockStmt) { // 如果是带括号的，则把这些语句并列起来，父节点就是if
			List<Statement> stmts = ((BlockStmt) node).getStmts();
			for (Statement stmt : stmts) {
				NeoNode stmtNN = create(stmt);
				service.saveEdge(nn, stmtNN, CypherStatment.PARNET);
			}
		} else {
			NeoNode stmtNN = create(node);
			service.saveEdge(nn, stmtNN, CypherStatment.PARNET);
		}
	}

	/**
	 * 根据CompilationUnit得到类成员
	 */
	public List<BodyDeclaration> getBodyList() {
		if (cu.getTypes() == null)
			return null;
		List<BodyDeclaration> bodyList = null;
		bodyList = cu.getTypes().stream().filter((n)->(n instanceof ClassOrInterfaceDeclaration)).findFirst().get().getMembers();
		for (TypeDeclaration type : cu.getTypes()) {
			if (type instanceof ClassOrInterfaceDeclaration) {
				bodyList = type.getMembers();
				return bodyList;
			}
		}
		return null;
	}

	/**
	 * 根据方法名得到body
	 */
	public MethodDeclaration getMethodBodyByName(String methodName) {
		List<BodyDeclaration> bodyList = getBodyList();
		for (BodyDeclaration body : bodyList) {
			if (body instanceof MethodDeclaration && ((MethodDeclaration) body).getName().equals(methodName))
				return (MethodDeclaration) body;
		}
		return null;
	}

	private boolean varExisted(Node node, String var) {
		if (node == null || var == null)
			return false;
		if (node instanceof NameExpr) {
			NameExpr nameExpr = (NameExpr) node;
			return var.equals(nameExpr.getName());
		} else if (node instanceof MethodCallExpr) {
			MethodCallExpr methodCallExpr = (MethodCallExpr) node;
			List<Expression> list = methodCallExpr.getArgs();
			for (Expression exp : list) {
				if (varExisted(exp, var))
					return true;
			}
		} else if (node instanceof BinaryExpr) {
			BinaryExpr binaryExpr = (BinaryExpr) node;
			return varExisted(binaryExpr.getLeft(), var) || varExisted(binaryExpr.getRight(), var);
		} else if (node instanceof AssignExpr) {
			AssignExpr assignExpr = (AssignExpr) node;
			return varExisted(assignExpr.getValue(), var);
		} else if (node instanceof ExpressionStmt) {
			ExpressionStmt es = (ExpressionStmt) node;
			return varExisted(es.getExpression(), var);
		}
		// TODO 待完成
		return false;
	}

	public CompilationUnit getCu() {
		return cu;
	}

	public void setCu(CompilationUnit cu) {
		this.cu = cu;
	}

	public INeoNodeService getService() {
		return service;
	}

	public void setService(INeoNodeService service) {
		this.service = service;
	}

	public static void main(String[] args) {
		GenerateGraph gg = new GenerateGraph(FileUtil.openCU("/Users/xiyaoguo/Desktop/test.java"));
		gg.analyzeMethod("a");
//		GenerateGraph gg2 = new GenerateGraph(FileUtil.openCU("/Users/xiyaoguo/Desktop/PullToRefreshLinearLayout.java"));
		//gg2.analyzeMethod("onTouch");
//		GenerateGraph gg3 = new GenerateGraph(FileUtil.openCU("/Users/xiyaoguo/Desktop/CustomFrameLayout.java"));
		//gg3.analyzeMethod("onTouchEvent");
//		GenerateGraph gg4 = new GenerateGraph(FileUtil.openCU("/Users/xiyaoguo/Desktop/WaterDropListView.java"));
		//gg4.analyzeMethod("onTouchEvent");
//		GenerateGraph gg5 = new GenerateGraph(FileUtil.openCU("/Users/xiyaoguo/Desktop/QuizActivity.java"));
//		gg5.analyzeMethod("onCreate");
	}
}
