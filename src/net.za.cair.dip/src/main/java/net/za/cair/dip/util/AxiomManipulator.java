package net.za.cair.dip.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;




//import org.protege.editor.owl.model.classexpression.OWLExpressionParserException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

/*
 * Copyright (C) 2011, Centre for Artificial Intelligence Research
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: Kody Moodley<br>
 * Centre for Artificial Intelligence Research<br>
 * UKZN and CSIR<br>
 * Date: 10-Oct-2011<br><br>
 */

public class AxiomManipulator {	
	private OWLOntology ontology;
	private OWLAnnotationProperty defeasibleAnnotationProperty;
	private static Set<OWLAnnotation> annos;
	private final OWLAnnotation defeasibleAnnotation = OWLManager.createOWLOntologyManager().getOWLDataFactory().getOWLAnnotation(Utility.defeasibleAnnotationProperty, OWLManager.createOWLOntologyManager().getOWLDataFactory().getOWLLiteral(true));
	
	public AxiomManipulator(){
		annos = new HashSet<OWLAnnotation>();				
		annos.add(defeasibleAnnotation);
		defeasibleAnnotationProperty = Utility.defeasibleAnnotationProperty;
	}
	
	public static AxiomManipulator getInstance(){			
		return new AxiomManipulator();
	}
	
	public AxiomManipulator(OWLOntology ontology){
		this.ontology = ontology;		
		annos = new HashSet<OWLAnnotation>();				
		annos.add(defeasibleAnnotation);
		defeasibleAnnotationProperty = Utility.defeasibleAnnotationProperty;
	}
	
	private Set<OWLAxiom> getLogicalAxioms(){
		Set<OWLAxiom> result = new HashSet<OWLAxiom>();
		Set<OWLLogicalAxiom> logicalAxioms = ontology.getLogicalAxioms();
		result.addAll(logicalAxioms);
		return result;
	}
	
	public static boolean isDefeasible(OWLAxiom axiom){
		Utility u = new Utility();
		return u.isDefeasible(axiom);
	}
	
	public static OWLAxiom getPreferentialAdditionAxiom(OWLAxiom axiom){
		if (axiom.isOfType(AxiomType.SUBCLASS_OF)){
			OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
			OWLDataFactory dataF = ontologyManager.getOWLDataFactory();
			OWLSubClassOfAxiom classAx = (OWLSubClassOfAxiom)axiom;
			OWLClassExpression lhs = classAx.getSubClass();
			OWLClassExpression rhs = dataF.getOWLObjectComplementOf(classAx.getSuperClass());
			OWLAxiom result = dataF.getOWLSubClassOfAxiom(lhs, rhs); 
			return result.getAnnotatedAxiom(annos);
		}
		else if (axiom.isOfType(AxiomType.CLASS_ASSERTION)){
			OWLClassAssertionAxiom clsAssertionAxiom = (OWLClassAssertionAxiom)axiom;
			OWLClassExpression newClsExp = OWLManager.getOWLDataFactory().getOWLObjectComplementOf(clsAssertionAxiom.getClassExpression());
			return OWLManager.getOWLDataFactory().getOWLClassAssertionAxiom(newClsExp, clsAssertionAxiom.getIndividual());
		}
		else
			return null;
	}
	
	public static OWLAxiom getPreferentialQueryAxiom(OWLAxiom axiom){
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataF = ontologyManager.getOWLDataFactory();
		OWLSubClassOfAxiom classAx = (OWLSubClassOfAxiom)axiom;
		OWLClassExpression lhs = classAx.getSubClass();
		OWLClassExpression rhs = dataF.getOWLNothing();
		Utility u = new Utility();
		if (u.isDefeasible(axiom)){
			OWLAxiom result = dataF.getOWLSubClassOfAxiom(lhs, rhs); 
			return result.getAnnotatedAxiom(annos);
		}
		else{
			return dataF.getOWLSubClassOfAxiom(lhs, rhs); 
		}
	}
	
	
	
	public static OWLAxiom getBottomNF(OWLAxiom axiom){		
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataF = ontologyManager.getOWLDataFactory();
						
		OWLSubClassOfAxiom asSubClsAxiom = (OWLSubClassOfAxiom)axiom;
		OWLClassExpression lhs = asSubClsAxiom.getSubClass();
		OWLClassExpression rhs = dataF.getOWLObjectComplementOf(asSubClsAxiom.getSuperClass());
		OWLClassExpression fLhs = dataF.getOWLObjectIntersectionOf(lhs, rhs);
		OWLSubClassOfAxiom result = dataF.getOWLSubClassOfAxiom(fLhs, dataF.getOWLNothing());
						
		return result;		
	}
	
	public static OWLClassExpression getMaterializationOfSet(Set<OWLSubClassOfAxiom> axioms){
		OWLDataFactory dataF = OWLManager.getOWLDataFactory();
		Set<OWLClassExpression> conjuncts = new HashSet<OWLClassExpression>();
		for (OWLSubClassOfAxiom a: axioms)
			conjuncts.add(getMaterialization(a));
		
		return dataF.getOWLObjectIntersectionOf(conjuncts);
	}
	
	public static OWLClassExpression getMaterialization(OWLAxiom axiom){
		OWLDataFactory dataF = OWLManager.getOWLDataFactory();
		OWLSubClassOfAxiom subClsAxiom = null;
		try{
			subClsAxiom = (OWLSubClassOfAxiom)axiom;
		}
		catch (ClassCastException cce){
			if (axiom.isOfType(AxiomType.EQUIVALENT_CLASSES)){
				OWLEquivalentClassesAxiom oeca = (OWLEquivalentClassesAxiom)axiom;
				return getMaterializationOfSet(oeca.asOWLSubClassOfAxioms());
			}
			if (axiom.isOfType(AxiomType.DISJOINT_CLASSES)){
				OWLDisjointClassesAxiom odca = (OWLDisjointClassesAxiom)axiom;
				return getMaterializationOfSet(odca.asOWLSubClassOfAxioms());
			}
			return null;
		}
		
		OWLClassExpression lhs = dataF.getOWLObjectComplementOf(subClsAxiom.getSubClass());
		return dataF.getOWLObjectUnionOf(lhs, subClsAxiom.getSuperClass());
	}
	
	public static OWLAxiom getSuperCNNF(OWLAxiom axiom){		
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataF = ontologyManager.getOWLDataFactory();
						
		OWLSubClassOfAxiom asSubClsAxiom = (OWLSubClassOfAxiom)axiom;
		OWLClassExpression lhs = asSubClsAxiom.getSubClass();
		OWLClassExpression rhs = dataF.getOWLObjectComplementOf(asSubClsAxiom.getSuperClass());
		OWLSubClassOfAxiom result = dataF.getOWLSubClassOfAxiom(lhs, rhs);
						
		return result;		
	}
	
	public OWLAxiom getPreferentialCheckAxiom(OWLAxiom axiom){
		if (isDefeasible(axiom)){
			OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
			OWLDataFactory dataF = ontologyManager.getOWLDataFactory();
			OWLSubClassOfAxiom asSubClsAxiom = (OWLSubClassOfAxiom)axiom;
			OWLClassExpression lhs = asSubClsAxiom.getSubClass();
			OWLClassExpression rhs = dataF.getOWLNothing();
			OWLAxiom result = dataF.getOWLSubClassOfAxiom(lhs, rhs);
			return result;
		}
		else{
			return axiom;
		}
	}
	
	public OWLAxiom getPreferentialAddCheckAxiom(OWLAxiom axiom){
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataF = ontologyManager.getOWLDataFactory();
		
		Set<OWLAnnotation> annos = new HashSet<OWLAnnotation>();
		annos.add(dataF.getOWLAnnotation(defeasibleAnnotationProperty, dataF.getOWLLiteral(true)));
		
		OWLSubClassOfAxiom asSubClsAxiom = (OWLSubClassOfAxiom)axiom;
		OWLClassExpression lhs = asSubClsAxiom.getSubClass();
		OWLClassExpression rhs = dataF.getOWLObjectComplementOf(asSubClsAxiom.getSuperClass());
		OWLAxiom result = dataF.getOWLSubClassOfAxiom(lhs, rhs);
		
		OWLAxiom finalResult = result.getAnnotatedAxiom(annos);
		
		return finalResult;
	}		
	
	public OWLOntology getNormalizedOntology() throws OWLOntologyCreationException{
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology resultOntology = ontologyManager.createOntology();
		ArrayList<OWLOntologyChange> axiomAdditions = new ArrayList<OWLOntologyChange>();
		for (OWLAxiom axiom: getLogicalAxioms()){
			if (isDefeasible(axiom)){
				if (axiom.getAxiomType() == AxiomType.SUBCLASS_OF){		
					axiomAdditions.add(new AddAxiom(resultOntology, axiom));
				}				
			}
			else{
				if (axiom.getAxiomType() == AxiomType.SUBCLASS_OF){
					OWLAxiom tmp = getTransformedAxiom(axiom);			
					axiomAdditions.add(new AddAxiom(resultOntology, tmp));
				}
				else{
					axiomAdditions.add(new AddAxiom(resultOntology, axiom));
				}								
			}
		}				
		ontologyManager.applyChanges(axiomAdditions);	
		return resultOntology;
	}
	
	public OWLAxiom getTransformedAxiom(OWLAxiom axiom){
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataF = ontologyManager.getOWLDataFactory();		
		OWLAxiom result = null;
			
		OWLSubClassOfAxiom sAx = (OWLSubClassOfAxiom)axiom;								
		OWLClassExpression subCls = sAx.getSubClass();				
		OWLClassExpression superCls = sAx.getSuperClass();			
		OWLClassExpression notSuperCls = dataF.getOWLObjectComplementOf(superCls);			
		Set<OWLClassExpression> conj = new HashSet<OWLClassExpression>();
			
		conj.add(subCls);
		conj.add(notSuperCls);
			
		OWLClassExpression aAndNotB = dataF.getOWLObjectIntersectionOf(conj);				
		result = dataF.getOWLSubClassOfAxiom(aAndNotB, dataF.getOWLNothing());
			
		return result;
	}
	
	public static OWLAxiom getANDAxiomCombiner(ArrayList<OWLAxiom> set){
		if (set.size() == 1){
			for (OWLAxiom a: set)
				return a;
		}
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = manager.getOWLDataFactory();
		//dl -> cr
		OWLClassExpression lhs = df.getOWLThing();
		Set<OWLClassExpression> rhsTerms = new HashSet<OWLClassExpression>();		
		for (OWLAxiom a: set){
			OWLSubClassOfAxiom sAx = (OWLSubClassOfAxiom)a;
			OWLClassExpression cLHS = sAx.getSubClass();
			OWLClassExpression cRHS = sAx.getSuperClass();
			OWLClassExpression negationCLHS = cLHS.getObjectComplementOf();
			OWLClassExpression cTerm = df.getOWLObjectUnionOf(negationCLHS, cRHS);
			
			rhsTerms.add(cTerm);
		}
		OWLClassExpression rhs = manager.getOWLDataFactory().getOWLObjectIntersectionOf(rhsTerms);
		Set<OWLAnnotation> annos = new HashSet<OWLAnnotation>();				
		annos.add(df.getOWLAnnotation(Utility.defeasibleAnnotationProperty, df.getOWLLiteral(true)));
		OWLAxiom result = df.getOWLSubClassOfAxiom(lhs, rhs).getNNF(); 
		
		return result.getAnnotatedAxiom(annos); 
	}
	
	public static OWLAxiom getORAxiomCombiner(ArrayList<OWLAxiom> set){
		if (set.size() == 1){
			for (OWLAxiom a: set)
				return a;
		}
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = manager.getOWLDataFactory();
		//cl -> dr
		Set<OWLClassExpression> lhs = new HashSet<OWLClassExpression>();
		Set<OWLClassExpression> rhs = new HashSet<OWLClassExpression>();		
		for (OWLAxiom a: set){
			OWLSubClassOfAxiom sAx = (OWLSubClassOfAxiom)a;
			lhs.add(sAx.getSubClass());
			rhs.add(sAx.getSuperClass());
		}
		
		OWLClassExpression cLhs = df.getOWLObjectIntersectionOf(lhs);
		OWLClassExpression dRhs = df.getOWLObjectUnionOf(rhs);
		OWLAxiom result = df.getOWLSubClassOfAxiom(cLhs, dRhs).getNNF();
		Set<OWLAnnotation> annos = new HashSet<OWLAnnotation>();				
		annos.add(df.getOWLAnnotation(Utility.defeasibleAnnotationProperty, df.getOWLLiteral(true)));
		
		return result.getAnnotatedAxiom(annos); 
	}
	
	public OWLAxiom getReverseTransformedAxiom(OWLAxiom axiom){
		if (!axiom.isOfType(AxiomType.SUBCLASS_OF)){
			return axiom;
		}
		Utility u = new Utility();
		if (!u.isDefeasible(axiom)){
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLSubClassOfAxiom sAx = (OWLSubClassOfAxiom)axiom;
			OWLClassExpression subCls = sAx.getSubClass();				
			OWLClassExpression superCls = sAx.getSuperClass();
			if (superCls.isBottomEntity()){
				OWLAxiom result = null;
				Set<OWLClassExpression> conjunctSet = subCls.asConjunctSet();
				Set<OWLClassExpression> lhsSet = new HashSet<OWLClassExpression>();
				OWLClassExpression lhsCls = null;
				OWLClassExpression rhsCls = null;
				for (OWLClassExpression ce: conjunctSet){
					if (ce.getClassExpressionType().equals(ClassExpressionType.OBJECT_COMPLEMENT_OF)){
						rhsCls = manager.getOWLDataFactory().getOWLObjectComplementOf(ce);		
					}
					else{
						lhsSet.add(ce);
					}
				}
				lhsCls = manager.getOWLDataFactory().getOWLObjectIntersectionOf(lhsSet);
				result = manager.getOWLDataFactory().getOWLSubClassOfAxiom(lhsCls, rhsCls);
				return result.getNNF().getAnnotatedAxiom(axiom.getAnnotations());
			}
			else{
				return axiom;
			}
		}
		else{
			Set<OWLAnnotation> a = axiom.getAnnotations();
			OWLAxiom tmp = axiom.getNNF();
			return tmp.getAnnotatedAxiom(a);
		}
	}
	
	public static OWLAxiom getDEquivToSubAxiom(OWLAxiom axiom){
		OWLEquivalentClassesAxiom eqAxiom = (OWLEquivalentClassesAxiom)axiom;
		ArrayList<OWLAxiom> axioms = new ArrayList<OWLAxiom>(eqAxiom.asOWLSubClassOfAxioms());
		return getANDAxiomCombiner(axioms);
	}
	
	public static OWLAxiom getDDisjToSubAxiom(OWLAxiom axiom){
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = manager.getOWLDataFactory();
		OWLDisjointClassesAxiom disjAxiom = (OWLDisjointClassesAxiom)axiom;
		List<OWLClassExpression> list = disjAxiom.getClassExpressionsAsList();
		OWLClassExpression lhs = list.get(0);
		OWLClassExpression rhs = list.get(1);
		OWLClassExpression fLHS = df.getOWLObjectUnionOf(lhs, rhs);
		OWLClassExpression fRHS = df.getOWLObjectComplementOf(df.getOWLObjectIntersectionOf(lhs, rhs));
		
		Set<OWLAnnotation> annos = new HashSet<OWLAnnotation>();				
		annos.add(df.getOWLAnnotation(Utility.defeasibleAnnotationProperty, df.getOWLLiteral(true)));
		
		return df.getOWLSubClassOfAxiom(fLHS, fRHS).getAnnotatedAxiom(annos);	
	}
	
}
