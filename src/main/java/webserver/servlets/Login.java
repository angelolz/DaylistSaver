package webserver.servlets;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.DaylistSaver;
import se.michaelthelin.spotify.enums.AuthorizationScope;

import java.io.IOException;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Base64;

public class Login extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException
    {
        String state = generateToken();
        req.getSession().setAttribute("state", state);
        URI uri = DaylistSaver.getSpotifyApi().authorizationCodeUri().scope(AuthorizationScope.PLAYLIST_READ_PRIVATE,
            AuthorizationScope.PLAYLIST_MODIFY_PRIVATE, AuthorizationScope.PLAYLIST_READ_COLLABORATIVE).state(state).build().execute();
        res.sendRedirect(uri.toString());
    }

    private String generateToken()
    {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getEncoder().encodeToString(tokenBytes);
    }
}
