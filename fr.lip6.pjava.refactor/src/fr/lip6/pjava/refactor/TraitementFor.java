package fr.lip6.pjava.refactor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.fix.CompilationUnitRewriteOperationsFix.CompilationUnitRewriteOperation;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalModel;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;

/**
 * Class looking for Enhanced For, that we can transform to stream equivalent.
 * This class also propose the changes for the enhanced for.
 * @author Teillet & Capitanio
 *
 */
@SuppressWarnings("restriction")
public class TraitementFor extends CompilationUnitRewriteOperation {
	//Permet de savoir les import ajouté pour un fichier et de ne pas le rajouter s'il y est déjà
	private static Map<String, List<Name>> importAdded = new HashMap<>();
	/**
	 * Permet de vider la liste des imports ajoutés
	 */
	static void clear () {
		importAdded.clear();
	}
	/**
	 * Catégorie des méthodes
	 */
	private HashMap<String, Set<String>> methode;
	/**
	 * Nom du fichier
	 */
	private String name;
	/**
	 * Noeud entrain d'être traiter
	 */
	private Statement node;

	/**
	 * CompilationUnit represents the AST, and to call a visitor on the tree
	 */
	private CompilationUnit unit;
	
	/**
	 * The constructor used to initiate the attribute
	 * @param unit The CompilationUnit of the document
	 * @param method 
	 * @param forATraiter 
	 */
	public TraitementFor(CompilationUnit unit, EnhancedForStatement node, HashMap<String,Set<String>> method) {
		name = unit.getJavaElement().getElementName();
		List<Name> l = importAdded.get(name);
		if (l == null) importAdded.put(name, new ArrayList<>());
		this.unit=unit;
		this.node = node;
		this.methode = method;
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
	/**
	 * Permet de vérifier si un import n'a pas déjà été ajouté
	 * @param name nom de l'import
	 * @return si l'import est présent
	 */
	private boolean containsImport(Name name) {
		boolean test1 = false;
		for (Object o : unit.imports()) {
			ImportDeclaration im = (ImportDeclaration) o;
			if (im.getName().getFullyQualifiedName().equals(name.getFullyQualifiedName())) {
				test1 = true;
				break;
			}
				
		}
		
		for(Name n :  importAdded.get(this.name)) {
			if (n.getFullyQualifiedName().equals(name.getFullyQualifiedName())) {
				return true;
			}
		}
		return test1;
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
				Name name = ast.newName(new String[] {"java", "util", "Arrays"});
				im.setName(name);
				
				if (!containsImport(name)) {
					importAdded.get(this.name).add(name);
					ListRewrite lrw = rewrite.getListRewrite(unit, CompilationUnit.IMPORTS_PROPERTY);
					lrw.insertLast(im, null);
				}
				
				
				
				return replaceMethod;
				
			}
		}
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void rewriteAST(CompilationUnitRewrite cuRewrite, LinkedProposalModel linkedModel) throws CoreException{
		AST ast = cuRewrite.getRoot().getAST();   //We obtain the AST from the CompilationUnit. The will be use as a factory
		final ASTRewrite rewrite = cuRewrite.getASTRewrite();  //We create a new ASTRewrite, that will contain all our modification
		
		Expression expression = ((EnhancedForStatement) node).getExpression();
		Statement body = ((EnhancedForStatement) node).getBody();
		SingleVariableDeclaration parameter = ((EnhancedForStatement) node).getParameter();
		
		//Récupération du type de tableau
		ITypeBinding t = expression.resolveTypeBinding();
		
		//Vérification du type de tableau et création du machin.stream
		MethodInvocation mapTo = verifParameter2(parameter, t, ast);
		
		//Verification du type de tableau sur lequel on veut stream
		MethodInvocation replaceMethod = detectCollectionType(ast, expression, t, rewrite);
		
		if(mapTo!=null) {
			LambdaExpression lb = ast.newLambdaExpression();
			lb.setParentheses(false);
			lb.setBody(ASTNode.copySubtree(ast,parameter.getName()));
			lb.parameters().add(ASTNode.copySubtree(ast, parameter));
			
			mapTo.arguments().add(lb);
		}
		
		//Creation of : <collection>.stream()
		
		// Method to copy an ASTNode and use it elsewhere : ASTNode.copySubtree(AST, nodeToCopy))
		TraitementForBodyVisitor tfb = new TraitementForBodyVisitor(node, ast);
		body.accept(tfb);
		
		
		TransformationMapVisitor tMap = new TransformationMapVisitor(parameter);
		if(tfb.getBody()!=null)tfb.getBody().accept(tMap);
		else body.accept(tMap);
		//tMap.end();
		
		// We apply Map transformation
		if(tMap.getNbInstruction()==1 && tMap.getMap()!=null && tMap.getTerminale()!=null) {
			// replace stream by parallelStream because it is a map with sum or addAll
			MethodInvocation parallel = ast.newMethodInvocation();

			parallel.setExpression(replaceMethod);
			parallel.setName(ast.newSimpleName("parallel"));
			
			if(tfb.getFirst()!=null && tfb.getLast()!=null) {
				tfb.getFirst().setExpression(parallel);
				tMap.getMap().setExpression(tfb.getLast());
			}else {
				if(tfb.getFirst()!=null) {
					tfb.getFirst().setExpression(parallel);
					tMap.getMap().setExpression(tfb.getFirst());
				}
				else {
					tMap.getMap().setExpression(parallel);
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
				Name name = ast.newName(new String[] {"java", "util", "stream", "Collectors"});
				im.setName(name);
				
				
				if (!containsImport(name) ) {
					importAdded.get(this.name).add(name);
					ListRewrite lrw = rewrite.getListRewrite(unit, CompilationUnit.IMPORTS_PROPERTY);
					lrw.insertLast(im, null);
				}
				
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
			//replaceMethod.setName(ast.newSimpleName("parallelStream"));
			if(mapTo!=null)mapTo.setExpression(replaceMethod);
			
			//There is no If, so there isn't a filter. We create directly the forEach Method
			MethodInvocation forEach = ast.newMethodInvocation();
			
			forEach.setName(ast.newSimpleName("forEach"));
			

			
			//We create the Lambda Expression for the ForEach
			boolean parallelizable;
			LambdaExpression forEachCorps = ast.newLambdaExpression();
			forEachCorps.setParentheses(false);
			if(tfb.getBody()!=null) {
				BodyParallelizableVisitor bP = new BodyParallelizableVisitor(methode);
				tfb.getBody().accept(bP);
				parallelizable = bP.isParallelizable(); 
				forEachCorps.setBody(ASTNode.copySubtree(ast, tfb.getBody())); //filter qui est appliquer
			}else {
				//pas de filter, juste for each
				//Vérification si le corps est un block, sinon on mets dans un Block
				if ( !(body instanceof Block) ){
					Block b = ast.newBlock();
					b.statements().add(ASTNode.copySubtree(ast, body));
					body = b;
				}
				BodyParallelizableVisitor bP = new BodyParallelizableVisitor(methode);
				body.accept(bP);
				parallelizable = bP.isParallelizable();
				forEachCorps.setBody(ASTNode.copySubtree(ast, body));
			}
			
			if(tfb.getFirst()!=null && tfb.getLast()!=null) {
				if(parallelizable) {
					MethodInvocation parallel = ast.newMethodInvocation();
					if(mapTo!=null)parallel.setExpression(mapTo);
					else parallel.setExpression(replaceMethod);
					parallel.setName(ast.newSimpleName("parallel"));
					tfb.getFirst().setExpression(parallel);
				}else {
					if(mapTo!=null)tfb.getFirst().setExpression(mapTo);
					else tfb.getFirst().setExpression(replaceMethod);
				}
				forEach.setExpression(tfb.getLast());
			}else {
				if(tfb.getFirst()!=null) {
					if (parallelizable) {
						MethodInvocation parallel = ast.newMethodInvocation();
						if(mapTo!=null)parallel.setExpression(mapTo);
						else parallel.setExpression(replaceMethod);
						parallel.setName(ast.newSimpleName("parallel"));
						tfb.getFirst().setExpression(parallel);
					}else {
						if(mapTo!=null)tfb.getFirst().setExpression(mapTo);
						else tfb.getFirst().setExpression(replaceMethod);
					}
					forEach.setExpression(tfb.getFirst());
				}
				else {
					if (parallelizable) {
						MethodInvocation parallel = ast.newMethodInvocation();
						if(mapTo!=null)parallel.setExpression(mapTo);
						else parallel.setExpression(replaceMethod);
						parallel.setName(ast.newSimpleName("parallel"));
						forEach.setExpression(parallel);
					}else {
						if(mapTo!=null)forEach.setExpression(mapTo);
						else forEach.setExpression(replaceMethod);
					}
				}
			}
			forEachCorps.parameters().add(ASTNode.copySubtree(ast,parameter));
			forEach.arguments().add(forEachCorps);
			ExpressionStatement st = ast.newExpressionStatement(forEach);
			rewrite.replace(node, st, null); //We add our modification to the record
		}
		 //Return a CompilationUnitChange that all our modification
	}

	@Override
	public String toString() {
		return "TraitementFor [unit=" + unit + ", node=" + node + ", name=" + name + "]";
	}
	
	private MethodInvocation verifParameter2(SingleVariableDeclaration parameter, ITypeBinding typeFor, AST ast) {
		Type t = parameter.getType();
		ITypeBinding b = typeFor.getElementType();
		if (t.isPrimitiveType() && !(typeFor.isArray() && !typeFor.isPrimitive() && !b.getBinaryName().equals(((PrimitiveType) t).toString()))) {
			MethodInvocation mapTo = ast.newMethodInvocation();
			PrimitiveType pT = (PrimitiveType) t;
			switch (pT.toString()) {
			case "int":
				mapTo.setName(ast.newSimpleName("mapToInt"));
				break;
			case "char":
				mapTo.setName(ast.newSimpleName("mapToChar"));
				break;
			case "boolean":
				mapTo.setName(ast.newSimpleName("mapToBool"));
				break;
			case "short":
				mapTo.setName(ast.newSimpleName("mapToShort"));
				break;
			case "long":
				mapTo.setName(ast.newSimpleName("mapToLong"));
				break;
			case "float":
				mapTo.setName(ast.newSimpleName("mapToFloat"));
				break;
			case "double":
				mapTo.setName(ast.newSimpleName("mapToDouble"));
				break;
			case "byte":
				mapTo.setName(ast.newSimpleName("mapToByte"));
				break;
			default:
				return mapTo;
			}
			return mapTo;
			
		}else {
			return null;
		}
	}
	
	
}
