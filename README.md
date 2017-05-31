# DIP (Defeasible-Inference Platform) Plugin:

A Protégé 5 plugin that enables users to specify defeasible subclass axioms and also compute the ``typical'' superclasses of a given class expression. The plugin is split into two parts: 1) the tab plugin (net.za.cair.dip.jar) and 2) a tiny extension of Protégé's editor plugin (protege-owl-editor.jar) that enables users to toggle subclass axioms to be defeasible.

#### Building the tab plugin from source

Requirements:

+ Apache's [Maven](http://maven.apache.org/index.html).
+ A tool for checking out a [Git](http://git-scm.com/) repository.

Steps:

1. Get a copy of the example code:

        git clone https://github.com/kodymoodley/defeasibleinferenceplatform.git
    
2. Change into the defeasibleinferenceplatform directory.

3. Type mvn clean package.  On build completion, the "target" directory will contain a net.za.cair.dip-${version}.jar file.

#### Installation

Requirements:

+ Protégé 5
+ Java 1.8

Steps:

1. Copy net.za.cair.dip-${version}.jar to the "plugins" subdirectory of your Protégé 5 distribution.

2. Backup protege-editor-owl.jar from the "bundles" subdirectory of your Protégé 5 distribution.

3. Replace your existing protege-editor-owl.jar in the "bundles" subdirectory of Protégé 5 with the one in this repository.
 
#### Accessing plugin features in Protégé

+ Enable DIP tab via the Window | Tabs menu.
+ To flag a subclass axiom as defeasible, toggle the button labelled "d" in the selected class description pane.
+ Access the list of defeasible subclass axioms in the ontology via Window | Views | DIP views.

#### License

[GNU General Public License](https://www.gnu.org/licenses/gpl-3.0.en.html)
