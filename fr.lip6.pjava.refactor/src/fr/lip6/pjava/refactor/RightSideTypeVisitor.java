package fr.lip6.pjava.refactor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

public class RightSideTypeVisitor extends ASTVisitor {
	private String type;
	
	@Override
	public boolean visit(SimpleName node) {
		type = node.resolveTypeBinding().getName();
		return false;
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		type = node.resolveTypeBinding().getName();
		return false;
	}
	
	public String getType() {
		return type;
	}

}
