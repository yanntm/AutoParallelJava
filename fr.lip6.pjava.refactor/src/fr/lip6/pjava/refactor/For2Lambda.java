package fr.lip6.pjava.refactor;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
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
import generation.graphe.methode.invocation.fr.lip6.puck.graph.MethodGraph;
import generation.graphe.methode.invocation.fr.lip6.puck.parse.GraphBuilder;
import generation.graphe.methode.invocation.fr.lip6.puck.parse.JavaParserHelper;
import generation.graphe.methode.invocation.structure.AdjacencyList;

/**
 * Class that will be call when we ask for a Clean-Up
 * @author Teillet & Capitanio
 *
 */
@SuppressWarnings("restriction")
public class For2Lambda extends AbstractMultiFix implements ICleanUp {	 
	private CleanUpOptions fOptions;
	private RefactoringStatus fStatus;
	private HashMap<String, Set<String>> methodTag;



	@Override
	public boolean canFix(ICompilationUnit compilationUnit, IProblemLocation problem) {
		return fOptions.isEnabled("cleanup.transform_enhanced_for");
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
	public RefactoringStatus checkPreConditions(IJavaProject project, ICompilationUnit[] compilationUnits,
			IProgressMonitor monitor) throws CoreException {
		if (fOptions.isEnabled("cleanup.transform_enhanced_for")) { //$NON-NLS-1$
			fStatus= new RefactoringStatus();
			
//			ICompilationUnit[] files = findAllFilesProject(project);
			
			List<CompilationUnit> parsedCu = JavaParserHelper.parseSources(project, compilationUnits,monitor);

			MethodGraph graph = GraphBuilder.collectGraph(parsedCu);

			//exportGraph(graph);
			try {
				graph.exportDot(project.getProject().getLocation().toFile().getCanonicalPath() + "/graph.dot");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			if (true) {
//				return new RefactoringStatus();
//			}
			
			initialiseMap();

			AdjacencyList graphAdj = new AdjacencyList(graph.getUseGraph().getGraph());
			
			List<List<Integer>> cycle = graphAdj.findCycles();
			List<List<MethodDeclaration>> cycleMeth = cycleMethodDeclaration(cycle, graph.getNodes());

			for (List<MethodDeclaration> list : cycleMeth) {
				List<MethodVisitor> visitors = new ArrayList<MethodVisitor>();
				List<Boolean> resVisitors = new ArrayList<Boolean>();
				for (MethodDeclaration meth : list) {
					MethodVisitor visit = new MethodVisitor(methodTag);
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
						methodDistribution(methodTag, list.get(i), visitor);
					}
				}
			}

			for(Integer i : graphAdj) {
				IMethodBinding bind = graph.getNodes().get(i);
				MethodDeclaration dec =  graph.getNodes().get(bind);
				if(!inCycle(dec,cycleMeth)) {
					MethodVisitor visit = new MethodVisitor(methodTag);
					dec.accept(visit);
					methodDistribution(methodTag, dec, visit);
				}
			}
		}

		return new RefactoringStatus();

	}

	@Override
	protected ICleanUpFix createFix(CompilationUnit cu) throws CoreException {
		if(cu == null || !fOptions.isEnabled("cleanup.transform_enhanced_for")) {return null;}
		List<CompilationUnitRewriteOperation> rewriteOperations = new ArrayList<>();
		TraitementFor.clear();
		
		cu.accept(new ASTVisitor() {
			
			public boolean visit(MethodInvocation node) {
				// x = coll.stream().filter().collect()

				// look for method invocation with type Stream such that : type of expression is not Stream
				ITypeBinding resType = node.resolveTypeBinding();
				if (isStreamType(resType)) {
					if (! isStreamType(node.getExpression().resolveTypeBinding())) {
						// node = coll.stream(), expr = coll
						rewriteOperations.add(new StreamToParallel(node));
					}
				}
	
				return true;
			}

			
		});
		
		
		cu.accept(new ASTVisitor() {

			@Override
			public boolean visit(EnhancedForStatement node) {

				ASTPreCondVisitor visitorPreCond = new  ASTPreCondVisitor(node);
				if (visitorPreCond.isUpgradable()) {
					node.getBody().accept(visitorPreCond);

					if (visitorPreCond.isUpgradable() )
					{
						//Ne pas ajouter d'�l�ment qui ne fait rien
						rewriteOperations.add(new TraitementFor(cu, node, methodTag));
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
	
	
	private boolean isStreamType(ITypeBinding resType) {
		// TODO : test subtype somehow ?
		//					type.isSubTypeCompatible(streamType.);
		try {
			String qname = resType.getName().replaceAll("<.*", "");
			String qpkg = resType.getPackage().getName();
			return qname.equals("Stream") && qpkg.equals("java.util.stream");
		} catch (NullPointerException e) {
			return false;
		}
	};


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

//	private void exportGraph(PuckGraph graph) {
//		try {
//			graph.exportDot("C:\\Users\\teill\\Documents\\test.dot");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}

	@Override
	public CleanUpRequirements getRequirements() {
		return new CleanUpRequirements(true, true, false, null);   
	}

	@Override
	public String[] getStepDescriptions() {
		if(fOptions.isEnabled("cleanup.transform_enhanced_for")) {
			return new String[] {"Transform Enhanced For to Stream"};
		}
		return null;
	}

	private boolean inCycle(MethodDeclaration node, List<List<MethodDeclaration>> cycleMeth) {
		for (List<MethodDeclaration> list : cycleMeth) {
			for (MethodDeclaration methodDeclaration : list) {
				if (methodDeclaration.resolveBinding().getKey().equals(node.resolveBinding().getKey()))return true;
			}
		}
		return false;
	}

	/**
	 * Initialise la map d'attribution des categories de methode
	 */
	private void initialiseMap() {
		methodTag = new HashMap<>();
		methodTag.put("ReadOnly", new HashSet<>());
		methodTag.put("ThreadSafe", new HashSet<>());
		methodTag.put("ModifLocal", new HashSet<>());
		methodTag.put("NotParallelizable", new HashSet<>());
	}

	/**
	 * Test si une methode est parallelisable
	 * @param node methode tester
	 * @param visitor visitor de methode
	 * @return True si parallelisable
	 */
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

	/**
	 * Place une methode selon sa categorie dans map
	 * @param methodTag categories des methodes
	 * @param node methode place dans map
	 * @param visitor vistor associer aux methodes
	 */
	private void methodDistribution(Map<String, Set<String>> methodTag, MethodDeclaration node,
			MethodVisitor visitor) {
		if (visitor.isReadOnly()) {
			methodTag.get("ReadOnly").add(node.resolveBinding().getKey());
		}
		else if (Modifier.isSynchronized(node.resolveBinding().getModifiers())) {
			methodTag.get("ThreadSafe").add(node.resolveBinding().getKey());
		}
		else if (visitor.isThreadSafe()) {
			methodTag.get("ThreadSafe").add(node.resolveBinding().getKey());
		}
		else if ( visitor.isModifLocal()) {
			methodTag.get("ModifLocal").add(node.resolveBinding().getKey());
		} else {
			methodTag.get("NotParallelizable").add(node.resolveBinding().getKey());
		}
	}

	@Override
	public void setOptions(CleanUpOptions options) {
		Assert.isLegal(options != null);
		Assert.isTrue(fOptions == null);
		fOptions= options;  
	}
	
}
