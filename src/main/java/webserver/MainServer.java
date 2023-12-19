package webserver;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import webserver.servlets.ListDaylists;

public class MainServer
{
    public static void start(String port)
    {
        try
        {
            Server server = new Server(Integer.parseInt(port));
            ServletHandler servletHandler = new ServletHandler();
            servletHandler.addServletWithMapping(ListDaylists.class, "/");

            server.setHandler(servletHandler);
            server.start();
            System.out.println("Started listening on port " + port);
            server.join();
        }

        catch(NumberFormatException e)
        {
            System.out.println("You have provided an invalid port number, webserver was not started. Please fix this in your config.properties.");
        }

        catch(Exception e)
        {
            System.out.println("Webserver error: " + e.getMessage());
        }
    }
}
