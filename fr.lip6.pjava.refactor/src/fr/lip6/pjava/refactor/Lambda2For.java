package fr.lip6.pjava.refactor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.internal.corext.fix.CompilationUnitRewriteOperationsFix;
import org.eclipse.jdt.internal.corext.fix.CompilationUnitRewriteOperationsFix.CompilationUnitRewriteOperation;
import org.eclipse.jdt.internal.ui.fix.AbstractMultiFix;
import org.eclipse.jdt.internal.ui.fix.MultiFixMessages;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.CleanUpRequirements;
import org.eclipse.jdt.ui.cleanup.ICleanUp;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Class that will be call when we ask for a Clean-Up
 * @author Teillet & Capitanio
 *
 */
@SuppressWarnings("restriction")
public class Lambda2For extends AbstractMultiFix implements ICleanUp {	 
	private CleanUpOptions fOptions;
	private RefactoringStatus fStatus;
	
	
	
	@Override
	public String[] getStepDescriptions() {
		if(fOptions.isEnabled("cleanup.transform_enhanced_for")) {
			return new String[] {"Transform Enhanced For to Stream"};
		}
		return null;
	}
	
	@Override
	public CleanUpRequirements getRequirements() {
		return new CleanUpRequirements(true, true, false, null);   
	}
	
	@Override
	public void setOptions(CleanUpOptions options) {
		Assert.isLegal(options != null);
		Assert.isTrue(fOptions == null);
		fOptions= options;  
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
		List<CompilationUnitRewriteOperation> rewriteOperations = new ArrayList<>();
		cu.accept(new ASTVisitor() {
			
			@Override
			public boolean visit(EnhancedForStatement node) {
				
				ASTVisitorPreCond visitorPreCond = new  ASTVisitorPreCond(node);
				node.getBody().accept(visitorPreCond);
				
				if (visitorPreCond.isUpgradable() )
				{
					//Ne pas ajouter d'élément qui ne fait rien
					rewriteOperations.add(new TraitementFor(cu, node));
				}
				return false;
			}
			
//			@Override
//			public boolean visit(WhileStatement node) {				
//				
//				ASTVisitorPreCond visitorPreCond = new  ASTVisitorPreCond(node);
//				node.getBody().accept(visitorPreCond);
//				
//				if (visitorPreCond.isUpgradable() )
//				{
//					rewriteOperations.add(new TraitementFor(cu, node));
//				}
//				return false;
//			}
//			
//			@Override
//			public boolean visit(ForStatement node) {
//				ASTVisitorPreCond visitorPreCond = new  ASTVisitorPreCond(node);
//				node.getBody().accept(visitorPreCond);
//				
//				if (visitorPreCond.isUpgradable() )
//				{
//					rewriteOperations.add(new TraitementFor(cu, node));
//				}
//				return false;
//			}
		});
		
		//return Lambda2For.createCleanUp(cu, forATraiter);
		
		 if(rewriteOperations.isEmpty())return null;
		 else return new CompilationUnitRewriteOperationsFix("Transformation of EnhancedFor to Stream", cu,
				 rewriteOperations.toArray(new CompilationUnitRewriteOperation[rewriteOperations.size()]));
	}
	
	@Override
	protected ICleanUpFix createFix(CompilationUnit unit, IProblemLocation[] problems) throws CoreException {
		return null;
	}

}
