package com.monitor_client.app.capture;

import com.monitor_client.app.define.ClientDataType;
import com.monitor_client.app.netty_client.NettyClient;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapBpfProgram;
import org.jnetpcap.PcapIf;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class Capture {

    private static List<PcapIf> alldevs;

    //private List<NettyClient> clientList = new ArrayList<NettyClient>();

    private Pcap pcap;

//    public void addClient( NettyClient client ) {
//        this.clientList.add(client);
//    }

    @Setter
    private NettyClient client;


    public int doConnect() {
        if(client == null) {
            log.error("Client NOT allocated ... ");
            return -1;
        }

        client.doConnect();

        return 0;
    }

    public void run() {
        Runnable runnable = new Runnable() {
            public void run() {
                if (pcap == null) {
                    System.err.printf("Error while run device for capture");
                    return;
                }
                pcap.loop(-1/* unlimit loop */, new CaptureHandler(client), "jNetPcap rocks!");
                pcap.close();
            }
        };

        Thread thread = new Thread(runnable, "capture");
        thread.start();
    }

//    public int doConnect(int srcPort) {
//        if(clientList.isEmpty()) {
//            log.error("Client NOT allocated ... ");
//            return -1;
//        }
//
//        for(NettyClient client:clientList) {
//            if(client.getSrcPort() == srcPort) {
//                client.doConnect();
//                return 0;
//            }
//        };
//
//        log.error("Can't found port {} client ... ",srcPort);
//        return -1;
//    }

    public int findAllDevs() {
        StringBuilder errbuf = new StringBuilder();
        alldevs = new ArrayList<PcapIf>();
        int ret = Pcap.findAllDevs(alldevs, errbuf);
        if (ret < 0 || alldevs.isEmpty()) {
            System.err.printf("Can't read list of devices, error is %s", errbuf.toString());
            return -1;
        }
        System.out.println("Network devices found:");

        int i = 0;
        for (PcapIf device : alldevs) {
            String description = (device.getDescription() != null) ? device.getDescription()
                    : "No description available";
            System.out.printf("#%d: %s [%s]\n", i++, device.getName(), description);
        }
        return 0;
    }

    public Pcap prepareIface(int id, String expression) {
        if (alldevs.isEmpty())
            findAllDevs();

        if (id >= alldevs.size()) {
            System.err.println("Id is invalid");
            return null;
        }

        /* Get iface */
        PcapIf device = alldevs.get(id);
        System.out.printf("\nChoosing '%s' on your behalf:\n",
                (device.getDescription() != null) ? device.getDescription() : device.getName());

        /* Configuration */
        StringBuilder errbuf = new StringBuilder();
        int snaplen = 64 * 1024;
        int flags = Pcap.MODE_PROMISCUOUS;
        int timeout = 10 * 1000;
        PcapBpfProgram filter = new PcapBpfProgram();

        /* Open */
        Pcap pcap = Pcap.openLive(device.getName(), snaplen, flags, timeout, errbuf);
        if (pcap == null) {
            System.err.printf("Error while opening device for capture: " + errbuf.toString());
            return null;
        }

        /* Compile command */
        int optimize = 0; // 0 = false
        int netmask = 0xFFFFFF00; // 255.255.255.0
        if (pcap.compile(filter, expression, optimize, netmask) != Pcap.OK) {
            System.err.println(pcap.getErr());
            return null;
        }

        /* Set Filter */
        if (pcap.setFilter(filter) != Pcap.OK) {
            System.err.println(pcap.getErr());
            return null;
        }

        this.pcap = pcap;

        return pcap;
    }

    public String createCommand(List<Integer> portList) {
        StringBuilder cmd = new StringBuilder();
        cmd.append("tcp");
//        for (int i = 0; i < portList.size(); i++) {
//            if (i > 0)
//                cmd.append("or ");
//            cmd.append("tcp dst port ");
//            cmd.append(portList.get(i));
//            cmd.append(" ");
//        }
        log.info("{}",cmd.toString());

        return cmd.toString();
    }
}