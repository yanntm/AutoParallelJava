package generation.graphe.methode.invocation.fr.lip6.puck.graph;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;


/**
 * The set of nodes in a Puck dependency graph : packages, types, methods and attributes.
 * 
 * @author Yann
 *
 */
public class DependencyNodes {
	// this holds the actual nodes, at their proper index. All nodes are here, this is the union of types+methods+attributes+ packages.
	//private List<IBinding> nodes = new ArrayList<>();
	// these separate lists of nodes by subtype also benefit from tighter type constraints, for refined analysis/use.
	// they form a partition of nodes.
	private List<IMethodBinding> nodes = new ArrayList<>();
	private Map<IMethodBinding, MethodDeclaration> mtb2mtd = new HashMap<IMethodBinding, MethodDeclaration>();

	public void addMethod(IMethodBinding mtb, MethodDeclaration mtd) {
		nodes.add(mtb);
		mtb2mtd.put(mtb, mtd);
//		methods.add(mtb);
	}

	public int size() {
		return nodes.size();
	}
	
	@Override
	public String toString() {
		return printNodes(nodes);
	}
	
	/**
	 * Find the index of a node or -1 if not found.
	 * TODO : we should really use a Map<IBinding,Integer> 
	 * @param tb the object
	 * @return the index of object in list or -1 if not found.
	 */
	public int findIndex(IBinding tb) {
		int indexDst = nodes.indexOf(tb);
		return indexDst;
	}

	
	/**
	 * Find the index of a node or -1 if not found.
	 * TODO : we should really use a Map<IBinding,Integer> 
	 * @param nodes the list
	 * @param tb the object
	 * @return the index of object in list or -1 if not found.
	 */
	public int findIndex(String tname) {
		int indexDst = -1;
		for (int i=0; i < nodes.size(); i++) {
			IBinding elt = nodes.get(i);
			if (elt instanceof ITypeBinding) {
				ITypeBinding tb = (ITypeBinding) elt;
				String id = tb.getQualifiedName(); 
				if (id.equals(tname)) {
					return i;
				}
			} else if (elt instanceof IPackageBinding) {
				IPackageBinding pkg = (IPackageBinding) elt;
				String id = pkg.getName(); 
				if (id.equals(tname)) {
					return i;
				}
			}
		}
		return indexDst;
	}

	
	/**
	 * Debug method used for printing graph in console mode.
	 * @param nodes the list of nodes we built
	 * @return a nice-ish String representation for these nodes.
	 */
	private static String printNodes(List<? extends IBinding> nodes) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (IBinding b : nodes) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			if (b instanceof ITypeBinding) {
				ITypeBinding tb = (ITypeBinding) b;
				sb.append("TYPE:"+tb.getQualifiedName());				
			} else if (b instanceof IMethodBinding) {
				IMethodBinding mb = (IMethodBinding) b;
				sb.append("METHOD:"+mb.getDeclaringClass().getQualifiedName()+ "::"  +mb);				
			} else if (b instanceof IPackageBinding) {
				IPackageBinding pb = (IPackageBinding) b;
				sb.append("PACKAGE:"+pb.getName());
			} else if (b instanceof IVariableBinding) {
				IVariableBinding vb = (IVariableBinding) b;
				sb.append("FIELD:"+vb.getDeclaringClass().getQualifiedName()+ "::"  +vb);
			}
		}
		return sb.toString();		
	}

	/**
	 * Dot format export of the set of nodes, with some custom shapes.
	 * @param out where we need to write.
	 */
	public void dotExport(PrintWriter out) {
		int index = 0;
		for (IBinding elt : nodes) {
			if (elt instanceof ITypeBinding) {
				ITypeBinding tb = (ITypeBinding) elt;
				out.println("  n"+index+ " [shape=box,label=\""+tb.getQualifiedName()+"\"]");
			} else if (elt instanceof IVariableBinding) {
				IVariableBinding vb = (IVariableBinding) elt;
				out.println("  n"+index+ " [shape=doubleellipse,label=\"" + vb.getDeclaringClass().getQualifiedName()+"."+vb.getName()+"\"];");
			} else if (elt instanceof IMethodBinding) {
				IMethodBinding mb = (IMethodBinding) elt;
				out.println("  n"+index+ " [shape=octagon,label=\"" + mb.getDeclaringClass().getQualifiedName()+"."+mb.getName()+"\"];");
			} else if (elt instanceof IPackageBinding) {
				IPackageBinding pkg = (IPackageBinding) elt;
				out.println("  n"+index+ " [shape=folder,label=\"" + pkg.getName() +"\"];");				
			}
			index++;
		}		
	}
	
	public IMethodBinding get(int i) {
		return nodes.get(i);
	}
	
	public MethodDeclaration get(IMethodBinding mtb) {
		return mtb2mtd.get(mtb);
	}
	
}
