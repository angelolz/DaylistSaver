package main;

import java.util.Arrays;
import java.util.Properties;
import java.util.TimeZone;

public class Config
{
    private final String spotifyClientId;
    private final String spotifyClientSecret;
    private final String redirectUrl;
    private final int port;
    private final boolean savePlaylists;
    private final boolean askForCode;
    private final String timezone;

    public Config(Properties prop) throws Exception
    {
        if(prop.getProperty("spotify_client_id") == null || prop.getProperty("spotify_client_secret") == null)
            throw new Exception("Spotify client ID or secret cannot be empty!");

        this.spotifyClientId = prop.getProperty("spotify_client_id");
        this.spotifyClientSecret = prop.getProperty("spotify_client_secret");

        if(!Utils.isNumber(prop.getProperty("port", "4050")))
            throw new Exception("Port must be a valid number");

        this.port = Integer.parseInt(prop.getProperty("port", "4050"));

        this.redirectUrl = prop.getProperty("redirect_url", String.format("http://localhost:%d/callback", port));
        this.savePlaylists = Boolean.parseBoolean(prop.getProperty("save_playlists", "false"));
        this.askForCode = Boolean.parseBoolean(prop.getProperty("ask_for_code", "false"));

        if(prop.getProperty("timezone") != null && !Arrays.asList(TimeZone.getAvailableIDs()).contains(prop.getProperty("timezone")))
            throw new Exception("Timezone is invalid.");

        this.timezone = prop.getProperty("timezone");
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

    public String getTimezone()
    {
        return timezone;
    }

    public boolean isSavePlaylists()
    {
        return savePlaylists;
    }

    public boolean isAskForCode() { return askForCode; }
}
