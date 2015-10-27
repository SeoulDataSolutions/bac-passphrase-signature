package bac.api;

import bac.helper.Helper;
import bac.settings.Settings;

// Import required java libraries

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
 
import java.io.IOException;
 
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;


public final class Api
{
     
    public static void init()
    {
        Server APIserver = new Server();
        ServerConnector connector;    
        connector = new ServerConnector(APIserver);    
    
            connector.setPort(Settings.APIport);
            connector.setHost(Settings.APIhost);
            connector.setIdleTimeout(Settings.APItimeout);
            connector.setReuseAddress(true);
            APIserver.addConnector(connector);    
    
            HandlerList APIHandlers = new HandlerList();

            ResourceHandler APIFileHandler = new ResourceHandler();
            APIFileHandler.setDirectoriesListed(false);
            APIFileHandler.setWelcomeFiles(new String[]{"index.html"});
            APIFileHandler.setResourceBase("html");

            APIHandlers.addHandler(APIFileHandler);    

            ServletContextHandler APIHandler = new ServletContextHandler();    
                                  
            APIHandler.addServlet(APIServlet.class, "/api");

            APIHandlers.addHandler(APIHandler);

            APIHandlers.addHandler(new DefaultHandler());

            APIserver.setHandler(APIHandlers);
            APIserver.setStopAtShutdown(true);

            
                try {
                    APIserver.start();
                    Helper.logMessage("API server started at " + Settings.APIhost + ":" + Settings.APIport);
                } catch (Exception e) {
                    Helper.logMessage("Failed to start API server.");
                }
            
        
    }
    
    
}

