package fr.lip6.pjava.refactor;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.internal.corext.fix.CompilationUnitRewriteOperationsFix;
import org.eclipse.jdt.internal.corext.fix.CompilationUnitRewriteOperationsFix.CompilationUnitRewriteOperation;
import org.eclipse.jdt.internal.ui.fix.AbstractMultiFix;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.CleanUpRequirements;
import org.eclipse.jdt.ui.cleanup.ICleanUp;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import generation.graphe.methode.invocation.fr.lip6.puck.graph.PuckGraph;
import generation.graphe.methode.invocation.fr.lip6.puck.parse.GraphBuilder;
import generation.graphe.methode.invocation.fr.lip6.puck.parse.JavaParserHelper;
import generation.graphe.methode.invocation.structure.AdjacencyList;

/**
 * Class that will be call when we ask for a Clean-Up
 * @author Teillet & Capitanio
 *
 */
@SuppressWarnings("restriction")
public class Lambda2For extends AbstractMultiFix implements ICleanUp {	 
	private CleanUpOptions fOptions;
	private RefactoringStatus fStatus;
	private HashMap<String, Set<String>> map;



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
			
			List<CompilationUnit> parsedCu = JavaParserHelper.parseSources(project, compilationUnits,monitor);
			
			PuckGraph graph = GraphBuilder.collectGraph(parsedCu);
			
			AdjacencyList graphAdj = new AdjacencyList(graph.getUseGraph().getGraph());
					
			map = new HashMap<>();
			map.put("ReadOnly", new HashSet<>());
			map.put("ThreadSafe", new HashSet<>());
			map.put("ModifLocal", new HashSet<>());
			map.put("NotParallelizable", new HashSet<>());
			
//			try {
//				graph.exportDot("D:\\Users\\teill\\Documents\\test.dot");
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			
			
			for(Integer i : graphAdj) {
				IMethodBinding bind = graph.getNodes().get(i);
				MethodDeclaration dec =  graph.getNodes().get(bind);
				MethodVisitor visit = new MethodVisitor(map);
				dec.accept(visit);
				methodDistribution(map, dec, visit);
			}
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
		TraitementFor.clear();
		cu.accept(new ASTVisitor() {

			@Override
			public boolean visit(EnhancedForStatement node) {

				ASTVisitorPreCond visitorPreCond = new  ASTVisitorPreCond(node);
				if (visitorPreCond.isUpgradable()) {
					node.getBody().accept(visitorPreCond);

					if (visitorPreCond.isUpgradable() )
					{
						//Ne pas ajouter d'élément qui ne fait rien
						rewriteOperations.add(new TraitementFor(cu, node, map));
					}
				}
				return false;
			}

		});


		if(rewriteOperations.isEmpty())return null;
		else return new CompilationUnitRewriteOperationsFix("Transformation of EnhancedFor to Stream", cu,
				rewriteOperations.toArray(new CompilationUnitRewriteOperation[rewriteOperations.size()]));
	}

	@Override
	protected ICleanUpFix createFix(CompilationUnit unit, IProblemLocation[] problems) throws CoreException {
		return null;
	}


	private static CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS15);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null); // parse
	}

	private void methodDistribution(Map<String, Set<String>> map, MethodDeclaration node,
			MethodVisitor visitor) {
		if (visitor.isReadOnly()) {
			map.get("ReadOnly").add(node.resolveBinding().getKey());
		}
		else if (Modifier.isSynchronized(node.resolveBinding().getModifiers())) {
			map.get("ThreadSafe").add(node.resolveBinding().getKey());
		}
		else if (visitor.isThreadSafe()) {
			map.get("ThreadSafe").add(node.resolveBinding().getKey());
		}
		else if ( visitor.isModifLocal()) {
			map.get("ModifLocal").add(node.resolveBinding().getKey());
		} else {
			map.get("NotParallelizable").add(node.resolveBinding().getKey());
		}
	}
	
	private List<ASTNode> findLeafs(PuckGraph pg){
		return null;
	}
	
	private List<List<ASTNode>>findCycle(PuckGraph pg){
		return null;
	}
	
	private void removeElement(MethodDeclaration node, PuckGraph pg) {
		int i = pg.findIndex(node.resolveBinding());
		pg.getUseGraph().getGraph().deleteColumn(i);
		pg.getUseGraph().getGraph().deleteRow(i);
//		pg.getNodes().
	}
}
