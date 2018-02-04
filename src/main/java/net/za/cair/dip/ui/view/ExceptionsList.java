package net.za.cair.dip.ui.view;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.za.cair.dip.model.Rank;
import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.ui.list.DIPListCellRenderer;
import net.za.cair.dip.ui.list.DIPQueryResultsSection;
import net.za.cair.dip.ui.list.DIPQueryResultsSectionItem;
import net.za.cair.dip.util.ManchesterOWLSyntaxOWLObjectRendererImpl;
import net.za.cair.dip.util.Utility;

import org.protege.editor.core.ui.list.MList;
import org.protege.editor.core.ui.list.MListButton;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponent;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponentMediator;
import org.protege.editor.owl.ui.view.Copyable;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;


/**
 * Author: Kody Moodley<br>
 * Centre for Artificial Intelligence Research (CAIR)<br>
 * Date: 20-Oct-2015<br><br>
 * <p/>
 * kmoodley@csir.co.za<br>
 * krr.meraka.org.za/~kmoodley<br><br>
 */
public class ExceptionsList extends MList implements LinkedObjectComponent, Copyable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8184853513690586368L;

	private OWLEditorKit owlEditorKit;

	private LinkedObjectComponentMediator mediator;

	private List<ChangeListener> copyListeners = new ArrayList<ChangeListener>();
	
	private OWLDataFactory df;
	
	private OWLReasonerFactory reasonerFactory;

	private ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	
	private final String PREFIX = "KodyMoodley";


	public ExceptionsList(OWLEditorKit owlEditorKit, OWLDataFactory df, OWLReasonerFactory reasonerFactory) {
		this.reasonerFactory = reasonerFactory;
		this.df = df;
		this.owlEditorKit = owlEditorKit;

		setCellRenderer(new DIPListCellRenderer(owlEditorKit));
		mediator = new LinkedObjectComponentMediator(owlEditorKit, this);

		getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent event) {
				ChangeEvent ev = new ChangeEvent(ExceptionsList.this);
				for (ChangeListener l : copyListeners){
					l.stateChanged(ev);
				}
			}
		});
	}
	
	private OWLClassExpression getCorrespondingComplexConcept(Set<OWLAxiom> axioms, OWLClass c){
		//System.out.println("got here");
		for (OWLAxiom a: axioms){
			if (a.isOfType(AxiomType.EQUIVALENT_CLASSES)){
				OWLEquivalentClassesAxiom equiv = (OWLEquivalentClassesAxiom)a;
				Set<OWLSubClassOfAxiom> subs = equiv.asOWLSubClassOfAxioms();
				OWLSubClassOfAxiom sub = null;
				
				for (OWLSubClassOfAxiom s: subs)
					sub = s;
				
				if (man.render(sub.getSubClass()).equals(man.render(c))){
					//System.out.println(sub.getSubClass());
					//System.out.println(sub.getSuperClass());
					return sub.getSuperClass();
				}
				
				if (man.render(sub.getSuperClass()).equals(man.render(c))){
					//System.out.println(sub.getSuperClass());
					//System.out.println(sub.getSubClass());
					return sub.getSubClass();
				}
			}
		}
		//Should be impossible
		return null;
	}
	
	private Set<OWLClassExpression> getPossibleExceptions(Ranking ranking) throws OWLOntologyCreationException, InconsistentOntologyException{
		/** Algorithm Outline */
		// Check for LHS of strict axioms which are exceptional
		// get all LHS of strict axioms that are NOT unsat w.r.t. just the strict axioms
		// if any of these LHSs become unsat when adding the remainder of axioms then they are exceptional
		// add another level and display these
		
		Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
		Set<OWLClassExpression> allLHSs = new HashSet<OWLClassExpression>();
		Set<OWLClassExpression> complexLHSConcepts = new HashSet<OWLClassExpression>();
		Set<OWLAxiom> strict_axioms = ranking.getInfiniteRank().getAxiomsAsSet();	
		Set<OWLAxiom> strict_axioms_minus_ABox = new HashSet<OWLAxiom>();
		
		for (OWLAxiom ax: strict_axioms) {
			if (!ax.isOfType(AxiomType.ABoxAxiomTypes)) {
				strict_axioms_minus_ABox.add(ax);
			}
		}
		
		Set<OWLAxiom> defeasible_axioms = ranking.getAxiomsMinusInfiniteRank();
				
		for (OWLAxiom a: strict_axioms_minus_ABox) {
			if (a.isOfType(AxiomType.SUBCLASS_OF)) {
				OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
				OWLClassExpression lhs = sub.getSubClass();
				allLHSs.add(lhs);
				if (lhs.isAnonymous()){
					complexLHSConcepts.add(lhs);
				}
			}
		}
		
		// Introduce names for them, add equivalence axioms to the ontology
		// linking them to their complex counterparts.
		
		int count = 0;

		for (OWLClassExpression c: complexLHSConcepts){
			OWLClass tmpCls = df.getOWLClass(IRI.create(this.PREFIX + count));
			allLHSs.add(tmpCls);
			OWLEquivalentClassesAxiom equiv = df.getOWLEquivalentClassesAxiom(tmpCls, c);
			strict_axioms_minus_ABox.add(equiv);
			count++;
		}
		
		// Classify new ontology and obtain unsatisfiable class names.
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology tmpOntology = ontologyManager.createOntology(strict_axioms_minus_ABox);	
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(tmpOntology);
		Node<OWLClass> unsatClses = null;
		
		try {
			unsatClses = reasoner.getUnsatisfiableClasses();
			/*System.out.println();
			System.out.println("unsatClses: " + unsatClses);
			System.out.println();*/
		}
		catch (InconsistentOntologyException e) {
			// Ontology is classically inconsistent here. Plus we do not consider ABox axioms. Therefore, must be also preferentially inconsistent (TBox inconsistent).
			// Theorem: A defeasible ontology <T,D> (no ABox) is preferentially inconsistent iff T U D' is classically inconsistent (D' is classical translation of D).  
			throw new InconsistentOntologyException();			
		}
		
		// Identify all classes NOT in unsatClses from allLHSs
		Set<OWLClass> possibleExceptions = new HashSet<OWLClass>();
		for (OWLClassExpression c: allLHSs) {
			if (!c.isAnonymous()) {
				if (!unsatClses.contains(c.asOWLClass())) {
					possibleExceptions.add(c.asOWLClass());
				}
			}
		}
		
		/*System.out.println();
		System.out.println("possibleExceptions: " + possibleExceptions);
		System.out.println();
		
		System.out.println();
		System.out.println("adding these axioms: " + defeasible_axioms);
		System.out.println();
		
		System.out.println();
		System.out.println("ontology axioms before: " + tmpOntology.getLogicalAxioms());
		System.out.println();*/
		
		ontologyManager.addAxioms(tmpOntology, defeasible_axioms);
		
		/*System.out.println();
		System.out.println("ontology axioms after: " + tmpOntology.getLogicalAxioms());
		System.out.println();*/
				
		Node<OWLClass> unsatClsesNew = null;
		
		try {
			//reasoner.flush();
			unsatClsesNew = reasoner.getUnsatisfiableClasses();
			/*System.out.println();
			System.out.println("unsatClsesNew: " + unsatClsesNew);
			System.out.println();*/
		}
		catch (InconsistentOntologyException e) {
			// Ontology is classically inconsistent here. Plus we do not consider ABox axioms. Therefore, must be also preferentially inconsistent (TBox inconsistent).
			// Theorem: A defeasible ontology <T,D> (no ABox) is preferentially inconsistent iff T U D' is classically inconsistent (D' is classical translation of D).  
			throw new InconsistentOntologyException();			
		}
		
		for (OWLClass cls: unsatClsesNew) {
			if (possibleExceptions.contains(cls)) {
				if (man.render(cls).contains("KodyMoodley")) {
					result.add(getCorrespondingComplexConcept(tmpOntology.getAxioms(), cls));
				}
				else {
					// Check if this concept is already appearing in the ranking
					boolean appears = false;
					Iterator<OWLAxiom> axiomIter = defeasible_axioms.iterator();
					while (axiomIter.hasNext() && !appears) {
						OWLAxiom current = axiomIter.next();
						if (current.isOfType(AxiomType.SUBCLASS_OF)) {
							OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)current;
							if (man.render(sub.getSubClass()).equals(man.render(cls))) {
								appears = true;
							}
						}
					}
					
					if (!appears) {
						result.add(cls);
					}
				}
			}
		}
		
		System.out.println();
		System.out.println("result: " + result);
		System.out.println();
		
		return result;
	}


	public void clear() {
		List<Object> data = new ArrayList<Object>();
		setListData(data.toArray());
	}
	
	public void setExceptionsList(Ranking ranking) throws OWLOntologyCreationException {    	
		List<Object> data = new ArrayList<Object>();

		Utility u = new Utility();
		/*int rankIdx = ranking.size()-1;
        Iterator<Rank> rIter = ranking.getRanking().iterator();
        boolean done = false;
        while (rIter.hasNext() && !done){
        	if (rankIdx == 0){
        		done = true;
        	}
        	else{
        		Rank rank = rIter.next();
        		data.add(new DIPQueryResultsSection("Level " + rankIdx));
        		Set<OWLClassExpression> lhss = new HashSet<OWLClassExpression>();
        		for (OWLAxiom a : rank.getAxioms()) {
        			OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
        			lhss.add(sub.getSubClass());

        		}
        		for (OWLClassExpression c : lhss) {
        			data.add(new DIPQueryResultsSectionItem(c));
        		}
        		rankIdx--;
        	}

        }*/

		ArrayList<Rank> ranks = ranking.getRanking();
		int level = 0;
		// If there are exceptions
		if (ranking.size() > 1) {
			for (Rank r: ranks) {
				level = r.getIndex()-1;
				// Skip non-exceptions (the first rank) 
				if (r.getIndex() > 1) {
					data.add(new DIPQueryResultsSection("Level " + (r.getIndex()-1)));
					Set<OWLClassExpression> lhss = new HashSet<OWLClassExpression>();
					for (OWLAxiom a : r.getAxioms()) {
						OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
						lhss.add(sub.getSubClass());
					}
					for (OWLClassExpression c : lhss) {
						data.add(new DIPQueryResultsSectionItem(c));
					}
				}
			}
		}
		
		Set<OWLClassExpression> strict_exceptions = getPossibleExceptions(ranking);
		
		System.out.println();
		System.out.println("strict_exceptions: " + strict_exceptions);
		System.out.println();
		
		if (strict_exceptions.size() > 0) {
			data.add(new DIPQueryResultsSection("Level " + (level+1)));
			for (OWLClassExpression c : strict_exceptions) {
				data.add(new DIPQueryResultsSectionItem(c));
			}
		}
		
		// If there are total exceptions
		boolean hasTotallyExceptionalAxioms = false;
		if (ranking.getInfiniteRank().size() > 0) {
			for (OWLAxiom a : ranking.getInfiniteRank().getAxioms()) {
				if (a.isOfType(AxiomType.SUBCLASS_OF) && u.isDefeasible(a)) {
					hasTotallyExceptionalAxioms = true;
				}    			
			}
		}

		if (hasTotallyExceptionalAxioms) {
			data.add(new DIPQueryResultsSection("Uninstantiable"));
			Set<OWLClassExpression> lhss = new HashSet<OWLClassExpression>();
			for (OWLAxiom a : ranking.getInfiniteRank().getAxioms()) {
				if (a.isOfType(AxiomType.SUBCLASS_OF) && u.isDefeasible(a)) {
					OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom)a;
					lhss.add(sub.getSubClass());
				}    			
			}
			for (OWLClassExpression c : lhss) {
				data.add(new DIPQueryResultsSectionItem(c));
			}
		}

		setListData(data.toArray());
	}


	protected List<MListButton> getButtons(Object value) {
		return Collections.emptyList();
		/* if (value instanceof DIPQueryResultsSectionItem) {
        	final OWLAxiom axiom = ((DIPQueryResultsSectionItem) value).getAxiom();
        	List<MListButton> buttons = new ArrayList<MListButton>();
        	buttons.add(new ExplainButton(new ActionListener() {
            	public void actionPerformed(ActionEvent e) {
            		ExplanationManager em = owlEditorKit.getOWLModelManager().getExplanationManager();
            		em.handleExplain((Frame) SwingUtilities.getAncestorOfClass(Frame.class, ExceptionsList.this), axiom);
            	}
            }));
            return buttons;
        }
        else {
            return Collections.emptyList();
        }*/
	}


	public JComponent getComponent() {
		return this;
	}


	public OWLObject getLinkedObject() {
		return mediator.getLinkedObject();
	}


	public Point getMouseCellLocation() {
		Rectangle r = getMouseCellRect();
		if (r == null) {
			return null;
		}
		Point mousePos = getMousePosition();
		if (mousePos == null) {
			return null;
		}
		return new Point(mousePos.x - r.x, mousePos.y - r.y);
	}


	public Rectangle getMouseCellRect() {
		Point mousePos = getMousePosition();
		if (mousePos == null) {
			return null;
		}
		int sel = locationToIndex(mousePos);
		if (sel == -1) {
			return null;
		}
		return getCellBounds(sel, sel);
	}


	public void setLinkedObject(OWLObject object) {
		mediator.setLinkedObject(object);
	}


	public boolean canCopy() {
		return getSelectedIndices().length > 0;
	}


	public List<OWLObject> getObjectsToCopy() {
		List<OWLObject> copyObjects = new ArrayList<OWLObject>();
		for (Object sel : getSelectedValues()){
			if (sel instanceof DIPQueryResultsSectionItem){
				copyObjects.add(((DIPQueryResultsSectionItem)sel).getOWLObject());
			}
		}
		return copyObjects;
	}


	public void addChangeListener(ChangeListener changeListener) {
		copyListeners.add(changeListener);
	}


	public void removeChangeListener(ChangeListener changeListener) {
		copyListeners.remove(changeListener);
	}
}
