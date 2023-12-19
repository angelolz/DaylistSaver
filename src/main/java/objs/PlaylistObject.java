package objs;

public class PlaylistObject
{
    public String id;
    public String date;
    public String timeOfDay;
    public String genres;
    public String description;
    public String name;

    public PlaylistObject(String id, String date, String timeOfDay, String genres, String description)
    {
        this.id = id;
        this.date = date;
        this.timeOfDay = timeOfDay;
        this.genres = genres;
        this.description = description;
        this.name = getPlaylistName();
    }

    public String getId()
    {
        return id;
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

    public String getPlaylistName() {
        return String.format("%s %s | %s", date, timeOfDay, genres);
    }
}
