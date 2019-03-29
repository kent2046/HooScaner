package com.hoo.hooscaner;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Scanner;

public class testPcClient {
    /**
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        try {
            Runtime.getRuntime().exec(
                    "adb shell am broadcast -a NotifyServiceStop");
            Thread.sleep(3000);
            Runtime.getRuntime().exec("adb forward tcp:10086 tcp:20000");
            Thread.sleep(3000);
            Runtime.getRuntime().exec(
                    "adb shell am broadcast -a NotifyServiceStart");
            Thread.sleep(3000);
        } catch (IOException e3) {
            e3.printStackTrace();
        }
        lo();
        Scanner sc = new Scanner(System.in);
        //利用hasNextXXX()判断是否还有下一输入项
        while (sc.hasNext()) {
            //利用nextXXX()方法输出内容
            String str = sc.next();
            cc.send(str);
        }
    }


    static WebSocketClient cc;

    public static void lo() {

        try {
            cc = new WebSocketClient(new URI("ws://172.17.100.93:9999/upper")) {

                @Override
                public void onMessage(String message) {
                    System.out.println("got: " + message + "\n");
                }

                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("You are connected to HooConnetServer: " + getURI() + "\n");
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("You have been disconnected from: " + getURI() + "; Code: " + code + " " + reason + "\n");
                }

                @Override
                public void onError(Exception ex) {

                }
            };

            cc.connect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
