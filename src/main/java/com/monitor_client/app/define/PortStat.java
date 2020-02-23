package com.monitor_client.app.define;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PortStat {

    private int port;

    public TcpConnectionState state;

}