package generation.graphe.methode.invocation.fr.lip6.puck.parse;

import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;

import generation.graphe.methode.invocation.fr.lip6.puck.graph.DependencyNodes;
import generation.graphe.methode.invocation.fr.lip6.puck.graph.MethodGraph;


/**
 * Traverses a set of input Java files to build the underlying containment and usage graphs.
 * @author Yann
 *
 */
public final class GraphBuilder  {

	/**
	 * A visitor for AST that collects edges for any usage or containment
	 * where both nodes belong to the graph.
	 *
	 */
	private static class EdgeCollector extends ASTVisitor {
		// method declaration that owns current statement
		private Stack<Integer> currentOwner = new Stack<>();

		private MethodGraph graph;

		public EdgeCollector(MethodGraph graph) {
			this.graph = graph;
		}


		/**
		 * Add a dependency from top of curentOwner stack (if any) to the node referred to in the binding (if any).
		 * @param tb A @see {@link IBinding} resolved name that might point to a node of the graph.
		 * @param node
		 */
		private void addDependency(IBinding tb, ASTNode node) {
			if (!currentOwner.isEmpty()) {
				int indexSrc = currentOwner.peek();
				int indexDst = graph.getNodes().findIndex(tb);
				if (indexDst != indexSrc && indexDst >= 0 && indexSrc >= 0) {
					graph.getUseGraph().addEdge(indexDst, indexSrc, node);
				}
			}
		}

		@Override
		public void endVisit(MethodDeclaration node) {
			currentOwner.pop();
		}


		/**
		 * The actual visit a "Name" case, resolve the node it points to, and add a dependency to it.
		 * @param node every node in the full AST gets this method invoked on it; the other visit methods are more selective, this is a catch-all.
		 */
		@Override
		public void preVisit(ASTNode node) {
			if (node instanceof Name) {
				Name name = (Name) node;
				IBinding b = name.resolveBinding();
				addDependency(b,node);
			}
			super.preVisit(node);
		}

		/**
		 * Current owner becomes the method declaration.
		 */
		@Override
		public boolean visit(MethodDeclaration node) {
			int cur = graph.getNodes().findIndex(node.resolveBinding());

			// nested method declarations ??
//			if (!currentOwner.isEmpty() && cur != -1) {
//				graph.getComposeGraph().addEdge(cur, currentOwner.peek(), node);
//			}
			currentOwner.push(cur);
			return true;
		}

	}

	/**
	 * Visits the parsed AST of these compilation units to first collect a set of nodes,
	 * then add all usage and containment edges to this graph.
	 * @param parsedCu A list of parsed Java DOM representations of sources.
	 * @return a graph containing the analysis results.
	 */
	public static MethodGraph collectGraph(List<CompilationUnit> parsedCu) {
		// The nodes we will collect in a first pass.
		DependencyNodes nodes = new DependencyNodes();

		parsedCu.stream().parallel().forEach((unit) -> {
			unit.accept(new ASTVisitor() {
				/**
				* Deal with TypeDeclaration : classes + interfaces : add nodes for them, their methods, their attributes.
				*/
				@Override
				public void endVisit(MethodDeclaration meth) {
					IMethodBinding mtb = meth.resolveBinding();
					nodes.addMethod(mtb, meth);
				}
			});
		});

		System.out.println("Found " + nodes.size() + " nodes : " + nodes);

		// Step 2 : we traverse the AST again, but this time we add edges to the picture
		// if either source or destination for a potential edge is not in the set of nodes
		// that we have collected above, the edge is discarded.

		// Let's initialize the graph with the context nodes we have collected.
		MethodGraph graph = new MethodGraph(nodes);

		// Now the graph builder; because it is stateful (it keeps track of which node is current owner) it is implemented as a separate Visitor class.
		EdgeCollector edgeCollector = new EdgeCollector(graph);

		parsedCu.stream().parallel().forEach((unit) -> {
			unit.accept(edgeCollector);
		});

		return graph;
	}

	private GraphBuilder() {}
}
