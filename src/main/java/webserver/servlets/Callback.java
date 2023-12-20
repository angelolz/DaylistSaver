package webserver.servlets;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.DaylistSaver;
import main.ScheduledTasks;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

import java.io.IOException;

public class Callback extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        String storedState = (String) req.getSession().getAttribute("state");

        if(storedState == null || !storedState.equals(req.getParameter("state")))
        {
            resp.setStatus(403);
            resp.getWriter().println("Invalid state");
            return;
        }

        try
        {
            SpotifyApi api = DaylistSaver.getSpotifyApi();
            String code = req.getParameter("code");
            AuthorizationCodeCredentials auth = api.authorizationCode(code).build().execute();
            api.setAccessToken(auth.getAccessToken());
            api.setRefreshToken(auth.getRefreshToken());

            resp.setStatus(200);
            resp.getWriter().println("Authorization successful. You may close this window.");

            DaylistSaver.getLogger().info("Authorization successful. Intializing tasks.");
            ScheduledTasks.init();
        }

        catch(ParseException | SpotifyWebApiException e)
        {
            resp.setStatus(500);
            resp.getWriter().println("Something went wrong, please try again!");
            DaylistSaver.getLogger().error("Unable to authorize with Spotify.", e);
        }
    }
}
