package org.ssatguru.vaadin.watson;

import org.vaadin.viritin.label.RichText;

import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class AboutView extends AboutDesign{
	
	public AboutView(){
		super();
		Panel aboutPanel = new Panel();
		aboutPanel.setWidth("800px");
		
		VerticalLayout vl = new VerticalLayout();
		vl.setMargin(true);
		//RichText withSafeHtmlResource(String resourceName) 
		RichText rt = new RichText().withMarkDownResource("/about.md");
		rt.setSizeFull();
		
		aboutPanel.setContent(vl);
		vl.addComponent(rt);
		
		this.main_content_wrapper.addComponent(aboutPanel);
		
	}
	

}
