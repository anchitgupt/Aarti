package ateam.com.aarti;

/**
 * Created by apple on 30/12/17.
 */

public class Song {

    String key,
    name,
    devta,
    days,
    media,
    aarti;

    public Song(String key, String name, String devta, String days, String media, String aarti) {
        this.key = key;
        this.name = name;
        this.devta = devta;
        this.days = days;
        this.media = media;
        this.aarti = aarti;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getDevta() {
        return devta;
    }

    public String getDays() {
        return days;
    }

    public String getMedia() {
        return media;
    }

    public String getAarti() {
        return aarti;
    }
}
