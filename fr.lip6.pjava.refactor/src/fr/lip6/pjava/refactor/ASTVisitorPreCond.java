package fr.lip6.pjava.refactor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;

/**
 * Class use to check the enhancedFor if this is possible to transform them
 * @author Teillet & Capitanio
 *
 */
public class ASTVisitorPreCond extends ASTVisitor {
	/**
	 * Use to verify if the enhancedFor can be upgradable
	 */
	private boolean isUpgradable;
	/**
	 * We keep the caller to be sure to not verify things outside of the EnhancedFor
	 */
	private ASTNode caller;
	
	/**
	 * The constructor use to initiate the attributes
	 * @param caller this the node calling this object
	 */
	public ASTVisitorPreCond(ASTNode caller) {
		isUpgradable = true;
		this.caller = caller;
	}

	/**
	 * Return if the given EnhancedFor is Upgradable
	 * @return if the given EnhancedFor is Upgradable
	 */
	public boolean isUpgradable() {
		return isUpgradable;
	}
	
	//Traitement des sortie anticipees
	@Override
	public  boolean visit(ReturnStatement node) {
		isUpgradable = false;
		return false;
	}
	
	@Override
	public  boolean visit(BreakStatement node) {
		ASTNode parent = node.getParent();
		
		while( parent.getNodeType() != ASTNode.ENHANCED_FOR_STATEMENT
				&& parent.getNodeType() != ASTNode.FOR_STATEMENT
				&& parent.getNodeType() != ASTNode.WHILE_STATEMENT
				&& parent.getNodeType() != ASTNode.DO_STATEMENT 
				&& parent.getNodeType() != ASTNode.SWITCH_CASE ) {
			parent = parent.getParent();
		}
		if(parent.equals(caller)) {
			isUpgradable = false;
		}
		return false;
	}
	
	@Override
	public boolean visit(ContinueStatement node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	//Traitement des exceptions qui peuvent sortir de la boucle for
	
	@Override
	public  boolean visit(ThrowStatement node) {
		ASTNode parent = node.getParent();
		String exceptionThrowed = ((SimpleType)((ClassInstanceCreation) node.getExpression()).getType())
				.resolveBinding().getQualifiedName();
		ArrayList<String> exceptions = new ArrayList<String>();
		exceptions.add(exceptionThrowed);
		if(!verifException(exceptions, parent)) {
			isUpgradable = false;
		}
		return false;
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		ASTNode parent = node.getParent();
		ArrayList<String> exceptions = new ArrayList<String>();
		for(ITypeBinding b : node.resolveMethodBinding().getExceptionTypes()) {
			exceptions.add(b.getQualifiedName());
		}
		System.out.println("taille exception " + exceptions.size());
		if(!verifException(exceptions, parent)) {
			isUpgradable = false;
		}
		return false;
	}
	
	@Override
	public boolean visit(ClassInstanceCreation node) {
		ASTNode parent = node.getParent();
		ArrayList<String> exceptions = new ArrayList<String>();
		for(ITypeBinding b : node.resolveConstructorBinding().getExceptionTypes()) {
			exceptions.add(b.getQualifiedName());
		}
		if(!verifException(exceptions, parent)) {
			isUpgradable = false;
		}
		return false;
	}
	

	/**
	 * Check if the given Exceptions are catched inside the enhancedFor
	 * @param exceptions The exceptions we are looking for
	 * @param parent the parent node of the node we are looking at
	 * @return if all the exceptions are handles inside the enhancedFor 
	 */
	private boolean verifException(List<String> exceptions, ASTNode parent) {
		if (exceptions.size()==0) return true;
		while(parent != caller) {
			if (parent.getNodeType() == ASTNode.TRY_STATEMENT) {
				TryStatement tryStat = (TryStatement) parent;
				for(Object o : tryStat.catchClauses()) {
					if(o instanceof CatchClause) {
						String exceptionCatched = ((CatchClause) o).getException().resolveBinding()
								.getType().getQualifiedName();
						if(exceptionCatched.equals("java.lang.Exception")) {
							return true;
						}
						if(exceptions.remove(exceptionCatched)) {
							if(exceptions.size() == 0) {
								return true;
							}
						}
					}
				}
			}
			parent = parent.getParent();
		}
		return false;
	}
	

}
