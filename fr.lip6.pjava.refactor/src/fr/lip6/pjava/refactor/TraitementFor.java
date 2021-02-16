package fr.lip6.pjava.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.text.edits.TextEdit;

/**
 * Class looking for Enhanced For, that we can transform to stream equivalent.
 * This class also propose the changes for the enhanced for.
 * @author teill
 *
 */
public class TraitementFor implements ICleanUpFix {
	/**
	 * CompilationUnit represents the AST, and to call a visitor on the tree
	 */
	private CompilationUnit unit;

	/**
	 * The constructor used to initiate the attribute
	 * @param unit The CompilationUnit of the document
	 */
	public TraitementFor(CompilationUnit unit) {
		this.unit=unit;
	}
	
	@Override
	public CompilationUnitChange createChange(IProgressMonitor progressMonitor) throws CoreException {
		AST ast = unit.getAST();   //We obtain the AST from the CompilationUnit. The will be use as a factory
		final ASTRewrite rewrite = ASTRewrite.create(ast);  //We create a new ASTRewrite, that will contain all our modification
		
		//We call the accept method on the AST, that will visit all the nodes, and use a personalized ASTVisitor to apply our changes
		unit.accept(new ASTVisitor() {
			@SuppressWarnings("unchecked")
			@Override
			public void endVisit(EnhancedForStatement node) {
				
				//Creation of : <collection>.stream()
				MethodInvocation replaceMethod = ast.newMethodInvocation();
				// Method to copy an ASTNode and use it elsewhere : ASTNode.copySubtree(AST, nodeToCopy))
				replaceMethod.setExpression((Expression) ASTNode.copySubtree(ast,node.getExpression())); //A eviter
				replaceMethod.setName(ast.newSimpleName("stream"));
				
				//Detection of the inside of the Enhanced For
				if(node.getBody().getNodeType()==ASTNode.BLOCK) {
					Block b = (Block) node.getBody();
					if(b.statements().size()==1) { //If there is only one things inside
						Statement insideBlock = (Statement) b.statements().get(0);
						if(insideBlock.getNodeType()==ASTNode.IF_STATEMENT) { //If the only things inside is a If_Block
							IfStatement insideIF = (IfStatement) insideBlock;
							if(insideIF.getElseStatement()==null) { //Check if the IF Block doesn't have a Else
								
								//We create the Method Invocation of the filter on the stream invocation
								MethodInvocation filterIF = ast.newMethodInvocation();
								filterIF.setExpression(replaceMethod); //We call the filter method after the stream call
								filterIF.setName(ast.newSimpleName("filter"));
								
								//We create the Lambda expression that will be inside the filter method
								LambdaExpression conditionFilter = ast.newLambdaExpression();
								conditionFilter.setBody(ASTNode.copySubtree(ast, insideIF.getExpression())); //We set the body of the lambda, same as the condition of the IF
								conditionFilter.parameters().add(ASTNode.copySubtree(ast,node.getParameter())); //We set the parameter of the lambda, same as the Variable declaration of the Enhanced For
								filterIF.arguments().add(conditionFilter); //We add the LambdaExpression to the call of the filter method
								
								//We create the method invocation for the forEach
								MethodInvocation forEach = ast.newMethodInvocation();
								forEach.setExpression(filterIF);//We call the forEach method after the filter call
								forEach.setName(ast.newSimpleName("forEach"));
								
								//We create the Lambda expression that will be inside the filter method
								LambdaExpression forEachCorps = ast.newLambdaExpression();
								forEachCorps.setBody(ASTNode.copySubtree(ast, insideIF.getThenStatement())); //We create the body of the lambdaExpression of the forEach same as the then IF
								forEachCorps.parameters().add(ASTNode.copySubtree(ast,node.getParameter())); //We set the parameter of the lambda, same as the Variable declaration of the Enhanced For
								forEach.arguments().add(forEachCorps); //We add the LambdaExpression to the call of the forEach method
								
								
								ExpressionStatement st = ast.newExpressionStatement(forEach);
								rewrite.replace(node, st, null); //We add our modification to the record
							}
						}
					}
					else {
						if(b.statements().size()>1) { //If there is more than one element present in the body of the Enhanced For
							//There is no If, so there isn't a filter. We create directly the forEach Method
							MethodInvocation forEach = ast.newMethodInvocation();
							forEach.setExpression(replaceMethod);
							forEach.setName(ast.newSimpleName("forEach"));
							
							//We create the Lambda Expression for the ForEach
							LambdaExpression forEachCorps = ast.newLambdaExpression();
							forEachCorps.setBody(ASTNode.copySubtree(ast, b));
							forEachCorps.parameters().add(ASTNode.copySubtree(ast,node.getParameter()));
							forEach.arguments().add(forEachCorps);
							ExpressionStatement st = ast.newExpressionStatement(forEach);
							rewrite.replace(node, st, null); //We add our modification to the record
						}	
					}	
				}
			}						
		});
		
		return applicationChangement(rewrite); //Return a CompilationUnitChange that all our modification
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
			e.printStackTrace();
		}
		
	    return compilationUnitChange;	
	}

}
