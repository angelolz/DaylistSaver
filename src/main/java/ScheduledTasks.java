import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
                makeNewPlaylist(daylist);
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

    private static void makeNewPlaylist(PlaylistSimplified playlistSimpllified) throws IOException, ParseException,
        SpotifyWebApiException
    {
        SpotifyApi api = DaylistSaver.getSpotifyApi();
        List<String> trackUris = getTrackUrisFromPlaylist(playlistSimpllified.getId());
        String playlistName = playlistSimpllified.getName();
        String[] nameSplit = playlistName.split("\\s+");
        String timeOfDay = nameSplit[nameSplit.length - 1];
        String newPlaylistName = String.format("%s %s | %s", getCurrentDateString(), timeOfDay,
            playlistName.substring(playlistName.indexOf("•") + 1, playlistName.indexOf(timeOfDay)).trim());

        Playlist playlist = api.getPlaylist(playlistSimpllified.getId()).build().execute();
        String userId = api.getCurrentUsersProfile().build().execute().getUri().split(":")[2];
        Playlist newPlaylist = api.createPlaylist(userId, newPlaylistName).public_(false).description(removeLinks(playlist.getDescription())).build().execute();

        api.addItemsToPlaylist(newPlaylist.getId(), trackUris.toArray(String[]::new)).build().execute();
        System.out.println("Finished creating new playlist: " + newPlaylistName);
    }

    private static List<String> getTrackUrisFromPlaylist(String playlistId) throws IOException, ParseException,
        SpotifyWebApiException
    {
        SpotifyApi api = DaylistSaver.getSpotifyApi();
        List<String> trackUris = new ArrayList<>();
        int offset = 0;
        boolean finished = false;
        while(!finished)
        {
            Paging<PlaylistTrack> daylistTracks = api.getPlaylistsItems(playlistId).limit(50).offset(offset).build().execute();
            for(PlaylistTrack track : daylistTracks.getItems())
                trackUris.add(track.getTrack().getUri());

            offset += 50;
            if(daylistTracks.getNext() == null)
                finished = true;
        }

        return trackUris;
    }

    private static String getCurrentDateString()
    {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");

        return currentDate.format(formatter);
    }

    private static String removeLinks(String description)
    {
        String regex = "<a\\s*(?:[^>]*\\s+)?href=(\"[^\"]*\"|'[^']*')[^>]*>(.*?)</a>";
        return description.replaceAll(regex, "$2");
    }
}