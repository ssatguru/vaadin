package org.ssatguru.vaadin.watson;

import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;

public class NavView extends NavDark{
	

	Component order;
	Component about;
	

	String button2Caption = "Visual Recognition";
	String button3Caption = "About";
	
	Button buttonSelected;
	
	HorizontalLayout navContent = new HorizontalLayout();
	
	public NavView(String username,Component order, Component about){
		super();

		this.order = order;
		this.about = about;
		
		//navContent.setSizeFull();
		this.scroll_panel.setSizeFull();
		this.scroll_panel.setContent(navContent);
		
		
		setContent(order);
		this.scroll_panel.getContent().setSizeUndefined();
		
		this.buttonSelected = this.menuButton2;
	
		NavButtonsListener navButtonsListener =  new NavButtonsListener();
		

		this.menuButton2.addClickListener(navButtonsListener);
		this.menuButton3.addClickListener(navButtonsListener);
		
	}
	

	
	public void setContent(Component component){
		navContent.removeAllComponents();
		navContent.addComponent(component);
		
		//this.scroll_panel.setContent(component);

	}
	
	class NavButtonsListener implements Button.ClickListener{

		@Override
		public void buttonClick(ClickEvent event) {
			String buttonId = event.getButton().getCaption();
			
		    if (buttonId.equals(button2Caption)) setContent(order);
			else if (buttonId.equals(button3Caption)) setContent(about);
			
			buttonSelected.setStyleName("menu-button");
			buttonSelected = event.getButton();
			buttonSelected.setStyleName("menu-button selected");
			
			
		}
		
	}
	
	

}
