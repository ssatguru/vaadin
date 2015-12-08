package org.ssatguru.vaadin.watson;

import java.util.List;

import javax.servlet.annotation.WebServlet;



import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.UI;


/**
 *
 */
@Theme("mytheme")
@Widgetset("org.ssatguru.vaadin.watson.MyAppWidgetset")
public class MyUI extends UI {


	@Override
	protected void init(VaadinRequest vaadinRequest) {

		Component form = new FormView();
		
		final DragAndDropWrapper dropBox = new DragAndDropWrapper(form);
		dropBox.setDropHandler((DropHandler) form);

		dropBox.setSizeFull();
		
		Component about = new AboutView();

		NavView nav = new NavView("ssatguru",dropBox,about);
		setContent(nav);
	}

	public void switchView(String username){

	}

	@WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
	@VaadinServletConfiguration(ui = MyUI.class, productionMode = true)
	public static class MyUIServlet extends VaadinServlet {
	}
}
