package main;

import objs.PlaylistObject;

public class Utils
{
    public static String getPlaylistName(PlaylistObject playlistObject)
    {
        return String.format("%s %s | %s", playlistObject.getDate(), playlistObject.getTimeOfDay(), playlistObject.getGenres());
    }
}
