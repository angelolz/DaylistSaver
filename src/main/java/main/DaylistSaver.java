package main;

import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.enums.AuthorizationScope;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.exceptions.detailed.BadRequestException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import webserver.MainServer;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.Scanner;

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
                .setRedirectUri(SpotifyHttpManager.makeUri(config.isAskForCode() ? config.getRedirectUrl() : String.format("http://localhost:%d/callback", config.getPort())))
                .build();

            if(config.isAskForCode())
            {
                AuthorizationCodeCredentials auth = retrieveAuth();
                spotifyApi.setAccessToken(auth.getAccessToken());
                spotifyApi.setRefreshToken(auth.getRefreshToken());
                ScheduledTasks.init();
            }

            logger.info("Started listening on port {}", config.getPort());
            MainServer.start(String.valueOf(config.getPort()));
        }

        catch(Exception e)
        {
            logger.error("A DaylistSaver error has occured:", e);
        }
    }

    public static SpotifyApi getSpotifyApi() { return spotifyApi; }

    public static Config getConfig() { return config; }

    public static Logger getLogger() { return logger; }

    private static AuthorizationCodeCredentials retrieveAuth() throws IOException, ParseException,
        SpotifyWebApiException
    {
        URI uri = DaylistSaver.getSpotifyApi().authorizationCodeUri().scope(AuthorizationScope.PLAYLIST_READ_PRIVATE,
            AuthorizationScope.PLAYLIST_MODIFY_PRIVATE, AuthorizationScope.PLAYLIST_READ_COLLABORATIVE).build().execute();

        Scanner input = new Scanner(System.in);
        System.out.println("Please enter your authorization token below. You can get a token from this link.");
        System.out.println(uri.toString());

        while(true)
        {
            try
            {
                String spAuth = input.nextLine();
                AuthorizationCodeCredentials auth = spotifyApi.authorizationCode(spAuth).build().execute();
                input.close();
                return auth;
            }

            catch(BadRequestException e)
            {
                System.out.println("Couldn't accept authorization code: " + e.getMessage() + "\n");
                System.out.println("Please try again. Enter your authorization token below.");
            }
        }

    }
}