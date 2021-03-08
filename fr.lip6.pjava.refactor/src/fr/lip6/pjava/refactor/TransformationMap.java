package fr.lip6.pjava.refactor;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class TransformationMap extends ASTVisitor {
	
	private MethodInvocation map = null;
	
	private ASTNode parent = null;
	
	private AST ast = null;
	
	public TransformationMap(ASTNode parent) {
		this.parent = parent;
		ast = parent.getAST();
	}
	
	@Override
	public boolean visit(Assignment node) {
		// TODO Auto-generated method stub
//		map = 
		return super.visit(node); 
	}
	
	
}
