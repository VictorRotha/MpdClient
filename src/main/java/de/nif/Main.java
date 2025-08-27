package de.nif;

import de.nif.client.Player;
import de.nif.data.Result;


public class Main {
    public static void main(String[] args) {
        Player player = new Player(0);

        //Example implementation

        String host = "raspimpd3";
        int port = 6600;

        player.connect(host, port, result -> {
            System.out.println("Connect : " + result);
            if (result.type == Result.ResultType.OK) {
                player.queryStatus(status -> {
                    System.out.println("status: " + status);
                });

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                player.pauseResume(null);

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                player.pauseResume(null);
            }
        });



//        player.pauseResume(result -> System.out.println("pauseResume: " + result));

    }
}