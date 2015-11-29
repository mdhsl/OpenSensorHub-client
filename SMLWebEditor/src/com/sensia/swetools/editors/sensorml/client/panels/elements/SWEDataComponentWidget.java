package com.sensia.swetools.editors.sensorml.client.panels.elements;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.sensia.relaxNG.RNGElement;
import com.sensia.swetools.editors.sensorml.client.AbstractSensorWidget;

public class SWEDataComponentWidget extends AbstractSensorWidget{

	private Label label;
	private Panel container;
	
	public SWEDataComponentWidget(final RNGElement elt) {
		super("", "");
		
		label = new Label("Type: " + toNiceLabel(elt.getName()));
        label.addStyleName("swe-object-type");
        label.addStyleName("swe-property-panel");
        
        container = new FlowPanel();
        container.add(label);
	}

	@Override
	public Widget getWidget() {
		return label;
	}

	@Override
	public Panel getPanel() {
		return container;
	}

}