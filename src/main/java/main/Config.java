package main;

import java.util.Properties;

public class Config
{
    private final String spotifyClientId;
    private final String spotifyClientSecret;
    private final String redirectUrl;
    private final int port;
    private final boolean savePlaylists;
    private final boolean askForCode;

    public Config(Properties prop)
    {
        this.spotifyClientId = prop.getProperty("spotify_client_id");
        this.spotifyClientSecret = prop.getProperty("spotify_client_secret");
        this.port = Integer.parseInt(prop.getProperty("port", "4050"));
        this.redirectUrl = prop.getProperty("redirect_url", String.format("http://localhost:%d/callback", port));
        this.savePlaylists = Boolean.parseBoolean(prop.getProperty("save_playlists", "false"));
        this.askForCode = Boolean.parseBoolean(prop.getProperty("ask_for_code", "false"));
    }

    public String getSpotifyClientId()
    {
        return spotifyClientId;
    }

    public String getSpotifyClientSecret()
    {
        return spotifyClientSecret;
    }

    public String getRedirectUrl()
    {
        return redirectUrl;
    }

    public int getPort()
    {
        return port;
    }

    public boolean isSavePlaylists()
    {
        return savePlaylists;
    }

    public boolean isAskForCode() { return askForCode; }
}
