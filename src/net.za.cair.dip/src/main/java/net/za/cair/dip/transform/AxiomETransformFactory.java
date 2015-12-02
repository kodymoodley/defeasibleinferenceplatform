package net.za.cair.dip.transform;

//import java.lang.management.ManagementFactory;
//import java.util.Collections;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;

import net.za.cair.dip.model.OntologyStructure;
import net.za.cair.dip.util.Utility;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

//import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

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

public class AxiomETransformFactory{
	private ArrayList<ArrayList<OWLAxiom>> eTransforms;
	private ArrayList<OWLAxiom> e0;		
	private OWLOntologyManager manager;
	private OWLReasonerFactory reasonerFactory;
	private OWLDataFactory dataF;
	private OntologyStructure ontologyStructure;
	public ArrayList<OWLAxiom> infiniteRank;
	public int entailmentChecks;
	public int recursiveCount;
	public int noOfBrokenAxioms;
	//private ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	
	
	private Set<OWLClassExpression> pE;
	
	public AxiomETransformFactory(OWLReasonerFactory reasonerFactory, OntologyStructure ontologyStructure, Set<OWLClassExpression> possibleExceptions) throws OWLException{
		//ManagementFactory.getThreadMXBean();
		this.infiniteRank = new ArrayList<OWLAxiom>();
		this.ontologyStructure = ontologyStructure;
		this.reasonerFactory = reasonerFactory;
		this.pE = new HashSet<OWLClassExpression>();
		this.pE.addAll(possibleExceptions);
		e0 = new ArrayList<OWLAxiom>();
		for (OWLAxiom a: this.ontologyStructure.dBox.getAxiomsAsList()){
			//System.out.println(man.render(a));
			e0.add(a);
		}
		eTransforms = new ArrayList<ArrayList<OWLAxiom>>();		
		eTransforms.add(e0);
		entailmentChecks = 0;
		recursiveCount = 0;
		noOfBrokenAxioms = 0;
		manager = OWLManager.createOWLOntologyManager();
		dataF = manager.getOWLDataFactory();
	}
	
	public ArrayList<ArrayList<OWLAxiom>> generateETransforms() throws OWLException{	
		   
		int count = 0;
		int broken = 0;
		ArrayList<OWLAxiom> dInfinity = new ArrayList<OWLAxiom>();
		for (OWLAxiom a: e0){
			dInfinity.add(a);
		}
		while (!dInfinity.isEmpty()){
			ArrayList<OWLAxiom> tmp2 = null;
			tmp2 = genNextETransform(e0);
			boolean fixedPointReached = (tmp2.containsAll(e0));
			if (!fixedPointReached){
				eTransforms.add(tmp2);
				pE = getExceptions(tmp2);
			}
			while (!fixedPointReached){
				e0 = tmp2;
				tmp2 = genNextETransform(e0);
				fixedPointReached = (tmp2.containsAll(e0));
				if (!fixedPointReached){
					eTransforms.add(tmp2);
					pE = getExceptions(tmp2);
				}
			}
			dInfinity = new ArrayList<OWLAxiom>();
			for (OWLAxiom a: tmp2){
				dInfinity.add(a);
			}
			//remove from eTransforms
			//eTransforms.removeAll(Collections.singleton(tmp2));
			if (!dInfinity.isEmpty()){
				//System.out.println("broken");
				//System.out.println(dInfinity.size());
				
				//infiniteRank.addAll(dInfinity);
				eTransforms.clear();
				//System.out.println("DBox before: " + ontologyStructure.dBox.getAxioms().size());
				//System.out.println("BBox before: " + ontologyStructure.bBox.getAxioms().size());
				for (OWLAxiom axiom: dInfinity){
					//System.out.println(man.render(axiom));
					this.ontologyStructure.transferFromDBoxToBBox(axiom);
					broken++;
				}
				//System.out.println();
				//System.out.println("DBox after: " + ontologyStructure.dBox.getAxioms().size());
				//System.out.println("BBox after: " + ontologyStructure.bBox.getAxioms().size());
				
				e0 = new ArrayList<OWLAxiom>();
				//System.out.println("After moving infinite axioms to T:");
				for (OWLAxiom a: this.ontologyStructure.dBox.getAxiomsAsList()){
					//System.out.println(man.render(a));
					e0.add(a);
				}
				eTransforms.add(e0);
				//System.out.println("End");
				pE = getExceptions(e0);
				count++;
			}
			
			
		}

		noOfBrokenAxioms = broken;
		recursiveCount = count;

		return eTransforms;
	}
	
	private Set<OWLClassExpression> getExceptions(ArrayList<OWLAxiom> exceptionalAxioms){
		Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
		for (OWLAxiom a: exceptionalAxioms){
			OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
			result.add(sub.getSubClass());
		}
		return result;
	}
	
	private ArrayList<OWLAxiom> genNextETransform(ArrayList<OWLAxiom> axioms) throws OWLException{
		ArrayList<OWLAxiom> result = new ArrayList<OWLAxiom>();
		//ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
		
		//System.out.println("Before: " + axioms.size());
		/*for (OWLAxiom a: axioms){
			System.out.println(renderer.render(a));
		}*/
	
		/************************************************Mat(O) subs not C METHOD***********************************************************/
		OWLOntology ontology = manager.createOntology(this.ontologyStructure.bBox.getAxioms());
		OWLReasoner entailChecker = reasonerFactory.createReasoner(ontology);
		Set<OWLAxiom> dBox = new HashSet<OWLAxiom>();
		for (OWLAxiom a: axioms){
			dBox.add(a);
		}
		
		OWLClassExpression lhs = Utility.getConjOfMateri(dBox);	
		
		OWLClassExpression rhs = null;
		for (OWLClassExpression exc: pE){
			rhs = dataF.getOWLObjectComplementOf(exc);
			try{
				entailmentChecks++;
		
				if (entailChecker.isEntailed(dataF.getOWLSubClassOfAxiom(lhs, rhs))){
					//if (man.render(exc).equals("concept104")){
					//	System.out.println("concept104 is exceptional!");
					//}
					//System.out.println("exceptional, " + renderer.render(exc));
					//We know that RHS = C is exceptional
					//Therefore, find ALL C subs D to add to the exceptional axioms list.
					for (OWLAxiom a: axioms){
						if (a.isOfType(AxiomType.SUBCLASS_OF)){
							OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
							if (exc.equals(sub.getSubClass()))
								result.add(a);
						}
					}
				}
				else{
					//if (man.render(exc).equals("concept104")){
					//	System.out.println("concept104 is not exceptional??");
					//}
				}
			}
			catch(Exception e){
				System.out.println("error doing entailment check during ranking!");
				e.printStackTrace();
			}
		}
		
		/*System.out.println("After: ");
		for (OWLAxiom a: result){
			System.out.println(renderer.render(a));
		}*/
		//System.out.println("After: " + result.size());
		return result;
	}
}
