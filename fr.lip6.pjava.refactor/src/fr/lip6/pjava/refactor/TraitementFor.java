package fr.lip6.pjava.refactor;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.text.edits.TextEdit;

/**
 * Class looking for Enhanced For, that we can transform to stream equivalent.
 * This class also propose the changes for the enhanced for.
 * @author Teillet & Capitanio
 *
 */
public class TraitementFor implements ICleanUpFix {
	/**
	 * CompilationUnit represents the AST, and to call a visitor on the tree
	 */
	private CompilationUnit unit;
	private List<EnhancedForStatement> listFor;

	/**
	 * The constructor used to initiate the attribute
	 * @param unit The CompilationUnit of the document
	 * @param forATraiter 
	 */
	public TraitementFor(CompilationUnit unit, List<EnhancedForStatement> forATraiter) {
		this.unit=unit;
		listFor = forATraiter;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public CompilationUnitChange createChange(IProgressMonitor progressMonitor) throws CoreException {
		AST ast = unit.getAST();   //We obtain the AST from the CompilationUnit. The will be use as a factory
		final ASTRewrite rewrite = ASTRewrite.create(ast);  //We create a new ASTRewrite, that will contain all our modification
		
		//We call the accept method on the AST, that will visit all the nodes, and use a personalized ASTVisitor to apply our changes
		for(EnhancedForStatement node : listFor) {
			ITypeBinding t = ((SimpleName)node.getExpression()).resolveTypeBinding();
			
			//Verification du type de tableau sur lequel on veut stream
			MethodInvocation replaceMethod = detectCollectionType(ast, node, t, rewrite);
			
			if(replaceMethod == null) break;
			//Creation of : <collection>.stream()
			
			// Method to copy an ASTNode and use it elsewhere : ASTNode.copySubtree(AST, nodeToCopy))
			TraitementForBody tfb = new TraitementForBody(node, ast);
			node.getBody().accept(tfb);
			
			
			//There is no If, so there isn't a filter. We create directly the forEach Method
			MethodInvocation forEach = ast.newMethodInvocation();
			
			forEach.setName(ast.newSimpleName("forEach"));
			
			if(tfb.getFirst()!=null && tfb.getLast()!=null) {
				tfb.getFirst().setExpression(replaceMethod);
				forEach.setExpression(tfb.getLast());
			}else {
				if(tfb.getFirst()!=null) {
					tfb.getFirst().setExpression(replaceMethod);
					forEach.setExpression(tfb.getFirst());
				}
				else {
					forEach.setExpression(replaceMethod);
				}
				
			}
				
			//We create the Lambda Expression for the ForEach
			LambdaExpression forEachCorps = ast.newLambdaExpression();
			if(tfb.getBody()!=null) forEachCorps.setBody(ASTNode.copySubtree(ast, tfb.getBody()));
			else forEachCorps.setBody(ASTNode.copySubtree(ast, node.getBody()));
			forEachCorps.parameters().add(ASTNode.copySubtree(ast,node.getParameter()));
			forEach.arguments().add(forEachCorps);
			ExpressionStatement st = ast.newExpressionStatement(forEach);
			rewrite.replace(node, st, null); //We add our modification to the record
		}
		
		return applicationChangement(rewrite); //Return a CompilationUnitChange that all our modification
	}

	/**
	 * Detect the type of the array 
	 * @param ast The ast that we are in
	 * @param node The node we are looking at
	 * @param t the type of the array
	 * @param rewrite 
	 * @return a new method Invocation fill with the good parameter or null if it is not an array
	 */
	@SuppressWarnings("unchecked")
	private MethodInvocation detectCollectionType(AST ast, EnhancedForStatement node, ITypeBinding t, ASTRewrite rewrite) {
		if(!t.isArray() && !containsCollection(t)) {
			return null;
		}else {
			if(containsCollection(t)) {
				MethodInvocation replaceMethod = ast.newMethodInvocation();
				replaceMethod.setExpression((Expression) ASTNode.copySubtree(ast,node.getExpression())); 
				replaceMethod.setName(ast.newSimpleName("stream"));
				return replaceMethod;
			}
			else {
				
				MethodInvocation replaceMethod = ast.newMethodInvocation();
				replaceMethod.arguments().add( ASTNode.copySubtree(ast,node.getExpression()));
				replaceMethod.setName(ast.newSimpleName("stream"));
				replaceMethod.setExpression(ast.newSimpleName("Arrays"));
				
				
				//Doit ajouter dans les import java.util.Arrays
				ImportDeclaration im = ast.newImportDeclaration();
				im.setName(ast.newName(new String[] {"java", "util", "Arrays"}));
				
				ListRewrite lrw = rewrite.getListRewrite(unit, CompilationUnit.IMPORTS_PROPERTY);
				lrw.insertLast(im, null);
				
				return replaceMethod;
				
			}
		}
	}


	/**
	 * Convert an ASTRewriter to a CompilationUnitChange
	 * @param rewriter the rewriter with our modification
	 * @return compilationUnitChange ready to apply all the changes
	 */
	private CompilationUnitChange applicationChangement(ASTRewrite rewriter) {
		CompilationUnitChange compilationUnitChange = new CompilationUnitChange("Refactor of EnhancedFor", (ICompilationUnit)unit.getJavaElement());
		
		try {
			TextEdit textEdit = rewriter.rewriteAST(); //create a TextEdit, that contain the modifications inside the rewriter
			compilationUnitChange.setEdit(textEdit);   //set the TextEdit as the things that contains our modification in the CompilationUnitChange
		} catch (Throwable e) {
			//Ne rien faire
		}
		
	    return compilationUnitChange;	
	}
	
	/**
	 * Method uses to verify if the type t is a Collection
	 * @param t the type we want to check
	 * @return if t is a Collection
	 */
	private boolean containsCollection(ITypeBinding t) {
		for (ITypeBinding i :t.getInterfaces()){
			if(i.getBinaryName().contains("java.util.Collection")) {
				return true;
			}
		}
		return false;
	}

}
