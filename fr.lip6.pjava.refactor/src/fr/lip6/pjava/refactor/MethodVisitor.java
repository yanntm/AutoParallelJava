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
	/**
	 * La liste des cycles présentant dans le graphe des dépendances
	 */
	private List<MethodDeclaration> listCycle;
	/**
	 * Ensemebles des variables locales
	 */
	private Set<String> localVariable = new HashSet<String>();
	/**
	 * Map des catégorie de méthodes
	 */
	private Map<String, Set<String>> methodTag;
	/**
	 * Si la méthode est dans la catégorie modifLocal
	 */
	private boolean modifLocal = true;
	/**
	 * S'il y a un probleme qui empeche la parallélisation
	 */
	private boolean notParallelisable = false;
	/**
	 * Si la méthode ne fait aucune modification d'attribut
	 */
	private boolean readOnly = true;
	/**
	 * Si des mécanismes de synchronisation sont présent
	 */
	private boolean threadSafe = false;

	/**
	 * Constructeur permettant d'initaliser la catégorie de la liste
	 * ainsi que si la méthode fait partie d'un cycle le cycle dans lequelle elle est présente
	 * @param map catégorie des méthodes
	 * @param listCycle liste du cycle du quelle elle fait partie
	 */
	public MethodVisitor(HashMap<String, Set<String>> map, List<MethodDeclaration> listCycle) {
		this(map);
		this.listCycle=listCycle;
	}

	/**
	 * Constructeur permettant d'initialiser le tag des méthodes
	 * @param map tag des méthodes
	 */
	public MethodVisitor(Map<String, Set<String>> map) {
		this.methodTag=map;
	}

	/**
	 * Retourne la valeur de modifLocal
	 * @return la valeur de modifLocal
	 */
	public boolean isModifLocal() {
		return modifLocal;
	}

	/**
	 * Retourne la valeur de notParallelizable
	 * @return
	 */
	public boolean isNotParallelisable() {
		return notParallelisable;
	}

	/**
	 * Retourne la valeur de readOnly
	 * @return readOnly
	 */
	public boolean isReadOnly() {
		return readOnly;
	}


	/**
	 * Permet de vérifier si un noeud fait partie du même cycle que l'objet courant
	 * @param node le noeud à vérifier
	 * @return si les deux noeuds font partie du même cycle
	 */
	private boolean isSameCycle(MethodInvocation node) {
		//Si listCycle == null la méthode courante n'est pas dans un cycle
		if (listCycle!=null) {
			for (MethodDeclaration methodDeclaration : listCycle) {
				if (methodDeclaration.resolveBinding().getKey().equals(node.resolveMethodBinding().getKey()))return true;
			}
		}
		return false;
	}

	/**
	 * Retourne si la méthode est threadSafe ou non
	 * @return si la méthode est threadSafe
	 */
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
			if(methodTag.get("NotParallelizable").contains(key)) {
				modifLocal = false;
				readOnly = false;
			} else if(!(methodTag.get("ThreadSafe").contains(key) && methodTag.get("ModifLocal").contains(key) && methodTag.get("ReadOnly").contains(key))) {
				notParallelisable = true;
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
