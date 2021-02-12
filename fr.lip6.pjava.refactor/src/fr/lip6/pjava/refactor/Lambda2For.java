package fr.lip6.pjava.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.fix.AbstractMultiFix;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.ICleanUp;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Class that will be call when we ask for a Clean-Up
 * @author teill
 *
 */
public class Lambda2For extends AbstractMultiFix implements ICleanUp {	 

	@Override
	public void setOptions(CleanUpOptions options) {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] getStepDescriptions() {
		// TODO Auto-generated method stub
		return new String[0];
	}
	
	/**
	 * Parse a ICompilationUnit to a CompilationUnit, use to generate an AST 
	 * @param lwUnit the source file we want to parse
	 * @return the AST generated
	 */
	protected CompilationUnit parse(ICompilationUnit lwUnit) {
		ASTParser parser = ASTParser.newParser(AST.JLS15);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(lwUnit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}

	@Override
	public RefactoringStatus checkPreConditions(IJavaProject project, ICompilationUnit[] compilationUnits,
			IProgressMonitor monitor) throws CoreException {
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
		if(unit == null)return null;
		return Lambda2For.createCleanUp(unit);
	}
	

	private static ICleanUpFix createCleanUp(CompilationUnit unit) {
		return new TraitementFor(unit);
	}

	@Override
	protected ICleanUpFix createFix(CompilationUnit unit, IProblemLocation[] problems) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

}
