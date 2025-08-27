package de.nif.idle;



import de.nif.client.Channel;
import de.nif.client.Connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IdleLoop  {

    private static final String LOGTAG = "IdleLoop";

    private boolean shouldIdle;
    private boolean isRunning;
    private final ExecutorService executor;
    private final Connection connection;
    private Channel channel;
    private final IdleListener listener;

    public interface IdleListener {
        void onIdleResult(List<String> idleResult);
    }

    public IdleLoop(Connection connection, IdleListener listener) {

        this.connection = connection;
        executor = Executors.newSingleThreadExecutor();
        this.listener = listener;


    }


    public void run() {

        shouldIdle = true;

        if (isRunning) return;



        executor.execute(() -> {
            Exception exception = null;
            try {
                isRunning = true;
                channel = new Channel(connection, false);
                channel.initSocket();

                while (shouldIdle) {
                    channel.send("idle");
                    List<String> result = channel.receive();
                    if (listener != null) {
                        listener.onIdleResult(extractTagsFromChannelResult(result));
                    }
                }

                System.out.println("IdleLoop run: end while");

            } catch (IOException e) {
                e.printStackTrace();

                exception = e;
            }


            isRunning = false;
            channel.close();
            System.out.println("IdleLoop run: channel closed, set connected false");
            if (connection.isConnected())
                connection.setConnected(false, null, exception);

        });

    }

    private List<String> extractTagsFromChannelResult(List<String> channelResult) {
        ArrayList<String> result = new ArrayList<>();
        String value;
        for (String s : channelResult) {
            if (s.startsWith("changed")) {
                value = s.split(": ", 2)[1];
                if (!value.isEmpty())
                    result.add(value);
            }
        }
        return result;
    }

    public void stop() {

        shouldIdle = false;

        if (channel != null && !channel.getSocket().isClosed()) {
            executor.execute(() -> {
                try {
                    channel.send("noidle");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            });


            }



    }

    public void setShouldIdle(boolean shouldIdle) {
        this.shouldIdle = shouldIdle;
    }
}
