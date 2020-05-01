package net.za.cair.dip.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.za.cair.dip.model.Rank;
import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.transform.RankingHelperClass;
import net.za.cair.dip.transform.RationalRankingAlgorithm;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.classexpression.OWLExpressionParserException;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

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
	private final IRI eTransformIRI = IRI.create("http://www.cair.za.net/eTransform");
	public final OWLAnnotationProperty rankAnnotationProperty = ontologyManager.getOWLDataFactory().getOWLAnnotationProperty(rankIRI); 
	public final OWLAnnotationProperty eTransformAnnotationProperty = ontologyManager.getOWLDataFactory().getOWLAnnotationProperty(eTransformIRI);
	public final OWLAnnotationProperty consistencyAnnotationProperty = ontologyManager.getOWLDataFactory().getOWLAnnotationProperty(consistencyIRI); 

	private String patternFreeString = "";
	private int startMatch = 0;
	private int endMatch = 0;
	private ManchesterOWLSyntaxOWLObjectRendererImpl man;

	public Utility() {	
		man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	}

	public boolean isDefeasible(OWLAxiom axiom){	
		Set<OWLAnnotation> annos = axiom.getAnnotations();
		for (OWLAnnotation anno: annos){
			if ((man.render(anno)).contains("defeasible"))
				return true;
		}
		return false;	    
	}

	private boolean hasRankAnnotation(OWLAxiom axiom){	
		Set<OWLAnnotation> annos = axiom.getAnnotations();
		if (annos.isEmpty()){
			return false;
		}

		for (OWLAnnotation anno: annos){
			if ((man.render(anno)).contains("rank"))
				return true;
		}
		return false;    
	}

	private boolean hasETransformAnnotation(OWLAxiom axiom){	
		Set<OWLAnnotation> annos = axiom.getAnnotations();
		if (annos.isEmpty()){
			return false;
		}

		for (OWLAnnotation anno: annos){
			if ((man.render(anno)).contains("eTransform"))
				return true;
		}
		return false;    
	}

	private int getRankIndex(OWLAxiom axiom){	
		Set<OWLAnnotation> annos = axiom.getAnnotations();

		for (OWLAnnotation anno: annos){
			if ((man.render(anno)).contains("rank")){
				String s = anno.getValue().toString();
				int i = 0;
				String str = "";
				while ((i < s.length())){
					if (s.charAt(i) != '\"' && s.charAt(i) != '^' && s.charAt(i) != ':' && !Character.isLetter(s.charAt(i)))
						str += s.charAt(i);
					i++;
				}

				return Integer.parseInt(str);
			}

		}
		return -1;    
	}

	private int getETransformIndex(OWLAxiom axiom){	
		Set<OWLAnnotation> annos = axiom.getAnnotations();
		for (OWLAnnotation anno: annos){
			if ((man.render(anno)).contains("eTransform")){
				String s = anno.getValue().toString();
				int i = 0;
				String str = "";
				while ((i < s.length())){
					if (s.charAt(i) != '\"' && s.charAt(i) != '^' && s.charAt(i) != ':' && !Character.isLetter(s.charAt(i)))
						str += s.charAt(i);
					i++;
				}
				return Integer.parseInt(str);
			}

		}
		return -1;    
	}
	
	private int getETransformIndexFromRanking(OWLAxiom axiom, Ranking ranking){	
		ArrayList<ArrayList<OWLAxiom>> eTransforms = ranking.getETransforms();
		
		int index = 0;
		for (int i = 0; i < eTransforms.size();i++) {
			Set<OWLAxiom> tmp = new HashSet<OWLAxiom>();
			tmp.addAll(eTransforms.get(i));
			if (containsAxiom(tmp, axiom)) {
				index = i;
			}
		}
		
		return index;
	}

	public boolean ontologyContainsRanking(OWLOntology theOntology) {
		System.out.println();
		System.out.print("Checking if there is a stored ranking...");
		Set<OWLAxiom> logicalAxioms = new HashSet<OWLAxiom>(theOntology.getLogicalAxioms());
		Iterator<OWLAxiom> iter = logicalAxioms.iterator();

		boolean containsDefeasibleAxioms = false;
		boolean containsUninstantiableStrictAxioms = false;
		while (iter.hasNext()){
			OWLAxiom tmp = iter.next();
			if (isDefeasible(tmp)){
				containsDefeasibleAxioms = true;
				if (!hasRankAnnotation(tmp) || !hasETransformAnnotation(tmp)){
					System.out.println("could not find one or it is incomplete!");
					return false;
				}
			}
			else {
				if (hasRankAnnotation(tmp)) {
					containsUninstantiableStrictAxioms = true;
				}
			}
		}

		System.out.println("FOUND!");
		if (containsDefeasibleAxioms || containsUninstantiableStrictAxioms)
			return true;
		else
			return false;
	}

	private boolean containsAxiom(Set<OWLAxiom> set, OWLAxiom axiom) {
		for (OWLAxiom a: set) {
			if (a.equalsIgnoreAnnotations(axiom))
				return true;
		}
		return false;
	}

	private int getRank(OWLOntology ontology, Ranking ranking, OWLAxiom a, OWLReasonerFactory rf) throws OWLOntologyCreationException {
		for (Rank rank: ranking.getRanking()) {
			if (containsAxiom(rank.getAxiomsAsSet(), a)) {
				return rank.getIndex();
			}
		}

		if (containsAxiom(ranking.getInfiniteRank().getAxiomsAsSet(), a)) {
			return -1;
		}
		else {
			ArrayList<OWLAxiom> infiniteRankAxioms = ranking.getInfiniteRank().getAxioms();	// Infinite rank axioms
			Set<OWLAxiom> t_starAxioms = new HashSet<OWLAxiom>();
			Set<OWLAxiom> strictAxioms = new HashSet<OWLAxiom>();
			for (OWLAxiom ax: ontology.getAxioms()) {
				if (!isDefeasible(ax)) {
					strictAxioms.add(ax);
				}
			}
			t_starAxioms.addAll(strictAxioms);
			t_starAxioms.addAll(infiniteRankAxioms);
			RankingHelperClass rhc = new RankingHelperClass();
			OWLClassExpression c = null;
			if (a.isOfType(AxiomType.SUBCLASS_OF)){
				System.out.println("SUBCLASS: " + man.render(a));
				OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
				c = sub.getSubClass();
			}
			else if (a.isOfType(AxiomType.EQUIVALENT_CLASSES)) {
				System.out.println("EQUIV: " + man.render(a));
				OWLEquivalentClassesAxiom equiv = (OWLEquivalentClassesAxiom)a;
				Iterator<OWLClassExpression> iter = equiv.getClassExpressions().iterator();
				c = iter.next();
			}
			else if (a.isOfType(AxiomType.DISJOINT_CLASSES)) {
				System.out.println("DISJOINT: " + man.render(a));
				OWLDisjointClassesAxiom disj = (OWLDisjointClassesAxiom)a;
				Iterator<OWLClassExpression> iter = disj.getClassExpressions().iterator();
				c = iter.next();
			}
			else {
				System.out.println("UNKNOWN AXIOM TYPE: " + man.render(a));
			}
			
			return rhc.getConceptRank(t_starAxioms, ranking.getETransforms(), c, rf);
		}

	}

	public void saveRanking(OWLOntology theOntology, Ranking ranking, boolean isPreferentiallyConsistent, OWLReasonerFactory rf) throws OWLOntologyCreationException {
		for (OWLAxiom a: theOntology.getTBoxAxioms(Imports.EXCLUDED)) {
			int rank = getRank(theOntology, ranking, a, rf);

			if (isDefeasible(a)) {
				int eTransformIndex = getETransformIndexFromRanking(a, ranking);
				Set<OWLAnnotation> annos = a.getAnnotations();
				Set<OWLAnnotation> newAnnos = new HashSet<OWLAnnotation>();
				for (OWLAnnotation an: annos){
					if (!an.getProperty().equals(rankAnnotationProperty) && !an.getProperty().equals(eTransformAnnotationProperty)){
						newAnnos.add(an);
					}
				}
				OWLAxiom aTmp = a.getAxiomWithoutAnnotations();
				OWLAnnotation rankAnnotation = theOntology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotation(rankAnnotationProperty, theOntology.getOWLOntologyManager().getOWLDataFactory().getOWLLiteral(rank));
				OWLAnnotation eTransformIndexAnnotation = theOntology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotation(eTransformAnnotationProperty, theOntology.getOWLOntologyManager().getOWLDataFactory().getOWLLiteral(eTransformIndex));
				newAnnos.add(rankAnnotation);
				newAnnos.add(eTransformIndexAnnotation);

				theOntology.getOWLOntologyManager().removeAxiom(theOntology, a);
				theOntology.getOWLOntologyManager().addAxiom(theOntology, aTmp.getAnnotatedAxiom(newAnnos));
			}
			else {
				Set<OWLAnnotation> annos = a.getAnnotations();
				Set<OWLAnnotation> newAnnos = new HashSet<OWLAnnotation>();
				for (OWLAnnotation an: annos){
					if (!an.getProperty().equals(rankAnnotationProperty)){
						newAnnos.add(an);
					}
				}
				OWLAxiom aTmp = a.getAxiomWithoutAnnotations();
				OWLAnnotation rankAnnotation = theOntology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotation(rankAnnotationProperty, theOntology.getOWLOntologyManager().getOWLDataFactory().getOWLLiteral(rank));
				newAnnos.add(rankAnnotation);
				theOntology.getOWLOntologyManager().removeAxiom(theOntology, a);
				theOntology.getOWLOntologyManager().addAxiom(theOntology, aTmp.getAnnotatedAxiom(newAnnos));
			}
		}

		// save ontology consistency status
		OWLAnnotation consistencyStatus = null;
		OWLDataFactory df = theOntology.getOWLOntologyManager().getOWLDataFactory();
		OWLAnnotation inconsistentAnn = df.getOWLAnnotation(consistencyAnnotationProperty, df.getOWLLiteral(false));
		OWLAnnotation consistentAnn = df.getOWLAnnotation(consistencyAnnotationProperty, df.getOWLLiteral(true));

		if (isPreferentiallyConsistent) {
			consistencyStatus = df.getOWLAnnotation(consistencyAnnotationProperty, df.getOWLLiteral(true));
		}
		else {
			consistencyStatus = df.getOWLAnnotation(consistencyAnnotationProperty, df.getOWLLiteral(false));
		}
		theOntology.getOWLOntologyManager().applyChange(new RemoveOntologyAnnotation(theOntology, inconsistentAnn));
		theOntology.getOWLOntologyManager().applyChange(new RemoveOntologyAnnotation(theOntology, consistentAnn));		
		theOntology.getOWLOntologyManager().applyChange(new AddOntologyAnnotation(theOntology, consistencyStatus));
	}

	private ArrayList<ArrayList<OWLAxiom>> loadETransforms(Set<OWLAxiom> set){
		Multimap<Integer, OWLAxiom> myMap = ArrayListMultimap.create();
		ArrayList<ArrayList<OWLAxiom>> result = new ArrayList<ArrayList<OWLAxiom>>();

		for (OWLAxiom a: set) {
			int idx = getETransformIndex(a);
			myMap.put(idx, a);
		}

		for (Integer key : myMap.keys()) {
			Collection<OWLAxiom> list = myMap.get(key);
			ArrayList<OWLAxiom> tmp = new ArrayList<OWLAxiom>();
			for (OWLAxiom ax: list) {
				tmp.add(ax);
			}
			result.add(tmp);
		}

		return result;
	}

	public Ranking loadRanking(OWLOntology theOntology, OWLReasonerFactory reasonerFactory) throws OWLException {
		System.out.println();
		System.out.println("Attempting to load axiom ranking from file...");
		Set<OWLAxiom> logicalAxioms = new HashSet<OWLAxiom>(theOntology.getLogicalAxioms());
		Set<OWLAxiom> strictAxioms = new HashSet<OWLAxiom>();
		Set<OWLAxiom> rankedAxioms = new HashSet<OWLAxiom>();
		Set<OWLAxiom> potentiallyETransforms = new HashSet<OWLAxiom>();
		ArrayList<ArrayList<OWLAxiom>> eTransforms = new ArrayList<ArrayList<OWLAxiom>>();
		Iterator<OWLAxiom> iter = logicalAxioms.iterator();
		Set<OWLAxiom> uninstantiableAxioms = new HashSet<OWLAxiom>();
		while (iter.hasNext()){
			OWLAxiom tmp = iter.next();
			if (isDefeasible(tmp)){
				if (hasETransformAnnotation(tmp))
					potentiallyETransforms.add(tmp);
				rankedAxioms.add(tmp);
			}
			else{
				if (hasRankAnnotation(tmp)){
					uninstantiableAxioms.add(tmp);
				}
				strictAxioms.add(tmp);
			}
		}

		if (!potentiallyETransforms.isEmpty()) {
			eTransforms = loadETransforms(potentiallyETransforms);
		}
		else {
			RationalRankingAlgorithm rankingalg = new RationalRankingAlgorithm(reasonerFactory, theOntology);
			Ranking ranking = rankingalg.computeRanking();
			eTransforms = ranking.getETransforms();
		}

		if (rankedAxioms.isEmpty()){//NO EXISTING/STORED RANKING
			System.out.println("Ontology is empty!");
			return new Ranking();
		}
		else{ //THERE IS A STORED RANKING
			Ranking ranking = new Ranking();
			System.out.println("There is a stored ranking...");
			ArrayList<Rank> ranks = new ArrayList<Rank>();
			int lowest = theOntology.getAxiomCount();
			int highest = -1;
			for (OWLAxiom axiom: rankedAxioms){
				int idx = getRankIndex(axiom);
				if (idx < lowest)
					lowest = idx;
				if (idx > highest)
					highest = idx;

				ranks.add(new Rank(new ArrayList<OWLAxiom>(Collections.singleton(axiom)), idx));
			}

			System.out.println("Attempting merging of ranks...");
			ranking = mergeRanks(ranks, lowest, highest);
			ranking.setUninstantiable(uninstantiableAxioms);
			ranking.setETransforms(eTransforms);

			if (ranking.getAxioms().isEmpty() && uninstantiableAxioms.isEmpty()){
				System.out.println("Merging unsuccessful");
				return ranking;
			}
			else{
				System.out.println("Merging successful");
				return ranking;
			}
		}
	}

	private Ranking mergeRanks(ArrayList<Rank> nranks, int lowest, int highest){
		ArrayList<Rank> ranks = new ArrayList<Rank>();ranks.addAll(nranks);
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
		return new Ranking(result);
	}

	//	private boolean isQueryAnnotation(OWLAnnotation anno){
	//		return anno.getProperty().getIRI().equals(rampQueryIRI);	    
	//	}
	//
	//	private String getRidOfWhiteSpace(String str){
	//		String result = "";
	//		for (int i = 0;i < str.length();i++){
	//			if (str.charAt(i) != ' '){
	//				result += str.charAt(i);
	//			}
	//		}
	//		return result;
	//	}
	//
	//	private String removeColon(String str){
	//		String result = "";
	//		for (int i = 0; i < str.length();i++){
	//			char tmp = str.charAt(i);
	//			if (tmp != ':'){
	//				result += tmp;
	//			}
	//		}
	//		return result;
	//	}


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

	private String getRidOfJunk(String str){
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

	private String getRidOfSpecialChars(String str){
		String result = "";
		for (int i = 0;i < str.length();i++){
			if ((str.charAt(i) != '|') && (str.charAt(i) != '\"')){
				result += str.charAt(i);
			}
		}
		return result;
	}

	private String getRidOfStar(String str){
		String result = "";
		for (int i = 0;i < str.length();i++){
			if (str.charAt(i) != '*'){
				result += str.charAt(i);
			}
		}
		return result;
	}

	public String toString(OWLObject obj){
		return man.render(obj);
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

	//	private void printSequences(ArrayList<Object[]> individualSequences){
	//		System.out.println("Individual-cluster sequences:");
	//		for (Object[] seq: individualSequences){
	//			for (Object element: seq){
	//				System.out.print(man.render((OWLNamedIndividual)element) + ", ");
	//			}
	//			System.out.println();
	//		}
	//	}
	//
	//	private boolean isConsistent(OWLReasonerFactory reasonerFactory, Set<OWLAxiom> aBox, Set<OWLAxiom> tBox) throws OWLOntologyCreationException{
	//		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();axioms.addAll(aBox);axioms.addAll(tBox);
	//		OWLOntology tmpOntology = OWLManager.createOWLOntologyManager().createOntology(axioms);
	//		return reasonerFactory.createNonBufferingReasoner(tmpOntology).isConsistent();
	//	}
	//
	//	private boolean containsAxiom(OWLReasonerFactory reasonerFactory, Set<OWLAxiom> axioms, OWLAxiom axiom) throws OWLOntologyCreationException{
	//		Set<OWLAxiom> allAxioms = new HashSet<OWLAxiom>();
	//		for (OWLAxiom a: axioms){
	//			allAxioms.add(a);
	//		}
	//		OWLOntology tmpOntology = OWLManager.createOWLOntologyManager().createOntology(allAxioms);
	//		return reasonerFactory.createNonBufferingReasoner(tmpOntology).isEntailed(axiom);
	//	}

//	private boolean containsOWLClass(Set<OWLClassExpression> set, OWLClassExpression element){
//		for (OWLClassExpression o: set){
//			if (man.render(element).equals(man.render(o))) {
//				return true;
//			}
//		}
//		return false;
//	}

	//	private boolean containsOWLAxiom(Set<OWLAxiom> set, OWLAxiom element){
	//		for (OWLAxiom o: set){
	//			if (man.render(element).equals(man.render(o))) {
	//				return true;
	//			}
	//		}
	//		return false;
	//	}
	//
	//	private static <T> boolean contains(Set<T> set, T element){
	//		for (T current: set){
	//			if (current.equals(element))
	//				return true;
	//		}
	//		return false;
	//	}
	//
	//	private static int getRankValue(OWLAxiom axiom, Ranking ranking){
	//		for (int i = ranking.size()-1; i >= 0;i--){
	//			if (ranking.get(i).getAxiomsAsSet().contains(axiom))
	//				return i;
	//		}
	//		return -1;
	//	}

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

	//	private static OWLAxiom getAxiomFromCombination(Set<Set<OWLAxiom>> combination){
	//		ArrayList<OWLAxiom> forORCombining = new ArrayList<OWLAxiom>();
	//		for (Set<OWLAxiom> clause: combination){
	//			ArrayList<OWLAxiom> ArrayListClause = new ArrayList<OWLAxiom>();
	//			ArrayListClause.addAll(clause);
	//			OWLAxiom tmp = AxiomManipulator.getInstance().getANDAxiomCombiner(ArrayListClause);
	//			forORCombining.add(tmp);
	//		}
	//		return AxiomManipulator.getInstance().getORAxiomCombiner(forORCombining);
	//	}
	//
	//	private ArrayList<OWLAxiom> getDefeasibleAxioms(Set<OWLAxiom> axioms) {
	//		System.out.println("total: " + axioms.size());
	//		ArrayList<OWLAxiom> def = new ArrayList<OWLAxiom>();
	//		for (OWLAxiom a: axioms){
	//			if (isDefeasible(a))
	//				def.add(a);
	//		}
	//		System.out.println("defeasible: " + def.size());
	//		return def;			
	//	}

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
