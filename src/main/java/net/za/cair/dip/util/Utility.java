package net.za.cair.dip.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.za.cair.dip.model.Rank;
import net.za.cair.dip.model.Ranking;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.classexpression.OWLExpressionParserException;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
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

public class Utility {
	private static OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
	private static IRI defeasibleIRI = IRI.create("http://cair.za.net/defeasible");
	
	private static IRI rampQueryIRI = IRI.create("http://www.cair.za.net/ontologyAnnotationProperties/rampQueries");
	public static IRI rampSelectedQueryIRI = IRI.create("http://www.cair.za.net/ontologyAnnotationProperties/rampSelectedQuery");
	
	public static OWLAnnotationProperty defeasibleAnnotationProperty = ontologyManager.getOWLDataFactory().getOWLAnnotationProperty(defeasibleIRI); 
	public static OWLAnnotationProperty rampQueryProperty = ontologyManager.getOWLDataFactory().getOWLAnnotationProperty(rampQueryIRI);
	public static OWLAnnotationProperty rampSelectedQueryProperty = ontologyManager.getOWLDataFactory().getOWLAnnotationProperty(rampSelectedQueryIRI);
	
	private final IRI consistencyIRI = IRI.create("http://cair.za.net/consistent");
	private final IRI rankIRI = IRI.create("http://www.cair.za.net/rank");
	    
	public final OWLAnnotationProperty rankAnnotationProperty = ontologyManager.getOWLDataFactory().getOWLAnnotationProperty(rankIRI); 
	public final OWLAnnotationProperty consistencyAnnotationProperty = ontologyManager.getOWLDataFactory().getOWLAnnotationProperty(consistencyIRI); 
	
    private String patternFreeString = "";
    private int startMatch = 0;
    private int endMatch = 0;
    private ManchesterOWLSyntaxOWLObjectRendererImpl man;// = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	
	public Utility() {	
		man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	}
	
	public boolean isDefeasible(OWLAxiom axiom){	
		//System.out.println("culprit: " + man.render(axiom));
		Set<OWLAnnotation> annos = axiom.getAnnotations();
		for (OWLAnnotation anno: annos){
			//System.out.println("culprit: " + man.render(anno));
			if ((man.render(anno)).contains("defeasible"))
				return true;
		}
	    return false;//!axiom.getAnnotations(defeasibleAnnotationProperty).isEmpty();	    
	}
	
	public boolean hasRankAnnotation(OWLAxiom axiom){	
		Set<OWLAnnotation> annos = axiom.getAnnotations();
		if (annos.isEmpty()){
			return false;
		}
		
		for (OWLAnnotation anno: annos){
			if ((man.render(anno)).contains("rank"))
				return true;
		}
	    return false;//!axiom.getAnnotations(defeasibleAnnotationProperty).isEmpty();	    
	}
	
	public int getRankIndex(OWLAxiom axiom){	
		Set<OWLAnnotation> annos = axiom.getAnnotations();
		
		for (OWLAnnotation anno: annos){
			if ((man.render(anno)).contains("rank")){
				System.out.println(anno.getValue());
				String s = anno.getValue().toString();
				int i = 0;
				String str = "";
				while ((i < s.length())){
					if (s.charAt(i) != '\"' && s.charAt(i) != '^' && s.charAt(i) != ':' && !Character.isLetter(s.charAt(i)))
						str += s.charAt(i);
					i++;
				}
				
				//s.replaceAll("[^^xsd:integer]", "");
				return Integer.parseInt(str);
			}
				
		}
		return -1;
	   // return false;//!axiom.getAnnotations(defeasibleAnnotationProperty).isEmpty();	    
	}
	
	public Ranking mergeRanks(ArrayList<Rank> nranks, int lowest, int highest){
		ArrayList<Rank> ranks = new ArrayList<Rank>();ranks.addAll(nranks);
		System.out.println("lowest: " + lowest + ", highest: " + highest);
		System.out.println();
	
		ArrayList<Rank> result = new ArrayList<Rank>();
		for (int i = lowest; i <= highest;i++){
			ArrayList<OWLAxiom> axioms = new ArrayList<OWLAxiom>();
			for (Rank rank: ranks){
				if (rank.getIndex() == i){
					axioms.addAll(rank.getAxioms());
				}
			}
			if (axioms.isEmpty()){
				return new Ranking();
			}
			result.add(new Rank(axioms, i));
		}
		
	    System.out.println("--before--");
	    System.out.println();
	    
		for (Rank r: result) {
		    System.out.println(r.getIndex() + ":");
		    for (OWLAxiom a: r.getAxioms()) {
		    	System.out.println(man.render(a));
		    }
		}
		
		System.out.println();
		
		/*** Reverse ranking order ****/
		/*int n = result.size();
	    for (int i = 0; i <= Math.floor((n-2)/2);i++){
	         Rank tmp = result.get(i+1);
	         result.set(i+1, result.get(n - i));
	         result.set(n - i, tmp);
		}
	    
	    System.out.println("--after--");
	    System.out.println();
	    
	    for (Rank r: result) {
		    System.out.println(r.getIndex() + ":");
		    for (OWLAxiom a: r.getAxioms()) {
		    	System.out.println(man.render(a));
		    }
		}
	    
		System.out.println();*/
		
		
	
		return new Ranking(result);
	}
	
	public boolean isQueryAnnotation(OWLAnnotation anno){
	  return anno.getProperty().getIRI().equals(rampQueryIRI);	    
	}
	
	public String getRidOfWhiteSpace(String str){
		String result = "";
		for (int i = 0;i < str.length();i++){
			if (str.charAt(i) != ' '){
				result += str.charAt(i);
			}
		}
		return result;
	}
	
	public String removeColon(String str){
		String result = "";
		for (int i = 0; i < str.length();i++){
			char tmp = str.charAt(i);
			if (tmp != ':'){
				result += tmp;
			}
		}
		return result;
	}
	
	
	public String addColon(String str){
		String result = "";
		StringTokenizer tokenizer = new StringTokenizer(str);
		while(tokenizer.hasMoreTokens()){
			String tmp = tokenizer.nextToken();
			if (tmp.equals("SubClassOf") || tmp.equals("Type")){
				tmp += ":";
			}
			if (tokenizer.hasMoreTokens())
				result += tmp + " ";
			else
				result += tmp;
		}
		return result;
	}
	
	public String getRidOfJunk(String str){
		String result = "";
		for (int i = 0;i < str.length();i++){
			//if ((str.charAt(i) != '\u005E') && (str.charAt(i) != ':') && (str.charAt(i) != '\\') && (str.charAt(i) != '\"')){
			if ((str.charAt(i) != '\u005E') && (str.charAt(i) != '\\') && (str.charAt(i) != '\"')){
				result += str.charAt(i);
			}
		}
		
		Pattern pattern = Pattern.compile("xsd");
		Matcher matcher = pattern.matcher(result);
		String output1 = matcher.replaceAll("");
		Pattern pattern2 = Pattern.compile("string");
		Matcher matcher2 = pattern2.matcher(output1);
		String output2 = matcher2.replaceAll("");
		Pattern pattern3 = Pattern.compile(";;");
		Matcher matcher3 = pattern3.matcher(output2);
		String output3 = matcher3.replaceAll(";");
		return output3;
	}
	
	public String getRidOfSpecialChars(String str){
		String result = "";
		for (int i = 0;i < str.length();i++){
			if ((str.charAt(i) != '|') && (str.charAt(i) != '\"')){
				result += str.charAt(i);
			}
		}
		return result;
	}
	
	public String getRidOfStar(String str){
		String result = "";
		for (int i = 0;i < str.length();i++){
			if (str.charAt(i) != '*'){
				result += str.charAt(i);
			}
		}
		return result;
	}
	
	public void print(OWLObject obj){
		System.out.print(man.render(obj));
	}
	
	public void println(OWLObject obj){
		System.out.println(man.render(obj));
	}
	
	private boolean foundMatch(String pattern, String str){
		boolean contains = false;
		if (str.contains(pattern)){
			contains = true;
			startMatch = str.indexOf(pattern);
			endMatch = startMatch + (pattern.length()-1);
			for (int i = 0; i < str.length();i++){
				if (i <= startMatch || i > endMatch){
					patternFreeString += str.charAt(i);
				}
			}
		}
		return contains;
	}
	
	public void printSequences(ArrayList<Object[]> individualSequences){
		System.out.println("Individual-cluster sequences:");
		for (Object[] seq: individualSequences){
			for (Object element: seq){
				System.out.print(man.render((OWLNamedIndividual)element) + ", ");
			}
			System.out.println();
		}
	}
	
	public boolean isConsistent(OWLReasonerFactory reasonerFactory, Set<OWLAxiom> aBox, Set<OWLAxiom> tBox) throws OWLOntologyCreationException{
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();axioms.addAll(aBox);axioms.addAll(tBox);
		OWLOntology tmpOntology = OWLManager.createOWLOntologyManager().createOntology(axioms);
		return reasonerFactory.createReasoner(tmpOntology).isConsistent();
	}
	
	public boolean containsAxiom(OWLReasonerFactory reasonerFactory, Set<OWLAxiom> axioms, OWLAxiom axiom) throws OWLOntologyCreationException{
		Set<OWLAxiom> allAxioms = new HashSet<OWLAxiom>();
		for (OWLAxiom a: axioms){
			allAxioms.add(a);
		}
		OWLOntology tmpOntology = OWLManager.createOWLOntologyManager().createOntology(allAxioms);
		return reasonerFactory.createReasoner(tmpOntology).isEntailed(axiom);
	}
	
	public static <T> boolean contains(Set<T> set, T element){
		for (T current: set){
			if (current.equals(element))
				return true;
		}
		return false;
	}
	
	private static int getRankValue(OWLAxiom axiom, Ranking ranking){
		for (int i = ranking.size()-1; i >= 0;i--){
			if (ranking.get(i).getAxiomsAsSet().contains(axiom))
				return i;
		}
		return -1;
	}
	
	/*public static Set<Set<Integer>> combinations2(ArrayList<Integer> groupSize, int k) {

	    Set<Set<Integer>> allCombos = new HashSet<Set<Integer>> ();
	    // base cases for recursion
	    if (k == 0) {
	        // There is only one combination of size 0, the empty team.
	        allCombos.add(new HashSet<Integer>());
	        return allCombos;
	    }
	    if (k > groupSize.size()) {
	        // There can be no teams with size larger than the group size,
	        // so return allCombos without putting any teams in it.
	        return allCombos;
	    }

	    // Create a copy of the group with one item removed.
	    ArrayList<Integer> groupWithoutX = new ArrayList<Integer> (groupSize);
	    Integer x = groupWithoutX.remove(groupWithoutX.size()-1);

	    Set<Set<Integer>> combosWithoutX = combinations2(groupWithoutX, k);
	    Set<Set<Integer>> combosWithX = combinations2(groupWithoutX, k-1);
	    for (Set<Integer> combo : combosWithX) {
	        combo.add(x);
	    }
	    allCombos.addAll(combosWithoutX);
	    allCombos.addAll(combosWithX);
	    return allCombos;
	}*/
	
	public static OWLAxiom getAxiomFromCombination(Set<Set<OWLAxiom>> combination){
		ArrayList<OWLAxiom> forORCombining = new ArrayList<OWLAxiom>();
		for (Set<OWLAxiom> clause: combination){
			ArrayList<OWLAxiom> ArrayListClause = new ArrayList<OWLAxiom>();
			ArrayListClause.addAll(clause);
			OWLAxiom tmp = AxiomManipulator.getInstance().getANDAxiomCombiner(ArrayListClause);
			forORCombining.add(tmp);
		}
		return AxiomManipulator.getInstance().getORAxiomCombiner(forORCombining);
	}

	public ArrayList<OWLAxiom> getDefeasibleAxioms(Set<OWLAxiom> axioms) {
		System.out.println("total: " + axioms.size());
		ArrayList<OWLAxiom> def = new ArrayList<OWLAxiom>();
		for (OWLAxiom a: axioms){
			if (isDefeasible(a))
				def.add(a);
		}
		System.out.println("defeasible: " + def.size());
		return def;			
	}
	
	public static OWLClassExpression getConjOfMateri(Set<OWLAxiom> axioms){
		Set<OWLClassExpression> classes = new HashSet<OWLClassExpression>();
		for (OWLAxiom axiom: axioms)
			classes.add(AxiomManipulator.getMaterialization(axiom));
		
		return ontologyManager.getOWLDataFactory().getOWLObjectIntersectionOf(classes);
	}
	
	public void removeQuery(OWLModelManager manager, OWLOntology ontology, OWLAxiom axiom){
		if (this.isDefeasible(axiom)){
			System.out.println("defeasible");
			String axiomStr_case1 = "|" + addColon(manager.getRendering(axiom)) + "*;";
			System.out.println(axiomStr_case1);
			String axiomStr_case2 = ";" + addColon(manager.getRendering(axiom)) + "*;";
			System.out.println(axiomStr_case2);
			
			for (OWLAnnotationAssertionAxiom anno: ontology.getAnnotationAssertionAxioms(rampQueryIRI)){
				String value = getRidOfJunk(anno.getValue().toString());
				//find and remove pattern
				if (foundMatch(axiomStr_case1, value)){
					System.out.println("case1");
					value = patternFreeString;
				}
				else if (foundMatch(axiomStr_case2, value)){
					System.out.println("case2");
					value = patternFreeString;
				}
				//remove old query annotation
				manager.getOWLOntologyManager().removeAxiom(ontology, anno);
				//add new one
				OWLAnnotationAssertionAxiom newAnno = manager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(rampQueryProperty, rampQueryIRI, manager.getOWLDataFactory().getOWLLiteral(value,""));
				manager.getOWLOntologyManager().addAxiom(ontology, newAnno);
			}
		}
		else{
			System.out.println("strict");
			String axiomStr_case1 = "|" + addColon(manager.getRendering(axiom)) + ";";
			String axiomStr_case2 = ";" + addColon(manager.getRendering(axiom)) + ";";
			
			for (OWLAnnotationAssertionAxiom anno: ontology.getAnnotationAssertionAxioms(rampQueryIRI)){
				String value = getRidOfJunk(anno.getValue().toString());
				//find and remove pattern
				if (foundMatch(axiomStr_case1, value)){
					value = patternFreeString;
				}
				else if (foundMatch(axiomStr_case2, value)){
					value = patternFreeString;
				}

				//remove old query annotation
				manager.getOWLOntologyManager().removeAxiom(ontology, anno);
				//add new one
				OWLAnnotationAssertionAxiom newAnno = manager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(rampQueryProperty, rampQueryIRI, manager.getOWLDataFactory().getOWLLiteral(value,""));
				manager.getOWLOntologyManager().addAxiom(ontology, newAnno);
			}
		}
	}
	
	/*public void printSequences(ArrayList<Object[]> individualSequences){
		System.out.println("Individual-cluster sequences:");
		for (Object[] seq: individualSequences){
			for (Object element: seq){
				System.out.print(man.render((OWLNamedIndividual)element) + ", ");
			}
			System.out.println();
		}
	}*/
	
	public Set<OWLAxiom> getQueries(OWLModelManager m, OWLOntology ontology) throws OWLExpressionParserException{
		String value = "";
		for (OWLAnnotationAssertionAxiom a: ontology.getAnnotationAssertionAxioms(rampQueryIRI)){
			value = getRidOfJunk(getRidOfSpecialChars(a.getValue().toString()));
		}
		Set<OWLAxiom> result = new HashSet<OWLAxiom>();
		OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		OWLExpressionChecker<OWLClassAxiom> oec = m.getOWLExpressionCheckerFactory().getClassAxiomChecker();
		StringTokenizer tokenizer = new StringTokenizer(value,";");
		
		while (tokenizer.hasMoreTokens()){
			String currentToken = tokenizer.nextToken();
			if (!currentToken.equals(" ")){
			if (currentToken.contains("*")){
				//System.out.println("token: " + currentToken);
				//defeasible
				currentToken = getRidOfStar(currentToken);
				OWLAxiom a = (OWLAxiom)oec.createObject(currentToken);
				//System.out.println(m.getRendering(a));
				Set<OWLAnnotation> annos = new HashSet<OWLAnnotation>();
				annos.add(df.getOWLAnnotation(defeasibleAnnotationProperty, df.getOWLLiteral(true)));
				result.add(a.getAnnotatedAxiom(annos));
			}
			else{
				//Strict
				//System.out.println("token: " + currentToken);
				OWLAxiom a = (OWLAxiom)oec.createObject(currentToken);
				//System.out.println(m.getRendering(a));
				result.add(a);
			}
			}
		}

		return result;	    
	}


}
