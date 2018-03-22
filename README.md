
# Defeasible-Inference Platform (DIP) Protégé Desktop Plugin

A Protégé Desktop plugin that enables users to specify defeasible subclass axioms and compute "typical" superclasses and instances for a given class expression. The plugin is split into two parts: 1) the tab plugin `net.za.cair.dip.jar` and 2) an **optional** extension of Protégé's editor plugin `protege-owl-editor.jar` that enables users to toggle subclass axioms to be defeasible.

#### For Users: Installing and using the plugin

Requirements:

+ Protégé Desktop
+ Java 1.8

Steps:

1. If Protégé is open, close it.

2. Copy net.za.cair.dip-${version}.jar, in the [releases](https://github.com/kodymoodley/defeasibleinferenceplatform/releases) section of this repository, to the `plugins/` subdirectory of your Protégé Desktop installation.

3. **Optional:** if you would like to add a button to subclass axioms in Protégé allowing you to automatically toggle axioms to be defeasible, replace `protege-editor-owl.jar` in the `bundles/` directory of your Protégé Desktop installation with the version in the [releases](https://github.com/kodymoodley/defeasibleinferenceplatform/releases) section of this repository. Backup the original `protege-editor-owl.jar` so you can revert back to it if you encounter any issues with the new file.

4. Fire up Protégé and consult the [wiki](https://github.com/kodymoodley/defeasibleinferenceplatform/wiki) section of this repository to learn how to use the DIP plugin.

**Note:** DIP uses the currently selected traditional OWL reasoner (from the Reasoner menu in Protégé) in its defeasible reasoning algorithms. Theoretically, you can select any reasoner suitable for the Description Logic (DL) used to formulate your ontology. However, in practice, OWL reasoners can differ in how they implement the [OWLReasoner](http://owlcs.github.io/owlapi/apidocs_4/org/semanticweb/owlapi/reasoner/OWLReasoner.html) interface in the [OWLAPI](http://owlcs.github.io/owlapi). For example, some reasoners will not update their inferences after an ontology changes unless the "classify" method is invoked explicitly. [HermiT](http://www.hermit-reasoner.com/) does not have this issue, and is the recommended choice to use in conjunction with DIP.

[Frequently Asked Questions (FAQs)](https://github.com/kodymoodley/defeasibleinferenceplatform/wiki/Home#frequently-asked-questions-faqs)
 
#### For Developers: Building the tab plugin from source

Requirements:

+ Apache's [Maven](http://maven.apache.org/index.html).
+ A tool for checking out a [Git](http://git-scm.com/) repository.

Steps:

1. Get a copy of the code:

        git clone https://github.com/kodymoodley/defeasibleinferenceplatform.git
    
2. Change into the defeasibleinferenceplatform directory.

3. Type mvn clean package.  On build completion, the "target" directory will contain a net.za.cair.dip-${version}.jar file.
