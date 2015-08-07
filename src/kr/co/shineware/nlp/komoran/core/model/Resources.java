package kr.co.shineware.nlp.komoran.core.model;

import java.io.File;

import kr.co.shineware.nlp.komoran.constant.FILENAME;
import kr.co.shineware.nlp.komoran.modeler.model.IrregularTrie;
import kr.co.shineware.nlp.komoran.modeler.model.Observation;
import kr.co.shineware.nlp.komoran.modeler.model.PosTable;
import kr.co.shineware.nlp.komoran.modeler.model.Transition;

public class Resources {
	private Transition transition;
	private Observation observation;
	private PosTable table;
	private IrregularTrie irrTrie;

	public Transition getTransition() {
		return transition;
	}
	public void setTransition(Transition transition) {
		this.transition = transition;
	}
	public Observation getObservation() {
		return observation;
	}
	public void setObservation(Observation observation) {
		this.observation = observation;
	}
	public PosTable getTable() {
		return table;
	}
	public void setTable(PosTable table) {
		this.table = table;
	}
	public IrregularTrie getIrrTrie() {
		return irrTrie;
	}
	public void setIrrTrie(IrregularTrie irrTrie) {
		this.irrTrie = irrTrie;
	}
	
	private void init(){
		this.table = null;
		this.observation = null;
		this.transition = null;
		this.irrTrie = null;

		this.table = new PosTable();
		this.observation = new Observation();
		this.transition = new Transition();
		this.irrTrie = new IrregularTrie();
	}
	
	public void load(String path) {
		this.init();
		this.table.load(path+File.separator+FILENAME.POS_TABLE);
		this.observation.load(path+File.separator+FILENAME.OBSERVATION);
		this.transition.load(path+File.separator+FILENAME.TRANSITION);
		this.irrTrie.load(path+File.separator+FILENAME.IRREGULAR_MODEL);
		
		this.observation.getTrieDictionary().buildFailLink();
		this.irrTrie.getTrieDictionary().buildFailLink();		
	}
}
