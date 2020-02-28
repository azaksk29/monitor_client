package com.monitor_client.app;

import java.io.File;
import java.util.ArrayList;

import com.monitor_client.app.capture.Capture;
import com.monitor_client.app.config.Config;
import com.monitor_client.app.netstat.Netstat;
import com.monitor_client.app.netty_client.NettyClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppApplication implements CommandLineRunner {

    @Autowired
    private Config config;

    @Autowired
    private Capture capture;

    @Autowired
    private Netstat netstat;

    @Value("${dst.host}")
    String host;

    @Value("${dst.port}")
    int port;

    @Value("${iface.id}")
    int ifaceId;

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
        config.loadConfigFromFile("src/main/java/com/monitor_client/app/config/config.txt");
        ArrayList<Integer> portList = config.getPortList();
        if (portList.isEmpty()) {
            System.err.print("Error : Can't find port list !!!\n");
            return;
        }

        NettyClient netstatClient = new NettyClient(host, port);
        netstatClient.start();
        // netstat.getPortStatus(portList);
        netstat.runNetstatTask(portList, netstatClient, 2);

        /* Run pcap task to provide data */
        NettyClient captureClient = new NettyClient(host, port);
        captureClient.start();
        capture = new Capture();
        capture.findAllDevs();

        /*
         * Make filter command : expression
         * "tcp dst port[num] and tcp dst port [num] tcp dst port [num] ....."
         */
        StringBuilder cmd = new StringBuilder();
        for (int i = 0; i < portList.size(); i++) {
            if (i > 0)
                cmd.append("or ");
            cmd.append("tcp dst port ");
            cmd.append(portList.get(i));
            cmd.append(" ");
        }
        System.out.println(cmd.toString());

        capture.prepareIface(ifaceId, cmd.toString());
        capture.runCaptureTask(netstatClient);
    }
}
