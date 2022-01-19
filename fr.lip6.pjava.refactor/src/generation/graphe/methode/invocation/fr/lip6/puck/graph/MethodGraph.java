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
public class PuckGraph {
	public static class Rule {
		public final Set<Integer> from;
		public final Set<Integer> hide;
		public String text;
		public Rule(Set<Integer> hide, Set<Integer> from, String text) {
			this.hide = hide;
			this.from = from;
			this.text = text;
		}
	}
	private DependencyGraph composeGraph;
	private DependencyNodes nodes;
	private List<Rule> rules = new ArrayList<>();
	private Map<String,Set<Integer>> setDeclarations = new HashMap<>();

	private DependencyGraph useGraph;

	public PuckGraph(DependencyNodes nodes) {
		this.nodes = nodes;
		this.useGraph = new DependencyGraph(nodes.size());
		this.composeGraph = new DependencyGraph(nodes.size());
	}

	public void addRule (Set<Integer> hide, Set<Integer> from, String text) {
		this.rules.add (new Rule(hide,from, text));
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
		getComposeGraph();
		PrintWriter out = new PrintWriter(new File(path));
		out.println("digraph  G {");
		nodes.dotExport(out);

		// usage edges
		useGraph.dotExport(out, "");

		// containment edges 
		composeGraph.dotExport(out, "[style=dotted]");

		boolean doRedArcs=true;
		if (! doRedArcs) {
			// named sets
			for (Entry<String, Set<Integer>> ent : setDeclarations.entrySet()) {
				out.println("  "+ent.getKey()+ " [color=blue] ;");
				for (Integer i : ent.getValue()) {
					out.println("  "+ent.getKey()+ " -> n" + i + " [color=blue] ;");				
				}
			}

			// broken right now
			//			for (Rule rule : rules) {
			//				out.println("  "+rule.hide+ " -> " + rule.from + " [color=red] ;");							
			//			}
		} else {
			for (Rule rule : rules) {
				Set<Integer> from = new HashSet<>(rule.from);
				Set<Integer> hide = new HashSet<>(rule.hide);

				for (Integer interloper : from) {
					for (Integer secret : hide) {
						if (useGraph.hasEdge(secret, interloper) || composeGraph.hasEdge(secret, interloper)) {
							out.println("  n"+interloper+ " -> n" + secret + " [color=red] ;");														
						}
					}
				}
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
	
	public DependencyGraph getComposeGraph() {
		return composeGraph;
	}

	public DependencyNodes getNodes() {
		return nodes;
	}

	public List<Rule> getRules() {
		return rules;
	}

	public Set<Integer> getSetDeclaration (String name) {
		return setDeclarations.getOrDefault(name, Collections.emptySet());
	}
	public DependencyGraph getUseGraph() {
		return useGraph;
	}



}
