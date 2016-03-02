package com.sensia.swetools.editors.sensorml.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.xml.client.Document;
import com.sensia.gwt.relaxNG.RNGParser;
import com.sensia.gwt.relaxNG.RNGParserCallback;
import com.sensia.gwt.relaxNG.RNGWriter;
import com.sensia.gwt.relaxNG.XMLSensorMLParser;
import com.sensia.relaxNG.RNGGrammar;
import com.sensia.swetools.editors.sensorml.client.panels.widgets.ISensorWidget;
import com.sensia.swetools.editors.sensorml.client.panels.widgets.ISensorWidget.MODE;
import com.sensia.swetools.editors.sensorml.client.renderer.RNGRendererSML;

public class RNGProcessorSML {

	private List<IParsingObserver> observers;
	private RNGGrammar loadedGrammar;
	private MODE mode = MODE.VIEW;
	
	public RNGProcessorSML(){
		this.observers = new ArrayList<IParsingObserver>();
	}
	
	public void addObserver(final IParsingObserver observer) {
		this.observers.add(observer);
	}
	
	public void parse(final String url) {
		if(url.toLowerCase().endsWith(".rng")) {
			final RNGParser parser = new RNGParser();
			parser.parse(url, new RNGParserCallback() {
				@Override
				public void onParseDone(final RNGGrammar grammar) {
					parseRNG(grammar);
				}
			});
		} else if(url.toLowerCase().endsWith(".xml")) {
		
			//transform XML document into RNG profile
			final XMLSensorMLParser parser = new XMLSensorMLParser();
			parser.parse(url, new RNGParserCallback() {
				
				@Override
				public void onParseDone(final RNGGrammar grammar) {
					parseRNG(grammar);
				}
			});
		}
	}

	public void parse(final String fileName,final String xmlContent) throws Exception {
		//transform XML document into RNG profile
		final XMLSensorMLParser parser = new XMLSensorMLParser();
		final RNGGrammar grammar = parser.parse(fileName, xmlContent);
		parseRNG(grammar);
	}
	
	private void parseRNG(final RNGGrammar grammar) {
		setLoadedGrammar(grammar);
		RNGRendererSML renderer = new RNGRendererSML();
		renderer.visit(grammar);
		ISensorWidget root = renderer.getRoot();
		for(final IParsingObserver observer : observers) {
			observer.parseDone(root);
		}
		
		if(mode == MODE.EDIT) {
			root.switchMode(MODE.EDIT);
		}
	}
	
	public RNGGrammar getLoadedGrammar() {
		return loadedGrammar;
	}

	public void setLoadedGrammar(RNGGrammar loadedGrammar) {
		this.loadedGrammar = loadedGrammar;
	}

	public MODE getMode() {
		return mode;
	}

	public void setMode(MODE mode) {
		this.mode = mode;
	}
}
