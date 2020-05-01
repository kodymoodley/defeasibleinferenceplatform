package net.za.cair.dip.transform;

//import java.lang.management.ManagementFactory;
//import java.util.Collections;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;

import net.za.cair.dip.model.OntologyStructure;
import net.za.cair.dip.util.Utility;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

//import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

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
	private ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	private Set<OWLClassExpression> pE;
	
	public AxiomETransformFactory(OWLReasonerFactory reasonerFactory, OntologyStructure ontologyStructure, Set<OWLClassExpression> possibleExceptions) throws OWLException{
		this.infiniteRank = new ArrayList<OWLAxiom>();
		this.ontologyStructure = ontologyStructure;
		this.reasonerFactory = reasonerFactory;
		this.pE = new HashSet<OWLClassExpression>();
		this.pE.addAll(possibleExceptions);
		e0 = new ArrayList<OWLAxiom>();
		for (OWLAxiom a: this.ontologyStructure.dBox.getAxiomsAsList()){
			e0.add(a);
		}
		
		eTransforms = new ArrayList<ArrayList<OWLAxiom>>();		
		eTransforms.add(e0);
		entailmentChecks = 0;
		recursiveCount = 0;
		noOfBrokenAxioms = 0;
		manager = OWLManager.createOWLOntologyManager();
		reasonerFactory.createNonBufferingReasoner(manager.createOntology(this.ontologyStructure.bBox.getAxioms()));
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
			if (fixedPointReached) {
				dInfinity = new ArrayList<OWLAxiom>();
				if (!tmp2.isEmpty()) {
					// We have infinite rank
					for (OWLAxiom a: tmp2) {
						infiniteRank.add(a);
					}
				}
			}
			else {
				// add etransform
				eTransforms.add(tmp2);
				pE = getExceptions(tmp2);
				// assign e0 to tmp2
				e0 = tmp2;
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
	
		/************************************************Mat(O) subs not C METHOD***********************************************************/
		Set<OWLAxiom> background = new HashSet<OWLAxiom>();
		for (OWLAxiom ax: this.ontologyStructure.bBox.getAxioms()) {
			if (!ax.isOfType(AxiomType.ABoxAxiomTypes))
				background.add(ax);
		}
		OWLOntology ontology = manager.createOntology(background);
		OWLReasoner entailChecker = reasonerFactory.createNonBufferingReasoner(ontology);
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
			}
			catch(Exception e){
				System.out.println("error doing entailment check during ranking!");
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public ArrayList<ArrayList<OWLAxiom>> getETransforms(){
		return eTransforms;
	}
}
