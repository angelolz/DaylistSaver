package webserver.servlets;

import de.neuland.pug4j.Pug4J;
import de.neuland.pug4j.PugConfiguration;
import de.neuland.pug4j.template.PugTemplate;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.PlaylistManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ListDaylists extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setContentType("text/html");
        resp.setStatus(HttpServletResponse.SC_OK);

        PugConfiguration config = new PugConfiguration();
        config.setCaching(false);

        PugTemplate template = Pug4J.getTemplate("web/templates/index.pug");
        Map<String, Object> model = new HashMap<>();
        model.put("playlists", PlaylistManager.getSavedPlaylists());
        String html = config.renderTemplate(template, model);
        resp.getWriter().println(html);
    }
}
