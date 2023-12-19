package main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import objs.PlaylistObject;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PlaylistManager
{
    public static void process(PlaylistSimplified playlistSimplified)
    {
        try
        {
            List<PlaylistObject> savedPlaylists = getSavedPlaylists();
            if(isSaved(savedPlaylists, playlistSimplified))
                return;

            SpotifyApi api = DaylistSaver.getSpotifyApi();
            Playlist playlist = api.getPlaylist(playlistSimplified.getId()).build().execute();
            PlaylistObject playlistObject = makeNewPlaylistObject(playlistSimplified, playlist);

            addPlaylistToFile(savedPlaylists, playlistObject);

            if(DaylistSaver.getConfig().isSavePlaylists())
                createNewPlaylist(playlistObject);
        }

        catch(Exception e)
        {
            System.out.println("Error processing playlist.");
            e.printStackTrace();
        }
    }

    public static List<PlaylistObject> getSavedPlaylists() throws IOException
    {
        File file = new File("web/playlists.json");
        if(!file.exists())
        {
            try(FileWriter fw = new FileWriter(file))
            {
                fw.write("[]");
                return new ArrayList<>();
            }
        }

        else
        {
            String playlistJson = new String(Files.readAllBytes(Paths.get("web/playlists.json")));
            return new Gson().fromJson(playlistJson, new TypeToken<List<PlaylistObject>>()
            {
            }.getType());
        }
    }

    private static boolean isSaved(List<PlaylistObject> savedPlaylists, PlaylistSimplified newPlaylist)
    {
        return savedPlaylists.stream().anyMatch(playlist -> playlist.getId().equals(newPlaylist.getId())
            && newPlaylist.getName().contains(playlist.getGenres())
        );
    }

    private static void addPlaylistToFile(List<PlaylistObject> savedPlaylists, PlaylistObject newPlaylist) throws
        IOException
    {
        try(FileWriter fw = new FileWriter("web/playlists.json"))
        {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            savedPlaylists.add(newPlaylist);

            String newJson = gson.toJson(savedPlaylists);
            fw.write(newJson);
        }
    }

    private static PlaylistObject makeNewPlaylistObject(PlaylistSimplified playlistSimplified, Playlist playlist)
    {
        String playlistName = playlistSimplified.getName();
        String[] nameSplit = playlistName.split("\\s+");
        String timeOfDay = nameSplit[nameSplit.length - 1];
        String genres = playlistName.substring(playlistName.indexOf("•") + 1, playlistName.indexOf(timeOfDay)).trim();
        return new PlaylistObject(playlistSimplified.getId(), getCurrentDateString(), timeOfDay, genres, removeLinks(playlist.getDescription()));
    }

    private static void createNewPlaylist(PlaylistObject playlistObject) throws IOException, ParseException,
        SpotifyWebApiException
    {
        SpotifyApi api = DaylistSaver.getSpotifyApi();
        List<String> trackUris = getTrackUrisFromPlaylist(playlistObject.getId());

        String userId = api.getCurrentUsersProfile().build().execute().getUri().split(":")[2]; //spotify:user:userIdhere
        Playlist newPlaylist = api.createPlaylist(userId, playlistObject.getPlaylistName()).public_(false).description(playlistObject.getDescription()).build().execute();

        api.addItemsToPlaylist(newPlaylist.getId(), trackUris.toArray(String[]::new)).build().execute();
        System.out.println("Finished creating new playlist: " + playlistObject.getPlaylistName());
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
