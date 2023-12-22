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
    private static boolean initialized = false;

    public static void init()
    {
        if(!initialized)
        {
            initialized = true;
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(ScheduledTasks::refreshToken, 50, 50, TimeUnit.MINUTES);
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(ScheduledTasks::checkUpdatedPlaylist, 10, 3600, TimeUnit.SECONDS);
            DaylistSaver.getLogger().info("Initialized tasks.");
        }
    }

    private static void refreshToken()
    {
        try
        {
            SpotifyApi api = DaylistSaver.getSpotifyApi();
            if(!api.getAccessToken().isEmpty())
            {
                AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = api.authorizationCodeRefresh()
                                                                                     .grant_type("refresh_token").refresh_token(api.getRefreshToken()).build();

                AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();
                api.setAccessToken(authorizationCodeCredentials.getAccessToken());

                DaylistSaver.getLogger().info("Refreshed authorization token.");
            }
        }

        catch(Exception e)
        {
            DaylistSaver.getLogger().error("Failed to get new token.", e);
        }
    }

    public static void checkUpdatedPlaylist()
    {
        SpotifyApi api = DaylistSaver.getSpotifyApi();

        if(api.getAccessToken() == null || api.getRefreshToken() == null)
            return;

        try
        {
            PlaylistSimplified daylist = findDaylist();

            if(daylist != null)
                PlaylistManager.process(daylist);
        }

        catch(Exception e)
        {
            DaylistSaver.getLogger().error("Unable to get updated daylist.", e);
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
                    return playlist;
            }

            offset += ITEM_LIMIT;
        }

        DaylistSaver.getLogger().warn("Couldn't find daylist.");
        return null;
    }
}