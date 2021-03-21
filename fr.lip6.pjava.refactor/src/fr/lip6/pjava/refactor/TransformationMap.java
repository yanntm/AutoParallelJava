package fr.lip6.pjava.refactor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

public class TransformationMap extends ASTVisitor {
	
	private MethodInvocation map = null;
	
	private MethodInvocation terminale = null;
	
	private EnhancedForStatement parent = null;
	
	private AST ast = null;
	
	private SimpleName left = null;
	
	private List<String> variableLocale = new ArrayList<>();
	
	private int cas = -1;
	
	
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
		if(node.getLeftHandSide().getNodeType()==ASTNode.SIMPLE_NAME && !variableLocale.contains(((SimpleName)node.getLeftHandSide()).resolveBinding().getKey())) {

			String type = node.getRightHandSide().resolveTypeBinding().getQualifiedName();
			
			switch (type) {
			case "int":
			case "java.lang.Integer":
				map = ast.newMethodInvocation();
				map.setName(ast.newSimpleName("mapToInt"));
				break;
			case "double":
			case "java.lang.Double":
				map = ast.newMethodInvocation();
				map.setName(ast.newSimpleName("mapToDouble"));
				break;
			case"long":
			case "java.lang.Long":
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
				
				cas=1;
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
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(MethodInvocation node) {
		if(node.getName().getIdentifier().equals("add") && node.getExpression()!=null && node.arguments().size()==1){
			ITypeBinding[] t = node.getExpression().resolveTypeBinding().getInterfaces();
			if(contains(t, "java.util.Collection")) {
				left = (SimpleName) node.getExpression();
				
				LambdaExpression lb = ast.newLambdaExpression();
				lb.setBody(ASTNode.copySubtree(ast, (ASTNode) node.arguments().get(0)));
				lb.parameters().add(ASTNode.copySubtree(ast, parent.getParameter()));
				
				map = ast.newMethodInvocation();
				map.setName(ast.newSimpleName("map"));
				map.arguments().add(lb);
				
				MethodInvocation toList = ast.newMethodInvocation();
				toList.setName(ast.newSimpleName("toList"));
				toList.setExpression(ast.newSimpleName("Collectors"));
				
				terminale = ast.newMethodInvocation();
				terminale.setName(ast.newSimpleName("collect"));
				terminale.arguments().add(toList);
				terminale.setExpression(map);
				
				cas=2;
			}
		}
		
		
		return false;
	}

	private boolean contains(ITypeBinding[] t, String string) {
		for(ITypeBinding type:t) {
			if(type.getQualifiedName().contains(string)) {
				return true;
			}
		}
		return false;
		
	}

	public int getCas() {
		return cas;
	}
	
	
	
}
