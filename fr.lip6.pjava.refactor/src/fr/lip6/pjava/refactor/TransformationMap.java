package fr.lip6.pjava.refactor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class TransformationMap extends ASTVisitor {
	
	private MethodInvocation map = null;
	
	private MethodInvocation terminale = null;
	
	private EnhancedForStatement parent = null;
	
	private AST ast = null;
	
	private SimpleName left = null;
	
	private List<String> variableLocale = new ArrayList<>();
	
	public TransformationMap(EnhancedForStatement parent) {
		this.parent = parent;
		ast = parent.getAST();
	}
	
	@Override
	public boolean visit(VariableDeclarationFragment node) {
		variableLocale.add(node.resolveBinding().getKey());
		return true;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(Assignment node) {
		// TODO Auto-generated method stub
		System.out.println("ICI");
		if(node.getLeftHandSide().getNodeType()==ASTNode.SIMPLE_NAME && !variableLocale.contains(((SimpleName)node.getLeftHandSide()).resolveBinding().getKey())) {
			
			RightSideTypeVisitor rSTV = new RightSideTypeVisitor();
			node.getRightHandSide().accept(rSTV);
			
			switch (rSTV.getType().toLowerCase()) {
			case "int":
				map = ast.newMethodInvocation();
				map.setName(ast.newSimpleName("mapToInt"));
				break;
			case "integer":
				map = ast.newMethodInvocation();
				map.setName(ast.newSimpleName("mapToInt"));
				break;
			case "double":
				map = ast.newMethodInvocation();
				map.setName(ast.newSimpleName("mapToDouble"));
				break;
			case"long":
				map = ast.newMethodInvocation();
				map.setName(ast.newSimpleName("mapToLong"));
			default:
				break;
			}
			if(map==null)return false;
			
			if(node.getOperator().equals(Assignment.Operator.PLUS_ASSIGN)) {
				left = (SimpleName) node.getLeftHandSide();
				LambdaExpression lb = ast.newLambdaExpression();
				lb.setBody(ASTNode.copySubtree(ast, node.getRightHandSide()));
				lb.parameters().add(ASTNode.copySubtree(ast, parent.getParameter()));
				
				map.arguments().add(lb);
				
				terminale = ast.newMethodInvocation();
				terminale.setName(ast.newSimpleName("sum"));
				terminale.setExpression(map);
			}
			
		}
		return false; 
	}
	
	public MethodInvocation getMap() {
		return map;
	}
	
	public MethodInvocation getTerminale() {
		return terminale;
	}
	
	public SimpleName getLeft() {
		return left;
	}
	
}
