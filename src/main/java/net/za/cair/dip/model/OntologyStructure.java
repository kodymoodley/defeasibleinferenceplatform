package net.za.cair.dip.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.za.cair.dip.util.AxiomManipulator;
import net.za.cair.dip.util.Utility;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

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
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
 */

/**
 * Author: Kody Moodley<br>
 * Centre for Artificial Intelligence Research<br>
 * UKZN and CSIR<br>
 * Date: 10-Oct-2011<br><br>
 */

public class OntologyStructure {
	
	public DBox dBox;
	public BBox bBox;
	public ABox aBox;
	public OWLOntology module;
	private OWLOntology ontology;
	private Map<OWLAxiom, OWLAxiom> originalToTransformedAxioms;
	
	public OntologyStructure(){
		aBox = new ABox();
		dBox = new DBox();
		bBox = new BBox();
		originalToTransformedAxioms = new HashMap<OWLAxiom, OWLAxiom>();
	}
	
	public OntologyStructure(OWLOntology ontology){
		this.ontology = ontology;
		dBox = new DBox();
		bBox = new BBox();
		System.out.println();
		System.out.println("HELLO!");
		ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
		Utility u = new Utility();
		for (OWLAxiom a: ontology.getLogicalAxioms()){
			if (u.isDefeasible(a)){
				System.out.println(man.render(a));//System.out.println("defeasible!");
				dBox.add(a);
			}
			else{
				bBox.add(a);
			}
		}
		System.out.println("HELLO! END!!!");
		System.out.println();
	}
	
	private void removeFromDBox(OWLAxiom a){
		if (dBox.getAxiomsAsList().contains(a) || dBox.getAxioms().contains(a))
			dBox.remove(a);
	}
	
	private void addToBBox(OWLAxiom a){
		if (!bBox.getAxiomsAsList().contains(a) && !bBox.getAxioms().contains(a))
			bBox.add(a);
	}
	
	public void transferFromDBoxToBBox(OWLAxiom a){
		ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
		removeFromDBox(a);
		addToBBox(a);
	}
	
	private OWLAxiom getSubClassVersionOfDefeasibleAxiom(OWLAxiom axiom){
		if (axiom.isOfType(AxiomType.EQUIVALENT_CLASSES)){	
			return AxiomManipulator.getDEquivToSubAxiom(axiom);
		}
		else if (axiom.isOfType(AxiomType.DISJOINT_CLASSES)){
			return AxiomManipulator.getDDisjToSubAxiom(axiom);
		}
		else{
			return axiom; 
		}
	}
	
	public void addAxiom(OWLAxiom axiom){
		if (axiom.isOfType(AxiomType.ABoxAxiomTypes)){
			aBox.add(axiom);
		}
		else{
			Utility u = new Utility();
			if (u.isDefeasible(axiom)){
				dBox.add(axiom);
			}
			else{
				bBox.add(axiom); // Everything except subclass, disjoint, equiv and abox axioms is background knowledge.
			}
		}
	}
	
	public OWLAxiom getAlternate(OWLAxiom axiom){
		return originalToTransformedAxioms.get(axiom);
	}
	
	public Map<OWLAxiom, OWLAxiom> getTransformationMap(){
		return originalToTransformedAxioms;
	}
	
	public OWLOntology asOWLOntology() throws OWLOntologyCreationException{
		Set<OWLAxiom> result = new HashSet<OWLAxiom>();
		
		for (OWLAxiom axiom: this.bBox.getAxioms()){
			result.add(axiom);
		}
		
		for (OWLAxiom axiom: this.dBox.getAxioms()){
			result.add(axiom);
		}
		
		return OWLManager.createOWLOntologyManager().createOntology(result);
	}
	
	public OWLOntology getOriginalOWLOntology(){
		return ontology;
	}
}
