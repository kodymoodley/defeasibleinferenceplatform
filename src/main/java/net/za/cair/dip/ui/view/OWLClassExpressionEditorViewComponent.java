package net.za.cair.dip.ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

//import org.apache.log4j.Logger;
import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.core.ui.util.InputVerificationStatusChangedListener;
import org.protege.editor.owl.model.cache.OWLExpressionUserCache;
import org.protege.editor.owl.model.entity.OWLEntityCreationSet;
import org.protege.editor.owl.model.inference.OWLReasonerManager;
import org.protege.editor.owl.ui.CreateDefinedClassPanel;
import org.protege.editor.owl.ui.clsdescriptioneditor.ExpressionEditor;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import net.za.cair.dip.DefeasibleInferenceComputer;
import net.za.cair.dip.model.Rank;
import net.za.cair.dip.model.Ranking;
import net.za.cair.dip.model.ReasoningType;
import net.za.cair.dip.transform.RationalRankingAlgorithm;
import net.za.cair.dip.util.ManchesterOWLSyntaxOWLObjectRendererImpl;
import net.za.cair.dip.util.Utility;

/**
 * Author: Kody Moodley<br>
 * Centre for Artificial Intelligence Research (CAIR)<br>
 * Date: 20-Oct-2015<br><br>
 * <p/>
 * kmoodley@csir.co.za<br>
 * krr.meraka.org.za/~kmoodley<br><br>
 */
public class OWLClassExpressionEditorViewComponent extends AbstractOWLViewComponent {
	private static final long serialVersionUID = 8268241587271333587L;

	//Logger log = Logger.getLogger(OWLClassExpressionEditorViewComponent.class);

	private ExpressionEditor<OWLClassExpression> owlDescriptionEditor;
	private ResultsList resultsList;
	private ExceptionsList exceptionsList;
	private ArrayList<ArrayList<OWLAxiom>> eTransforms;
	private boolean isConsistent;
	private boolean isTBoxConsistent;
	//private JCheckBox showSuperClassesCheckBox;
	private JComboBox reasoningList; 
	//private JComboBox roleList; 
	//private JCheckBox showSubClassesCheckBox;
	private JButton executeButton;
	private JButton refreshButton;
	//private JButton addButton;
	private OWLOntologyChangeListener ontListener;
	//private OWLModelManagerListener listener;
	private boolean requiresRefresh = true;
	private Ranking ranking;
	private ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();

	protected void initialiseOWLView() throws Exception {
		setLayout(new BorderLayout(10, 10));
		JComponent editorPanel = createQueryPanel();
		JComponent resultsPanel = createResultsPanel();
		JComponent exceptionsPanel = createExceptionsPanel();
		resultsPanel.add(exceptionsPanel, BorderLayout.WEST);
		JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorPanel, resultsPanel);
		splitter.setDividerLocation(0.3);
		add(splitter, BorderLayout.CENTER);

		//updateGUI();

		loadRanking();
		isTBoxConsistent = true;
		ontListener = new OWLOntologyChangeListener() {
			@Override
			public void ontologiesChanged(List<? extends OWLOntologyChange> changes)
					throws OWLException {
				boolean logicalChange = false;
				for (OWLOntologyChange ch: changes){
					if (ch.isAddAxiom() || ch.isRemoveAxiom() || ch.isAxiomChange()){
						if (ch.getAxiom().isLogicalAxiom()) {
							requiresRefresh = true;
							isTBoxConsistent = true;
						}

					}
				}
			}
		};

		getOWLModelManager().addOntologyChangeListener(ontListener);
	}

	@SuppressWarnings("unchecked")
	private JComponent createQueryPanel() {
		JPanel editorPanel = new JPanel(new BorderLayout());

		final OWLExpressionChecker<OWLClassExpression> checker = getOWLModelManager().getOWLExpressionCheckerFactory().getOWLClassExpressionChecker();
		owlDescriptionEditor = new ExpressionEditor<OWLClassExpression>(getOWLEditorKit(), checker);
		owlDescriptionEditor.addStatusChangedListener(new InputVerificationStatusChangedListener(){
			public void verifiedStatusChanged(boolean newState) {
				executeButton.setEnabled(newState);
			}
		});
		owlDescriptionEditor.setPreferredSize(new Dimension(100, 50));

		editorPanel.add(ComponentFactory.createScrollPane(owlDescriptionEditor), BorderLayout.CENTER);
		JPanel buttonHolder = new JPanel(new FlowLayout(FlowLayout.LEFT));
		executeButton = new JButton(new AbstractAction("Execute") {
			/**
			 * 
			 */
			private static final long serialVersionUID = -1833321282125901561L;

			public void actionPerformed(ActionEvent e) {
				doQuery();
			}
		});

		/*addButton = new JButton(new AbstractAction("Save"){
            /**
		 * 
		 */
		/*private static final long serialVersionUID = -6050625862820344594L;

            public void actionPerformed(ActionEvent event) {
                doAdd();
            }
        });*/

		//Object [] roles = getOWLModelManager().getActiveOntology().getObjectPropertiesInSignature().toArray();

		//ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
		/*roleList = new JComboBox();
        if (roles.length > 0){
        String [] roleStr = new String[roles.length];

        for (int i = 0; i < roles.length;i++){
        	roleStr[i] = man.render((OWLObject)roles[i]);
        }*/

		//roleList = new JComboBox(roleStr);
		//}
		/*roleList.setRenderer(new BasicComboBoxRenderer(){
    		private static final long serialVersionUID = 3125429064480964846L;

			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    			setText(man.render((OWLObject)value)); 	
    			return this;
    		}  
    	});*/

		buttonHolder.add(executeButton);

		/** List of Defeasible Reasoning Algorithms **/
		reasoningList = new JComboBox(ReasoningType.REASONING_TYPE_NAMES);
		reasoningList.setRenderer(new BasicComboBoxRenderer(){
			/**
			 * 
			 */
			private static final long serialVersionUID = 3125429064480964846L;

			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				setText(value.toString()); 	
				return this;
			}  
		});

		buttonHolder.add(reasoningList);
		editorPanel.add(buttonHolder, BorderLayout.SOUTH);
		editorPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
				Color.LIGHT_GRAY), "Query (class expression)"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
		return editorPanel;
	}

	private JComponent createResultsPanel() {
		JComponent resultsPanel = new JPanel(new BorderLayout(10, 10));
		resultsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
				Color.LIGHT_GRAY), "Results"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
		resultsList = new ResultsList(getOWLEditorKit());
		resultsList.setShowSuperClasses(true);
		//resultsList.setShowSubClasses(true);
		JComponent mainResultPanel = new JPanel(new GridLayout(1,1));
		mainResultPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
				Color.LIGHT_GRAY), "Super Classes & Instances"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
		mainResultPanel.add(ComponentFactory.createScrollPane(resultsList));
		resultsPanel.add(mainResultPanel, BorderLayout.CENTER);
		return resultsPanel;
	}

	private JComponent createExceptionsPanel() {
		JComponent exceptionsPanel = new JPanel(new BorderLayout(10, 10));
		exceptionsPanel.setPreferredSize(new Dimension(500,500));
		exceptionsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
				Color.LIGHT_GRAY), "Exceptions"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
		OWLReasonerManager reasonerManager = getOWLModelManager().getOWLReasonerManager();
		exceptionsList = new ExceptionsList(getOWLEditorKit(), getOWLModelManager().getOWLDataFactory(), reasonerManager.getCurrentReasonerFactory().getReasonerFactory());
		exceptionsPanel.add(ComponentFactory.createScrollPane(exceptionsList));
		refreshButton = new JButton(new AbstractAction("Refresh") {
			/**
			 * 
			 */
			private static final long serialVersionUID = -1833321282125901561L;

			public void actionPerformed(ActionEvent e) {
				computeRanking();
				if (isTBoxConsistent)
					saveRanking();
			}
		});

		exceptionsPanel.add(refreshButton, BorderLayout.SOUTH);
		return exceptionsPanel;
	}

	/*private JComponent createOptionsBox() {
        Box optionsBox = new Box(BoxLayout.Y_AXIS);
        showSuperClassesCheckBox = new JCheckBox(new AbstractAction("Super classes") {
            /**
	 * 
	 */
	/*private static final long serialVersionUID = 1531417504526875891L;

            public void actionPerformed(ActionEvent e) {
                resultsList.setShowSuperClasses(showSuperClassesCheckBox.isSelected());
                doQuery();
            }
        });
        optionsBox.add(showSuperClassesCheckBox);
        optionsBox.add(Box.createVerticalStrut(3));

        showSubClassesCheckBox = new JCheckBox(new AbstractAction("Subclasses") {
            private static final long serialVersionUID = 696913194074753412L;

            public void actionPerformed(ActionEvent e) {
                resultsList.setShowSubClasses(showSubClassesCheckBox.isSelected());
                doQuery();
            }
        });
        optionsBox.add(showSubClassesCheckBox);
        optionsBox.add(Box.createVerticalStrut(3));

        return optionsBox;
    }*/


	protected void disposeOWLView() {
		getOWLModelManager().removeOntologyChangeListener(ontListener);   
	}

	/*private void updateGUI() {
        showSuperClassesCheckBox.setSelected(resultsList.isShowSuperClasses());
        showSubClassesCheckBox.setSelected(resultsList.isShowSubClasses());
    }*/

	private void loadRanking() {
		System.out.println("ATTEMPTING TO LOAD RANKING FROM FILE");
		OWLOntology theOntology = getOWLModelManager().getActiveOntology();
		Set<OWLAxiom> logicalAxioms = new HashSet<OWLAxiom>(theOntology.getLogicalAxioms());

		Set<OWLAxiom> strictAxioms = new HashSet<OWLAxiom>();
		Set<OWLAxiom> rankedAxioms = new HashSet<OWLAxiom>();

		Utility u = new Utility();
		Iterator<OWLAxiom> iter = logicalAxioms.iterator();

		boolean done = false;
		while (iter.hasNext() && !done){
			OWLAxiom tmp = iter.next();
			if (u.isDefeasible(tmp)){
				if (!u.hasRankAnnotation(tmp)){
					done = true;
				}
				else{
					//core case
					rankedAxioms.add(tmp);
				}
			}
			else{
				strictAxioms.add(tmp);
			}
		}

		if (done || rankedAxioms.isEmpty()){//NO EXISTING/STORED RANKING
			System.out.println("NO STORED RANKING SO RECOMPUTING");
			computeRanking(); 
			if (isTBoxConsistent)
				saveRanking();
		}
		else{ //THERE IS A STORED RANKING
			System.out.println("THERE IS A STORED RANKING SO JUST LOADING IT");
			ArrayList<Rank> ranks = new ArrayList<Rank>();

			int lowest = theOntology.getAxiomCount();
			int highest = -1;
			for (OWLAxiom axiom: rankedAxioms){
				int idx = u.getRankIndex(axiom);
				if (idx < lowest)
					lowest = idx;
				if (idx > highest)
					highest = idx;

				ranks.add(new Rank(new ArrayList<OWLAxiom>(Collections.singleton(axiom)), idx));
			}

			System.out.println("ATTEMPTING MERGING OF RANKS");
			ranking = u.mergeRanks(ranks, lowest, highest);
			if (ranking.getAxioms().isEmpty()){
				System.out.println("MERGING WAS UNSUCCESSFUL - RECOMPUTING");
				computeRanking();
				if (isTBoxConsistent)
					saveRanking();
			}
			else{
				System.out.println("MERGING SUCCESSFUL - SAVING RANKING");
				ranking.setInfiniteRank(new Rank(new ArrayList<OWLAxiom>(strictAxioms)));
				//System.out.println("before saving:");
				//System.out.println("--------------");
				////System.out.println(ranking);
				//System.out.println();
				if (isTBoxConsistent)
					saveRanking();
			}
		}

		if (isTBoxConsistent) {
			try {
				//System.out.println("after loading:");
				//System.out.println("--------------");
				//System.out.println(ranking);
				//System.out.println();

				exceptionsList.setExceptionsList(ranking);
			} 
			catch (OWLOntologyCreationException e) {
				System.out.println("Exception caught trying to display exceptions");
				e.printStackTrace();
			}
		}
	}

	private void saveRanking() {
		OWLOntologyManager man = getOWLModelManager().getOWLOntologyManager();
		OWLOntology theOntology = getOWLModelManager().getActiveOntology();
		Utility u = new Utility();

		try {
			for (Rank rank: ranking.getRanking()){
				for (OWLAxiom a: rank.getAxioms()){
					Set<OWLAnnotation> annos = a.getAnnotations();
					Set<OWLAnnotation> newAnnos = new HashSet<OWLAnnotation>();
					for (OWLAnnotation an: annos){
						if (!an.getProperty().equals(u.rankAnnotationProperty)){
							newAnnos.add(an);
						}
					}

					OWLAxiom aTmp = a.getAxiomWithoutAnnotations();
					OWLAnnotation anno = getOWLModelManager().getOWLDataFactory().getOWLAnnotation(u.rankAnnotationProperty, getOWLModelManager().getOWLDataFactory().getOWLLiteral(rank.getIndex()));
					newAnnos.add(anno);    			
					man.removeAxiom(theOntology, a);
					man.addAxiom(theOntology, aTmp.getAnnotatedAxiom(newAnnos));    			
				}
			}

			// save ontology consistency status
			OWLAnnotation consistencyStatus = null;
			OWLDataFactory df = getOWLModelManager().getOWLDataFactory();
			OWLAnnotation inconsistentAnn = df.getOWLAnnotation(u.consistencyAnnotationProperty, df.getOWLLiteral(false));
			OWLAnnotation consistentAnn = df.getOWLAnnotation(u.consistencyAnnotationProperty, df.getOWLLiteral(true));

			if (isConsistent) {
				consistencyStatus = df.getOWLAnnotation(u.consistencyAnnotationProperty, df.getOWLLiteral(true));
			}
			else {
				consistencyStatus = df.getOWLAnnotation(u.consistencyAnnotationProperty, df.getOWLLiteral(false));
			}
			man.applyChange(new RemoveOntologyAnnotation(theOntology, inconsistentAnn));
			man.applyChange(new RemoveOntologyAnnotation(theOntology, consistentAnn));		
			man.applyChange(new AddOntologyAnnotation(theOntology, consistencyStatus));
		}
		catch (NullPointerException n) {
			System.out.println("No ranking to save to file: perhaps TBox is inconsistent so no ranking can be computed.");
			
			Set<OWLAxiom> axioms = theOntology.getAxioms();
			for (OWLAxiom a: axioms) {
				Set<OWLAnnotation> annos = a.getAnnotations();
				Set<OWLAnnotation> finalAnnos = new HashSet<OWLAnnotation>();
				boolean hasRankAnno = false;
				for (OWLAnnotation ann: annos) {
					if (!ann.getProperty().equals(u.rankAnnotationProperty)) {
						finalAnnos.add(ann);
					}
					else {
						hasRankAnno = true;
					}
				}
				
				if (hasRankAnno) {
					OWLAxiom bareAx = a.getAxiomWithoutAnnotations();
					OWLAxiom newAx = bareAx.getAnnotatedAxiom(finalAnnos);
					man.removeAxiom(theOntology, a);
					man.addAxiom(theOntology, newAx);
				}
				
			}
		}
	}

	private boolean ontHasConsistencyAnnotation() {
		Set<OWLAnnotation> annotations = getOWLModelManager().getActiveOntology().getAnnotations();
		Utility u = new Utility();
		for (OWLAnnotation an: annotations) {
			if (an.getProperty().equals(u.consistencyAnnotationProperty)){
				return true;
			}
		}		
		return false;
	}

	private void doQuery() {

		// Clear results
		resultsList.clear();

		try {
			// If ranking needs to be recomputed
			if (requiresRefresh || !ontHasConsistencyAnnotation()) {
				System.out.println();
				System.out.println("RECOMPUTING RANKING");
				System.out.println();
				computeRanking();
				if (isTBoxConsistent)
					saveRanking();
			}

			// If TBox is preferentially consistent
			if (isTBoxConsistent) {
				System.out.println();
				System.out.println("IS TBOX CONSISTENT");

				// Sanity check: print ranking
				System.out.println();
				System.out.println("Ranking:");
				System.out.println(ranking);
				System.out.println();

				// First check preferential consistency. At this point we know for sure that the ontology is NOT TBox inconsistent.
				OWLReasonerManager reasonerManager = getOWLModelManager().getOWLReasonerManager();         	
				OWLClassExpression desc = owlDescriptionEditor.createObject();
				DefeasibleInferenceComputer dic = new DefeasibleInferenceComputer(reasonerManager.getCurrentReasonerFactory().getReasonerFactory(), ranking);
				boolean ontologyConsistent = dic.isConsistent(ranking);

				// If the ontology as a whole is consistent (including ABox if applicable)
				if (ontologyConsistent) {  
					System.out.println();
					System.out.println("IS PREFERENTIALLY CONSISTENT");
					System.out.println();

					isConsistent = true;

					// If I have a valid query (class expression)
					if (desc != null){
						OWLExpressionUserCache.getInstance(getOWLModelManager()).add(desc, owlDescriptionEditor.getText());
						ReasoningType algorithm = ReasoningType.NAME_TYPE_MAP.get((String)reasoningList.getSelectedItem());
						// compute all super classes and instances for the given class expression
						resultsList.setOWLClassExpression(desc, algorithm, ranking, eTransforms);
					}
				}
				else {
					// The TBox is preferentially consistent but when we add the ABox then it becomes preferentially inconsistent
					isConsistent = false;
					System.out.println("Ontology is ABox inconsistent!");
					JOptionPane.showMessageDialog(null, "The Individual assertions in your ontology conflict with the Class axioms.\n DIP will no longer be able to make useful inferences about this ontology.\n Please check your assertions and class definitions.", "DIP: Inconsistent Ontology Warning", JOptionPane.WARNING_MESSAGE);
				}
			}
			else {
				requiresRefresh = true;
			}
		}
		catch (OWLException e) {
			System.out.println("Exception when executing query.");
			e.printStackTrace();
		}
	}

	private void computeRanking() throws InconsistentOntologyException{
		try {
			// Compute ranking
			RationalRankingAlgorithm rankingalg = new RationalRankingAlgorithm(getOWLEditorKit().getModelManager().getOWLReasonerManager().getCurrentReasonerFactory().getReasonerFactory(), getOWLModelManager().getActiveOntology());
			ranking = rankingalg.computeRanking();
			// Collect E_0, E_1,  ... , E_n
			eTransforms = rankingalg.getETransforms();
			// Render the ranking in the exceptions panel 
			exceptionsList.setExceptionsList(ranking);
		} catch (OWLOntologyCreationException e) {
			System.out.println("Ontology creation exception.");
			e.printStackTrace();
		} catch (OWLException e) {
			System.out.println("Error computing ranking.");
			e.printStackTrace();
		}
		catch (InconsistentOntologyException e) {
			//This can only mean TBox inconsistency e.g. "A or not A SubClassOf Nothing"
			// TBox inconsistency
			exceptionsList.clear();
			resultsList.clear();
			isTBoxConsistent = false;
			requiresRefresh = true;
			System.out.println("Ontology is TBox inconsistent!");
			JOptionPane.showMessageDialog(null, "The Class definitions in your ontology are contradictory.\n DIP will no longer be able to make useful inferences about this ontology.\n Please check your class definitions.", "DIP: Inconsistent Ontology Warning", JOptionPane.WARNING_MESSAGE);            
		}
		finally {
			if (isTBoxConsistent) {
				System.out.println("isTBoxConsistent.");
				requiresRefresh = false;
				isConsistent = true;
			}
			else {
				System.out.println("isTBox IN Consistent.");
				requiresRefresh = true;
				isConsistent = false;
			}
		}
	}

	private void doAdd() {
		try {
			OWLClassExpression desc = owlDescriptionEditor.createObject();
			OWLEntityCreationSet<OWLClass> creationSet = CreateDefinedClassPanel.showDialog(desc, getOWLEditorKit());
			if (creationSet != null) {
				List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>(creationSet.getOntologyChanges());
				OWLDataFactory factory = getOWLModelManager().getOWLDataFactory();
				OWLAxiom equiv = factory.getOWLEquivalentClassesAxiom(creationSet.getOWLEntity(), desc);
				changes.add(new AddAxiom(getOWLModelManager().getActiveOntology(), equiv));
				getOWLModelManager().applyChanges(changes);
				if (isSynchronizing()){
					getOWLEditorKit().getOWLWorkspace().getOWLSelectionModel().setSelectedEntity(creationSet.getOWLEntity());    
				}
			}
		}
		catch (OWLException e) {
			System.out.println("Exception caught trying to parse query.");
			e.printStackTrace();
		}
	}
}
