package ru.nsu.fit.egorkuzn.snakes.controller.network;

import me.ippolitov.fit.snakes.SnakesProto;
import ru.nsu.fit.egorkuzn.snakes.model.SnakeData;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SenderAsMaster {
    int currentGame = 0;
    ListenerAsMaster listener;
    MulticastSocket socket;
    Boolean canJoin = true;
    SnakesProto.GameConfig gameConfig;
    String groupAddress;
    Integer groupPort;
    int stateOrder = 0;

    public SenderAsMaster(ListenerAsMaster listener_, MulticastSocket socket_, SnakesProto.GameConfig gameConfig_, String groupAddress_, Integer groupPort_) {
        listener = listener_;
        socket = socket_;
        gameConfig = gameConfig_;
        groupAddress = groupAddress_;
        groupPort = groupPort_;
        sendAnMsg();
    }

    // Send messages AnnouncementMsg with once a sec
    private void sendAnMsg() {
        Thread sender = new Thread(() -> {
            long start = System.currentTimeMillis();
            long finish;

            while (true) {
                HashMap<InetAddress, Integer> mapPlayers = listener.getMapPlayers();
                SnakesProto.GamePlayers gamePlayers = getGamePlayers(mapPlayers);

                // Message constructor
                SnakesProto.GameMessage.Builder gameMessageBuilder = SnakesProto.GameMessage.newBuilder(SnakesProto.GameMessage.getDefaultInstance());
                gameMessageBuilder.setMsgSeq(100);
                // Announcement message constructor
                SnakesProto.GameMessage.AnnouncementMsg.Builder msg = SnakesProto.GameMessage.AnnouncementMsg.newBuilder();
                SnakesProto.GameAnnouncement.Builder gameAnnouncement = SnakesProto.GameAnnouncement.newBuilder();
                gameAnnouncement.setCanJoin(canJoin).setConfig(gameConfig).setPlayers(gamePlayers);
                msg.setGames(currentGame, gameAnnouncement.build());
                gameMessageBuilder.setAnnouncement(msg.build());
                byte[] data = gameMessageBuilder.build().toByteArray();

                finish = System.currentTimeMillis();

                if (finish - start >= 1000) {
                    try {
                        DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(groupAddress), groupPort);
                        socket.send(packet);
                        start = System.currentTimeMillis();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        sender.start();
    }

    public void sendStateMsg(int appleX, int appleY, ArrayList<SnakeData> snakes) {
        // Message build
        HashMap<InetAddress, Integer> mapPlayers = listener.getMapPlayers();
        SnakesProto.GamePlayers gamePlayers = getGamePlayers(mapPlayers);
        // State build
        SnakesProto.GameState.Coord coord = SnakesProto.GameState.Coord.newBuilder().setX(appleX).setY(appleY).build();
        SnakesProto.GameState.Builder state = SnakesProto.GameState.newBuilder()
                .setStateOrder(stateOrder)
                .addFoods(coord)
                .setPlayers(gamePlayers);
        stateOrder++;

        for (SnakeData snake : snakes) {
            SnakesProto.GameState.Snake.Builder snakeMsg = SnakesProto.GameState.Snake.newBuilder()
                    .setHeadDirection(getNewDirection(snake.directionNumber))
                    .setPlayerId(0)
                    .setState(SnakesProto.GameState.Snake.SnakeState.ALIVE);
            setPoints(snakeMsg, snake);
            state.addSnakes(snakeMsg.build());
        }
        // Constructor of message
        SnakesProto.GameMessage.Builder gameMessageBuilder = SnakesProto.GameMessage.newBuilder(SnakesProto.GameMessage.getDefaultInstance());
        gameMessageBuilder.setMsgSeq(100);
        // StateMsg constructor
        SnakesProto.GameMessage.StateMsg.Builder msg = SnakesProto.GameMessage.StateMsg.newBuilder();
        msg.setState(state);

        gameMessageBuilder.setState(msg.build());
        // Response for all players
        byte[] data = gameMessageBuilder.build().toByteArray();

        for (Map.Entry<InetAddress, Integer> entry : mapPlayers.entrySet()) {
            try {
                DatagramPacket packet = new DatagramPacket(data, data.length, entry.getKey(), entry.getValue());
                socket.send(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Returns modified direction number
    public SnakesProto.Direction getNewDirection(int direction) {
        return SnakesProto.Direction.forNumber(direction);
    }

    // Returns modified array with snake coords
    public int[] getNewArray(int[] array) {
        int[] newArr = new int[array.length];

        for (int i = 0; i < array.length; i++) {
            if (i == 0) {
                newArr[i] = array[i];
            } else {
                newArr[i] = array[i] - array[i - 1];
            }
        }

        return newArr;
    }

    public SnakesProto.GameState.Snake.Builder setPoints(SnakesProto.GameState.Snake.Builder snakeMsg, SnakeData snake) {
        int[] x = getNewArray(snake.x);
        int[] y = getNewArray(snake.y);

        SnakesProto.GameState.Coord.Builder coord = SnakesProto.GameState.Coord.newBuilder();

        for (int i = 0; i < snake.dots; i++) {
            coord.setX(x[i]).setY(y[i]);
            snakeMsg.addPoints(coord.build());
        }

        return snakeMsg;
    }

    // Build gamePlayers on the current map
    public SnakesProto.GamePlayers getGamePlayers(HashMap<InetAddress, Integer> map) {
        // Put u in gamePlayer
        SnakesProto.GamePlayer myDataPlayer = SnakesProto.GamePlayer.newBuilder()
                .setId(0)
                .setPort(getPort())
                .setRole(getRole())
                .setScore(100)
                .setType(SnakesProto.PlayerType.HUMAN)
                .setIpAddress("")
                .setName("master")
                .build();
        
        return SnakesProto.GamePlayers.newBuilder().addPlayers(0, myDataPlayer).build();
    }
    
    // Returns port of own socket
    public int getPort() {
        return 4000;
    }
    
    // Returns own role
    public SnakesProto.NodeRole getRole() {
        return SnakesProto.NodeRole.MASTER;
    }
}
