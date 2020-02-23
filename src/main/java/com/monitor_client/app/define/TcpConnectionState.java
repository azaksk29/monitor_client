package com.monitor_client.app.define;

public enum TcpConnectionState {
    LISTENING("LISTENING"),
    SYN_SENT("SYN_SENT"),
    SYN_RECEIVED("SYN_RECEIVED"),
    ESTABLISHED("ESTABLISHED"),
    FIN_WAIT_1("FIN_WIAT_1"),
    FIN_WAIT_2("FIN_WIAT_2"),
    CLOSE_WAIT("CLOSE_WAIT"),
    CLOSING("CLOSING"),
    LAST_ACK("LAST_ACK"),
    TIME_WAIT("TIME_WAIT"),
    CLOSED("CLOSED");

    private String state;
    
    private TcpConnectionState(String state) { 
        this.state = state; 
    }

    public String getState() {
        return state;
    }
}