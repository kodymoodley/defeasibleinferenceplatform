package net.za.cair.dip.ui.listener;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.za.cair.dip.ui.list.QueryList;
import net.za.cair.dip.util.Utility;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;

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

public class QuerySelectionListener implements ListSelectionListener{
	
	public void valueChanged(ListSelectionEvent e) {
		//if (e.getValueIsAdjusting()){
			QueryList ql = (QueryList)e.getSource();
			//if ql.
			OWLAxiom selectedAxiom = ql.getSelectedAxiom(); 
			if (selectedAxiom != null){
				System.out.println("selection has changed!");
				System.out.println(ql.modelManager.getRendering(selectedAxiom));
				OWLModelManager m = ql.modelManager;
				for (OWLAnnotationAssertionAxiom anno: m.getActiveOntology().getAnnotationAssertionAxioms(Utility.rampSelectedQueryIRI)){
					m.getOWLOntologyManager().removeAxiom(m.getActiveOntology(), anno);
				}
			
				OWLDataFactory df = m.getOWLDataFactory();
				OWLAnnotationAssertionAxiom axiom = null;
				Utility u = new Utility();
				if (u.isDefeasible(selectedAxiom))
					axiom = df.getOWLAnnotationAssertionAxiom(Utility.rampSelectedQueryProperty, Utility.rampSelectedQueryProperty.getIRI(), df.getOWLLiteral(m.getRendering(selectedAxiom)+"*",""));
				else
					axiom = df.getOWLAnnotationAssertionAxiom(Utility.rampSelectedQueryProperty, Utility.rampSelectedQueryProperty.getIRI(), df.getOWLLiteral(m.getRendering(selectedAxiom),""));
				
				m.getOWLOntologyManager().addAxiom(m.getActiveOntology(), axiom);
			}
		//}
    }
}
