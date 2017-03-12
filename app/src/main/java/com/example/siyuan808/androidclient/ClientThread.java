package com.example.siyuan808.androidclient;

/**
 * Created by siyuan808 on 3/11/17.
 */

import android.os.Message;
import android.renderscript.ScriptGroup;

import java.io.*;
import java.net.*;

public class ClientThread extends Thread{

    String dstAddress;
    int dstPort;
    String dstMsg;
    private boolean running;

    MainActivity.ClientHandler handler;

    Socket socket;
    PrintWriter printWriter;
    BufferedReader bufferedReader;

    public ClientThread(String addr, int port, String msg, MainActivity.ClientHandler handler) {
        super();
        dstAddress = addr;
        dstPort = port;
        dstMsg = msg;
        this.handler = handler;
    }

    public void setRunning(boolean running){
        this.running = running;
    }

    private void sendState(String state){
        handler.sendMessage(
                Message.obtain(handler,
                        MainActivity.ClientHandler.UPDATE_STATE, state));
    }

    public void txMsg(String msgToSend){
        if(printWriter != null){
            printWriter.println(msgToSend);
        }
    }

    @Override
    public void run() {
        sendState("connecting...");
        running = true;

        try {
            sendState("connected");
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName(dstAddress);
            byte[] sendData = new byte[1024];
            byte[] receiveData = new byte[1024];

            while(running){
                sendData = dstMsg.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, dstPort);
                clientSocket.send(sendPacket);

                if(dstMsg != null){
                    handler.sendMessage(Message.obtain(handler, MainActivity.ClientHandler.UPDATE_MSG, dstMsg));
                }

                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                clientSocket.receive(receivePacket);

                String modifiedSentence = new String(receivePacket.getData());
                handler.sendMessage(Message.obtain(handler, MainActivity.ClientHandler.UPDATE_MSG, modifiedSentence));

//              clientSocket.close();
                setRunning(false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        
        handler.sendEmptyMessage(MainActivity.ClientHandler.UPDATE_END);
    }
}
