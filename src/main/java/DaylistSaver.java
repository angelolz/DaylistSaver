import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.exceptions.detailed.BadRequestException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.Scanner;

public class DaylistSaver
{
    private static SpotifyApi spotifyApi;

    public static void main(String[] args)
    {
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

            AuthorizationCodeCredentials authorizationCodeCredentials = getAuthCredentials();
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