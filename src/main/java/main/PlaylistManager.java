package main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import objs.PlaylistObject;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PlaylistManager
{
    public static void process(PlaylistSimplified daylistSimplified)
    {
        try
        {
            List<PlaylistObject> savedPlaylists = getSavedPlaylists();
            if(isSaved(savedPlaylists, daylistSimplified))
                return;

            SpotifyApi api = DaylistSaver.getSpotifyApi();
            Playlist playlist = api.getPlaylist(daylistSimplified.getId()).build().execute();
            PlaylistObject playlistObject = makeNewPlaylistObject(daylistSimplified, playlist);

            if(DaylistSaver.getConfig().isSavePlaylists())
                createNewPlaylist(playlistObject);

            addPlaylistToFile(savedPlaylists, playlistObject);
        }

        catch(Exception e)
        {
            DaylistSaver.getLogger().error("Error processing playlist.", e);
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
        return savedPlaylists.stream().anyMatch(playlist -> playlist.getSnapshotId().equals(newPlaylist.getSnapshotId()));
    }

    private static void addPlaylistToFile(List<PlaylistObject> savedPlaylists, PlaylistObject newPlaylistObject) throws
        IOException
    {
        try(FileWriter fw = new FileWriter("web/playlists.json"))
        {
            Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
            String playlistName = Utils.getPlaylistName(newPlaylistObject);
            savedPlaylists.add(newPlaylistObject);

            String newJson = gson.toJson(savedPlaylists);
            fw.write(newJson);
            DaylistSaver.getLogger().info("Saved new entry to playlist file: {}", playlistName);
        }
    }

    private static PlaylistObject makeNewPlaylistObject(PlaylistSimplified playlistSimplified, Playlist playlist) throws
        Exception
    {
        PlaylistObject playlistObject = new PlaylistObject()
            .setSnapshotId(playlistSimplified.getSnapshotId())
            .setDate(Utils.getCurrentDate())
            .setDescription(removeLinks(playlist.getDescription()))
            .setTimeOfDay(getTimeOfDay(playlistSimplified.getName()))
            .setGenres(getGenres(playlistSimplified.getName()));

        List<String> trackIds = getTrackUrisFromPlaylist(playlist.getId());
        playlistObject.setTrackIds(trackIds);

        return playlistObject;
    }

    private static void createNewPlaylist(PlaylistObject playlistObject) throws Exception
    {
        SpotifyApi api = DaylistSaver.getSpotifyApi();
        String userId = api.getCurrentUsersProfile().build().execute().getUri().split(":")[2]; //spotify:user:userIdhere
        String playlistName = Utils.getPlaylistName(playlistObject);
        Playlist newPlaylist = api.createPlaylist(userId, playlistName).public_(false).description(playlistObject.getDescription()).build().execute();

        api.addItemsToPlaylist(newPlaylist.getId(), playlistObject.getTrackIds().toArray(String[]::new)).build().execute();
        playlistObject.setPlaylistId(newPlaylist.getId());
        DaylistSaver.getLogger().info("Finished creating new playlist: {}", playlistName);
    }

    private static List<String> getTrackUrisFromPlaylist(String playlistId) throws Exception
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

    private static String removeLinks(String description)
    {
        String regex = "<a\\s*(?:[^>]*\\s+)?href=(\"[^\"]*\"|'[^']*')[^>]*>(.*?)</a>";
        return description.replaceAll(regex, "$2");
    }

    private static String getTimeOfDay(String playlistName)
    {
        String trimmedName = playlistName.substring(playlistName.indexOf("•") + 1).toLowerCase().trim();

        if(trimmedName.endsWith("late night"))
            return "late night";
        else if(trimmedName.endsWith("early morning"))
            return "early morning";
        else
        {
            String[] nameSplit = trimmedName.split("\\s+");
            return nameSplit[nameSplit.length - 1];
        }
    }

    private static String getGenres(String playlistName)
    {
        String trimmedName = playlistName.substring(playlistName.indexOf("•") + 1).toLowerCase().trim();

        if(trimmedName.endsWith("late night"))
            return trimmedName.substring(0, trimmedName.indexOf("late night")).trim();
        else if(trimmedName.endsWith("early morning"))
            return trimmedName.substring(0, trimmedName.indexOf("early morning")).trim();
        else
        {
            String[] nameSplit = trimmedName.split("\\s+");
            return trimmedName.substring(0, trimmedName.indexOf(nameSplit[nameSplit.length - 1])).trim();
        }
    }
}
