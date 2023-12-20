package main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import webserver.MainServer;

import java.io.FileInputStream;
import java.util.Properties;

public class DaylistSaver
{
    private static final Logger logger = LoggerFactory.getLogger(DaylistSaver.class);
    private static SpotifyApi spotifyApi;
    private static Config config;

    public static void main(String[] args)
    {
        try(FileInputStream propFile = new FileInputStream("config.properties"))
        {
            Properties prop = new Properties();
            prop.load(propFile);
            config = new Config(prop);

            spotifyApi = new SpotifyApi.Builder()
                .setClientId(config.getSpotifyClientId())
                .setClientSecret(config.getSpotifyClientSecret())
                .setRedirectUri(SpotifyHttpManager.makeUri(config.getRedirectUrl()))
                .build();

            logger.info("Started listening on port {}", config.getPort());
            MainServer.start(config.getPort());
        }

        catch(Exception e)
        {
            logger.error("A DaylistSaver error has occured:", e);
        }
    }

    public static SpotifyApi getSpotifyApi() { return spotifyApi; }

    public static Config getConfig() { return config; }

    public static Logger getLogger() { return logger; }
}