package fr.lip6.pjava.refactor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.internal.ui.fix.AbstractMultiFix;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.CleanUpRequirements;
import org.eclipse.jdt.ui.cleanup.ICleanUp;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Class that will be call when we ask for a Clean-Up
 * @author teill
 *
 */
@SuppressWarnings("restriction")
public class Lambda2For extends AbstractMultiFix implements ICleanUp {	 
	private CleanUpOptions fOptions;
	private RefactoringStatus fStatus;
	List<EnhancedForStatement> forATraiter = new ArrayList<EnhancedForStatement>();
	
	public Lambda2For() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String[] getStepDescriptions() {
		if(fOptions.isEnabled("cleanup.transform_enhanced_for")) {
			return new String[] {"Transform Enhanced For to Stream"};
		}
		return null;
	}
	
	public CleanUpRequirements getRequirements() {
		return new CleanUpRequirements(true, true, false, null);   
	}
	
	public void setOptions(CleanUpOptions options) {
		Assert.isLegal(options != null);
		Assert.isTrue(fOptions == null);
		fOptions= options;  
	}
	
	/**
	 * Parse a ICompilationUnit to a CompilationUnit, use to generate an AST 
	 * @param lwUnit the source file we want to parse
	 * @param monitor 
	 * @return the AST generated
	 */
	protected CompilationUnit parse(ICompilationUnit lwUnit, IProgressMonitor monitor) {
		ASTParser parser = ASTParser.newParser(AST.JLS15);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(lwUnit);
		parser.setBindingsRecovery(true);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		return (CompilationUnit) parser.createAST(monitor);
	}

	@Override
	public RefactoringStatus checkPreConditions(IJavaProject project, ICompilationUnit[] compilationUnits,
			IProgressMonitor monitor) throws CoreException {
		if (fOptions.isEnabled("cleanup.transform_enhanced_for")) { //$NON-NLS-1$
			fStatus= new RefactoringStatus();
			
		}
		
		return new RefactoringStatus();
		
	}

	@Override
	public RefactoringStatus checkPostConditions(IProgressMonitor monitor) throws CoreException {
		try {
			if (fStatus == null || fStatus.isOK()) {
				return new RefactoringStatus();
			} else {
				return fStatus;
			}
		} finally {
			fStatus= null;
		}
	}

	@Override
	public boolean canFix(ICompilationUnit compilationUnit, IProblemLocation problem) {
		return fOptions.isEnabled("cleanup.transform_enhanced_for");
	}

	@Override
	protected ICleanUpFix createFix(CompilationUnit cu) throws CoreException {
		if(cu == null || !fOptions.isEnabled("cleanup.transform_enhanced_for")) {return null;}
		
		cu.accept(new ASTVisitor() {
			@Override
			public boolean visit(EnhancedForStatement node) {
				
				ASTVisitorPreCond visitorPreCond = new  ASTVisitorPreCond(node);
				node.getBody().accept(visitorPreCond);
				
				if (visitorPreCond.isUpgradable())
				{
					forATraiter.add(node);
				}
				return false;
			}
					
		});
		return Lambda2For.createCleanUp(cu, forATraiter);
	}
	

	private static ICleanUpFix createCleanUp(CompilationUnit unit, List<EnhancedForStatement> forATraiter) {
		return new TraitementFor(unit, forATraiter);
	}

	@Override
	protected ICleanUpFix createFix(CompilationUnit unit, IProblemLocation[] problems) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

}
