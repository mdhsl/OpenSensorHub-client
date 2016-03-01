package com.sensia.swetools.editors.sensorml.client.listeners;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.xml.client.Document;
import com.sensia.gwt.relaxNG.RNGInstanceWriter;
import com.sensia.gwt.relaxNG.XMLSerializer;
import com.sensia.relaxNG.RNGGrammar;
import com.sensia.swetools.editors.sensorml.client.RNGProcessorSML;
import com.sensia.swetools.editors.sensorml.client.panels.source.FileUploadPanel;
import com.sensia.swetools.editors.sensorml.client.panels.source.ISourcePanel;
import com.sensia.swetools.editors.sensorml.client.panels.source.LocalFileSourcePanel;
import com.sensia.swetools.editors.sensorml.client.utils.Utils;

public class ViewAsXMLButtonClickListener implements ClickHandler{

	private RNGProcessorSML sgmlEditorProcessor;
	
	public ViewAsXMLButtonClickListener(final RNGProcessorSML sgmlEditorProcessor) {
		this.sgmlEditorProcessor = sgmlEditorProcessor;
	}
	
	@Override
	public void onClick(ClickEvent event) {
		RNGGrammar grammar = sgmlEditorProcessor.getLoadedGrammar();
		if(grammar != null) {
			RNGInstanceWriter instanceWriter = new RNGInstanceWriter();
			Document dom = instanceWriter.writeInstance(grammar);
			
			final String xml = XMLSerializer.serialize(dom);
			
			String xmlText = xml.replaceAll("<", "&#60;");
	        xmlText = xmlText.replaceAll(">", "&#62;");
	        Label labelXml = new HTML("<pre>" + xmlText + "</pre>", false);
			
	        ScrollPanel panel = new ScrollPanel(labelXml);
			panel.setHeight("768px");
			panel.setWidth("1024px");
			
			final Button validateButton = new Button("Validate");
			final Button closeButton = new Button("Close");
			
			//init file upload panel
			final FileUploadPanel fileUploadPanel = new FileUploadPanel();
			final HTML schemaLabel = new HTML("<b>Schema:</b>");
			
			final DialogBox dialogBox = Utils.createCustomDialogBox(panel, "Sensor ML document",validateButton,closeButton,schemaLabel,fileUploadPanel.getPanel());
			
			closeButton.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					dialogBox.hide();
				}
			});
			
			validateButton.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					validate(xml,fileUploadPanel.getContents());
				}
			});
		}
	}
	
	private void validate(String xmlData,String xmlSchema) {
		GWT.log(xmlData);
		GWT.log(xmlSchema);
		validateFromJs(xmlData,xmlSchema);
		
	}

	private native String validateFromJs(String xmlData,String schemaData) /*-{
		//create an object
		var Module = {
		      xml: xmlData,
		      schema: schemaData,
		      arguments: ["--noout", "--schema", "1", "2"]
		};
		 
		//and call function
		var result = $wnd.validateXML(Module);
		
	}-*/;
}
