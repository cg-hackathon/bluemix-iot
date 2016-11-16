package com.capgemini.mqtt;


import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 
 * @author cmammado
 *
 */
public class AgentListener implements ServletContextListener {

	private static Agent agent;
	// Path of the graphhopper folder
	public static String graphhopperPath;

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		getAgent().disconnect();

	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		ServletContext context = servletContextEvent.getServletContext();

		// Get the path of file application.prop
		String appPropPath = context.getRealPath("application.prop");
		agent = new Agent(appPropPath);

		// Get the path of the folder containing graphhopper files
		graphhopperPath = context.getRealPath("graphhopper");
		
	}

	public static Agent getAgent() {
		return agent;
	}

}
