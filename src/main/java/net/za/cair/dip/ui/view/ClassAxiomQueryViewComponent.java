//package net.za.cair.dip.ui.view;
//
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Component;
//import java.awt.Dimension;
//import java.awt.FlowLayout;
//import java.awt.GridLayout;
//import java.awt.event.ActionEvent;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import javax.swing.*;
//import javax.swing.plaf.basic.BasicComboBoxRenderer;
//import java.lang.management.ManagementFactory;
//import net.za.cair.dip.DefeasibleInferenceComputer;
//import net.za.cair.dip.DefeasibleInferenceHelperClass;
//import net.za.cair.dip.model.OntologyStructure;
//import net.za.cair.dip.model.Rank;
//import net.za.cair.dip.model.Ranking;
//import net.za.cair.dip.model.ReasoningType;
//import net.za.cair.dip.transform.RationalRankingAlgorithm;
//import net.za.cair.dip.util.Utility;
//import org.protege.editor.core.ui.util.ComponentFactory;
//import org.protege.editor.core.ui.util.InputVerificationStatusChangedListener;
//import org.protege.editor.owl.model.classexpression.OWLExpressionParserException;
//import org.protege.editor.owl.model.inference.OWLReasonerManager;
//import org.protege.editor.owl.ui.clsdescriptioneditor.ExpressionEditor;
//import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
//import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
//import org.semanticweb.owlapi.model.*;
//import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
//
///*
// * Copyright (C) 2011, Centre for Artificial Intelligence Research
// *
// * Modifications to the initial code base are copyright of their
// * respective authors, or their employers as appropriate.  Authorship
// * of the modifications may be determined from the ChangeLog placed at
// * the end of this file.
// *
// * This library is free software; you can redistribute it and/or
// * modify it under the terms of the GNU Lesser General Public
// * License as published by the Free Software Foundation; either
// * version 2.1 of the License, or (at your option) any later version.
//
// * This library is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// * Lesser General Public License for more details.
//
// * You should have received a copy of the GNU Lesser General Public
// * License along with this library; if not, write to the Free Software
// * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
// */
//
///**
// * Author: Kody Moodley<br>
// * Centre for Artificial Intelligence Research<br>
// * UKZN and CSIR<br>
// * Date: 10-Oct-2011<br><br>
// */
//
//public class ClassAxiomQueryViewComponent extends AbstractOWLViewComponent{
//    private static final long serialVersionUID = -4515710047558710080L;
//    private JComponent resultsPanel, editorPanel;
//    private OWLReasonerFactory reasonerFactory;
//    public static OWLAnnotationProperty rampQueryProperty;
//    private JPanel editorToolsPanel, clsExpressionCheckPanel;
//    private ExpressionEditor<OWLClassExpression> classInclusionBox;
//    private ExceptionsList exceptionRanking;
//    private ArrayList<OWLAxiom> ontology;
//    private JButton checkButton;             
//    private JCheckBox box;        
//    private JComboBox reasoningList; 
//    private Utility u; 
//    
//    private OWLOntologyChangeListener listener = new OWLOntologyChangeListener() {
//        public void ontologiesChanged(List<? extends OWLOntologyChange> changes) {
//            try {
//				processChanges(changes);
//			} catch (OWLException e) {
//				e.printStackTrace();
//			}
//        }
//    };
//    
//    @Override
//    protected void disposeOWLView() {
//    	getOWLModelManager().removeOntologyChangeListener(listener);
//    }
//
//    @Override
//    protected void initialiseOWLView() throws Exception { 
//    	ManagementFactory.getThreadMXBean();
//    	setLayout(new BorderLayout());    	    
//    	reasonerFactory = getOWLModelManager().getOWLReasonerManager().getCurrentReasonerFactory().getReasonerFactory();
//    	getOWLModelManager().addOntologyChangeListener(listener);
//    	u = new Utility();
//        initOntologyPlusDesc();     
//        reasonerBoxSetup(); 
//        editorPanel = createEditor();
//        resultsPanel = createResult();
//        add(editorPanel, BorderLayout.NORTH);
//        add(resultsPanel, BorderLayout.CENTER);
//    }
//    
//    /**
//     * Initialization of text to axiom parser and throwing away all non-logical axioms.
//     */
//    private void initOntologyPlusDesc(){
//    	final OWLExpressionChecker<OWLClassExpression> checker = getOWLModelManager().getOWLExpressionCheckerFactory().getOWLClassExpressionChecker();
//        classInclusionBox = new ExpressionEditor<OWLClassExpression>(getOWLEditorKit(), checker);
//        classInclusionBox.addStatusChangedListener(new InputVerificationStatusChangedListener(){
//            public void verifiedStatusChanged(boolean newState) {
//                checkButton.setEnabled(newState);
//            }
//        });
//        classInclusionBox.setPreferredSize(new Dimension(100, 50));
//        
//        ontology = new ArrayList<OWLAxiom>();
//        Set<OWLLogicalAxiom> logicalAxioms = getOWLModelManager().getActiveOntology().getLogicalAxioms();
//        ontology.addAll(logicalAxioms);
//    } 
//    
//    /**
//     * Reasoning algorithm selector drop-down.
//     */
//    @SuppressWarnings("unchecked")
//	private void reasonerBoxSetup(){
//    	reasoningList = new JComboBox(ReasoningType.REASONING_TYPE_NAMES);
//    	reasoningList.setRenderer(new BasicComboBoxRenderer(){
//    		/**
//			 * 
//			 */
//			private static final long serialVersionUID = 3125429064480964846L;
//
//			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
//    			setText(value.toString()); 	
//    			return this;
//    		}  
//    	});
//    	//reasoningList.setSelectedIndex(0);
//   } 
//    
//    /**
//     * Obtains class expression from the query text box.
//     * @return
//     * @throws OWLException 
//     */
//   private OWLClassExpression getClassExpression() throws OWLException{
//	   OWLClassExpression clsEx = null;
//	   try {
//			clsEx = (OWLClassExpression)classInclusionBox.createObject();
//		} catch (OWLExpressionParserException e1) {
//			JOptionPane.showMessageDialog(this, "Invalid Class Expression");
//		}
//	   return clsEx;
//   }
//   
//   /**
//    * Is there an identical axiom in the saved query list to the one we are trying to add?
//    * @param axiom
//    * @return
//    */
//   private boolean matchFound(OWLAxiom axiom){
//	   OWLAnnotationAssertionAxiom queryAnno = null;
//	   OWLOntology ontology = getOWLModelManager().getActiveOntology();
//	   for (OWLAnnotationAssertionAxiom anno: ontology.getAnnotationAssertionAxioms(Utility.rampQueryProperty.getIRI())){
//		   queryAnno = anno;
//	   }
//	   if (queryAnno == null){
//		   return false;
//	   }
//	   String queryAnnoStr = queryAnno.getValue().toString();
//	   Pattern pattern = Pattern.compile(getOWLModelManager().getRendering(axiom));
//	   Matcher matcher = pattern.matcher(queryAnnoStr);
//	   return matcher.find();
//   }
//   
//   /** 
//    * Handles addition of queries to the save query list.
//    * @param axiom
//    */
//   private void addToQueryBox(OWLAxiom axiom){
//	   if (!matchFound(axiom)){
//	   OWLDataFactory df = getOWLModelManager().getOWLDataFactory();
//	   
//	   OWLAnnotationAssertionAxiom queryAnno = null;
//	   OWLOntology ontology = getOWLModelManager().getActiveOntology();
//	   for (OWLAnnotationAssertionAxiom anno: ontology.getAnnotationAssertionAxioms(Utility.rampQueryProperty.getIRI())){
//		   queryAnno = anno;
//	   }
//	   
//	   if (queryAnno == null){
//		  //No query annotation so have to create one
//		  if (!box.isSelected()){
//			  String axiomStr = "|" + getOWLModelManager().getRendering(axiom) + "*;";
//			  axiomStr = u.addColon(axiomStr);
//			  queryAnno = df.getOWLAnnotationAssertionAxiom(Utility.rampQueryProperty, Utility.rampQueryProperty.getIRI(), df.getOWLLiteral(axiomStr, ""));
//		  }
//		  else{
//			  String axiomStr = "|" + getOWLModelManager().getRendering(axiom) + ";";
//			  axiomStr = u.addColon(axiomStr);
//			  queryAnno = df.getOWLAnnotationAssertionAxiom(Utility.rampQueryProperty, Utility.rampQueryProperty.getIRI(), df.getOWLLiteral(axiomStr, ""));
//		  }
//		  
//		  getOWLModelManager().getOWLOntologyManager().addAxiom(ontology, queryAnno);
//	   }
//	   else{
//		   //Append axiom to existing query annotation
//		   String queryAnnoStr = u.getRidOfJunk(queryAnno.getValue().toString());
//		   getOWLModelManager().getOWLOntologyManager().removeAxiom(ontology, queryAnno);
//		   int lastChar = queryAnnoStr.length() - 1;
//		   if (queryAnnoStr.charAt(lastChar) == ';' || queryAnnoStr.charAt(lastChar) == '|'){
//			   String axiomStr = getOWLModelManager().getRendering(axiom);
//			   axiomStr = u.addColon(axiomStr);
//			   queryAnnoStr += axiomStr;
//		   }
//		   else{
//			   String axiomStr = getOWLModelManager().getRendering(axiom);
//			   axiomStr = u.addColon(axiomStr);
//			   queryAnnoStr += ";" + axiomStr;
//		   }
//		   OWLAnnotationAssertionAxiom newQueryAnno = null;
//		   if (!box.isSelected()){
//			   queryAnnoStr += "*;";
//			   newQueryAnno = df.getOWLAnnotationAssertionAxiom(Utility.rampQueryProperty, Utility.rampQueryProperty.getIRI(), df.getOWLLiteral(queryAnnoStr, ""));
//		   }
//		   else{
//			   queryAnnoStr += ";";
//			   newQueryAnno = df.getOWLAnnotationAssertionAxiom(Utility.rampQueryProperty, Utility.rampQueryProperty.getIRI(), df.getOWLLiteral(queryAnnoStr, ""));
//		   }
//	
//		   getOWLModelManager().getOWLOntologyManager().addAxiom(ontology, newQueryAnno);
//	   }  
//	   }
//   	}
//    
//   /**
//    * Executes the specified query in the query text box.
//    * @param OWLClassExpression
//    * @throws OWLException
//    */
//   private void executeQuery(OWLClassExpression clsEx) throws OWLException{
//	   //test
//	   System.out.println("look what im doing!!!");
//	   clsEx = getOWLModelManager().getOWLDataFactory().getOWLThing();
//	   System.out.println("look what I did!!!");
//	   //test
//	   /********** Preprocessing Stage ***********/
//	   OntologyStructure structure = new OntologyStructure(getOWLModelManager().getActiveOntology());
//	   ReasoningType algorithm = ReasoningType.NAME_TYPE_MAP.get((String)reasoningList.getSelectedItem());
//	   
//	   RationalRankingAlgorithm rankingConstruction = new RationalRankingAlgorithm(reasonerFactory, structure, new HashSet<OWLClassExpression>());
//	   Ranking ranking = rankingConstruction.computeRanking();
//	   Rank infiniteRank = rankingConstruction.getInfiniteRank();
//	   DefeasibleInferenceHelperClass dihc = new DefeasibleInferenceHelperClass(reasonerFactory, ranking);
////	   ArrayList<Rank> ccompatRanks =  dihc.getCCompatibleSubset(ranking.getRanking(), clsEx);
//	   ArrayList<Rank> ccompatRanks =  dihc.getCCompatibleSubset(ranking.getRanking(), getOWLModelManager().getOWLDataFactory().getOWLThing());	   
//	   OWLClassExpression ccompatConcept = dihc.getInternalisation(ccompatRanks);
//			   
//	   boolean defeasible = false;
//	   if (!box.isSelected())
//		   defeasible = true;
//	   
//	   /********* Defeasible query ************/
//	   if (defeasible){
//		   DefeasibleInferenceComputer dic = new DefeasibleInferenceComputer(reasonerFactory, getOWLModelManager().getActiveOntology());
//		   dic.computeSuperClasses(clsEx, ccompatConcept, infiniteRank);
//		   Set<OWLClass> typicalSuperClasses = dic.getSuperClassesTypical();
//		   Set<OWLClass> strictSuperClasses = dic.getSuperClassesStrict();
//		   exceptionRanking.setExceptionsList(ranking);
//	   } 	  
//   }
//    
//   /**
//    * Construct entailment checking button and other aspects of the tool-box.
//    * @return
//    */
//   public JComponent createEditor(){
//    	initializeComponents();    	    	       
//               
//    	for (OWLAnnotationAssertionAxiom anno: getOWLModelManager().getActiveOntology().getAnnotationAssertionAxioms(Utility.rampSelectedQueryIRI)){
//    		String axiomStr = u.getRidOfJunk(anno.getValue().toString());
//    		int length = axiomStr.length();
//    		if (axiomStr.charAt(length-1) == '*'){
//    			//defeasible query
//    			classInclusionBox.setText(axiomStr.substring(0, length-1));
//    		}
//    		else{
//    			//strict query
//    			classInclusionBox.setText(axiomStr);
//    			box.setSelected(true);
//    		}
//    	}    	    
//    	
//        checkButton = new JButton(new AbstractAction("Check") {			
//			private static final long serialVersionUID = 1L;
//			public void actionPerformed(ActionEvent e){
//				OWLClassExpression c = null;
//				try {
//					c = getClassExpression();
//				} catch (OWLException e2) {
//					// TODO Auto-generated catch block
//					e2.printStackTrace();
//				}
//				try {
//					executeQuery(c);
//				} catch (OWLException e1) {	}
//				catch (NullPointerException n){	}
//			}	
//		});               
//         
//        constructFinalPanel();                                             
//        return editorPanel;
//    }
//    
//   /**
//    * Constructs the results UI panel.
//    * @return
//    */
//    public JComponent createResult(){
//        JComponent resultsPanel = new JPanel(new BorderLayout());
//        resultsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
//                 Color.LIGHT_GRAY), "Exceptions"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));                
//        OWLReasonerManager reasonerManager = getOWLModelManager().getOWLReasonerManager();
//		exceptionRanking = new ExceptionsList(getOWLEditorKit(), getOWLModelManager().getOWLDataFactory(), reasonerManager.getCurrentReasonerFactory().getReasonerFactory());
//        
//        resultsPanel.add(ComponentFactory.createScrollPane(exceptionRanking));       
//        return resultsPanel;
//     }
//    
//    /**
//     * Constructs individual components of the reasoner tool-box UI.
//     */
//    private void initializeComponents(){
//    	editorPanel = new JPanel(new BorderLayout());    	        
//    	clsExpressionCheckPanel = new JPanel(new GridLayout(2,1));                                                                                                 
//        clsExpressionCheckPanel.add(ComponentFactory.createScrollPane(classInclusionBox), BorderLayout.CENTER);
//        clsExpressionCheckPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(      
//        		Color.LIGHT_GRAY), "Query (class expression)"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));        
//        editorToolsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));   
//        box = new JCheckBox("Strict axiom?"); 
//    }
//    
//    /**
//     * Combines components of the reasoner tool-box UI.
//     */
//    private void constructFinalPanel(){
//    	editorToolsPanel.add(reasoningList);
//    	editorToolsPanel.add(checkButton);
//        editorToolsPanel.add(box);                
//        clsExpressionCheckPanel.add(editorToolsPanel);    
//        editorPanel.add(clsExpressionCheckPanel, BorderLayout.NORTH);
//    }
//
//    /**
//     * Updates query text box to the selected axiom from the saved query list.
//     * @param changes
//     * @throws OWLException
//     */
//	public void processChanges(List<? extends OWLOntologyChange> changes)
//			throws OWLException {
//		for (OWLOntologyChange c: changes){
//			if (c.getAxiom() instanceof OWLAnnotationAssertionAxiom){
//				OWLAnnotationAssertionAxiom anno = (OWLAnnotationAssertionAxiom)c.getAxiom();
//				if (anno.getProperty().getIRI().equals(Utility.rampSelectedQueryProperty.getIRI())){
//					String axiomStr = u.getRidOfJunk(anno.getValue().toString());
//		    		int length = axiomStr.length();
//		    		if (axiomStr.charAt(length-1) == '*'){
//		    			//defeasible query
//		    			classInclusionBox.setText(axiomStr.substring(0, length-1));
//		    			box.setSelected(false);
//		    		}
//		    		else{
//		    			//strict query
//		    			classInclusionBox.setText(axiomStr);
//		    			box.setSelected(true);
//		    		}
//		 
//				}
//			}
//		}
//	}                     
//}
