package com.monitor_client.app;

import java.io.File;
import java.util.List;

import com.monitor_client.app.capture.Capture;
import com.monitor_client.app.config.Config;
import com.monitor_client.app.netstat.Netstat;
import com.monitor_client.app.define.ClientDataType;
import com.monitor_client.app.netty_client.NettyClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class AppApplication implements CommandLineRunner {

    @Autowired
    private Config config;

//    @Autowired
//    private Capture capture;

//    @Autowired
//    private Netstat netstat;

    @Value("${dst.host}")
    String host;

    @Value("${dst.port}")
    int port;

    @Value("${iface.id}")
    int ifaceId;

    @Value("${config.file.path}")
    String configFilePath;

    static {
        try {
            System.load(new File("lib/jnetpcap.dll").getAbsolutePath());
            System.out.println(new File("lib/jnetpcap.dll").getAbsolutePath());
        } catch (final UnsatisfiedLinkError e) {
            System.out.println("Native code library failed to load.\n" + e);
            System.exit(1);
        }
    }

    public static void main(final String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }

    @Override
    public void run(String... args) {
        /* Run App indicator */
        System.out.println("Hello Java");

        /* Loading port info from File */
        config.loadConfigFromFile(configFilePath);
        List<Integer> portList = config.getPortList();
        if (portList.isEmpty()) {
            System.err.print("Error : Can't find port list !!!\n");
            return;
        }

        int server_port = port;
        /* Run netstat task to provide port stat data */
        Netstat netstat = new Netstat();
        netstat.setClient( new NettyClient(ClientDataType.DATA_TYPE_PORT_STAT, host, server_port) );
        netstat.doConnect();
        netstat.run(portList, 2);

        /* Run pcap task to provide packet data */
        Capture capture = new Capture();
        /* To emulate device, need to split client by using port value */
        for(Integer srcPort:portList) {
            capture.addClient(new NettyClient(ClientDataType.DATA_TYPE_CAPTURED_PACKET, host, server_port, srcPort));
            capture.doConnect(srcPort);
        }
        capture.findAllDevs();
        capture.prepareIface(ifaceId, capture.createCommand(portList));
        capture.run();
    }
}
