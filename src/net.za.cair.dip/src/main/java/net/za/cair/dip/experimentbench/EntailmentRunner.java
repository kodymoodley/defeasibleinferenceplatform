package net.za.cair.dip.experimentbench;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.semanticweb.owl.explanation.api.ExplanationException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import net.za.cair.dip.DefeasibleInferenceComputer;
import net.za.cair.dip.model.Query;

public class EntailmentRunner implements Callable<DefeasibleInferenceComputer>{

	public final DefeasibleInferenceComputer dic;
	public final Query query;
	
	public EntailmentRunner(DefeasibleInferenceComputer dic, Query query){
		this.dic = dic;
		this.query = query;
	}
	
	@Override
	public DefeasibleInferenceComputer call() throws TimeoutException, ExplanationException, OWLOntologyCreationException, InterruptedException, ExecutionException {
			dic.isEntailed(query);
			return dic;
	}

}
