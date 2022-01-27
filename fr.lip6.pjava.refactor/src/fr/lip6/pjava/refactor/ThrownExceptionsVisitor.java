package fr.lip6.pjava.refactor;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;


/**
 * Compute for a given piece of code which exceptions are potentially thrown
 * @author ythierry
 *
 */
public class ThrownExceptionsVisitor extends ASTVisitor {

	/** these are exceptions raised but not caught, it is the result of the traversal */
	private List<ITypeBinding> raisedExceptions = new ArrayList<>(); 
	
	/** this stack is used to deal with try/catch blocks : when entering a try block, 
	 * we push on the top of stack the caught exceptions (i.e. all those in the catch clause;) when exiting the try block 
	 * the top of the stack is popped.
	 */
	private Stack<List<ITypeBinding>> caughtExceptions = new Stack<>(); 
	
	public List<ITypeBinding> getRaisedExceptions() {
		return raisedExceptions;
	}
	
	@Override
	public boolean visit(ThrowStatement node) {
		// grab the type of the exception we are throwing
		ITypeBinding exType = node.getExpression().resolveTypeBinding();
		// test if it is caught already
		if (! isCaught(exType)) {
			raisedExceptions.add(exType);
		}
		// we still might need to investigate method calls in the throw clause
		return true;
	}

	/** navigate caught exceptions and look exType or a super type thereof.
	 * @return true if this exType is actually caught
	 * */ 
	private boolean isCaught(ITypeBinding exType) {
		for (List<ITypeBinding> list : caughtExceptions) {
			for (ITypeBinding caught : list) {
				if (exType.isSubTypeCompatible(caught)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean preVisit2(ASTNode node) {
		//are we entering a "try" clause ?
		if (node.getNodeType()==ASTNode.TRY_STATEMENT) {
			TryStatement trys = (TryStatement) node;
			/*
			    try [ ( Resources ) ]
         			Block
         			[ { CatchClause } ]
         			[ finally Block ]
			 */
			List<ITypeBinding> caughtHere = new ArrayList<>();
			for (Object o: trys.catchClauses()) {
				if (o instanceof CatchClause) {
					CatchClause catcher = (CatchClause) o;
					SingleVariableDeclaration ex = catcher.getException();
					/**
					 * TODO : 
					 * Not sure this deals with multi catch :
					 * try { ... } catch (E1|E2 e) { ... }
					 */
					ITypeBinding exType = ex.resolveBinding().getType();
					caughtHere.add(exType);
				}				
			}
			// now push on stack 
			caughtExceptions.push(caughtHere);
						
			for (Object r : trys.resources()) {
				ASTNode res = (ASTNode) r;
				res.accept(this);				
			}
			
			trys.getBody().accept(this);
			
			// end of caucght exceptions zone
			caughtExceptions.pop();
			
			for (Object r : trys.catchClauses()) {
				ASTNode res = (ASTNode) r;
				res.accept(this);
			}
			trys.getFinally().accept(this);
		}
		// we have visited manually
		return false;
	}
	
	@Override
	public boolean visit(MethodInvocation invoke) {
		IMethodBinding method = invoke.resolveMethodBinding();
		for (ITypeBinding exType :  method.getExceptionTypes()) {
			// test if it is caught already
			if (! isCaught(exType)) {
				raisedExceptions.add(exType);
			}
		}
		return true;
	}
	
}
