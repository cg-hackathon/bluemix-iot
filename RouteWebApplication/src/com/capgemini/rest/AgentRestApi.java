package com.capgemini.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * 
 * @author cmammado
 *
 */
//Specify the application path to the REST service. Required to switch between different methods in different classes.
@ApplicationPath("/RESTCALL")
public class AgentRestApi extends Application {

}
