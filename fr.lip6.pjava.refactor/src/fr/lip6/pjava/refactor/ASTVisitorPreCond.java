package fr.lip6.pjava.refactor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

/**
 * Class use to check the enhancedFor if this is possible to transform them
 * @author Teillet & Capitanio
 *
 */
public class ASTVisitorPreCond extends ASTVisitor {
	/**
	 * Use to verify if the enhancedFor can be upgradable
	 */
	private boolean isUpgradable, notInterrupted;
	/**
	 * We keep the caller to be sure to not verify things outside of the EnhancedFor
	 */
	private final EnhancedForStatement caller;
	/**
	 * List of variables keys encountered in the EnhancedFor
	 */
	private List<String> varDeclaredInFor;
	
	private List<String> varUsedInsideDeclaredOutside;
	
	/**
	 * The constructor use to initiate the attributes
	 * @param caller this the node calling this object
	 */
	public ASTVisitorPreCond(EnhancedForStatement caller) {
		
		this.caller = caller;
		varDeclaredInFor = new ArrayList<String>();
		varUsedInsideDeclaredOutside = new ArrayList<String>();
		isUpgradable = isCollection(caller.getExpression());
		notInterrupted = true;
	}
	
	/**
	 * Return if the given EnhancedFor is Upgradable
	 * @return if the given EnhancedFor is Upgradable
	 */
	public boolean isUpgradable() {
		// TODO ajouter cond 
		return notInterrupted && (isUpgradable || varUsedInsideDeclaredOutside.size()==1);
	}
	

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		varDeclaredInFor.add(node.resolveBinding().getKey());
		return true;
	}

	@Override
	public boolean visit(Assignment node) {
		//no problems with the for parameter variables 
		String paramterKey;
		if(caller instanceof EnhancedForStatement) {
			paramterKey =((EnhancedForStatement) caller).getParameter().resolveBinding().getKey();
		}else {
			return false;
		}
		Expression left = node.getLeftHandSide();
		String varKey = null;
		//verification du statut final
		if(node.getRightHandSide().getNodeType()==ASTNode.QUALIFIED_NAME
			&& ((QualifiedName) node.getRightHandSide()).getQualifier().resolveBinding().getKind()==ITypeBinding.VARIABLE 
			&& ( !( (IVariableBinding )((QualifiedName) node.getRightHandSide()).getQualifier().resolveBinding()).isEffectivelyFinal() &&
			!Modifier.isFinal(( (IVariableBinding )((QualifiedName) node.getRightHandSide()).getQualifier().resolveBinding()).getModifiers()) ) 
			||
			node.getRightHandSide().getNodeType()==ASTNode.SIMPLE_NAME && ((SimpleName) node.getRightHandSide()).resolveBinding().getKind()==ITypeBinding.VARIABLE 
			&& ( !( (IVariableBinding )((SimpleName) node.getRightHandSide()).resolveBinding()).isEffectivelyFinal() &&
			!Modifier.isFinal(( (IVariableBinding )((SimpleName) node.getRightHandSide()).resolveBinding()).getModifiers())
			
			
		) ){
			isUpgradable = false;
			return false;
		}
		
		if(left instanceof QualifiedName) {
			varKey = ((QualifiedName) left).getQualifier().resolveBinding().getKey();
			if(paramterKey == varKey) {
				return true;
			}
		} else if(left instanceof SimpleName){
			varKey = ((SimpleName) left).resolveBinding().getKey();
		}
		
	
		
		if(!varDeclaredInFor.contains(varKey)) {
			isUpgradable = false;
			varUsedInsideDeclaredOutside.add(varKey);
			return false;
		}
		return true;
	}
	
	@Override
	public boolean visit(PrefixExpression node) {
		//no problems with the for parameter variables 
		String paramterKey;
		if(caller instanceof EnhancedForStatement) {
			paramterKey =((EnhancedForStatement) caller).getParameter().resolveBinding().getKey();
		}else {
			return false;
		}
		Expression left = node.getOperand();
		String varKey = null;
		if(left instanceof QualifiedName) {
			varKey = ((QualifiedName) left).getQualifier().resolveBinding().getKey();
			if(paramterKey == varKey) {
				return true;
			}
		} else if(left instanceof SimpleName){
			varKey = ((SimpleName) left).resolveBinding().getKey();
		}
		
		if(!varDeclaredInFor.contains(varKey)) {
			isUpgradable = false;
			return false;
		}
		return true;
	}
	
	@Override
	public boolean visit(PostfixExpression node) {
		//no problems with the for parameter variables 
		String paramterKey;
		if(caller instanceof EnhancedForStatement) {
			paramterKey =((EnhancedForStatement) caller).getParameter().resolveBinding().getKey();
		}else {
			return false;
		}
		Expression left = node.getOperand();
		String varKey = null;
		if(left instanceof QualifiedName) {
			varKey = ((QualifiedName) left).getQualifier().resolveBinding().getKey();
			if(paramterKey == varKey) {
				return true;
			}
		} else if(left instanceof SimpleName){
			varKey = ((SimpleName) left).resolveBinding().getKey();
		}
		
		if(!varDeclaredInFor.contains(varKey)) {
			varUsedInsideDeclaredOutside.add(varKey);
			return false;
		}
		return true;
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
		// find out which loop or switch has been broken
		while( parent.getNodeType() != ASTNode.ENHANCED_FOR_STATEMENT
				&& parent.getNodeType() != ASTNode.FOR_STATEMENT
				&& parent.getNodeType() != ASTNode.WHILE_STATEMENT
				&& parent.getNodeType() != ASTNode.DO_STATEMENT 
				&& parent.getNodeType() != ASTNode.SWITCH_CASE ) {
			parent = parent.getParent();
		}
		
		if(parent.equals(caller)) {
			notInterrupted = false;
			return false;
		}
		return true;
	}
	
	//Identique à celui de Break sauf sans le SWITCH dans la condition
	@Override
	public boolean visit(ContinueStatement node) {
		ASTNode parent = node.getParent();
		// find out which loop has been continued
		System.out.println("continue found");
		while( parent.getNodeType() != ASTNode.ENHANCED_FOR_STATEMENT
				&& parent.getNodeType() != ASTNode.FOR_STATEMENT
				&& parent.getNodeType() != ASTNode.WHILE_STATEMENT
				&& parent.getNodeType() != ASTNode.DO_STATEMENT ) {
			parent = parent.getParent();
		}
		
		System.out.println("equals="+(parent.equals(caller)));
		System.out.println("parent=\n" + parent +"\ncaller=\n"+ caller);
		
		if(parent.equals(caller)) {
			System.out.println("upgradble is false");
			notInterrupted = false;
			return false;
		}
		return true;
	}

	//Traitement des exceptions qui peuvent sortir de la boucle for
	
	@Override
	public  boolean visit(ThrowStatement node) {
		ASTNode parent = node.getParent();
		String exceptionThrowed = ((SimpleType)((ClassInstanceCreation) node.getExpression()).getType())
				.resolveBinding().getQualifiedName();
		ArrayList<String> exceptions = new ArrayList<String>();
		exceptions.add(exceptionThrowed);
		if(!checkExceptionCatch(exceptions, parent)) {
			notInterrupted = false;
			return false;
		}
		return true;
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		ASTNode parent = node.getParent();
		ArrayList<String> exceptions = new ArrayList<String>();
		for(ITypeBinding b : node.resolveMethodBinding().getExceptionTypes()) {
			exceptions.add(b.getQualifiedName());
		}
		if(!checkExceptionCatch(exceptions, parent)) {
			isUpgradable = false;
			return false;
		}
		return true;

	}
	
	@Override
	public boolean visit(ClassInstanceCreation node) {
		ASTNode parent = node.getParent();
		ArrayList<String> exceptions = new ArrayList<String>();
		for(ITypeBinding b : node.resolveConstructorBinding().getExceptionTypes()) {
			exceptions.add(b.getQualifiedName());
		}
		if(!checkExceptionCatch(exceptions, parent)) {
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
	protected boolean checkExceptionCatch(List<String> exceptions, ASTNode parent) {
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
	
	private boolean isCollection(Expression expression) {
		ITypeBinding t = expression.resolveTypeBinding();
		if (t == null) {
			return false;
		}
		if(!t.isArray() && !containsCollection(t)) {
			return false;
		}else {
			return true;
		}
	}
	
	private boolean containsCollection(ITypeBinding t) {
		for (ITypeBinding i :t.getInterfaces()){
			if(i.getBinaryName().contains("java.util.Collection")) {
				return true;
			}
		}
		return false;
	}

}
