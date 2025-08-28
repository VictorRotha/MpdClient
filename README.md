# Mpd Client

A client for the mpd player.

## How to use

1. Create an instance of the Player class. You can specify the number of threads, the Player will use to connect to the mpd server or leave it empty to use the default value of 6. If you set the number to 0, all queries will be executed synchronous.
2. Connect the Player to the server.
3. Use the public Player methods to control the mpd server.

```java

    var host = "localhost";
    var port = 6600;
       
    Player player = new Player();
    player.connect(host, port, result -> {
        if (result.type == Result.ResultType.OK)
            System.out.println("Connected to server!");
        else
            System.out.println("Connection failed: " + result.message);
    });

    player.play(null);


```

### Idle

To listen to mpd changes, you can use the IdleLoop. This class connects to the server with the special "idle" with no server timeout.
1. Get the Connection object from your (connected) player instance.
2. Initialize the loop with the connection and provide an IdleListener to listen for server changes
3. run the loop
4. If you don't need the loop anymore, stop the loop.

```java

        var connection = player.getConnection();
              
        var loop = new IdleLoop(connection, idleResult -> {
            System.out.println("idleResult: " + idleResult);
        });
        loop.run();
        
        //stop the loop when not needed anymore:
        loop.stop();


```

The idle result provides an array of keywords, which describes the changes. (see full list [here](https://mpd.readthedocs.io/en/stable/protocol.html#querying-mpd-s-status) )

| tag | description                                                                                                                   | 
|-----|-------------------------------------------------------------------------------------------------------------------------------|
| database  | the song database has been modified after update.                                                                             |
| stored_playlist | a stored playlist has been modified, renamed, created or deleted.                                                             |
| playlist | the queue (i.e. the current playlist) has been modified.                                                                      |
| player | the player has been started, stopped or seeked or tags of the currently playing song have changed (e.g. received from stream) | 
| output | an audio output has been added, removed or modified (e.g. renamed, enabled or disabled).                                      |

