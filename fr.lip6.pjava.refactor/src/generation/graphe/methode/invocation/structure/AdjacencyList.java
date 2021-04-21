package generation.graphe.methode.invocation.structure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import generation.graphe.methode.invocation.fr.lip6.move.gal.util.MatrixCol;

public class AdjacencyList {
	private Set<Integer>[] graph;
	
	@SuppressWarnings("unchecked")
	public AdjacencyList(MatrixCol matrix) {
		int[][] mat = matrix.explicit();
		graph = new Set[mat.length];
		for (int i = 0; i < mat.length; i++) {
			graph[i]= new HashSet<Integer>();
			for (int j = 0; j < mat[i].length; j++) {
				if (mat[i][j] == 1) {
					graph[i].add(j);
				}
			}
		}
	}
	
	public Set<Integer>[] getGraph() {
		return graph;
	}
	
	public List<Integer> findLeaf(){
		List<Integer> res = new ArrayList<Integer>();
		for (int i = 0; i < graph.length; i++) {
			if (graph[i].isEmpty()) {
				res.add(i);
			}
		}
		return res;
	}
	
	public static AdjacencyList matrixProduct(AdjacencyList graph) {
		return null;
	}
	
	
}
