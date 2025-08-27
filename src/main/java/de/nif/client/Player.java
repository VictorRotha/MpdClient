package de.nif.client;


import de.nif.data.AudioOutput;
import de.nif.data.Result;
import de.nif.util.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Player {

    public static final int DEFAULT_NUMBER_OF_THREADS = 6;

    private final Connection connection;
    private final ChannelHelper channelHelper;
    private final ExecutorService threadPool;

    public interface Callback<T> {
        void onResult(Result<T> result);
    }


    public Player() {
        this(DEFAULT_NUMBER_OF_THREADS);
    }

    public Player(int numberOfThreads) {
        connection = new Connection();

        threadPool = (numberOfThreads > 0 ) ? Executors.newFixedThreadPool(numberOfThreads) : null;

        channelHelper = new ChannelHelper(connection, threadPool);

    }

    public void connect(String host, int port, Callback<String> callback) {
        System.out.println("player connect " + host + " " + port);

        if (threadPool != null)
            threadPool.execute(() -> connectExecute(host, port, callback));
        else
            connectExecute(host, port, callback);



    }

    private void connectExecute(String host, int port, Callback<String> callback) {

        Result<String> result = connection.connect(host, port);
        if (callback != null) {
            callback.onResult(result);
        }

    }

    public void setConnectionListener(Connection.ConnectionListener listener) {
        if (connection != null) {
            connection.setListener(listener);
        }

    }

    public boolean isConnected() {
        return connection.isConnected();
    }
    public Connection getConnection() {
        return connection;
    }

    public ChannelHelper getChannelHelper() {
        return channelHelper;
    }
    public ExecutorService getThreadPool() {
        return threadPool;
    }


    public void query(Callback<List<String>> callback, String... commands) {

        channelHelper.simpleQuery(callback, commands);

    }

    public void idle(Callback<List<String>> callback) {

            Channel channel = new Channel(connection, false);
            Result<List<String>> result = channelHelper.query(channel, "idle");
            channel.close();

            if (callback != null) {
                callback.onResult(result);
            }
    }


    public void queryQueue(Callback<List<String>> callback) {
        String command = "playlistinfo";
        channelHelper.simpleQuery(callback, command);

    }



    public void queryPlaylists(Callback<List<String>> callback) {
        String command = "listplaylists";
        channelHelper.simpleQuery(callback, command);
    }

    public void queryPlaylistByName(String name, Callback<List<String>> callback) {
        String command = "listplaylistinfo \"" + name + "\"";
        channelHelper.simpleQuery(callback, command);
    }

    public void queryAlbumArtists(Callback<List<String>> callback) {
        String command = "list albumartist";
        channelHelper.simpleQuery(callback, command);
    }

    public void queryArtists(Callback<List<String>> callback) {
        String command = "list artist";
        channelHelper.simpleQuery(callback, command);

    }

    public void queryAlbumsByArtist(String albumArtist, Callback<List<String>> callback) {
        String command = "list album albumartist \"" + albumArtist + "\"";
        channelHelper.simpleQuery(callback, command);

    }

    public void querySongsByAlbum(String albumArtist, String album, Callback<List<String>> callback) {
        String command = String.format("find \"((albumartist == \\\"%s\\\") AND (album == \\\"%s\\\"))\"", albumArtist, album);
        channelHelper.simpleQuery(callback, command);

    }

    private Result<List<String>> queryArtistSongFilenamesSync(Channel channel, String artist) {

        String command = String.format("list file \"(artist == \\\"%s\\\")\"", artist);

        Result<List<String>> result = channelHelper.query(channel, command);

        if (result.message != null)
            result.message = Parser.resultToList(result.message);

        return result;

    }

    public void addArtistToQueue(String artist, boolean clear, int pos, Callback<List<String>> callback) {

        if (threadPool != null)
            threadPool.execute(() -> addArtistToQueueExecute(artist, clear, pos, callback));
        else
            addArtistToQueueExecute(artist, clear, pos, callback);

    }

    private void addArtistToQueueExecute(String artist, boolean clear, int pos, Callback<List<String>> callback) {

            Channel channel = new Channel(getConnection());

            Result<List<String>> result = queryArtistSongFilenamesSync(channel, artist);

            if (result.type == Result.ResultType.ERROR || result.message.isEmpty()) {
                if (callback != null)
                    callback.onResult(result);
                return;
            }

            ArrayList<String> commandList = new ArrayList<>();

            if (clear)
                commandList.add("clear");
            for (String s : result.message) {
                commandList.add("addid \"" + s + "\"");
            }
            if (pos > -1)
                commandList.add("play " + pos);

            result = channelHelper.query(channel, commandList.toArray(new String[0]));
            result.message = Parser.resultToList(result.message);

            channel.close();

            if (callback != null)
                callback.onResult(result);

    }




    private Result<List<String>> queryAlbumSongFilenamesSync(Channel channel, String albumArtist, String album) {

        String command = String.format("list file \"((albumartist == \\\"%s\\\") AND (album == \\\"%s\\\"))\"", albumArtist, album);

        Result<List<String>> result = channelHelper.query(channel, command);

        if (result.message != null)
            result.message = Parser.resultToList(result.message);

        return result;


    }


    public void addSongsToQueue(String[] filenames, Callback<List<String>> callback) {
        StringBuilder builder = new StringBuilder();
        for (String filename : filenames) {
            builder.append("addid \"").append(filename).append("\"").append("\n");
        }
        String command = builder.toString();

        channelHelper.simpleQuery(callback, command.trim());
    }

    public void addAlbumToPlaylist(String albumArtist, String album, String playlist, Callback<List<String>> callback) {
        if (threadPool !=  null)
            threadPool.execute(() -> addAlbumToPlaylistExecute(albumArtist, album, playlist, callback));
        else
            addAlbumToPlaylistExecute(albumArtist, album, playlist, callback);

    }

    private void addAlbumToPlaylistExecute(String albumArtist, String album, String playlist, Callback<List<String>> callback) {

            Channel channel = new Channel(getConnection());

            Result<List<String>> result = queryAlbumSongFilenamesSync(channel, albumArtist, album);

            List<String> urls = result.message;

            if (result.type == Result.ResultType.OK) {
                result = addUrlsToPlaylistSync(channel, playlist, result.message.toArray(new String[0]));
                if (result.type == Result.ResultType.OK) {
                    result.message = urls;
                }
            }

            channel.close();

            if (callback != null)
                callback.onResult(result);

    }
    public void addArtistToPlaylist(String artist, String playlist, Callback<List<String>> callback) {

        if (threadPool !=  null)
            threadPool.execute(() -> addArtistToPlaylistExecute(artist, playlist, callback));
        else
            addArtistToPlaylistExecute(artist, playlist, callback);
    }

    private void addArtistToPlaylistExecute(String artist, String playlist, Callback<List<String>> callback) {

            Channel channel = new Channel(getConnection());

            Result<List<String>> result = queryArtistSongFilenamesSync(channel, artist);

            List<String> urls = result.message;

            if (result.type == Result.ResultType.OK) {
                result = addUrlsToPlaylistSync(channel, playlist, result.message.toArray(new String[0]));
                if (result.type == Result.ResultType.OK) {
                    result.message = urls;
                }
            }

            channel.close();

            if (callback != null)
                callback.onResult(result);
    }




    public void addAlbumToQueue(String albumArtist, String album, Callback<List<String>> callback) {

        if (threadPool != null)
            threadPool.execute(() -> addAlbumToQueueExecute(albumArtist, album, callback));
        else
            addAlbumToQueueExecute(albumArtist, album, callback);

    }

    private void addAlbumToQueueExecute(String albumArtist, String album, Callback<List<String>> callback) {

            Channel channel = new Channel(getConnection());

            Result<List<String>> result = queryAlbumSongFilenamesSync(channel, albumArtist, album);

            if (result.type == Result.ResultType.ERROR) {
                if (callback != null)
                    callback.onResult(result);
                return;
            }

            ArrayList<String> commandList = new ArrayList<>();
            for (String s : result.message) {
                commandList.add("addid \"" + s + "\"");
            }

            result = channelHelper.query(channel, commandList.toArray(new String[0]));

            channel.close();

            if (callback != null) {
                result.message = Parser.resultToList(result.message);
                callback.onResult(result);
            }

    }

    private Result<List<String>> addUrlsToPlaylistSync(Channel channel, String playlist, String... urls) {


        String[] commands = new String[urls.length];
        for (int i = 0; i < urls.length; i++) {
            commands[i] = String.format("playlistadd \"%s\" \"%s\"", playlist, urls[i]);
        }

        return channelHelper.query(channel, commands);

    }

    public void playAlbum(String albumArtist, String album, int pos, Callback<List<String>> callback) {

        if (threadPool != null)
            threadPool.execute(() -> playAlbumExecute(albumArtist, album, pos, callback));
        else
            playAlbumExecute(albumArtist, album, pos, callback);
    }

    private void playAlbumExecute(String albumArtist, String album, int pos, Callback<List<String>> callback) {

            Channel channel = new Channel(getConnection());

            Result<List<String>> result = queryAlbumSongFilenamesSync(channel, albumArtist, album);

            if (result.type == Result.ResultType.ERROR) {
                if (callback != null)
                    callback.onResult(result);
                return;
            }

            ArrayList<String> commandList = new ArrayList<>();
            commandList.add("clear");
            for (String s : result.message) {
                commandList.add("addid \"" + s + "\"");
            }
            if (pos > -1)
                commandList.add("play " + pos);

            result = channelHelper.query(channel, commandList.toArray(new String[0]));

            channel.close();

            if (callback != null) {
                result.message = Parser.resultToList(result.message);
                callback.onResult(result);
            }

    }

    public void addPlaylistToQueue2(String playlistName, boolean clear, Callback<List<String>> callback) {

        if (threadPool != null)
            threadPool.execute(() -> addPlaylistToQueue2Execute(playlistName, clear, callback));
        else
            addPlaylistToQueue2Execute(playlistName, clear, callback);

    }
    private void addPlaylistToQueue2Execute(String playlistName, boolean clear, Callback<List<String>> callback) {

        //protocol: command "load [playlist]" loads playlist to queue, ignores files without permissions, respects m3u EXTM3U tags,
        // but don't return valid ids or the number of successfully added songs

            Channel channel = new Channel(getConnection());

            ArrayList<String> commandList = new ArrayList<>();

            if (clear)
                commandList.add("clear");

            commandList.add("load \"" +  playlistName + "\"");

            Result<List<String>> result = channelHelper.query(channel, commandList.toArray(new String[0]));

            channel.close();

            if (callback != null) {
                callback.onResult(result);
            }

    }

    public void addPlaylistToQueue(String playlistName, boolean clear, int pos, Callback<List<String>> callback) {
        if (threadPool != null)
            threadPool.execute( () -> addPlaylistToQueueExecute(playlistName, clear, pos, callback));
        else
            addPlaylistToQueueExecute(playlistName, clear, pos, callback);
    }

    private void addPlaylistToQueueExecute(String playlistName, boolean clear, int pos, Callback<List<String>> callback) {

        //protocol: command "load [playlist]" loads playlist direct to queue, but don't return valid ids or the number of successfully added songs"

            Channel channel = new Channel(getConnection());

            String command = "listplaylistinfo \"" + playlistName + "\"";

            Result<List<String>> result = channelHelper.query(channel, command);

            if (result.type == Result.ResultType.ERROR) {
                if (callback != null)
                    callback.onResult(result);
                return;
            }

            System.out.println("PlaylistSongs: " + result);

            for (String line : result.message) {
                System.out.println(line);
            }

            ArrayList<String> commandList = new ArrayList<>();
            if (clear)
                commandList.add("clear");

            int noFiles = 0;
            int filesAdded = 0;
            String file = null;
            for (String line : result.message) {
                if (line.startsWith("file:")) {
                    file = line.split(": ", 2)[1] + "\"";
                    noFiles++;
                    if (file.startsWith("http")) {
                        commandList.add("addid \"" + file);
                        filesAdded++;
                        file = null;
                    }

                } else if (line.startsWith("Last-Modified") && file != null) {
                    commandList.add("addid \"" + file);
                    filesAdded++;
                    file = null;
                }
            }

            if (pos > -1 && filesAdded == noFiles)
                commandList.add("play " + pos);

            System.out.println("addPlaylistoQueue: " + commandList);

            result = channelHelper.query(channel, commandList.toArray(new String[0]));

            channel.close();

            if (callback != null) {
                result.message = Parser.resultToList(result.message);
                callback.onResult(result);
            }

    }

    public void deletePlaylist(String playlist, Callback<List<String>> callback) {
        String command = String.format("rm \"%s\"", playlist );
        channelHelper.simpleQuery(callback, command);
    }

    public void renamePlaylist(String oldName, String newName, Callback<List<String>> callback) {
        String command = String.format("rename \"%s\" \"%s\"", oldName, newName);
        channelHelper.simpleQuery(callback, command);
    }

    public void addToPlaylist(Callback<List<String>> callback, String playlist, String... urls) {

        String[] commands = new String[urls.length];
        for (int i = 0; i < urls.length; i++) {
            commands[i] = String.format("playlistadd \"%s\" \"%s\"", playlist, urls[i]);
        }

        channelHelper.simpleQuery(callback, commands);
    }

    public void saveQueueToPlaylist(String playlist, Callback<List<String>> callback) {
        String command = String.format("save \"%s\"", playlist);
        channelHelper.simpleQuery(callback, command);
    }

    public void removeFromPlaylist(String playlist, int pos, Callback<List<String>> callback) {
        String command = String.format("playlistdelete \"%s\" %s", playlist, pos);
        channelHelper.simpleQuery(callback, command);
    }

    public void moveItemInPlaylist(String playlist, int fromPos, int toPos, Callback<List<String>> callback) {

        String command = String.format("playlistmove \"%s\" %s %s", playlist, fromPos, toPos);
        channelHelper.simpleQuery(callback, command);

    }

    public void moveItemInQueue(int from, int to, Callback<List<String>> callback) {
        String command = String.format("move %s %s", from, to);
        channelHelper.simpleQuery(callback, command);
    }

    public void removeItemFromQueue(int pos, Callback<List<String>> callback) {
        String command = "delete " + pos;
        channelHelper.simpleQuery(callback, command);

    }

    public void removeItemsFromQueue(int startPos, int endPos, Callback<List<String>> callback) {

        String command = String.format("delete %s:%s", startPos, endPos);
        System.out.println("PLAYER remove Items from Queue " + command);
        channelHelper.simpleQuery(callback, command);

    }



    public void play(int pos, Callback<List<String>> callback) {
        String command = "play " + pos;
        channelHelper.simpleQuery(callback, command);

    }

    public void playid(int id, Callback<List<String>> callback) {
        String command = "playid " + id;
        channelHelper.simpleQuery(callback, command);
    }


    public void play(Callback<List<String>> callback) {
        String command = "play";
        channelHelper.simpleQuery(callback, command);

    }

    public void pauseResume(Callback<List<String>> callback) {
        String command = "pause";
        channelHelper.simpleQuery(callback, command);
    }

    public void stop(Callback<List<String>> callback) {
        String command = "stop";
        channelHelper.simpleQuery(callback, command);
    }

    public void next(Callback<List<String>> callback) {
        String command = "next";
        channelHelper.simpleQuery(callback, command);

    }

    public void previous(Callback<List<String>> callback) {
        String command = "previous";
        channelHelper.simpleQuery(callback, command);

    }

    public void seekCurrentTo(int second, boolean relative, Callback<List<String>> callback) {
        String pos = String.valueOf(second);
        if (relative && second>0)
            pos = "+" + pos;
        String command = "seekcur " + pos;
        channelHelper.simpleQuery(callback, command);
    }

    public void seekIdTo(int id, int second, Callback<List<String>> callback) {

        String command = "seekid " + id + " " + second;
        channelHelper.simpleQuery(callback, command);

    }

    public void queryStatus(Callback<Map<String, String>> callback) {

        String command = "status";

        Callback<List<String>> listCallback = mpdResult -> {
            Result<Map<String, String>> result = new Result<>(null, mpdResult.exception, mpdResult.type);
            if (result.type == Result.ResultType.OK) {
                result.message = Parser.resultToMap(mpdResult.message);
            }
            callback.onResult(result);
        };

        channelHelper.simpleQuery(listCallback, command);


    }

    public void queryCurrentSong(Callback<Map<String, String>> callback) {

        String command = "currentsong";

        Callback<List<String>> listCallback = mpdResult -> {
            Result<Map<String, String >> result = new Result<>(null, mpdResult.exception, mpdResult.type);
            if (result.type == Result.ResultType.OK) {
                result.message = Parser.resultToMap(mpdResult.message);
            }
            callback.onResult(result);
        };

        channelHelper.simpleQuery(listCallback, command);

    }

    public void queryServerStats(Callback<Map<String, String>> callback) {

        String command = "stats";

        Callback<List<String>> callback1 = result -> {
            Result<Map<String, String >> result1 = new Result<>(null, result.exception, result.type);
            if (result.type == Result.ResultType.OK) {
                result1.message = Parser.resultToMap(result.message);
            }
            callback.onResult(result1);
        };

        channelHelper.simpleQuery(callback1, command);

    }

    public void updateDatabase(Callback<List<String>> callback) {

        String command = "update";
        channelHelper.simpleQuery(callback, command);

    }

    public void queryAudioOutputs(Callback<List<AudioOutput>> callback) {

        String command = "outputs";

        Callback<List<String>> callback1 = result -> {
            Result<List<AudioOutput>> result1 = new Result<>(null, result.exception, result.type);
            if (result.type == Result.ResultType.OK) {
                result1.message = Parser.parseAudioOutputs(result.message);
            }
            callback.onResult(result1);
        };

        channelHelper.simpleQuery(callback1, command);

    }

    public void toggleAudioOutput(int id, Callback<List<String>> callback) {

        String command = "toggleoutput " + id;
        channelHelper.simpleQuery(callback, command);

    }

    public void repeat(Callback<List<String>> callback, boolean repeat) {
        String command = "repeat " + ((repeat) ? "1" : "0");
        channelHelper.simpleQuery(callback, command);
    }

    public void consume(Callback<List<String>> callback, boolean consume) {
        String command = "consume " + ((consume) ? "1" : "0");
        channelHelper.simpleQuery(callback, command);
    }

    public void random(Callback<List<String>> callback, boolean random) {
        String command = "random " + ((random) ? "1" : "0");
        channelHelper.simpleQuery(callback, command);
    }

    public void single(Callback<List<String>> callback, boolean single) {
        String command = "single " + ((single) ? "1" : "0");
        channelHelper.simpleQuery(callback, command);
    }

    public void shuffleQueue(Callback<List<String>> callback) {
        String command = "shuffle";
        channelHelper.simpleQuery(callback, command);

    }

    public void clearQueue(Callback<List<String>> callback) {
        String command = "clear";
        channelHelper.simpleQuery(callback, command);

    }


}
