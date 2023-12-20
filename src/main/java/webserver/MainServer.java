package webserver;

import main.DaylistSaver;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import webserver.servlets.Callback;
import webserver.servlets.ListDaylists;
import webserver.servlets.Login;

public class MainServer
{
    public static void start(String port)
    {
        try
        {
            Server server = new Server(Integer.parseInt(port));
            ServletHandler servletHandler = initServletHandler();
            setSessionEnableContext(server, servletHandler);

            server.setHandler(servletHandler);
            server.start();

            if(!DaylistSaver.getConfig().isAskForCode())
                DaylistSaver.getLogger().info("DaylistSaver webserver started. Please authorize with Spotify here: http://localhost:4050/login");

            server.join();
        }

        catch(NumberFormatException e)
        {
            DaylistSaver.getLogger().error("You have provided an invalid port number, webserver was not started. Please fix this in your config.properties.");
        }

        catch(Exception e)
        {
            DaylistSaver.getLogger().error("Webserver error!", e);
        }
    }

    private static ServletHandler initServletHandler()
    {
        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(ListDaylists.class, "/");
        servletHandler.addServletWithMapping(Login.class, "/login");
        servletHandler.addServletWithMapping(Callback.class, "/callback");

        return servletHandler;
    }

    private static void setSessionEnableContext(Server server, ServletHandler handlerServlet)
    {
        SessionIdManager idmanager = new DefaultSessionIdManager(server);
        SessionHandler sessionsHandler = new SessionHandler();
        server.setSessionIdManager(idmanager);
        handlerServlet.setHandler(sessionsHandler);
    }
}
