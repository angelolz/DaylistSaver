package objs;

public class PlaylistObject
{
    public String snapshotId;
    public String playlistId;
    public String date;
    public String timeOfDay;
    public String genres;
    public String description;

    public PlaylistObject(String id, String date, String timeOfDay, String genres, String description)
    {
        this.snapshotId = id;
        this.date = date;
        this.timeOfDay = timeOfDay;
        this.genres = genres;
        this.description = description;
        this.playlistId = null;
    }

    public String getSnapshotId()
    {
        return snapshotId;
    }

    public String getDate()
    {
        return date;
    }

    public String getTimeOfDay()
    {
        return timeOfDay;
    }

    public String getGenres()
    {
        return genres;
    }

    public String getDescription()
    {
        return description;
    }

    public String getPlaylistId() { return playlistId; }

    public void setPlaylistId(String playlistId) { this.playlistId = playlistId; }
}
