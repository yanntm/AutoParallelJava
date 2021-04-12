package fr.lip6.pjava.refactor;

import java.util.HashMap;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class BodyParallelizable extends ASTVisitor {
	static int problem = 0;
	
	private HashMap<String, Set<String>> methode;

	public BodyParallelizable(HashMap<String, Set<String>> methode) {
		this.methode=methode;
	}

	private boolean parallelizable;
	
	@Override
	public boolean visit(MethodInvocation node) {
		//System.out.println(node);
		IMethodBinding bind = node.resolveMethodBinding();
		if (bind==null) {
			problem++;
			System.out.println("Problemes : " + problem);
		}else {
			if (methode.get("NotParallelizable").contains(bind.getKey())) {
				parallelizable=false;
			}
		}

		return super.visit(node);
	}

	public boolean isParallelizable() {
		return parallelizable;
	}
		
}
