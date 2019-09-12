package com.hoo.hooscaner;

import android.util.Log;

import com.hoo.hooscaner.modle.SocketErrorBean;

import org.greenrobot.eventbus.EventBus;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.text.DecimalFormat;

public class HooConnetServer extends WebSocketServer {

    private DataCallBackListener dataListener;

    public DataCallBackListener getDataListener() {
        return dataListener;
    }

    private WebSocket con;

    public void setDataListener(DataCallBackListener listener) {
        this.dataListener = listener;
    }

    public HooConnetServer(int port) {
        super(new InetSocketAddress(port));
    }

    public HooConnetServer(InetSocketAddress address) {
        super(address);
    }

    DecimalFormat df = new DecimalFormat("000000");

    public void send(String msg) {
        if (con != null) {
            con.send(df.format(msg.length()) + "|" + msg);
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        this.con = conn;
        if (dataListener != null) {
            dataListener.onState(DataCallBackListener.SocketState.CONNET);
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        this.con = null;
        if (dataListener != null) {
            dataListener.onState(DataCallBackListener.SocketState.DISCONNET);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        if (dataListener != null) {
            dataListener.onReceive(message);
        }
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        if (dataListener != null) {
            dataListener.onReceive(getString(message));
        }
    }


    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        Log.e("NONO","NONONONONO");
        if (conn != null) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
        EventBus.getDefault().post(new SocketErrorBean());
    }

    @Override
    public void onStart() {
        if (dataListener != null) {
            dataListener.onState(DataCallBackListener.SocketState.WAITING);
        }
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

    /**
     * ByteBuffer 转换 String
     *
     * @param buffer
     * @return
     */
    public String getString(ByteBuffer buffer) {
        Charset charset = null;
        CharsetDecoder decoder = null;
        CharBuffer charBuffer = null;
        try {
            charset = Charset.forName("UTF-8");
            decoder = charset.newDecoder();
            charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
            return charBuffer.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }


}