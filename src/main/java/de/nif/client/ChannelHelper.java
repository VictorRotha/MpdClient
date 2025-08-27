package de.nif.client;

import de.nif.data.AckException;
import de.nif.data.Result;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;


public class ChannelHelper {

    private final Connection connection;
    private final ExecutorService threadPool;

    public ChannelHelper(Connection connection, ExecutorService threadpool) {

        this.connection = connection;
        this.threadPool = threadpool;

    }



    public Result<List<String>> query(Channel channel, String... commands) {

        Result<List<String>> result = new Result<>();

        if (!connection.isConnected()) {
            result.type = Result.ResultType.ERROR;
            result.exception = new Exception("Connection is not connected");
            return result;
        }


        try {
            if (channel.getSocket() == null)
                channel.initSocket();
            channel.send(commands);
            result.message = channel.receive();
            result.type = Result.ResultType.OK;

        } catch (IOException e) {
            result.type = Result.ResultType.ERROR;
            result.exception = e;
            e.printStackTrace();
            if (! (e instanceof AckException)) {
                connection.setConnected(false, null, e);
            }
        }

        return result;

    }

    public Result<List<String>> simpleQuery(String... commands) {

        Channel channel = new Channel(connection);

        Result<List<String>> result = query(channel, commands);

        channel.close();

        return result;
    }

    public void simpleQueryASync(Player.Callback<List<String>> callback, String... commands) {

        threadPool.execute(() -> {

            Result<List<String>> result = simpleQuery(commands);
            System.out.println(result);
            if (callback != null)
                callback.onResult(result);


        });

    }


}
