package net.za.cair.dip.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;

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

public class BBox { // NO_UCD (use default)
	
	private Set<OWLAxiom> axioms;
	private ArrayList<OWLAxiom> axiomsAsList;
	
	public BBox(){
		this.axioms = new HashSet<OWLAxiom>();
		this.axiomsAsList = new ArrayList<OWLAxiom>();  
	}
	
	public BBox(Set<OWLAxiom> axioms){
		this.axioms = axioms;
	}
	
	public BBox(ArrayList<OWLAxiom> axiomsAsList){ // NO_UCD (unused code)
		this.axiomsAsList = axiomsAsList;
	}
	
	public void add(OWLAxiom axiom){
		this.axioms.add(axiom);
		this.axiomsAsList.add(axiom);
	}
	
	public void addAll(Set<OWLAxiom> axioms){ // NO_UCD (unused code)
		this.axioms.addAll(axioms);
		this.axiomsAsList.addAll(axioms);
	}
	
	public void addAll(ArrayList<OWLAxiom> axioms){ // NO_UCD (unused code)
		this.axioms.addAll(axioms);
		this.axiomsAsList.addAll(axioms);
	}
	
	public Set<OWLAxiom> getAxioms(){
		return axioms;
	}
	
	public ArrayList<OWLAxiom> getAxiomsAsList(){
		return axiomsAsList;
	}
}
