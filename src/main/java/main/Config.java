package main;

import java.util.Properties;

public class Config
{
    private final String spotifyClientId;
    private final String spotifyClientSecret;
    private final String redirectUrl;
    private final String port;
    private final boolean savePlaylists;

    public Config(Properties prop)
    {
        this.spotifyClientId = prop.getProperty("spotify_client_id");
        this.spotifyClientSecret = prop.getProperty("spotify_client_secret");
        this.redirectUrl = prop.getProperty("redirect_url");
        this.port = prop.getProperty("port", "4050");
        this.savePlaylists = Boolean.parseBoolean(prop.getProperty("save_playlists", "false"));
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

    public String getPort()
    {
        return port;
    }

    public boolean isSavePlaylists()
    {
        return savePlaylists;
    }
}
