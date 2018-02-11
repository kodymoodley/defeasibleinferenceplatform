package net.za.cair.dip.ui.view;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.za.cair.dip.DefeasibleInferenceComputer;
import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.model.ReasoningType;
import net.za.cair.dip.ui.list.DIPListCellRenderer;
import net.za.cair.dip.ui.list.DIPQueryResultsSection;
import net.za.cair.dip.ui.list.DIPQueryResultsSectionItem;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.protege.editor.core.ui.list.MList;
import org.protege.editor.core.ui.list.MListButton;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.OWLClassExpressionComparator;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponent;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponentMediator;
import org.protege.editor.owl.ui.view.Copyable;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;


/**
 * Author: Kody Moodley<br>
 * Centre for Artificial Intelligence Research (CAIR)<br>
 * Date: 20-Oct-2015<br><br>
 * <p/>
 * kmoodley@csir.co.za<br>
 * krr.meraka.org.za/~kmoodley<br><br>
 */
public class ResultsList extends MList implements LinkedObjectComponent, Copyable {

    /**
     * 
     */
    private static final long serialVersionUID = 8184853513690586368L;

    private OWLEditorKit owlEditorKit;

    private boolean showSuperClasses;

    private boolean showAncestorClasses;

    private boolean showDescendantClasses;

    private boolean showSubClasses;

    private boolean showInstances;

    private boolean showEquivalentClasses;

    private LinkedObjectComponentMediator mediator;

    private List<ChangeListener> copyListeners = new ArrayList<ChangeListener>();
    
    private ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();


    public ResultsList(OWLEditorKit owlEditorKit) {
        this.owlEditorKit = owlEditorKit;
        setCellRenderer(new DIPListCellRenderer(owlEditorKit));
        mediator = new LinkedObjectComponentMediator(owlEditorKit, this);

        getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event) {
                ChangeEvent ev = new ChangeEvent(ResultsList.this);
                for (ChangeListener l : copyListeners){
                    l.stateChanged(ev);
                }
            }
        });
    }


    public boolean isShowAncestorClasses() {
        return showAncestorClasses;
    }


    public void setShowAncestorClasses(boolean showAncestorClasses) {
        this.showAncestorClasses = showAncestorClasses;
    }


    public boolean isShowDescendantClasses() {
        return showDescendantClasses;
    }


    public void setShowDescendantClasses(boolean showDescendantClasses) {
        this.showDescendantClasses = showDescendantClasses;
    }


    public boolean isShowInstances() {
        return showInstances;
    }


    public void setShowInstances(boolean showInstances) {
        this.showInstances = showInstances;
    }


    public boolean isShowSubClasses() {
        return showSubClasses;
    }


    public void setShowSubClasses(boolean showSubClasses) {
        this.showSubClasses = showSubClasses;
    }


    public boolean isShowSuperClasses() {
        return showSuperClasses;
    }


    public void setShowSuperClasses(boolean showSuperClasses) {
        this.showSuperClasses = showSuperClasses;
    }


    public boolean isShowEquivalentClasses() {
        return showEquivalentClasses;
    }


    public void setShowEquivalentClasses(boolean showEquivalentClasses) {
        this.showEquivalentClasses = showEquivalentClasses;
    }


    private List<OWLClass> toSortedList(Set<OWLClass> clses) {
        OWLClassExpressionComparator descriptionComparator = new OWLClassExpressionComparator(owlEditorKit.getModelManager());
        List<OWLClass> list = new ArrayList<OWLClass>(clses);
        Collections.sort(list, descriptionComparator);
        return list;
    }
    
    public void clear(){
    	List<Object> data = new ArrayList<Object>();
    	setListData(data.toArray());
    }
    
    private boolean containsABoxAxioms(Ranking ranking) {
    	ArrayList<OWLAxiom> axioms = ranking.getInfiniteRank().getAxioms();
    	for (OWLAxiom a: axioms) {
    		if (a.isOfType(AxiomType.ABoxAxiomTypes))
    			return true;
    	}
    	return false;
    }
    
    private boolean compare(Set<OWLAxiom> one, Set<OWLAxiom> two) {
    	/*Set<String> string1 = new HashSet<String>();
    	Set<String> string2 = new HashSet<String>();
    	
    	System.out.println();
    	System.out.println("one:");
    	System.out.println("----");
    	for (OWLAxiom a: one) {
    		string1.add(man.render(a));
    		System.out.println(man.render(a));
    	}
    	System.out.println();
    	
    	System.out.println();
    	System.out.println("two:");
    	System.out.println("----");
    	for (OWLAxiom a2: two) {
    		string2.add(man.render(a2));
    		System.out.println(man.render(a2));
    	}
    	System.out.println();*/    	
    	
    	boolean forward = true;
    	for (OWLAxiom a: one) {
    		if (!two.contains(a)) {
    			System.out.println("forward culprit: " + man.render(a));
    			forward = false;
    		}
    	}
    	
    	boolean backward = true;
    	for (OWLAxiom a2: two) {
    		if (!one.contains(a2)) {
    			System.out.println("backward culprit: " + man.render(a2));
    			backward = false;
    		}
    	}
    	
    	return forward && backward;
    }


    public void setOWLClassExpression(OWLClassExpression description, ReasoningType algorithm, Ranking ranking, ArrayList<ArrayList<OWLAxiom>> eTransforms) throws OWLOntologyCreationException {
    	List<Object> data = new ArrayList<Object>();
    	OWLDataFactory factory = owlEditorKit.getOWLModelManager().getOWLDataFactory();
    	
    	// Super classes should be displayed
    	if (showSuperClasses) {
    		// Print class expression
    		System.out.println("Query: " + man.render(description));
    		System.out.println();

    		// Set up defeasible reasoner and compute super classes
    		DefeasibleInferenceComputer dic = new DefeasibleInferenceComputer(owlEditorKit.getModelManager().getOWLReasonerManager().getCurrentReasonerFactory().getReasonerFactory(), ranking);
    		OWLClassExpression cc = dic.getCCompatibility(description, algorithm, ranking);
    		dic.computeSuperClasses(description, cc, ranking.getInfiniteRank());
    		final List<OWLClass> results1 = new ArrayList<OWLClass>(dic.getSuperClassesStrict());
    		final List<OWLClass> results2 = new ArrayList<OWLClass>(dic.getSuperClassesTypical());

    		// Remove OWLThing from results: strict superclasses
    		int newSize1 = 0;
    		if (results1.size() > 0){
    			for (OWLClass superClass : results1) {
    				if (!superClass.isOWLThing()){
    					newSize1++;
    				}
    			}
    		}

    		// Remove OWLThing from results: typical superclasses
    		int newSize2 = 0;
    		if (results2.size() > 0){
    			for (OWLClass superClass : results2) {
    				if (!superClass.isOWLThing()){
    					newSize2++;
    				}
    			}
    		}

    		// If there is at least one strict super class
    		if (newSize1 > 0){
    			data.add(new DIPQueryResultsSection("Strict super classes (" + newSize1 + ")"));
    			for (OWLClass superClass : results1) {
    				if (!superClass.isOWLThing()){
    					data.add(new DIPQueryResultsSectionItem(superClass, factory.getOWLSubClassOfAxiom(description, superClass)));
    				}
    			}
    		}
    		else {
    			data.add(new DIPQueryResultsSection("No strict super classes."));
    		}

    		// If there is at least one typical super class
    		if (newSize2 > 0){
    			data.add(new DIPQueryResultsSection("Typical super classes (" + newSize2 + ")"));
    			for (OWLClass superClass : results2) {
    				if (!superClass.isOWLThing()){
    					data.add(new DIPQueryResultsSectionItem(superClass, factory.getOWLSubClassOfAxiom(description, superClass)));
    				}
    			}
    		}
    		else {
    			data.add(new DIPQueryResultsSection("No typical super classes."));
    		}

    	}

    	// If there is at least one ABox axiom in the ontology
    	if (containsABoxAxioms(ranking)) {
    		System.out.println();
    		System.out.println("Has ABox Axioms.");
    		System.out.println();

    		// Setup defeasible reasoner
    		DefeasibleInferenceComputer dic = new DefeasibleInferenceComputer(owlEditorKit.getModelManager().getOWLReasonerManager().getCurrentReasonerFactory().getReasonerFactory(), ranking);

    		// If this ontology has a single ABox extension
    		if (dic.hasSingleABoxExtension(ranking, algorithm)) {
    			System.out.println();
    			System.out.println("Single Extension");
    			System.out.println();
    			System.out.println("Query: " + man.render(description));

    			// Get T (strict axioms) + A (single ABox extension) and construct ontology with them
    			Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
    			Set<OWLAxiom> singleExtension = dic.getSingleExtension();
    			Set<OWLAxiom> infiniteRank = dic.getRanking().getInfiniteRank().getAxiomsAsSet();        		
    			axioms.addAll(singleExtension);								// A_D
    			axioms.addAll(infiniteRank);	// T
    			final OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();    		
    			final OWLOntology o = ontologyManager.createOntology(axioms);

    			// Sanity check: print IRIs of entities to make sure all have the same prefix
    			Set<OWLEntity> entities = o.getSignature();        		
    			System.out.println();
    			System.out.println("IRIs:");
    			System.out.println("-----");
    			for (OWLEntity e: entities) {
    				System.out.println(man.render(e) + " : " + e.getIRI());
    			}
    			System.out.println();

    			// Set up reasoner and ask for all instances of the given class expression
    			OWLReasoner reasoner = owlEditorKit.getModelManager().getOWLReasonerManager().getCurrentReasonerFactory().getReasonerFactory().createNonBufferingReasoner(o);
    			Set<OWLNamedIndividual> results = reasoner.getInstances(description, false).getFlattened();

    			// If there are no instances
    			if (results.isEmpty()) {
    				System.out.println("No instances!");
    				data.add(new DIPQueryResultsSection("No instances."));
    			}
    			else {
    				data.add(new DIPQueryResultsSection("Instances (" + results.size() + ")"));
    				for (OWLNamedIndividual ind: results) {
    					data.add(new DIPQueryResultsSectionItem(ind, factory.getOWLClassAssertionAxiom(description, ind)));
    				}
    			}
    		}
    		else {
    			// Ontology has multiple extensions
    			System.out.println();
    			System.out.println("Multiple Extensions");
    			System.out.println();
    			
    			// Compute multiple ABox extensions
    			dic.computeMultipleExtensions(ranking, algorithm);
    			// Compute instances of given class expression w.r.t. each ABox extension
    			dic.computeMultipleExtensionInstances(ranking, description);
    			// First get instances of given class expression that appear in ALL extensions (definite instances)
    			Set<OWLNamedIndividual> definite_instances = dic.getDefiniteInstances();
    			// Then get instances of given class expression that appear in AT LEAST ONE extension (plausible instances)
    			Set<OWLNamedIndividual> plausible_instances = dic.getPlausibleInstances();
    			
    			// If there is at least one instance of the given class expression (either plausible or definite) 
    			if (definite_instances.size() > 0 || plausible_instances.size() > 0) {
    				
    				// If there is at least one definite instance
    				if (definite_instances.size() > 0) {
    					data.add(new DIPQueryResultsSection("Definite Instances (" + definite_instances.size() + ")"));
        				for (OWLNamedIndividual ind: definite_instances) {
        					data.add(new DIPQueryResultsSectionItem(ind, factory.getOWLClassAssertionAxiom(description, ind)));
        				}
    				}
    				else {
    					System.out.println("Multiple Extensions: No definite instances!");
        				data.add(new DIPQueryResultsSection("No definite instances."));
    				}
    				
    				// If there is at least one plausible instance
    				if (plausible_instances.size() > 0) {
        				data.add(new DIPQueryResultsSection("Plausible Instances (" + plausible_instances.size() + ")"));
        				for (OWLNamedIndividual ind: plausible_instances) {
        					data.add(new DIPQueryResultsSectionItem(ind, factory.getOWLClassAssertionAxiom(description, ind)));
        				}
        			}
    				else {
    					System.out.println("Multiple Extensions: No plausible instances!");
        				data.add(new DIPQueryResultsSection("No plausible instances."));
    				}
    			}
    			else {
    				System.out.println("Multiple Extensions: No instances!");
    				data.add(new DIPQueryResultsSection("No instances."));
    			}
    		}
    	}
    	else {
    		// Ontology does not have ABox axioms
    		System.out.println();
    		System.out.println("No ABox Axioms.");
    		System.out.println();
    		System.out.println("No ABox: No instances!");
			data.add(new DIPQueryResultsSection("No instances."));
    	}
    	// Sub classes should also be displayed (this will be harder to do computationally)
    	// if (showSubClasses) {
    	// flatten and filter out owl:Nothing
    	/*OWLClass owlNothing = owlEditorKit.getOWLModelManager().getOWLDataFactory().getOWLNothing();
            DefeasibleInferenceComputer dic = new DefeasibleInferenceComputer(owlEditorKit.getModelManager().getOWLReasonerManager().getCurrentReasonerFactory().getReasonerFactory(), ranking);
        	final List<OWLClass> results = toSortedList(dic.getSubClasses(description, algorithm, ranking));//toSortedList(reasoner.getSuperClasses(description, true).getFlattened());
            data.add(new DIPQueryResultsSection("Typical sub classes (" + results.size() + ")"));
            for (OWLClass subClass : results) {
                data.add(new DIPQueryResultsSectionItem(subClass, factory.getOWLSubClassOfAxiom(subClass, description)));
            }*/
    	//}
    	setListData(data.toArray());
    }
    
    


    protected List<MListButton> getButtons(Object value) {
        /*if (value instanceof DIPQueryResultsSectionItem) {
        	final OWLAxiom axiom = ((DIPQueryResultsSectionItem) value).getAxiom();
        	List<MListButton> buttons = new ArrayList<MListButton>();
        	/*buttons.add(new ExplainButton(new ActionListener() {
            	public void actionPerformed(ActionEvent e) {
            		ExplanationManager em = owlEditorKit.getOWLModelManager().getExplanationManager();
            		em.handleExplain((Frame) SwingUtilities.getAncestorOfClass(Frame.class, ResultsList.this), axiom);
            	}
            }));
           // return buttons;
        }
        else {*/
            return Collections.emptyList();
        //}
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
