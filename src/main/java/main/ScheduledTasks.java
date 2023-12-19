package main;

import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ScheduledTasks
{
    private static final int ITEM_LIMIT = 50;
    private static String lastPlaylistId;

    public static void init()
    {
        System.out.println("Initializing tasks...");
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(ScheduledTasks::refreshToken, 1, 1, TimeUnit.HOURS);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(ScheduledTasks::checkUpdatedPlaylist, 5, 10, TimeUnit.SECONDS);
    }

    private static void refreshToken()
    {
        try
        {
            SpotifyApi api = DaylistSaver.getSpotifyApi();
            if(!api.getAccessToken().isEmpty())
            {
                AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = api.authorizationCodeRefresh().build();
                AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();
                api.setAccessToken(authorizationCodeCredentials.getAccessToken());
            }
        }

        catch(Exception e)
        {
            System.out.println("Failed to get new token: " + e.getMessage());
        }
    }

    public static void checkUpdatedPlaylist()
    {
        try
        {
            PlaylistSimplified daylist = findDaylist();

            if(daylist != null)
                PlaylistManager.process(daylist);
        }

        catch(Exception e)
        {
            System.out.println("Unable to get updated daylist: " + e.getMessage());
        }
    }

    private static PlaylistSimplified findDaylist() throws IOException, ParseException, SpotifyWebApiException
    {
        SpotifyApi api = DaylistSaver.getSpotifyApi();

        int offset = 0;
        Paging<PlaylistSimplified> pagedPlaylists;

        while((pagedPlaylists = api.getListOfCurrentUsersPlaylists().limit(ITEM_LIMIT).offset(offset).build().execute()).getNext() != null)
        {
            PlaylistSimplified[] playlists = pagedPlaylists.getItems();
            for(PlaylistSimplified playlist : playlists)
            {
                if(playlist.getName().startsWith("daylist • "))
                {
                    if(!playlist.getId().equals(lastPlaylistId))
                    {
                        lastPlaylistId = playlist.getId();
                        return playlist;
                    }

                    else
                        return null;
                }
            }

            offset += ITEM_LIMIT;
        }

        System.out.println("Couldn't find daylist.");
        return null;
    }
}