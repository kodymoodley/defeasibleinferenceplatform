package net.za.cair.dip.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.za.cair.dip.util.AxiomManipulator;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;

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

public class Rank implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<OWLAxiom> rank;
	private int index;
	
	public Rank(ArrayList<OWLAxiom> rank, int index){
		this.rank = new ArrayList<OWLAxiom>();
		for (OWLAxiom a: rank) {
			this.rank.add(a);
		}
		this.index = index;
	}
	
	public Rank(ArrayList<OWLAxiom> rank){
		this.rank = new ArrayList<OWLAxiom>();
		for (OWLAxiom a: rank) {
			this.rank.add(a);
		}
		index = -1;
	}
	
	public void setRank(ArrayList<OWLAxiom> rankAxioms){
		rank = new ArrayList<OWLAxiom>();
		for (OWLAxiom a: rankAxioms) {
			rank.add(a);
		}
	}
	
	public int getIndex(){
		return index;
	}
	
	public ArrayList<OWLAxiom> getAxioms(){
		return rank;
	}
	
	public Set<OWLAxiom> getAxiomsAsSet(){
		Set<OWLAxiom> result = new HashSet<OWLAxiom>();
		for (OWLAxiom a: rank){
			result.add(a);
		}
		return result;
	}
	
	public int size(){
		return rank.size();
	}
	
	public MaterializationRank materialConversion(){
		ArrayList<OWLClassExpression> result = new ArrayList<OWLClassExpression>();
		for (OWLAxiom axiom: getAxioms()){
			OWLClassExpression current = AxiomManipulator.getMaterialization(axiom);
			if (current != null)
				result.add(current);
		}
		MaterializationRank tmp = new MaterializationRank(result);
		return tmp;
	}
	
	public String toString(){
		ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
		String result = "Level " + getIndex() + ":\n";
		result += "-------------------\n";
		for (OWLAxiom a: this.rank){
			result += man.render(a) + "\n";
		}
		return result;
	}
}
