package com.sensia.swetools.editors.sensorml.client.panels.widgets.base.line;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sensia.swetools.editors.sensorml.client.listeners.IButtonCallback;
import com.sensia.swetools.editors.sensorml.client.panels.widgets.AbstractSensorElementWidget;
import com.sensia.swetools.editors.sensorml.client.panels.widgets.ISensorWidget;

public class SensorGenericLineWidget extends AbstractSensorElementWidget{
	
	protected VerticalPanel multiLinesPanel;
	protected HorizontalPanel linePanel;
	protected Panel labelPanel;
	protected HTML dotSeparatorLabel;
	protected Panel optPanel;
	protected Panel iconPanel;
	protected Panel defPanel;
	
	protected HorizontalPanel advancedPanel;
	
	private boolean isLabelProvided = false;
	
	private ISensorWidget titleValueWidget;
	private boolean hasTitle = false;
	public SensorGenericLineWidget(String name, TAG_DEF def, TAG_TYPE type) {
		super(name, def, type);
		linePanel = new HorizontalPanel();
		labelPanel = new HorizontalPanel();
		dotSeparatorLabel = new HTML(getDotsLine());
		optPanel = new HorizontalPanel();
		multiLinesPanel = new VerticalPanel();
		advancedPanel = new HorizontalPanel();
		iconPanel = new HorizontalPanel();
		
		advancedPanel.addStyleName("rng-advanced-button");
		advancedPanel.setTitle("Edit");
		
		labelPanel.addStyleName("line-generic-label-panel");
		
		FocusPanel wrapper = new FocusPanel();
		wrapper.add(advancedPanel);
		wrapper.addClickHandler(new ClickHandler() {
		  @Override
		  public void onClick(ClickEvent event) {
			  VerticalPanel container = new VerticalPanel();
			  container.addStyleName("advanced-panel");
			  getAdvancedPanel(container);
			  if(container != null) {
				  displayEditPanel(container,"Edit",new IButtonCallback() {
						@Override
						public void onClick() {
							refresh();
							if(titleValueWidget != null) {
								labelPanel.clear();
								labelPanel.add(new HTML(splitAndCapitalize(titleValueWidget.getName())));
							}
						}
					});
			  }
		  }
		});
		
		//for generic ones
		defPanel = new HorizontalPanel();
		
		//order elements
		linePanel.add(labelPanel);
		linePanel.add(dotSeparatorLabel);
		linePanel.add(optPanel);
		linePanel.add(iconPanel);
		linePanel.add(defPanel);
		linePanel.add(wrapper);
		
		multiLinesPanel.add(linePanel);
		activeMode(getMode());
	}

	@Override
	protected void addSensorWidget(ISensorWidget widget) {
		if(widget.appendTo() == APPENDER.OVERRIDE_LINE) {
			multiLinesPanel.clear();
			multiLinesPanel.add(widget.getPanel());
		}else if(widget.getType() == TAG_TYPE.ATTRIBUTE && widget.getName().equals("name")){
			//provide name as label only if label does not exist
			if(!isLabelProvided) {
				//get the associated value
				titleValueWidget = widget.getElements().get(0);
				labelPanel.add(new HTML(splitAndCapitalize(titleValueWidget.getName())));
				hasTitle = true;
			}
		} 
		//handle generic panel like identifier
		else if(widget.getType() == TAG_TYPE.ATTRIBUTE && widget.getName().equals("definition")){
			defPanel.add(widget.getPanel());
		} else if(widget.isIcon()) {
			iconPanel.add(widget.getPanel());
		} 
		else if(widget.getType() == TAG_TYPE.VALUE || (widget.getName().equals("value"))){//case of Term: value
			optPanel.add(widget.getPanel());
			if(!hasTitle) {
				String parentName = widget.getParent().getName();
				if(parentName != null) {
					labelPanel.add(new HTML(toNiceLabel(widget.getParent().getName())));
					//get the associated value
					titleValueWidget = widget.getParent();
				}
			}
		} else if (widget.getType() == TAG_TYPE.ELEMENT && widget.getName().equals("label")) {
			labelPanel.clear();
			labelPanel.add(widget.getPanel());
			isLabelProvided=true;
			hasTitle = true;
		} else {
			//looking for label
			//prior display for label if exists
			for(ISensorWidget child : widget.getElements()) {
				//for <element name="swe:label"> or <element name="gml:name">
				if(child.getType() == TAG_TYPE.ELEMENT) {
					if((child.getName().equals("label"))) {
						labelPanel.clear();
						labelPanel.add(child.getPanel());
						isLabelProvided=true;
						hasTitle = true;
						//get the associated value
						titleValueWidget = findWidget(child, "value", TAG_DEF.RNG, TAG_TYPE.VALUE);
						break;
					} else if((child.getName().equals("name") && !isLabelProvided)) {
						labelPanel.clear();
						labelPanel.add(child.getPanel());
						//get the associated value
						titleValueWidget = findWidget(child, "value", TAG_DEF.RNG, TAG_TYPE.VALUE);
						hasTitle = true;
					}
				}
			}
			
			//looking for element to append to line
			recursiveAppendTo(widget);
		}
	}

	private boolean recursiveAppendTo(ISensorWidget widget) {
		if(widget.appendTo() == APPENDER.HORIZONTAL || widget.appendTo() == APPENDER.HORIZONTAL_STRICT) {
			if(widget.isIcon()) {
				iconPanel.add(widget.getPanel());
			} //handle generic panel like identifier
			else if(widget.getType() == TAG_TYPE.ATTRIBUTE && widget.getName().equals("definition")){
				defPanel.add(widget.getPanel());
			} else if(widget.isIcon()) {
				iconPanel.add(widget.getPanel());
			} else {
				optPanel.add(widget.getPanel());
			}
			return widget.appendTo() == APPENDER.HORIZONTAL_STRICT;
		} else if(widget.appendTo() == APPENDER.VERTICAL || widget.appendTo() == APPENDER.VERTICAL_STRICT) {
			multiLinesPanel.add(widget.getPanel());
			return widget.appendTo() == APPENDER.VERTICAL_STRICT;
		} else {
			boolean append = false;
			for(ISensorWidget child : widget.getElements()) {
				append = recursiveAppendTo(child);
				if(append) {
					break;
				}
			}
			return append;
		}
	}
	
	@Override
	protected AbstractSensorElementWidget newInstance() {
		return new SensorGenericLineWidget(getName(),getDef(),getType());
	}

	@Override
	public Panel getPanel() {
		return multiLinesPanel;
	}

	@Override
	protected void activeMode(MODE mode) {
		advancedPanel.setVisible(getMode() == MODE.EDIT);
	}
}
