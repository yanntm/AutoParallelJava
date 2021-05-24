package fr.lip6.pjava.refactor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class TransformationMapVisitor extends ASTVisitor {
	
	private AST ast = null;
	
	private int cas = -1;
	
	private Expression left = null;
	
	private MethodInvocation map = null;
	
	private int nbInstruction = 0;
	
	private SingleVariableDeclaration parameter = null;
	
	private MethodInvocation terminale = null;
	
	private List<String> variableLocale = new ArrayList<>();
	
	
	public TransformationMapVisitor(SingleVariableDeclaration parameter) {
		this.parameter = parameter;
		ast = parameter.getAST();
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

	public Expression getLeft() {
		return left;
	}
	
	public MethodInvocation getMap() {
		return map;
	}
	
	public int getNbInstruction() {
		return nbInstruction;
	}


	public MethodInvocation getTerminale() {
		return terminale;
	}


	private void initMap(String type) {
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
	}
	
	@Override
	public String toString() {
		return "TransformationMap [map=" + map + ", terminale=" + terminale + ", parameter=" + parameter + ", ast="
				+ ast + ", left=" + left + ", variableLocale=" + variableLocale + ", cas=" + cas + "]";
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(Assignment node) {
		// TODO verifier qu'il n'y est qu'une seul operation dans le block et seul block
		nbInstruction++;
		if(node.getLeftHandSide().getNodeType()==ASTNode.SIMPLE_NAME && !variableLocale.contains(((SimpleName)node.getLeftHandSide()).resolveBinding().getKey())) {

			String type = node.getRightHandSide().resolveTypeBinding().getQualifiedName();
			initMap(type);
			if(map==null) return false;
			
			if(node.getOperator().equals(Assignment.Operator.PLUS_ASSIGN)) {
				
				left = node.getLeftHandSide();
				LambdaExpression lb = ast.newLambdaExpression();
				lb.setParentheses(false);
				lb.setBody(ASTNode.copySubtree(ast, node.getRightHandSide()));
				lb.parameters().add(ASTNode.copySubtree(ast, parameter));
				
				map.arguments().add(lb);
				
				terminale = ast.newMethodInvocation();
				terminale.setName(ast.newSimpleName("sum"));
				terminale.setExpression(map);
				
				cas=1;
			}	
		}
		return false; 
	}
	
	@Override
	public boolean visit(EnhancedForStatement node) {
		nbInstruction++;
		return super.visit(node);
	}
	
	
	@Override
	public boolean visit(ForStatement node) {
		nbInstruction++;
		return super.visit(node);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(MethodInvocation node) {
		nbInstruction++;
		if(node.getName().getIdentifier().equals("add") && node.getExpression()!=null && node.arguments().size()==1){
			ITypeBinding[] t = node.getExpression().resolveTypeBinding().getInterfaces();
			if(contains(t, "java.util.Collection")) {
				left = node.getExpression(); 
				
				LambdaExpression lb = ast.newLambdaExpression();
				lb.setParentheses(false);
				lb.setBody(ASTNode.copySubtree(ast, (ASTNode) node.arguments().get(0)));
				lb.parameters().add(ASTNode.copySubtree(ast,parameter));
				
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

	@Override
	public boolean visit(PostfixExpression node) {
		// TODO verifier si seul operation ne marche pas a refaire
		nbInstruction++;
		if (!variableLocale.contains(((SimpleName)node.getOperand()).resolveBinding().getKey())
			&& node.getOperator().equals(PostfixExpression.Operator.INCREMENT)) {
			String type = node.getOperand().resolveTypeBinding().getQualifiedName();
			initMap(type);
			map = ast.newMethodInvocation();
			map.setName(ast.newSimpleName("map"));
			
			left = node.getOperand();
			
			terminale = ast.newMethodInvocation();
			terminale.setName(ast.newSimpleName("count"));
			terminale.setExpression(map);
			
			cas=1;
		}
		return false;
	}


	@Override
	public boolean visit(PrefixExpression node) {
		nbInstruction++;
		return false;
	}
	
	@Override
	public boolean visit(VariableDeclarationFragment node) {
		variableLocale.add(node.resolveBinding().getKey());
		return true;
	}
	
}
