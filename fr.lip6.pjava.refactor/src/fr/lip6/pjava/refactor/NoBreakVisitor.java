package fr.lip6.pjava.refactor;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BreakStatement;


/**
 * Invoke this on the body of a foreach to test whether there is a break in there.
 * @author ythierry
 *
 */
public class NoBreakVisitor extends ASTVisitor {
	private boolean containsBreak = false;
	
	public boolean isContainsBreak() {
		return containsBreak;
	}
	
	@Override
	public boolean preVisit2(ASTNode node) {
		// optimize : if we have found a break, stop looking
		if (containsBreak)
			return false;
		// do not explore children statements that will own the break statements.
		switch (node.getNodeType()) {
		case ASTNode.ENHANCED_FOR_STATEMENT :
		case ASTNode.FOR_STATEMENT:
		case ASTNode.WHILE_STATEMENT:
		case ASTNode.DO_STATEMENT: 
		case ASTNode.SWITCH_CASE:
			return false; // interrupt visit, any break inside of instructions is not for the parent foreach
		default :
			return true;
		}
	}
	
	@Override
	public boolean visit(BreakStatement node) {
		containsBreak = true;
		return false;
	}
}
