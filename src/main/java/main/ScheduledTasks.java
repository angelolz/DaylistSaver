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
    private static boolean initialized = false;

    public static void init()
    {
        if(!initialized)
        {
            initialized = true;
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(ScheduledTasks::refreshToken, 1, 1, TimeUnit.HOURS);
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(ScheduledTasks::checkUpdatedPlaylist, 5, 3600, TimeUnit.SECONDS);
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
                api.setAccessToken(null);
                api.setRefreshToken(null);

                AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = api.authorizationCodeRefresh().build();
                AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();
                api.setAccessToken(authorizationCodeCredentials.getAccessToken());
                api.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
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

        DaylistSaver.getLogger().warn("Couldn't find daylist.");
        return null;
    }
}