package generation.graphe.methode.invocation.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import generation.graphe.methode.invocation.android.util.SparseIntArray;
import generation.graphe.methode.invocation.fr.lip6.move.gal.util.MatrixCol;

public class AdjacencyList implements Iterable<Integer>{
	
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
	
	private List<Integer>[] graph;
	


	@SuppressWarnings("unchecked")
	public AdjacencyList(MatrixCol matrix) {
		graph = new List[matrix.getColumnCount()];
		for (int i = 0; i < matrix.getColumnCount(); i++) {
			SparseIntArray coli = matrix.getColumn(i);
			int colisz = coli.size();
			graph[i] = new ArrayList<Integer>(colisz);
			for (int j = 0; j < colisz; j++) {
				graph[i].add(coli.keyAt(j));
			}
		}
	}
	
	private List<List<Integer>> createHierarchy() {
		Map<Integer, List<Integer>> dfsValue = new HashMap<Integer, List<Integer>>();
		List<List<Integer>> res = new ArrayList<List<Integer>>();
		for (int i = 0; i < graph.length; i++) {
			int dfsVal = DFS(i);
			if(dfsValue.get(dfsVal)==null) dfsValue.put(dfsVal, new ArrayList<Integer>());
			dfsValue.get(dfsVal).add(i);
		}
		List<Integer> valKeyMap = new ArrayList<Integer>(dfsValue.keySet());
		Collections.sort(valKeyMap);
		for (Integer integer : valKeyMap) {
			res.add(dfsValue.get(integer));
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

	// The function to do DFS traversal. It uses recursive
    // DFSUtil()
    int DFS(int  val)
    {
        // Mark all the vertices as not visited(set as
        // false by default in java)
        boolean visited[] = new boolean[graph.length];
         
        // Call the recursive helper function to print DFS
        // traversal starting from all vertices one by one
        return DFSUtil(val, visited);
            
    }
	
	// A function used by DFS
    int DFSUtil(int v, boolean visited[])
    {
        // Mark the current node as visited and print it
        visited[v] = true;        
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
	

    public List<List<Integer>> findCycles() {
		List<List<Integer>> res = new ArrayList<>();
		for (int i=0; i < graph.length; i++) {
			dfs(res, i, i, new ArrayList<Integer>(), new boolean[graph.length] );
			
		}
		return res;
	}
 
    public List<Integer>[] getGraph() {
		return graph;
	}



	@Override
	public Iterator<Integer> iterator() {
		return new IteratorGraph();
	}
	
	public int size() {
		return graph.length;
	}
}
