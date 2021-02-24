package fr.lip6.pjava.refactor;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class TraitementForBody extends ASTVisitor {
	private ASTNode parent;
	private AST ast;
	private MethodInvocation last=null;
	private MethodInvocation first=null;
	private ASTNode body = null;
	
	public TraitementForBody(ASTNode parent, AST ast) {
		this.parent = parent;
		this.ast = ast;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(IfStatement node) {
		if(node.getElseStatement()==null && ((Block)node.getParent()).statements().size()==1) {
			if(first==null) {
				first = ast.newMethodInvocation();
				first.setName(ast.newSimpleName("filter"));
				
				LambdaExpression lb = ast.newLambdaExpression();
				lb.setBody(ASTNode.copySubtree(ast, node.getExpression()));
				lb.parameters().add(ASTNode.copySubtree(ast, ((EnhancedForStatement) parent).getParameter()));
				first.arguments().add(lb);
				
			}else {
				if(last==null) {
					last = ast.newMethodInvocation();
					last.setName(ast.newSimpleName("filter"));
					last.setExpression(first);
					
					LambdaExpression lb = ast.newLambdaExpression();
					lb.setBody(ASTNode.copySubtree(ast, node.getExpression()));
					lb.parameters().add(ASTNode.copySubtree(ast, ((EnhancedForStatement) parent).getParameter()));
					last.arguments().add(lb);
				}else {
					MethodInvocation temp = last;
					last = ast.newMethodInvocation();
					last.setName(ast.newSimpleName("filter"));
					last.setExpression(temp);
					
					LambdaExpression lb = ast.newLambdaExpression();
					lb.setBody(ASTNode.copySubtree(ast, node.getExpression()));
					lb.parameters().add(ASTNode.copySubtree(ast, ((EnhancedForStatement) parent).getParameter()));
					last.arguments().add(lb);
				}

			}
			body = node.getThenStatement();
		}
		return true;
	}
	
	public MethodInvocation getLast() {
		return last;
	}
	
	public MethodInvocation getFirst() {
		return first;
	}
	
	public ASTNode getBody() {
		return body;
	}
	
	

}
