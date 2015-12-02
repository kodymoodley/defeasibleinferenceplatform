package net.za.cair.dip.ui.view;

import java.awt.BorderLayout;
import javax.swing.JScrollPane;

import net.za.cair.dip.ui.list.QueryList;
import net.za.cair.dip.util.Utility;

import org.protege.editor.owl.ui.view.AbstractActiveOntologyViewComponent;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;


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

public class DipQueriesViewComponent extends AbstractActiveOntologyViewComponent {
	private static final long serialVersionUID = 2478857043399890008L;	
	private QueryList qlList;
	public OWLAxiom selectedAxiom;
	
	@Override
	protected void disposeOntologyView() {	
	}

	@Override
	protected void initialiseOntologyView() throws Exception {									
		qlList = new QueryList(getOWLEditorKit(), getOWLModelManager());
		Utility u = new Utility();
		qlList.setAxioms(u.getQueries(getOWLModelManager(), getOWLModelManager().getActiveOntology()));
		setLayout(new BorderLayout());
		add(new JScrollPane(qlList));
	}

	@Override
	protected void updateView(OWLOntology activeOntology) throws Exception {
		Utility u = new Utility();
		qlList.setAxioms(u.getQueries(getOWLModelManager(), activeOntology));
	}
}
