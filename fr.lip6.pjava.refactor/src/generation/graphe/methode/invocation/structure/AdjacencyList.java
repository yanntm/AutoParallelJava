package generation.graphe.methode.invocation.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;


import generation.graphe.methode.invocation.fr.lip6.move.gal.util.MatrixCol;

public class AdjacencyList implements Iterable<Integer>{
	
	private List<Integer>[] graph;
	
	@SuppressWarnings("unchecked")
	public AdjacencyList(MatrixCol matrix) {
		int[][] mat = matrix.explicit();
		graph = new List[mat.length];
		for (int i = 0; i < mat.length; i++) {
			graph[i]= new ArrayList<Integer>();
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
	
	public List<Integer>[] getGraph() {
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

	private List<List<Integer>> createHierarchy() {
		Map<Integer, List<Integer>> dpsValue = new HashMap<Integer, List<Integer>>();
		for (int i = 0; i < graph.length; i++) {
			int dfsVal = DFS(i);
		}
		
		//TODO : creer la hierarchie niveau par niveau pour ensuite la parcourir en commencant par le niveau 1 élément 1

		
		return null;
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
	

    // A function used by DFS
    int DFSUtil(int v, boolean visited[])
    {
        // Mark the current node as visited and print it
        visited[v] = true;
        System.out.print(v + " ");
        
        List<Integer> res = new ArrayList<Integer>();
        
        // Recur for all the vertices adjacent to this
        // vertex
        Iterator<Integer> i = graph[v].listIterator();
        while (i.hasNext()) {
            int n = i.next();
            if (!visited[n]) {
            	res.add(DFSUtil(n, visited));
            }
                
        }
        try {
        	return Collections.max(res)+1;
        }catch (NoSuchElementException e) {
			return 0;
		}
        
    }
 
    // The function to do DFS traversal. It uses recursive
    // DFSUtil()
    int DFS()
    {
        // Mark all the vertices as not visited(set as
        // false by default in java)
        boolean visited[] = new boolean[graph.length];
 
        List<Integer> res = new ArrayList<Integer>();
        
        // Call the recursive helper function to print DFS
        // traversal starting from all vertices one by one
        for (int i = 0; i < graph.length; ++i) {
        	if (visited[i] == false) {
        		res.add(DFSUtil(i, visited));
        	}
                
        }
        try {
        	return Collections.max(res);
        }catch (NoSuchElementException e) {
			return 0;
		}
            
    }



	@Override
	public Iterator<Integer> iterator() {
		return new IteratorGraph();
	}
	
	private class IteratorGraph implements Iterator<Integer>{
		List<List<Integer>> hierarchy;	
		private int i=0;
		private int j=0;
		
		public IteratorGraph() {
			hierarchy=createHierarchy();
		}
		
		@Override
		public boolean hasNext() {
			return i<hierarchy.size() && j<hierarchy.get(i).size();
		}

		@Override
		public Integer next() {
			Integer res = hierarchy.get(i).get(j);
			j++;
			if(j==hierarchy.get(i).size()) {
				j=0;
				i++;
			}
			return res;
		}
		
	}
}
