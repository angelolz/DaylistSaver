package main;

import objs.PlaylistObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

public class Utils
{
    public static String getPlaylistName(PlaylistObject playlistObject)
    {
        return String.format("%s %s | %s", playlistObject.getDate(), playlistObject.getTimeOfDay(), playlistObject.getGenres());
    }

    public static String getCurrentDate()
    {
        String timezone;

        if(DaylistSaver.getConfig().getTimezone() == null)
            timezone = ZoneId.systemDefault().toString();
        else
            timezone = DaylistSaver.getConfig().getTimezone();

        Date date = new Date();
        DateFormat df = new SimpleDateFormat("MM/dd/yy");
        df.setTimeZone(TimeZone.getTimeZone(timezone));
        return df.format(date);
    }

    public static boolean isNumber(String num)
    {
        if(num == null)
            return false;

        try
        {
            Integer.parseInt(num);
            return true;
        }

        catch(NumberFormatException e)
        {
            return false;
        }
    }
}
