package net.za.cair.dip;

import net.za.cair.dip.model.OntologyStructure;
import net.za.cair.dip.model.Ranking;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

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

public class RationalClosureRankComparison{

	public Ranking computeRanking(OntologyStructure ontologyStructure) throws OWLException {
		return null;
	}
	
	public boolean checkEntailment(OWLReasonerFactory reasonerFactory, Ranking local_ranking, OWLAxiom axiom) throws OWLException{
		//int rankC = getHighestRankNumber(reasonerFactory, local_ranking, axiom);
		//OWLAxiom axiomBNF = AxiomManipulator.getBottomNF(axiom);
		//int rankCNOTD = getHighestRankNumber(reasonerFactory, local_ranking, axiomBNF);
		
		/*if ((rankC != rankCNOTD) || rankC == 0)
			return true;
		else
			return false;*/
		return false; //temporary
	}
}

