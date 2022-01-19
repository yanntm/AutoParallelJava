package generation.graphe.methode.invocation.fr.lip6.puck.graph;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.IBinding;

/**
 * The PuckGraph is the central operational Puck graph model object.
 * 
 * It stores : the nodes of the graph (as DependencyNodes), the "use" graph
 *  and the "composition" graphs (as DependencyGraph), the named set declarations
 *  parsed to a set of integer indexes, the Puck rules resolved to a "hide" and
 *  a "from" set of integer indexes.
 *   
 * @author Yann
 *
 */
public class MethodGraph {
	
	private DependencyNodes nodes;
	private Map<String,Set<Integer>> setDeclarations = new HashMap<>();

	private DependencyGraph useGraph;

	public MethodGraph(DependencyNodes nodes) {
		this.nodes = nodes;
		this.useGraph = new DependencyGraph(nodes.size());
	}

	public void addSetDeclaration(String name, Set<Integer> nodes) {
		setDeclarations.put(name, nodes);
	}

	/**
	 * A visual representation for our graphs.
	 * @param path where will we build this dot file 
	 * @throws IOException if we couldn't write to that place.
	 */
	public void exportDot (String path) throws IOException {
		PrintWriter out = new PrintWriter(new File(path));
		out.println("digraph  G {");
		nodes.dotExport(out);

		// usage edges
		useGraph.dotExport(out, "");

		// containment edges 

		boolean doRedArcs=true;
			// named sets
			for (Entry<String, Set<Integer>> ent : setDeclarations.entrySet()) {
				out.println("  "+ent.getKey()+ " [color=blue] ;");
				for (Integer i : ent.getValue()) {
					out.println("  "+ent.getKey()+ " -> n" + i + " [color=blue] ;");				
				}
			}

		out.println("}");
		out.close();
	}

	public int findIndex(IBinding key) {
		return nodes.findIndex(key);
	}
	
	public int findIndex(String key) {
		return nodes.findIndex(key);
	}
	

	public DependencyNodes getNodes() {
		return nodes;
	}


	public Set<Integer> getSetDeclaration (String name) {
		return setDeclarations.getOrDefault(name, Collections.emptySet());
	}
	public DependencyGraph getUseGraph() {
		return useGraph;
	}



}
