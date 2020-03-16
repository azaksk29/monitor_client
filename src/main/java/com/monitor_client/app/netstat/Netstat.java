package com.monitor_client.app.netstat;

import com.monitor_client.app.define.ClientDataType;
import com.monitor_client.app.define.PortStat;
import com.monitor_client.app.define.TcpConnectionState;
import com.monitor_client.app.netty_client.NettyClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class Netstat {

    private Process netstatProcess;

    @Setter
    private NettyClient client;

    public void run(List<Integer> filterPortList, int interval) {
        Runnable runnable = new Runnable() {
            public void run() {
                List<PortStat> portStatList = parse( doNetstatCommand(), filterPortList );
                waitFor();
                /* Make Json format data */
                ByteBuf bbuf = encode(portStatList);
                /* send to server */
                if(client != null)
                    //client.send(bbuf);
                    bbuf.release();
            }
        };

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, 0, interval, TimeUnit.SECONDS);
    }

    public int doConnect() {
        if(client == null) {
            log.error("Client NOT allocated ... ");
            return -1;
        }
        client.doConnect();
        return 0;
    }

    public ByteBuf encode( List<PortStat> portStatList ) {
        /* Make Json data */
        JSONObject jsonObject = new JSONObject();
        JSONArray portArray = new JSONArray();
        portStatList.forEach((portStat) -> {
            HashMap<String, Object> portMap = new HashMap<>();
            portMap.put("number", portStat.getPort());
            portMap.put("state", portStat.getState() != null ? portStat.getState().getState() : "UNKNOWN");
            JSONObject portInfo = new JSONObject(portMap);
            portArray.add(portInfo);
            jsonObject.put("ports", portArray);
        });
        String jsonInfo = jsonObject.toJSONString();

        //ByteBuf bbuf = Unpooled.buffer(jsonInfo.length() + 5);
        ByteBuf bbuf = PooledByteBufAllocator.DEFAULT.buffer(jsonInfo.length() + 5);
        bbuf.writeByte(0x99); // start byte
        bbuf.writeInt(jsonInfo.length());
        bbuf.writeBytes(jsonInfo.getBytes());

        return bbuf;
    }

    public List<PortStat> parse(BufferedReader in, List<Integer> portList) {

        List<PortStat> portStateList = new ArrayList<PortStat>();

        portList.forEach((port) -> {
            PortStat portStat = new PortStat();
            portStat.setPort(port);
            portStateList.add(portStat);
        });

        /* Make port state list */
        in.lines()
                .filter(line -> line.contains("TCP"))
                // get "Port number : Port state"
                .map(line -> line.split("\\s+")[2].split(":")[1] /* port */
                        + ":" + line.split("\\s+")[4] /* state */)
                // filter desire port info
                .filter(line -> portList.indexOf(Integer.parseInt(line.split(":")[0])) != -1)
                // set port state to list
                .forEach(line -> {
                    String[] info = line.split(":");
                    // TODO : Need to handle multiple same port ...
                    PortStat ps = portStateList.get(portList.indexOf(Integer.parseInt(info[0])));
                    ps.setState(TcpConnectionState.valueOf(info[1]));
                });

        return portStateList;
    }


    public BufferedReader doNetstatCommand() {

        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "netstat.exe", "-an", "-p", "tcp");
        try {
            netstatProcess = builder.start();
        } catch (IOException e) {
            System.err.println("Can't start netstat process !" + e);
            e.printStackTrace();
        }

        BufferedReader output = null;
        output = new BufferedReader(new InputStreamReader(netstatProcess.getInputStream()));

        return output;
    }

    public void waitFor() {
        if (netstatProcess == null) {
            System.out.println("Error : Not allocated netstat process");
            return;
        }

        try {
            if (netstatProcess.waitFor() != 0) {
                throw new IOException("netstat command failed");
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}