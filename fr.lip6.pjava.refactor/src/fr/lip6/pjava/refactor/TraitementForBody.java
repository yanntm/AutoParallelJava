package fr.lip6.pjava.refactor;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Class used to verify the inside of the forEach and to create the filter
 * @author Teillet & Capitanio
 *
 */
public class TraitementForBody extends ASTVisitor {
	/**
	 * The parent that called the transformation
	 */
	private ASTNode parent;
	/**
	 * The AST in which the transformation is done
	 */
	private AST ast;
	/**
	 * Use if their is multiple filter that chain
	 */
	private MethodInvocation last=null;
	/**
	 * Use for the first filter
	 */
	private MethodInvocation first=null;
	/**
	 * the body of the last if
	 */
	private ASTNode body = null;
	
	private boolean filterPossible = true;
	/**
	 * The constructor used to initialize all the attributes
	 * @param parent the parent who call the operation
	 * @param ast the in which we do the operation
	 */
	public TraitementForBody(ASTNode parent, AST ast) {
		this.parent = parent;
		this.ast = ast;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(IfStatement node) {
		if(filterPossible && node.getElseStatement()==null && ((Block)node.getParent()).statements().size()==1) {
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
		}else {
			filterPossible=false;
		}
		return true;
	}
	
	/**
	 * Return the last filter
	 * @return the last filter, none if there are only a filter
	 */
	public MethodInvocation getLast() {
		return last;
	}
	
	/**
	 * The first filter
	 * @return the first filter, none if there are no filter
	 */
	public MethodInvocation getFirst() {
		return first;
	}
	
	/**
	 * Return the body of the last if
	 * @return the body of the last if
	 */
	public ASTNode getBody() {
		return body;
	}

	@Override
	public String toString() {
		return "TraitementForBody [parent=" + parent + ", ast=" + ast + ", last=" + last + ", first=" + first
				+ ", body=" + body + ", filterPossible=" + filterPossible + "]";
	}
	
	

}
