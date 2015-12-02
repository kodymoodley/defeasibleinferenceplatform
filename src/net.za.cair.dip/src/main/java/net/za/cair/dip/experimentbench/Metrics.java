package net.za.cair.dip.experimentbench;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

public class Metrics {
	
	private static double avgNestingDepth;
	private static double avgConjunctLength;
	private static double avgDisjunctLength;
	
	private static int getMaxDisjunctLength(OWLOntology ontology){
		
		Set<OWLAxiom> tBoxAxioms = new HashSet<OWLAxiom>();
		tBoxAxioms.addAll(ontology.getTBoxAxioms(false));
		int maxDisjuncts = 0;
		int count = 0;
		int totalDisjuncts = 0;
		for (OWLAxiom a: tBoxAxioms){
			Set<OWLClassExpression> classExpressions = a.getNestedClassExpressions();
			for (OWLClassExpression c: classExpressions){
				
				if (c.isAnonymous()){
					if (c.getClassExpressionType().compareTo(ClassExpressionType.OBJECT_UNION_OF) == 0){
						count++;
						int disjunctSize = c.asDisjunctSet().size();totalDisjuncts += disjunctSize;
						if (disjunctSize > maxDisjuncts)
							maxDisjuncts = disjunctSize;
					}
				}
			}
		}
		if (totalDisjuncts > 0)
			avgDisjunctLength = totalDisjuncts/count;
		else
			avgDisjunctLength = 0.0;
		return maxDisjuncts;
	}
	
	private static double getAvgDisjunctLength(){
		return avgDisjunctLength;
	}
	
	private static int getMaxConjunctLength(OWLOntology ontology){
		
		Set<OWLAxiom> tBoxAxioms = new HashSet<OWLAxiom>();
		tBoxAxioms.addAll(ontology.getTBoxAxioms(false));
		int maxConjuncts = 0;
		int count = 0;
		int totalConjuncts = 0;
		for (OWLAxiom a: tBoxAxioms){
			Set<OWLClassExpression> classExpressions = a.getNestedClassExpressions();
			for (OWLClassExpression c: classExpressions){
				
				if (c.isAnonymous()){
					if (c.getClassExpressionType().compareTo(ClassExpressionType.OBJECT_INTERSECTION_OF) == 0){
						count++;
						int conjunctSize = c.asConjunctSet().size();totalConjuncts += conjunctSize;
						if (conjunctSize > maxConjuncts)
							maxConjuncts = conjunctSize;
					}
				}
			}
		}
		if (totalConjuncts > 0)
			avgConjunctLength = totalConjuncts/count;
		else
			avgConjunctLength = 0.0;
		return maxConjuncts;
	}
	
	private static double getAvgConjunctLength(){
		return avgConjunctLength;
	}
	
	private static int getMaxNestingDepth(OWLOntology ontology){
		ManchesterOWLSyntaxOWLObjectRendererImpl rendering = new ManchesterOWLSyntaxOWLObjectRendererImpl();
		Set<OWLAxiom> tBoxAxioms = new HashSet<OWLAxiom>();
		tBoxAxioms.addAll(ontology.getTBoxAxioms(false));
		int maxNestDepth = 0;
		int count = 0;
		int totalNestingCount = 0;
		for (OWLAxiom a: tBoxAxioms){
			
			if (a.isOfType(AxiomType.SUBCLASS_OF)){
				
				OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
				OWLClassExpression lhs = sub.getSubClass();
				OWLClassExpression rhs = sub.getSuperClass();
				int lhsDepth = lhs.getNestedClassExpressions().size();totalNestingCount += lhsDepth;
				int rhsDepth = rhs.getNestedClassExpressions().size();totalNestingCount += rhsDepth;
				
				if (lhsDepth >= 1){
					count++;
					/*System.out.println("LHS: " + rendering.render(a));
					int counter = 1;
					for (OWLClassExpression c: lhs.getNestedClassExpressions()){
						System.out.println(counter + ": " + rendering.render(c));
						counter++;
					}*/
				}
				if (rhsDepth >= 1){
					count++;
					/*System.out.println("RHS: " + rendering.render(a));
					int counter = 1;
					for (OWLClassExpression c: rhs.getNestedClassExpressions()){
						System.out.println(counter + ": " + rendering.render(c));
						counter++;
					}*/
					
				}
				
				if (lhsDepth >= rhsDepth){
					if (lhsDepth > maxNestDepth)
						maxNestDepth = lhsDepth;
				}
				else{
					if (rhsDepth > maxNestDepth)
						maxNestDepth = rhsDepth;
				}
			}
			if (a.isOfType(AxiomType.DISJOINT_CLASSES)){
				
				OWLDisjointClassesAxiom disj = (OWLDisjointClassesAxiom)a;
				boolean done = false;
				Iterator<OWLSubClassOfAxiom> iter = disj.asOWLSubClassOfAxioms().iterator();
				while (!done){
					OWLSubClassOfAxiom sub = iter.next();
					done = true;
					OWLClassExpression lhs = sub.getSubClass();
					OWLClassExpression rhs = sub.getSuperClass();
					int lhsDepth = lhs.getNestedClassExpressions().size();totalNestingCount += lhsDepth;
					int rhsDepth = rhs.getNestedClassExpressions().size();totalNestingCount += rhsDepth;
					
					
					
					if (lhsDepth >= 1){
						count++;
						/*System.out.println("LHS: " + rendering.render(a));
						int counter = 1;
						for (OWLClassExpression c: lhs.getNestedClassExpressions()){
							System.out.println(counter + ": " + rendering.render(c));
							counter++;
						}*/
					}
					if (rhsDepth >= 1){
						count++;
						/*System.out.println("RHS: " + rendering.render(a));
						int counter = 1;
						for (OWLClassExpression c: rhs.getNestedClassExpressions()){
							System.out.println(counter + ": " + rendering.render(c));
							counter++;
						}*/
					}
					
					if (lhsDepth >= rhsDepth){
						if (lhsDepth > maxNestDepth)
							maxNestDepth = lhsDepth;
					}
					else{
						if (rhsDepth > maxNestDepth)
							maxNestDepth = rhsDepth;
					}
				}
			}
			if (a.isOfType(AxiomType.EQUIVALENT_CLASSES)){
				count++;
				OWLEquivalentClassesAxiom equiv = (OWLEquivalentClassesAxiom)a;
				boolean done = false;
				Iterator<OWLSubClassOfAxiom> iter = equiv.asOWLSubClassOfAxioms().iterator();
				while (!done){
					OWLSubClassOfAxiom sub = iter.next();
					done = true;
					OWLClassExpression lhs = sub.getSubClass();
					OWLClassExpression rhs = sub.getSuperClass();
					int lhsDepth = lhs.getNestedClassExpressions().size();totalNestingCount += lhsDepth;
					int rhsDepth = rhs.getNestedClassExpressions().size();totalNestingCount += rhsDepth;
					
					if (lhsDepth >= 1){
						count++;
						/*System.out.println("LHS: " + rendering.render(a));
						int counter = 1;
						for (OWLClassExpression c: lhs.getNestedClassExpressions()){
							System.out.println(counter + ": " + rendering.render(c));
							counter++;
						}*/
					}
					if (rhsDepth >= 1){
						count++;
						/*System.out.println("RHS: " + rendering.render(a));
						int counter = 1;
						for (OWLClassExpression c: rhs.getNestedClassExpressions()){
							System.out.println(counter + ": " + rendering.render(c));
							counter++;
						}*/
					}
					
					if (lhsDepth >= rhsDepth){
						if (lhsDepth > maxNestDepth)
							maxNestDepth = lhsDepth;
					}
					else{
						if (rhsDepth > maxNestDepth)
							maxNestDepth = rhsDepth;
					}
				}
			}
		}
		if (totalNestingCount > 0)
			avgNestingDepth = totalNestingCount/(count);
		else
			avgNestingDepth = 0.0;
		
		return maxNestDepth;
	}
	
	private static double getAvgNestingDepth(){
		return avgNestingDepth;
	}
	
	public static void main(String[] args) throws FileNotFoundException, OWLOntologyCreationException{
		avgNestingDepth = 0.0;
		avgConjunctLength = 0.0;
		avgDisjunctLength = 0.0;
		
		int [] classCounts = new int[35];
		int [] roleCounts = new int[35];
		int [] tboxCounts = new int[35];
		int [] rboxCounts = new int[35];
		int [] maxC = new int[35];
		int [] maxD = new int[35];
		int [] maxNC = new int[35];
		double [] avgC = new double[35];
		double [] avgD = new double[35];
		double [] avgNC = new double[35];
		
        for (int j = 1; j <= 1;j++){
			System.out.println();
			System.out.println((j*10) + " percent");
			System.out.println("------------------");
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			for (int i = 1;i <= 35;i++){		
				avgNestingDepth = 0.0;
				avgConjunctLength = 0.0;
				avgDisjunctLength = 0.0;
				
				System.out.println("Ontology " + i + ": ");				
				File ontologyFile = new File("test/"+(j*10)+"/Ontology"+i+"/ontology"+i+".owl");							
				OWLOntology ontology = manager.loadOntologyFromOntologyDocument(ontologyFile);			
		    	
				classCounts[i-1] = ontology.getClassesInSignature().size();		
				roleCounts[i-1] = ontology.getObjectPropertiesInSignature().size();				            
				tboxCounts[i-1] = ontology.getTBoxAxioms(true).size();				            
				rboxCounts[i-1] = ontology.getRBoxAxioms(true).size();
				
		        maxC[i-1] = getMaxConjunctLength(ontology);
		        maxD[i-1] = getMaxDisjunctLength(ontology);
		        maxNC[i-1] = getMaxNestingDepth(ontology);
		        
		        avgC[i-1] = getAvgConjunctLength();
		        avgD[i-1] = getAvgDisjunctLength();
		        avgNC[i-1] = getAvgNestingDepth();
		        
           }
		   System.out.println("Class counts:");
		   for (int i = 0;i < 35;i++){
			 System.out.println(classCounts[i]);
		   }
		   
		   System.out.println("Role counts:");
		   for (int i = 0;i < 35;i++){
			 System.out.println(roleCounts[i]);
		   }
		   
		   System.out.println("TBox counts:");
		   for (int i = 0;i < 35;i++){
			 System.out.println(tboxCounts[i]);
		   }
		   
		   System.out.println("RBox counts:");
		   for (int i = 0;i < 35;i++){
			 System.out.println(rboxCounts[i]);
		   }
		   
		   System.out.println("MaxC counts:");
		   for (int i = 0;i < 35;i++){
			 System.out.println(maxC[i]);
		   }
		   
		   System.out.println("MaxD counts:");
		   for (int i = 0;i < 35;i++){
			 System.out.println(maxD[i]);
		   }
		   
		   System.out.println("MaxNC counts:");
		   for (int i = 0;i < 35;i++){
			 System.out.println(maxNC[i]);
		   }
		   
		   System.out.println("AvgC counts:");
		   for (int i = 0;i < 35;i++){
			 System.out.println(avgC[i]);
		   }
		   
		   System.out.println("AvgD counts:");
		   for (int i = 0;i < 35;i++){
			 System.out.println(avgD[i]);
		   }
		   
		   System.out.println("AvgNC counts:");
		   for (int i = 0;i < 35;i++){
			 System.out.println(avgNC[i]);
		   }
        }
        System.out.println("FINISHED!"); 
    }	

}
