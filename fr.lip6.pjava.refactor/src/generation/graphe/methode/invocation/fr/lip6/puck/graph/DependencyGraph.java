package generation.graphe.methode.invocation.fr.lip6.puck.graph;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

import generation.graphe.methode.invocation.android.util.SparseIntArray;
import generation.graphe.methode.invocation.fr.lip6.move.gal.util.GraphUtils;
import generation.graphe.methode.invocation.fr.lip6.move.gal.util.MatrixCol;

/**
 * A dependency graph, i.e. a set of edges relating nodes of a DependencyNodes instance.
 * 
 * For each edge we also store a set of "reasons", that is a set of syntactic elements
 * that induced that edge for diagnosis.
 * @author Yann
 *
 */
public class DependencyGraph {
	private MatrixCol graph;
	// source -> ((dest1, {r1,r2}),(dest2,{r3}))
	//
	// public void meth() { a.m1(); b.m1(); c.m2() ; for ()(...) }
	// 0 => meth
	// 1 => m1
	// 2 => m2
	// 0 => (1, {"a.m1()","b.m1()"}),(2,{"c.m2()"}))
	
	
	
	private Map<Integer,Map<Integer,List<ASTNode>>> reasons = new HashMap<>();
	
	
	public DependencyGraph(int nbnodes) {
		graph = new MatrixCol(nbnodes,nbnodes);
	}
	
	public void addEdge(int indexDst, int indexSrc, ASTNode reason) {
		if(indexDst == -1 || indexSrc == -1) return;
		graph.set(indexDst, indexSrc, 1);
		reasons.computeIfAbsent(indexDst, k -> new HashMap<>()).computeIfAbsent(indexSrc, k -> new ArrayList<>()).add(reason);
	}
	public void collectSuffix (Set<Integer> nodes) {
		GraphUtils.collectSuffix(nodes, graph);
	}
	
	
	public void dotExport(PrintWriter out, String style) {
		for (int coli=0,colie=graph.getColumnCount(); coli<colie;coli++ ) {
			SparseIntArray col = graph.getColumn(coli);
			for (int i=0,ie=col.size();i<ie;i++) {
				int colj = col.keyAt(i);
				out.println("  n"+coli+ " -> n" + colj + " " + style + " ;");
			}
		}
	}
	public MatrixCol getGraph() {
		return graph;
	}

	public List<ASTNode> getReasons(Integer dest, Integer source) {
		return reasons.getOrDefault(dest, Collections.emptyMap()).getOrDefault(source, Collections.emptyList());
	}

	public boolean hasEdge(Integer dest, Integer source) {
		return graph.get(dest, source) != 0;
	}
}
