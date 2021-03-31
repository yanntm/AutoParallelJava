package fr.lip6.pjava.refactor;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalModel;
import org.eclipse.jdt.internal.corext.fix.CompilationUnitRewriteOperationsFix.CompilationUnitRewriteOperation;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.text.edits.TextEdit;

/**
 * Class looking for Enhanced For, that we can transform to stream equivalent.
 * This class also propose the changes for the enhanced for.
 * @author Teillet & Capitanio
 *
 */
public class TraitementFor extends CompilationUnitRewriteOperation {
	/**
	 * CompilationUnit represents the AST, and to call a visitor on the tree
	 */
	private CompilationUnit unit;
	private Statement node;

	/**
	 * The constructor used to initiate the attribute
	 * @param unit The CompilationUnit of the document
	 * @param forATraiter 
	 */
	public TraitementFor(CompilationUnit unit, Statement node) {
		this.unit=unit;
		this.node = node;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void rewriteAST(CompilationUnitRewrite cuRewrite, LinkedProposalModel linkedModel) throws CoreException{
		AST ast = cuRewrite.getRoot().getAST();   //We obtain the AST from the CompilationUnit. The will be use as a factory
		final ASTRewrite rewrite = cuRewrite.getASTRewrite();  //We create a new ASTRewrite, that will contain all our modification
		
		Expression expression = null;
		Statement body = null;
		SingleVariableDeclaration parameter = null;
		
		if(node instanceof EnhancedForStatement) {
			expression = ((EnhancedForStatement) node).getExpression();
			body = ((EnhancedForStatement) node).getBody();
			parameter = ((EnhancedForStatement) node).getParameter();
		}else {
			if (node instanceof WhileStatement) {
				
				if (((WhileStatement) node).getExpression().getNodeType()==ASTNode.METHOD_INVOCATION) {
					MethodInvocation meth = (MethodInvocation) ((WhileStatement) node).getExpression();
					IMethodBinding methBind = meth.resolveMethodBinding();
					if(methBind.getName().equals("hasNext") && methBind.getDeclaringClass().getBinaryName().equals("java.util.Iterator")) {
						if( ((WhileStatement) node).getBody().getNodeType()==ASTNode.BLOCK && ((Block)((WhileStatement) node).getBody()).statements().size()>0 ) {
							ASTNode a = (ASTNode) ((Block)((WhileStatement) node).getBody()).statements().get(0); //Probleme de cast.
							if( a.getNodeType()==ASTNode.VARIABLE_DECLARATION_STATEMENT) {
								VariableDeclarationStatement v = (VariableDeclarationStatement) a;
								if( contains(methBind.getDeclaringClass().getTypeArguments(), v.getType().resolveBinding().getBinaryName()) ) {
									VariableDeclarationFragment vdf = (VariableDeclarationFragment) v.fragments().get(0);
									if(vdf.getInitializer().getNodeType()==ASTNode.METHOD_INVOCATION && ((MethodInvocation) vdf.getInitializer()).resolveMethodBinding().getName().equals("next")) {
										expression = ((MethodInvocation) ((MethodInvocation) ((WhileStatement) node).getExpression()).getExpression()).getExpression();
										parameter = ast.newSingleVariableDeclaration();
										parameter.setName(ast.newSimpleName(vdf.getName().getIdentifier()));
										parameter.setType((Type) ASTNode.copySubtree(ast, v.getType()));
										body = ast.newBlock();
										int i =0;
										for(Object o : ((Block)((WhileStatement) node).getBody()).statements()) {
											if (i!=0)((Block) body).statements().add(ASTNode.copySubtree(ast, (ASTNode) o));
											i++;
										}
										
									}else return;
								}else return;
							}else return;
						}else return;
					}else return;
				}else return;
			}else { 	
				
				if( node instanceof ForStatement) {
					ForStatement f = (ForStatement) node;
					if(f.getExpression().getNodeType()==ASTNode.INFIX_EXPRESSION && ((InfixExpression) f.getExpression()).getRightOperand().getNodeType()==ASTNode.METHOD_INVOCATION) {
						MethodInvocation m = (MethodInvocation) ((InfixExpression) f.getExpression()).getRightOperand(); //l.size()
						IMethodBinding methBind = m.resolveMethodBinding();
						
						if( methBind.getName().equals("size") && containsCollection(methBind.getDeclaringClass()) ) {
							ASTNode a = (ASTNode) ((Block)((ForStatement) node).getBody()).statements().get(0); 
							
							if( a.getNodeType()==ASTNode.VARIABLE_DECLARATION_STATEMENT) {
								VariableDeclarationStatement v = (VariableDeclarationStatement) a;
								
								if( contains(methBind.getDeclaringClass().getTypeArguments(), v.getType().resolveBinding().getBinaryName()) ) {
									VariableDeclarationFragment vdf = (VariableDeclarationFragment) v.fragments().get(0);
									
									
									if(vdf.getInitializer().getNodeType()==ASTNode.METHOD_INVOCATION && ((MethodInvocation) vdf.getInitializer()).resolveMethodBinding().getName().equals("get")) {
										expression = m.getExpression();
										parameter = ast.newSingleVariableDeclaration();
										parameter.setName(ast.newSimpleName(vdf.getName().getIdentifier()));
										parameter.setType((Type) ASTNode.copySubtree(ast, v.getType()));
										body = ast.newBlock();
										int i =0;
										for(Object o : ((Block)((ForStatement) node).getBody()).statements()) {
											if (i!=0)((Block) body).statements().add(ASTNode.copySubtree(ast, (ASTNode) o));
											i++;
										}
										System.out.println("ICICILALALALA");
									}else return;
								}else return;
							}else return;
						}else return;
					}else return;
				}else return;
			}return;
			
		}
		
		
		//We call the accept method on the AST, that will visit all the nodes, and use a personalized ASTVisitor to apply our changes
		ITypeBinding t = expression.resolveTypeBinding();
		
		//Verification du type de tableau sur lequel on veut stream
		MethodInvocation replaceMethod = detectCollectionType(ast, expression, t, rewrite);
		
		if(replaceMethod == null) return;
		//Creation of : <collection>.stream()
		
		// Method to copy an ASTNode and use it elsewhere : ASTNode.copySubtree(AST, nodeToCopy))
		TraitementForBody tfb = new TraitementForBody(node, ast);
		body.accept(tfb);
		
		TransformationMap tMap = new TransformationMap(parameter);
		if(tfb.getBody()!=null)tfb.getBody().accept(tMap);
		else body.accept(tMap);
		
		if(tMap.getMap()!=null && tMap.getTerminale()!=null) {
			//There is no If, so there isn't a filter. We create directly the forEach Method

			if(tfb.getFirst()!=null && tfb.getLast()!=null) {
				tfb.getFirst().setExpression(replaceMethod);
				tMap.getMap().setExpression(tfb.getLast());
			}else {
				if(tfb.getFirst()!=null) {
					tfb.getFirst().setExpression(replaceMethod);
					tMap.getMap().setExpression(tfb.getFirst());
				}
				else {
					tMap.getMap().setExpression(replaceMethod);
				}
			}
			
			switch (tMap.getCas()) {
			case 1:
				Assignment a = ast.newAssignment();
				a.setOperator(Assignment.Operator.PLUS_ASSIGN);
				a.setLeftHandSide((Expression) ASTNode.copySubtree(ast, tMap.getLeft()));
				a.setRightHandSide(tMap.getTerminale());
				
				ExpressionStatement st = ast.newExpressionStatement(a);
				rewrite.replace(node, st, null); //We add our modification to the record
				break;
			case 2:
				ImportDeclaration im = ast.newImportDeclaration();
				im.setName(ast.newName(new String[] {"java", "util", "stream", "Collectors"}));
				
				ListRewrite lrw = rewrite.getListRewrite(unit, CompilationUnit.IMPORTS_PROPERTY);
				lrw.insertLast(im, null);
				
				MethodInvocation res = ast.newMethodInvocation();
				res.setExpression((Expression) ASTNode.copySubtree(ast, tMap.getLeft()));
				res.arguments().add(tMap.getTerminale());
				res.setName(ast.newSimpleName("addAll"));
				
				st = ast.newExpressionStatement(res);
				rewrite.replace(node, st, null); //We add our modification to the record
				
				
				break;
			default:
				break;
			}
			

		}else {
				
			
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
			else forEachCorps.setBody(ASTNode.copySubtree(ast, body));
			forEachCorps.parameters().add(ASTNode.copySubtree(ast,parameter));
			forEach.arguments().add(forEachCorps);
			ExpressionStatement st = ast.newExpressionStatement(forEach);
			rewrite.replace(node, st, null); //We add our modification to the record
		}
		 //Return a CompilationUnitChange that all our modification
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
	private MethodInvocation detectCollectionType(AST ast, Expression expression, ITypeBinding t, ASTRewrite rewrite) {
		if(!t.isArray() && !containsCollection(t)) {
			return null;
		}else {
			if(containsCollection(t)) {
				MethodInvocation replaceMethod = ast.newMethodInvocation();
				replaceMethod.setExpression((Expression) ASTNode.copySubtree(ast,expression)); 
				replaceMethod.setName(ast.newSimpleName("stream"));
				return replaceMethod;
			}
			else {
				
				MethodInvocation replaceMethod = ast.newMethodInvocation();
				replaceMethod.arguments().add( ASTNode.copySubtree(ast,expression));
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
	
	private boolean contains(ITypeBinding[] typeArguments, String binaryName) {
		for (ITypeBinding iTypeBinding : typeArguments) {
			if (iTypeBinding.getBinaryName().equals(binaryName)) return true;
		}
		return false;
	}

}
