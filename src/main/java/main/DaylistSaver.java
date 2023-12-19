package main;

import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
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

            AuthorizationCodeCredentials authorizationCodeCredentials = getAuthCredentials();
            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            System.out.println("Set access/refresh token.");

            ScheduledTasks.init();
            MainServer.start(config.getPort());
        }

        catch(Exception e)
        {
            System.out.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static SpotifyApi getSpotifyApi() { return spotifyApi; }

    public static Config getConfig() { return config; }

    private static AuthorizationCodeCredentials getAuthCredentials() throws IOException, ParseException,
        SpotifyWebApiException
    {
        URI uri = spotifyApi.authorizationCodeUri()
                            .scope("playlist-read-private playlist-modify-private playlist-read-collaborative")
                            .build().execute();

        Scanner input = new Scanner(System.in);
        System.out.println("Please enter your authorization token below. You can get a token from this link.");
        System.out.println(uri.toString());

        while(true)
        {
            try
            {
                String spAuth = input.nextLine();
                return spotifyApi.authorizationCode(spAuth).build().execute();
            }

            catch(BadRequestException e)
            {
                System.out.println("Couldn't accept authorization code: " + e.getMessage() + "\n");
                System.out.println("Please try again. Enter your authorization token below.");
            }
        }
    }
}