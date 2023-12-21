package objs;

import java.util.List;

public class PlaylistObject
{
    public String snapshotId;
    public String playlistId;
    public String date;
    public String timeOfDay;
    public String genres;
    public String description;
    public List<String> trackIds;

    public String getSnapshotId()
    {
        return snapshotId;
    }

    public PlaylistObject setSnapshotId(String snapshotId)
    {
        this.snapshotId = snapshotId;
        return this;
    }

    public String getPlaylistId()
    {
        return playlistId;
    }

    public PlaylistObject setPlaylistId(String playlistId)
    {
        this.playlistId = playlistId;
        return this;
    }

    public String getDate()
    {
        return date;
    }

    public PlaylistObject setDate(String date)
    {
        this.date = date;
        return this;
    }

    public String getTimeOfDay()
    {
        return timeOfDay;
    }

    public PlaylistObject setTimeOfDay(String timeOfDay)
    {
        this.timeOfDay = timeOfDay;
        return this;
    }

    public String getGenres()
    {
        return genres;
    }

    public PlaylistObject setGenres(String genres)
    {
        this.genres = genres;
        return this;
    }

    public String getDescription()
    {
        return description;
    }

    public PlaylistObject setDescription(String description)
    {
        this.description = description;
        return this;
    }

    public List<String> getTrackIds()
    {
        return trackIds;
    }

    public PlaylistObject setTrackIds(List<String> trackIds)
    {
        this.trackIds = trackIds;
        return this;
    }
}
