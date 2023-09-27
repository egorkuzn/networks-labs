package ru.nsu.fit.egorkuzn.snakes.controller.network;

import me.ippolitov.fit.snakes.SnakesProto;
import ru.nsu.fit.egorkuzn.snakes.model.SnakeData;

import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

public class ListenerAsNormal {
    private MulticastSocket multicastSocket;
    private DatagramSocket datagramSocket;
    private HashMap<InetAddress, SnakesProto.GameConfig> mapGameConfig = new HashMap<>();
    private HashMap<InetAddress, Integer> mapMasters = new HashMap<>();
    private int appleX;
    private int appleY;
    private ArrayList<SnakeData> snakes;
    public boolean dataUpdate = false;
    public boolean inGame = false;
    
    public ListenerAsNormal(MulticastSocket socket_, DatagramSocket datagramSocket_) {
        multicastSocket = socket_;
        datagramSocket = datagramSocket_;
        
        // Thread that listen messages on multicast
        Thread listenerMulticast = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    listenMulticast();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        listenerMulticast.start();
        
        // Thread that listen unicast:
        Thread listenerUnicast = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    listenUnicast();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        listenerUnicast.start();
    }

    private void listenUnicast() {
        System.out.println("listenUnicast start");
        
        while (true) {
            if (inGame) {
                System.out.println("normal listen datagram");
                byte[] data = new byte[4096];
                DatagramPacket packet = new DatagramPacket(data, 0, data.length);
                
                try {
                    datagramSocket.receive(packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                byte[] newData = getRealBytes(packet.getData(), packet.getLength());
                SnakesProto.GameMessage msg = null;
                
                try {
                    msg = SnakesProto.GameMessage.parseFrom(newData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                if (msg.hasState()) {
                    System.out.println("normal have state msg");
                    SnakesProto.GameMessage.StateMsg stateMsg = msg.getState();
                    SnakesProto.GameState state = stateMsg.getState();
                    appleX = state.getFoods(0).getX();
                    appleY = state.getFoods(0).getY();
                    List<SnakesProto.GameState.Snake> snakeList = state.getSnakesList();
                    
                    actionFirstState(snakeList);
                    
                    dataUpdate = true;
                }
            }
            
            try {
                sleep(100);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    private void actionFirstState(List<SnakesProto.GameState.Snake> snakeList) {
        ArrayList<SnakeData> mySnakes = new ArrayList<>();

        for (SnakesProto.GameState.Snake snake : snakeList) {
            SnakeData newSnake = new SnakeData(true);
            SnakesProto.GameState.Snake currentSnake = snake;
            setXYArray(currentSnake.getPointsList(), newSnake);
            setDirection(currentSnake.getHeadDirection(), newSnake);
            mySnakes.add(newSnake);
        }
        
        snakes = mySnakes;
    }

    private void setDirection(SnakesProto.Direction headDirection, SnakeData newSnake) {
        int number = headDirection.getNumber();

        switch (number) {
            case (1) : {
                newSnake.directionNumber = 1;
                newSnake.right = false;
                newSnake.left = false;
                newSnake.up = true;
                newSnake.down = false;
                break;
            }

            case (2) : {
                newSnake.directionNumber = 2;
                newSnake.right = false;
                newSnake.left = false;
                newSnake.up = false;
                newSnake.down = true;
                break;
            }

            case (3) : {
                newSnake.directionNumber = 3;
                newSnake.right = false;
                newSnake.left = true;
                newSnake.up = false;
                newSnake.down = false;
                break;
            }

            case (4) : {
                newSnake.directionNumber = 4;
                newSnake.right = true;
                newSnake.left = false;
                newSnake.up = false;
                newSnake.down = false;
                break;
            }

            default: System.out.println("Some irregular case for direction");
        }
    }

    private void setXYArray(List<SnakesProto.GameState.Coord> pointsList, SnakeData newSnake) {
        int myDots = 0;

        for (int i = 0; i < pointsList.size(); i++) {
            SnakesProto.GameState.Coord point = pointsList.get(i);
            newSnake.x[i] = point.getX();
            newSnake.y[i] = point.getY();
            myDots++;
        }

        newSnake.dots = myDots;
    }

    private byte[] getRealBytes(byte[] oldData, int size) {
        byte[] newData = new byte[size];
        System.arraycopy(oldData, 0, newData, 0, size);
        return newData;
    }

    // Multicast listener function for getting actual games
    private void listenMulticast() {
        while (true) {
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, 0, data.length);

            try {
                multicastSocket.receive(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }

            byte[] newData = getRealBytes(packet.getData(), packet.getLength());
            SnakesProto.GameMessage msg = null;

            try {
                msg = SnakesProto.GameMessage.parseFrom(newData);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (msg.hasAnnouncement()) {
                SnakesProto.GameMessage.AnnouncementMsg anMsg = msg.getAnnouncement();
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                SocketAddress port2 = packet.getSocketAddress();

                for (var config : anMsg.getGamesList()) {
                    mapGameConfig.put(address, config.getConfig());
                }

                mapMasters.put(address, port);
            }
        }
    }

    // Getting list of all available games
    public ArrayList<String> getListGame() {
        ArrayList<String> array = new ArrayList<>();
        boolean flag = true;

        while (flag) {
            if (mapGameConfig.size() != 0) {
                flag = false;

                for (Map.Entry<InetAddress, SnakesProto.GameConfig> entry : mapGameConfig.entrySet()) {
                    array.add(entry.getKey().toString());
                }
            }

            try {
                sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return array;
    }

    // Returns masters port from mapMAsters
    public int getPortMaster(InetAddress address) {
        return mapMasters.get(address);
    }

    // Returns array of apple coords
    public int[] getAppleLocation() {
        int[] location = new int[2];
        location[0] = appleX;
        location[1] = appleY;
        return location;
    }

    // Return array of snakes coord
    public ArrayList<SnakeData> getSnakes() {
        return snakes;
    }
}
