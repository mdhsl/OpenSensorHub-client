/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are Copyright (C) 2016 DHAINAUT.
 All Rights Reserved.
 
 Contributor(s): 
    Mathieu DHAINAUT <mathieu.dhainaut@gmail.com>
 
 ******************************* END LICENSE BLOCK ***************************/

package com.sensia.swetools.editors.sensorml.client.panels;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sensia.swetools.editors.sensorml.client.IParsingObserver;
import com.sensia.swetools.editors.sensorml.client.RNGProcessorSML;
import com.sensia.swetools.editors.sensorml.client.listeners.LoadProfileButtonClickListener;
import com.sensia.swetools.editors.sensorml.client.listeners.ViewAsXMLButtonClickListener;
import com.sensia.swetools.editors.sensorml.client.panels.source.ISourcePanel;
import com.sensia.swetools.editors.sensorml.client.panels.source.LocalFileSourcePanel;
import com.sensia.swetools.editors.sensorml.client.panels.source.UrlSourcePanel;
import com.sensia.swetools.editors.sensorml.client.panels.widgets.ISensorWidget;
import com.sensia.swetools.editors.sensorml.client.panels.widgets.ISensorWidget.MODE;

public class CenterPanel extends Composite implements IParsingObserver{
	private static final long serialVersionUID = -7684111574093800909L;

	private VerticalPanel dynamicCenterPanel;
	private CheckBox edit;
	private ISensorWidget root;
	private RNGProcessorSML smlEditorProcessor;
	
	private static Map<String,String> profiles = new HashMap<String,String>();
	
	static {
		profiles.put("Gamma2070","rng1.0/profiles/CSM/gamma2070.rng");
		profiles.put("Anemometer","rng1.0/profiles/CSM/anemometer.rng");
		profiles.put("Thermometer","rng1.0/profiles/CSM/thermometer-minimal-view.rng");
		
	}
	
	public CenterPanel(final RNGProcessorSML sgmlEditorProcessor){
		sgmlEditorProcessor.addObserver(this);
		this.smlEditorProcessor = sgmlEditorProcessor;

		final Panel profilePanel = getProfilePanel();
		final Panel viewXmlPanel = getXMLViewPanel();

		final VerticalPanel verticalPanel = new VerticalPanel();
		
		//add View as XML button
		Button viewAsXML = new Button("View as XML");
		viewAsXML.addClickHandler(new ViewAsXMLButtonClickListener(sgmlEditorProcessor));
		
		String passedFile = com.google.gwt.user.client.Window.Location.getParameter("url");
		
		String testXDomain = test(passedFile);
		GWT.log(testXDomain);
		if(passedFile == null) {
			HorizontalPanel panel = new HorizontalPanel();
			panel.add(viewXmlPanel);
			panel.add(profilePanel);
			panel.add(viewAsXML);
			
			verticalPanel.add(panel);
		} else {
			//load the file given the url passed as parameter
			//do not display the edit/view options
			smlEditorProcessor.setMode(MODE.VIEW);
			smlEditorProcessor.parse(passedFile);
		}
		
		dynamicCenterPanel = new VerticalPanel();
		verticalPanel.add(dynamicCenterPanel);
		initWidget(verticalPanel);
		
	}

	private String test(String urlF) {
		String message = "";


		try {
		    URL url = new URL(urlF);
		    URLConnection urlConn = url.openConnection();
		    urlConn.setReadTimeout(100000);
		    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
		    String line;

		    while ((line = reader.readLine()) != null) {
		        message = message.concat(line);
		    }
		    reader.close();
		} catch (MalformedURLException e) {
		message = e.getMessage();
		} catch (IOException e) {
		message = e.getMessage();
		}
		
		return message;
	}
	private Panel getXMLViewPanel() {
		final HorizontalPanel panel = new HorizontalPanel();
		panel.setSpacing(20);
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		
		HTML title = new HTML("<b>SensorML XML:</b>");
		final Button load = new Button("Load");
		
		//init radio buttons choices
		final RadioButton fromLocalFileSystem = new RadioButton("myRadioGroup", "from local");
		final RadioButton fromUrl = new RadioButton("myRadioGroup", "from url");
		
		HorizontalPanel hPanel = new HorizontalPanel();
		hPanel.add(fromLocalFileSystem);
		hPanel.add(fromUrl);
		
		//container for either local file system input panel or url valueBox
		final SimplePanel fromPanel = new SimplePanel();
		
		//init file upload panel
		final ISourcePanel fileUploadPanel = new LocalFileSourcePanel(smlEditorProcessor, edit);
		
		//init url load
		final ISourcePanel urlDownloadPanel = new UrlSourcePanel(smlEditorProcessor, edit);
		
		//add to xml panel
		panel.add(title);
		panel.add(hPanel);
		panel.add(fromPanel);
		panel.add(load);
		
		//set from local file system panel as default choice
		fromLocalFileSystem.setChecked(true);
		fromPanel.add(fileUploadPanel.getPanel());
				
		//add listener to handle event
		load.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if(fromLocalFileSystem.getValue()) {
					fileUploadPanel.parseContent();
				} else if(fromUrl.getValue()){
					urlDownloadPanel.parseContent();
				}
			}
		});
		
		//add listener to handle from local file system handler
		fromLocalFileSystem.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if(event.getValue()) {
					fromPanel.clear();
					fromPanel.add(fileUploadPanel.getPanel());
				}
			}
		});
		
		//add listener to handle from local file system handler
		fromUrl.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if(event.getValue()) {
					fromPanel.clear();
					fromPanel.add(urlDownloadPanel.getPanel());
				}
			}
		});
		
		return panel;
	}
	
	private Panel getProfilePanel() {
		final HorizontalPanel panel = new HorizontalPanel();
		panel.setSpacing(20);
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		
		final ListBox profileListBox = new ListBox(false);
		
		profileListBox.addItem("");
		for(final String profileName : profiles.keySet()) {
			profileListBox.addItem(profileName);
		}
		
		HTML titleProfile = new HTML("<b>Profiles:</b>");
		
		Button load = new Button("Apply");
		edit = new CheckBox("Edit");
		
		panel.add(titleProfile);
		panel.add(profileListBox);
		panel.add(load);
		panel.add(edit);
		
		edit.setVisible(false);
		
		edit.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if(root != null){
					MODE mode = (edit.isChecked()) ? MODE.EDIT : MODE.VIEW;
					root.switchMode(mode);
				}
			}
		});
		
		load.addClickHandler(new LoadProfileButtonClickListener(profileListBox,profiles, smlEditorProcessor));
		
		
		return panel;
	}
	/*
	 * (non-Javadoc)
	 * @see com.sensia.swetools.editors.sensorml.client.IParsingObserver#parseDone(com.sensia.swetools.editors.sensorml.client.panels.model.INodeWidget)
	 */
	@Override
	public void parseDone(final ISensorWidget topElement) {
		dynamicCenterPanel.clear();
		dynamicCenterPanel.add(topElement.getPanel());
		root = topElement;
	}
}
