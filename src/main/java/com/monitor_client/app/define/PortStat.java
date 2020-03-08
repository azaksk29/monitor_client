package com.monitor_client.app.define;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class PortStat {

    private int port;

    public TcpConnectionState state;

    public LocalDateTime date;
}