package ru.nsu.fit.egorkuzn.snakes.controller.network;

import me.ippolitov.fit.snakes.SnakesProto;

import java.net.DatagramSocket;
import java.net.InetAddress;

public class SenderAsNormal {
    DatagramSocket socket;
    InetAddress addressSelectedGame_;
    Integer portSelectedGame_;

    public SenderAsNormal(DatagramSocket socket_) {
        socket = socket_;
    }

    public boolean sendJoinMsg(InetAddress addressSelectedGame, Integer portSelectedGame) {
        addressSelectedGame_ = addressSelectedGame;
        portSelectedGame_ = portSelectedGame;

        boolean answer = false;
        // Constructor of massage
        SnakesProto.GameMessage.Builder gameMessageBuilder = SnakesProto.GameMessage.newBuilder(SnakesProto.GameMessage.getDefaultInstance());
        gameMessageBuilder.setMsgSeq(101);
        // Constructor JoinMsg

    }
}
