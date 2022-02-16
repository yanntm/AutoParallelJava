package fr.lip6.pjava.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.fix.CompilationUnitRewriteOperationsFix.CompilationUnitRewriteOperation;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalModel;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;

public class StreamToParallel extends CompilationUnitRewriteOperation {

	private MethodInvocation node;

	public StreamToParallel(MethodInvocation node) {
		this.node = node ;
	}

	@Override
	public void rewriteAST(CompilationUnitRewrite cuRewrite, LinkedProposalModel linkedModel) throws CoreException {
		AST ast = cuRewrite.getRoot().getAST();   //We obtain the AST from the CompilationUnit. The will be use as a factory
		final ASTRewrite rewrite = cuRewrite.getASTRewrite();  //We create a new ASTRewrite, that will contain all our modification
		MethodInvocation invoke = ast.newMethodInvocation();
		invoke.setExpression((Expression) ASTNode.copySubtree(ast,node));
		invoke.setName(ast.newSimpleName("parallel"));
		rewrite.replace(node, invoke, null); //We add our modification to the record
	}

}
