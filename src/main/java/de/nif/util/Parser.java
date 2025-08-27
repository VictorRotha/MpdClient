package de.nif.util;


import de.nif.data.AudioOutput;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Parser {

    /**
     * Extrahiert key-value Paare aus mpd query results;
     * OK/ACK Kennzeichnungen werden entfernt;
     * ACHTUNG: Doppelte Keys werden überschrieben;
     *
     * converts
     * List<String> {"OK MPD 0.21.11", "artist: someArtist", "album: someAlbum", "OK"}
     * to
     * HashMap<String, String> {"artist"="someArtist", "album"="someAlbum"}
     *
     * @param channelResult List<String>
     * @return Map<String, String>
     *
     */
    public static Map<String, String> resultToMap(List<String> channelResult) {

        HashMap<String, String> result = new HashMap<>();

        for (String line: channelResult) {
            if (line.startsWith("OK") || line.startsWith("ACK")) continue;
            String[] pair = line.split(":", 2);
            result.put(pair[0].trim(), pair[1].trim());
        }
        return result;

    }



    /**
     * Wandelt query results der Form "tag: value" in Listen aus values um;
     * OK/ACK Kennzeichnungen werden entfernt;
     * Leere tags werden entfernt
     * @param channelResult ArrayList<"key: value">
     * @return ArrayList<"value">
     */
    public static ArrayList<String> resultToList(List<String> channelResult) {
        System.out.println("resultToList: " + channelResult );
        if (channelResult == null)
            return null;

        ArrayList<String> result = new ArrayList<>();

        String value;
        for (String s : channelResult) {
            if (s.startsWith("OK") || s.startsWith("ACK")) continue;
            value = s.split(": ", 2)[1];
            if (!value.equals("")) result.add(value);
        }
        return result;

    }



    /**
     * Wandelt Sekunden in einen Zeit-String der Form
     * "dd:hh:mm:ss"
     * @param _seconds Seconds
     * @return Time String
     */
    public static String secondsToTime(double _seconds) {
        return secondsToTime(_seconds, false, true);
    }

    /**
     * Wandelt Sekunden in einen Zeit-String der Form
     * "dd:hh:mm:ss" (_long == false) oder
     * "dd days hh hours, mm min, ss sec" (_long == true) um
     * @param _seconds  Sekunden
     * @param _long Langes oder kurzes Ausgabeformat
     * @param _days Max Auflösung nach Tagen oder Stunden
     * @return Time-String
     */
    public static String secondsToTime(double _seconds, boolean _long, boolean _days) {
        int secstotal = (int) _seconds;
        int secs = secstotal % 60;
        int minstotal = secstotal / 60;
        int mins = minstotal % 60;
        int hourstotal = minstotal / 60;
        int hours = hourstotal % 24;
        int days = hourstotal / 24;

        String result, s;
        if (secstotal < 60) {
            s = (_long) ? "%02d sec" : "00:%02d";
            result = String.format(s, secstotal);
        } else if (secstotal < 60*60) {
            s = (_long) ? "%02d min %02d sec" : "%02d:%02d";
            result = String.format(s, minstotal, secs);
        } else if (secstotal < 60*60*24 || !_days) {
            s = (_long) ? "%02d hours %02d min %02d sec" : "%02d:%02d:%02d";
            result = String.format(s, hourstotal, mins, secs);
        } else {
            s = (_long) ? "%02d days %02d hours %02d min %02d sec" : "%02d:%02d:%02d:%02d";
            result = String.format(s, days, hours, mins, secs);
        }

        return result;


    }


    public static List<AudioOutput> parseAudioOutputs(List<String> channelResult) {

        ArrayList<AudioOutput> outputs = new ArrayList<>();

        AudioOutput tmp = new AudioOutput();

        for (String s : channelResult) {
            if (s.startsWith("OK") || s.startsWith("ACK")) continue;

            String[] split = s.split(": ", 2);

            switch (split[0]) {
                case "outputid":
                    if (tmp.getId() > -1) outputs.add(tmp);
                    tmp = new AudioOutput();
                    tmp.setId(Integer.parseInt(split[1]));
                    break;
                case "outputname":
                    tmp.setName(split[1]);
                    break;
                case "outputenabled":
                    tmp.setEnabled(split[1].equals("1"));
                    break;
                case "plugin":
                    tmp.setPlugin(split[1]);
                    break;
                case "attribute":
                    String[] attr = split[1].split("=", 2);
                    tmp.addAttribute(attr[0], attr[1]);
                    break;
            }

        }

        if (tmp.getId() > -1) outputs.add(tmp);

        return outputs;

    }

    public static String secondsToDateTime(long seconds) {

        long millis = seconds * 1000;
        Date date = new Date(millis);
        return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss z", Locale.GERMANY).format(date);

    }

    public static String quote(String s) {
        return "\"" + s + "\"";
    }


}
