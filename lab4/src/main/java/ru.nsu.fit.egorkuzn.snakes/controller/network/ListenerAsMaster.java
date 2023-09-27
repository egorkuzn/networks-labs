package ru.nsu.fit.egorkuzn.snakes.controller.network;

import me.ippolitov.fit.snakes.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;

// Listens different messages
public class ListenerAsMaster {
    public boolean newPlayer = false;
    public InetAddress newPlayerInetAddress;
    private MulticastSocket socket;
    private final HashMap<InetAddress, Integer> mapPlayers = new HashMap<InetAddress, Integer>();
    private final HashMap<Integer, Integer> mapChanges = new HashMap<Integer, Integer>();
    private Integer lastNewPort = null;
    public boolean newChange = false;


    public ListenerAsMaster(MulticastSocket socket_) {
        socket = socket_;
        //поток который слушает сообщения
        Thread listener = new Thread(new Runnable() {
            public void run() {
                try {
                    listen();
                } catch (Exception exp) {
                    exp.printStackTrace();
                }

            }
        });
        listener.start();
    }

    // Takes messages from players and updates maps
    public void listen() throws IOException {
        while (true) {
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, 0, data.length);

            try {
                socket.receive(packet);
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

            if (msg.hasJoin()) {
                actionJoinMsg(packet, msg);
            }

            if (msg.hasSteer()) {
                actionSteerMsg(packet, msg);
            }
        }
    }

    // Action after join message
    private void actionJoinMsg(DatagramPacket packet, SnakesProto.GameMessage msg) {
        System.out.println("Master get join msg");
        // Save joined player
        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        lastNewPort = port;
        mapPlayers.put(address, port);
        // Update response message and constructor message
        long seq = msg.getMsgSeq();
        SnakesProto.GameMessage.Builder gameMessageBuilder = SnakesProto.GameMessage.newBuilder(SnakesProto.GameMessage.getDefaultInstance());
        gameMessageBuilder.setMsgSeq(seq);
        // AckMsg constructor
        SnakesProto.GameMessage.AckMsg.Builder msgAck = SnakesProto.GameMessage.AckMsg.newBuilder();
        // Message build
        gameMessageBuilder.setAck(msgAck.build()).setReceiverId(1);
        byte[] dataSecond = gameMessageBuilder.build().toByteArray();
        DatagramPacket packetAckMsg = new DatagramPacket(dataSecond, dataSecond.length, address, port);

        try {
            socket.send(packetAckMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Master send ackMsg");
        newPlayer = true;
    }

    // Action after steer message
    private void actionSteerMsg(DatagramPacket packet, SnakesProto.GameMessage msg) {
        // Save
        SnakesProto.GameMessage.SteerMsg steer = msg.getSteer();
        mapChanges.put(packet.getPort(), steer.getDirection().getNumber());
        // Set flag
        newChange = true;
        System.out.println("Master get steerMsg");
        // For debug
        System.out.println(mapChanges);
    }

    // Gives from steer msg
    public HashMap<Integer, Integer> getMapChanges() {
        return mapChanges;
    }

    // Takes useful bytes
    public byte[] getRealBytes(byte[] oldData, int size) {
        byte[] newData = new byte[size];
        System.arraycopy(oldData, 0, newData, 0, size);
        return newData;
    }

    // Give players map for sender
    public HashMap<InetAddress, Integer> getMapPlayers() {
        return mapPlayers;
    }

    // Gives port
    public Integer getLastNewPort() {
        return lastNewPort;
    }
}