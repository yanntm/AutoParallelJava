package fr.lip6.pjava.refactor;

import java.util.HashMap;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class BodyParallelizable extends ASTVisitor {
	private HashMap<String, Set<String>> methode;
	private boolean parallelizable=true;
	private int problem = 0;

	public BodyParallelizable(HashMap<String, Set<String>> methode) {
		this.methode=methode;
	}

	
	
	public boolean isParallelizable() {
		return parallelizable;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		//System.out.println(node);
		IMethodBinding bind = node.resolveMethodBinding();
		if (bind==null) {
			problem++;
			System.out.println("Problemes : " + problem);
		}else {
			if ( methode.get("ReadOnly").contains(bind.getKey()) || methode.get("ThreadSafe").contains(bind.getKey()) 
					|| methode.get("ModifLocal").contains(bind.getKey())) {
//			if(!methode.get("NotParallelizable").contains(bind.getKey())
//				parallelizable=false;
			}else {
				parallelizable=false;
			}
		}

		return super.visit(node);
	}
	
	
		
}
