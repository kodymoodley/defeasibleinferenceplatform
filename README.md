# Defeasible-Inference Platform (DIP) Protégé 5 Plugin

A Protégé 5 plugin that enables users to specify defeasible subclass axioms and compute "typical" superclasses and instances for a given class expression. The plugin is split into two parts: 1) the tab plugin (net.za.cair.dip.jar) and 2) a tiny extension of Protégé's editor plugin (protege-owl-editor.jar) that enables users to toggle subclass axioms to be defeasible.


#### For Users: Installing and using the plugin

Requirements:

+ Protégé 5
+ Java 1.8

Steps:

1. Copy net.za.cair.dip-${version}.jar in this repository to the "plugins" subdirectory of your Protégé 5 distribution.

2. Backup protege-editor-owl.jar from the "bundles" subdirectory of your Protégé 5 distribution.

3. Replace your existing protege-editor-owl.jar in the "bundles" subdirectory of Protégé 5 with the protege-editor-owl-${version}.jar in this repository.
 
#### Accessing plugin features in Protégé

+ Enable DIP tab via the Window | Tabs menu.
+ To flag a subclass axiom as defeasible, toggle the button labelled "d" in the selected class description pane.
+ Access the list of defeasible subclass axioms in the ontology via Window | Views | DIP views.
+ We strongly recommend to use DIP in conjunction with the [HermiT](http://www.hermit-reasoner.com/) Protégé plugin for accurate results. 
+ DIP will only list **named** superclasses for the given class expression. E.g., if the ontology is about university students, and the given class expression is "Student and employedBy some Company", then one might expect the typical superclass: "pays some TaxFee". However, DIP will **not** list this class. **TIP**: add an equivalence class axiom to the ontology e.g. "TaxPayer EquivalentTo pays some TaxFee". DIP will then be able to list "TaxPayer" as a typical superclass of "Student and employedBy some Company".
+ **Note:** Theoretically, DIP can be used with any sound and complete reasoning implementation. However, in practice, reasoners differ in how they implement the OWLReasoner interface. E.g., some reasoners will not update their inferences after an ontology changes unless the "classify" method is invoked explicitly. HermiT does not have this issue.

#### For Developers: Building the tab plugin from source

Requirements:

+ Apache's [Maven](http://maven.apache.org/index.html).
+ A tool for checking out a [Git](http://git-scm.com/) repository.

Steps:

1. Get a copy of the code:

        git clone https://github.com/kodymoodley/defeasibleinferenceplatform.git
    
2. Change into the defeasibleinferenceplatform directory.

3. Type mvn clean package.  On build completion, the "target" directory will contain a net.za.cair.dip-${version}.jar file.

#### License

[GNU General Public License](https://www.gnu.org/licenses/gpl-3.0.en.html)
