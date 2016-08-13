package net.za.cair.dip.ui.renderer;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import net.za.cair.dip.ui.list.DIPQueryResultsSectionItem;
import net.za.cair.dip.util.ManchesterOWLSyntaxOWLObjectRendererImpl;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

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
 * Authors: Kody Moodley, Matthew Horridge<br>
 * Centre for Artificial Intelligence Research, The University Of Manchester<br>
 * Knowledge Representation and Reasoning Group, Bio-Health Informatics Group<br>
 * Date: July-2013, 24-Jan-2007<br><br>
 */

public class IndividualsRenderer extends JLabel implements ListCellRenderer {
	private static final long serialVersionUID = 1L;
	private OWLEditorKit owlEditorKit;
	private ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	
	public IndividualsRenderer(OWLEditorKit owlEditorKit){
		this.owlEditorKit = owlEditorKit;
	}
	
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		
		setPreferredSize(new Dimension(250, 20));
        /*if (value instanceof OWLNamedIndividual) {
     	   OWLNamedIndividual oni = (OWLNamedIndividual)value;
            setText(modelManager.getRendering(oni));
        }*/
        //if (value instanceof OWLClassExpression) {
		DIPQueryResultsSectionItem item = (DIPQueryResultsSectionItem)value;
        setText(man.render(item.getOWLObject()));
         //}
        return this;
	}
}
