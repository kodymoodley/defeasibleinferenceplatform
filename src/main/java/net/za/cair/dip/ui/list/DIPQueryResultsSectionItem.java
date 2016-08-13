package net.za.cair.dip.ui.list;

import org.protege.editor.core.ui.list.MListItem;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
/*
 * Copyright (C) 2007, University of Manchester
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
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 27-Feb-2007<br><br>
 */
public class DIPQueryResultsSectionItem implements MListItem {

    private OWLObject object;
    private OWLAxiom  axiom;


    public DIPQueryResultsSectionItem(OWLObject object, OWLAxiom axiom) {
        this.object = object;
        this.axiom  = axiom;
    }
    
    public DIPQueryResultsSectionItem(OWLObject object) {
        this.object = object;
    }



    public OWLObject getOWLObject() {
        return object;
    }
    
    public OWLAxiom getAxiom() {
		return axiom;
	}


    public String toString() {
        return object.toString();
    }


    public boolean isEditable() {
        return false;
    }


    public void handleEdit() {
    }


    public boolean isDeleteable() {
        return false;
    }


    public boolean handleDelete() {
        return false;
    }


    public String getTooltip() {
        return null;
    }
}
