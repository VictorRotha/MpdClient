package de.nif;

import de.nif.client.Player;
import de.nif.data.Result;
import de.nif.idle.IdleLoop;

import java.util.List;


public class Main {
    public static void main(String[] args) {
        Player player = new Player(0);

        //Example implementation

        String host = "raspimpd3";
        int port = 6600;

        idleExample(host, port);

//        player.connect(host, port, result -> {
//            System.out.println("Connect : " + result);
//            if (result.type == Result.ResultType.OK) {
//                player.queryStatus(status -> {
//                    System.out.println("status: " + status);
//                });
//
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//
//                player.pauseResume(null);
//
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//
//                player.pauseResume(null);
//            }
//        });


//        player.pauseResume(result -> System.out.println("pauseResume: " + result));

    }


    public static void idleExample(String host, int port) {

        var player = new Player();

        player.connect(host, port, new Player.Callback<String>() {
            @Override
            public void onResult(Result<String> result) {

                var idle = new IdleLoop(player.getConnection(), new IdleLoop.IdleListener() {
                    @Override
                    public void onIdleResult(List<String> idleResult) {
                        System.out.println("IdleResult : " + idleResult);
                    }
                });

                idle.run();


            }
        });


    }
}
