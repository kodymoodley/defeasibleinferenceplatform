package net.za.cair.dip.transform;

import java.util.ArrayList;
import java.util.ArrayList;

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

public class DTransformFactory{
	private ArrayList<ArrayList<OWLAxiom>> eTransforms;
	private int n;

	public DTransformFactory(ArrayList<ArrayList<OWLAxiom>> e){
		this.eTransforms = e;
		n = eTransforms.size() - 1;
	}			
	
	public ArrayList<ArrayList<OWLAxiom>> getDTransforms() throws OWLException{
		if (n == 0){
			return eTransforms;
		}
		
		ArrayList<ArrayList<OWLAxiom>> result = new ArrayList<ArrayList<OWLAxiom>>();
		ArrayList<OWLAxiom> d0 = eTransforms.get(n);
		result.add(0, d0);
		
		int i = 1;		
		
		while (i <= n){		
			ArrayList<OWLAxiom> currD = new ArrayList<OWLAxiom>();
			currD = getSubtraction(eTransforms.get(n - i), eTransforms.get(n - i + 1));
			result.add(i, currD);			
			i++;
		}
		return result;
	}
		
	private ArrayList<OWLAxiom> getSubtraction(ArrayList<OWLAxiom> e1, ArrayList<OWLAxiom> e2){							
		ArrayList<OWLAxiom> result = new ArrayList<OWLAxiom>();				
		for (OWLAxiom axiom : e1){
			if (!(contains(axiom, e2))){
				result.add(axiom);
			}
		}
		return result;
	}		
	
	private boolean contains(OWLAxiom axiom, ArrayList<OWLAxiom> arr){
		for (int j = 0; j < arr.size();j++){
			if (arr.get(j).equals(axiom))
				return true;
		}
		return false;
	}	
}

