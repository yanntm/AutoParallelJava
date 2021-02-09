package fr.lip6.pjava.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.ui.fix.AbstractMultiFix;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.ICleanUp;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jface.action.IAction;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class Lambda2For extends AbstractMultiFix implements ICleanUp {

	@Override
	public void setOptions(CleanUpOptions options) {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] getStepDescriptions() {
		// TODO Auto-generated method stub
		//vsrbfd
		return new String[0];
	}

	protected CompilationUnit parse(ICompilationUnit lwUnit) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(lwUnit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}

	@Override
	public RefactoringStatus checkPreConditions(IJavaProject project, ICompilationUnit[] compilationUnits,
			IProgressMonitor monitor) throws CoreException {

		for (ICompilationUnit cu : compilationUnits) {
			CompilationUnit unit = parse(cu);
			final ASTRewrite rewrite = ASTRewrite.create(unit.getAST());

			unit.accept(new ASTVisitor() {
				@Override
				public void endVisit(LambdaExpression node) {
					super.endVisit(node);
					rewrite.remove(node, null);
					int u=0;
//					ASTNode replacement=AST.newAST(O);
//					
//						rewrite.replace(node, "dtd", null);
//						 unit.recordModifications ();
//						 // ...			
//						 IAction VariableDeclarationStatement = createNewVariableDeclarationStatement (manager, ast);
//						 block.statements (). add (firstReferenceIndex, instruction); 
					//TextEditGroup editGroup;
					//rewrite.replace(node, replacement, editGroup);
				}
						
			});
		}

		return new RefactoringStatus();
	}

	@Override
	public RefactoringStatus checkPostConditions(IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canFix(ICompilationUnit compilationUnit, IProblemLocation problem) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected ICleanUpFix createFix(CompilationUnit unit) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ICleanUpFix createFix(CompilationUnit unit, IProblemLocation[] problems) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

}
