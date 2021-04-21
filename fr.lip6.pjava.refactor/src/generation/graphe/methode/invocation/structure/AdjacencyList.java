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
	


	public int size() {
		return graph.length;
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
	
	public List<List<Integer>> findCycles() {
		List<List<Integer>> res = new ArrayList<>();
		for (int i=0; i < graph.length; i++) {
			dfs(res, i, i, new ArrayList<Integer>(), new boolean[graph.length] );
			
		}
		return res;
	}

	
	
	public void dfs(List<List<Integer>> res, int src, int current, List<Integer> stack, boolean[] visited) {
		stack.add(current);
		visited[current] = true;
		for(int i : graph[current]) {
			if (current == src) {
				res.add(new ArrayList<>(stack)); // TODO gerer les multi insertion
			} else if (!visited[i]) {
				dfs(res, src, i, stack, visited);
			}
		}
		stack.remove(stack.size()-1);
		
	}
}
