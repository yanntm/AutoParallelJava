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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
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

import generation.graphe.methode.invocation.fr.lip6.puck.graph.DependencyNodes;
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
			
//			ICompilationUnit[] files = findAllFilesProject(project);
			
			List<CompilationUnit> parsedCu = JavaParserHelper.parseSources(project, compilationUnits,monitor);

			PuckGraph graph = GraphBuilder.collectGraph(parsedCu);

			exportGraph(graph);
			
			initialiseMap();

			AdjacencyList graphAdj = new AdjacencyList(graph.getUseGraph().getGraph());
			
			List<List<Integer>> cycle = graphAdj.findCycles();
			List<List<MethodDeclaration>> cycleMeth = cycleMethodDeclaration(cycle, graph.getNodes());

			for (List<MethodDeclaration> list : cycleMeth) {
				List<MethodVisitor> visitors = new ArrayList<MethodVisitor>();
				List<Boolean> resVisitors = new ArrayList<Boolean>();
				for (MethodDeclaration meth : list) {
					MethodVisitor visit = new MethodVisitor(map);
					meth.accept(visit);
					visitors.add(visit);
					resVisitors.add(isParallelizable(meth, visit));
				}
				boolean cycleParallelizable = true;
				for (Boolean b : resVisitors) {
					if(!b)cycleParallelizable=false;
				}
				if(cycleParallelizable) {
					int i=0;
					for (MethodVisitor visitor : visitors) {
						methodDistribution(map, list.get(i), visitor);
					}
				}


			}


			for(Integer i : graphAdj) {
				IMethodBinding bind = graph.getNodes().get(i);
				MethodDeclaration dec =  graph.getNodes().get(bind);
				if(!inCycle(dec,cycleMeth)) {
					MethodVisitor visit = new MethodVisitor(map);
					dec.accept(visit);
					methodDistribution(map, dec, visit);
				}



			}
		}

		return new RefactoringStatus();

	}

//	private ICompilationUnit[] findAllFilesProject(IJavaProject project) {
//		List<ICompilationUnit> res = new ArrayList<ICompilationUnit>();
//		try {
//			for(IPackageFragmentRoot pack : project.getAllPackageFragmentRoots()) {
//				for(IJavaElement javElem : pack.getChildren()) {
//					IResource resource = javElem.getResource();
//					System.out.println(javElem + " type " + javElem.getElementType());
////					try {
////						resource.accept(new IResourceVisitor() {
////							
////							@Override
////							public boolean visit(IResource resource)  {
////								if(resource instanceof IFile) {
////									IFile file = (IFile) resource;
////									res.add(JavaCore.createCompilationUnitFrom(file));
////								}
////								return true;
////							}
////						});
////					} catch (CoreException e) {
////						// TODO Auto-generated catch block
////						e.printStackTrace();
////					}
//				}
//			}
//		} catch (JavaModelException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}

	private void exportGraph(PuckGraph graph) {
		try {
			graph.exportDot("D:\\Users\\teill\\Documents\\test.dot");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private boolean inCycle(MethodDeclaration node, List<List<MethodDeclaration>> cycleMeth) {
		for (List<MethodDeclaration> list : cycleMeth) {
			for (MethodDeclaration methodDeclaration : list) {
				if (methodDeclaration.resolveBinding().getKey().equals(node.resolveBinding().getKey()))return true;
			}
		}
		return false;
	}

	private void initialiseMap() {
		map = new HashMap<>();
		map.put("ReadOnly", new HashSet<>());
		map.put("ThreadSafe", new HashSet<>());
		map.put("ModifLocal", new HashSet<>());
		map.put("NotParallelizable", new HashSet<>());
	}

	private int verifCycle(Integer valMatrix, List<List<Integer>> cycle) {
		for (int i = 0; i < cycle.size(); i++) {
			if(cycle.get(i).contains(valMatrix))return i;
		}
		return -1;
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

	private boolean isParallelizable(MethodDeclaration node,MethodVisitor visitor) {
		if (visitor.isReadOnly()) {
			return true;
		}
		else if (visitor.isThreadSafe()) {
			return true;
		}
		else if (visitor.isModifLocal()) {
			return true;
		} else {
			return false;
		}
	}

	private List<List<MethodDeclaration>> cycleMethodDeclaration(List<List<Integer>> cycle, DependencyNodes nodes){
		List<List<MethodDeclaration>> res = new ArrayList<List<MethodDeclaration>>();
		for (List<Integer> list : cycle) {
			List<MethodDeclaration> meth = new ArrayList<MethodDeclaration>();
			res.add(meth);
			for (Integer i : list) {
				meth.add(nodes.get(nodes.get(i)));
			}
		}

		return res;
	}
}
