package fr.lip6.pjava.refactor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * Classe implémentant l'interface visiteur et visitant des Method déclarations
 * pour pouvoir classer les méthodes en catégorie
 * @author Teillet & Capitanio
 *
 */
public class MethodVisitor extends ASTVisitor {
	private List<MethodDeclaration> listCycle;
	private Set<String> localVariable = new HashSet<String>();
	private Map<String, Set<String>> map;
	private boolean modifLocal = true;
	private boolean problem = false;
	private boolean readOnly = true;
	private boolean threadSafe = false;

	public MethodVisitor(HashMap<String, Set<String>> map, List<MethodDeclaration> list) {
		this(map);
		listCycle=list;
	}

	public MethodVisitor(Map<String, Set<String>> map) {
		this.map=map;
	}

	public boolean isModifLocal() {
		return modifLocal;
	}

	public boolean isProblem() {
		return problem;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	private boolean isSameCycle(MethodInvocation node) {
		if (listCycle!=null) {
			for (MethodDeclaration methodDeclaration : listCycle) {
				if (methodDeclaration.resolveBinding().getKey().equals(node.resolveMethodBinding().getKey()))return true;
			}
		}
		
		return false;
	}

	public boolean isThreadSafe() {
		return threadSafe;
	}

	@Override
	public boolean visit(Assignment node) {	
		node.getLeftHandSide().accept(new ASTVisitor() {
			@Override
			public boolean visit(SimpleName node) {
				IBinding bind = node.resolveBinding();
				if (bind instanceof IVariableBinding) {
					IVariableBinding varBind = (IVariableBinding) bind;
					if (!localVariable.contains(bind.getKey()) && varBind.isParameter() && varBind.isField()) {
						modifLocal = false;
					}
				}
				return false;
			}
		});

		readOnly=false;
		return false;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if (!isSameCycle(node)) {
			if (node.getExpression() != null) {
				ITypeBinding expressionType = node.getExpression().resolveTypeBinding();
				if (expressionType.getQualifiedName().equals("java.util.concurrent.locks.Lock")) threadSafe=true;
			}
			String key = node.resolveMethodBinding().getKey();
			//System.out.println(key);
			if(map.get("NotParallelizable").contains(key)) {
				modifLocal = false;
				readOnly = false;
				// TODO : threadSafe = false ?
			} else if(!(map.get("ThreadSafe").contains(key) && map.get("ModifLocal").contains(key) && map.get("ReadOnly").contains(key))) {
				problem = true;
			}
		}

		return false;
	}

	@Override
	public boolean visit(PostfixExpression node) {
		readOnly=false;
		if (node.getNodeType()==ASTNode.QUALIFIED_NAME || node.getNodeType()==ASTNode.SIMPLE_NAME ) {
			((Name) node.getOperand()).getFullyQualifiedName();
		}
		node.getOperand().accept(new ASTVisitor() {
			@Override
			public boolean visit(SimpleName node) {
				IBinding bind = node.resolveBinding();
				if (bind instanceof IVariableBinding) {
					IVariableBinding varBind = (IVariableBinding) bind;
					if (!localVariable.contains(bind.getKey()) && varBind.isParameter() && varBind.isField()) {
						modifLocal = false;
					}
				}
				return false;
			}
		});
		return false;
	}

	@Override
	public boolean visit(PrefixExpression node) {
		readOnly=false;
		node.getOperand().accept(new ASTVisitor() {
			@Override
			public boolean visit(SimpleName node) {
				IBinding bind = node.resolveBinding();
				if (bind instanceof IVariableBinding) {
					IVariableBinding varBind = (IVariableBinding) bind;
					if (!localVariable.contains(bind.getKey()) && varBind.isParameter() && varBind.isField()) {
						modifLocal = false;
					}
				}
				return false;
			}
		});
		return false;
	}

	@Override
	public boolean visit(SynchronizedStatement node) {
		threadSafe = true;
		return false;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		localVariable.add(node.resolveBinding().getKey());
		return super.visit(node);
	}

}
