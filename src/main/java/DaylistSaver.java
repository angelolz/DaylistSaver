import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import java.io.FileInputStream;
import java.net.URI;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Logger;

public class DaylistSaver
{
    private static SpotifyApi spotifyApi;
    private static Logger logger;

    public static void main(String[] args)
    {
        logger = Logger.getLogger(DaylistSaver.class.getName());
        try
        {
            Properties prop = new Properties();
            FileInputStream propFile = new FileInputStream("config.properties");
            prop.load(propFile);


            String spClientId = prop.getProperty("spotify_client_id");
            String spClientSecret = prop.getProperty("spotify_client_secret");


            spotifyApi = new SpotifyApi.Builder()
                .setClientId(spClientId)
                .setClientSecret(spClientSecret)
                .setRedirectUri(SpotifyHttpManager.makeUri("https://angelolz.one"))
                .build();

            AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                                                                                .scope("playlist-read-private playlist-modify-private playlist-read-collaborative")
                                                                                .build();

            URI uri = authorizationCodeUriRequest.execute();

            Scanner input = new Scanner(System.in);
            System.out.println("Please enter your authorization token below. You can get a token from this link.");
            System.out.println(uri.toString());

            String spAuth = input.nextLine();
            AuthorizationCodeCredentials authorizationCodeCredentials = spotifyApi.authorizationCode(spAuth).build().execute();
            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            System.out.println("Set access/refresh token.");

            ScheduledTasks.init();
        }

        catch(Exception e)
        {
            System.out.println("oopsies something went wrong");
            e.printStackTrace();
        }
    }

    public static SpotifyApi getSpotifyApi() { return spotifyApi; }


    public static Logger getLogger()
    {
        return logger;
    }
}