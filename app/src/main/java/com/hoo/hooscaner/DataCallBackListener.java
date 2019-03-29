package com.hoo.hooscaner;

public interface DataCallBackListener {

    enum SocketState{WAITING,CONNET,DISCONNET};
    void onReceive(String clientString);
    void onState(SocketState state);
}
