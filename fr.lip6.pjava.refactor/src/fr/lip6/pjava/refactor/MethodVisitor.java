package fr.lip6.pjava.refactor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SynchronizedStatement;

public class MethodVisitor extends ASTVisitor {
	private boolean readOnly = true;
	private boolean threadSafe = false;

	@Override
	public boolean visit(Assignment node) {
		readOnly=false;
		return false;
	}
	
	@Override
	public boolean visit(PostfixExpression node) {
		readOnly=false;
		return false;
	}
	
	@Override
	public boolean visit(PrefixExpression node) {
		readOnly=false;
		return false;
	}
	
	public boolean isReadOnly() {
		return readOnly;
	}
	
	@Override
	public boolean visit(SynchronizedStatement node) {
		threadSafe = true;
		return false;
	}

	public boolean isThreadSafe() {
		return threadSafe;
	}
	
	

}
