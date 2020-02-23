package com.monitor_client.app.netstat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.monitor_client.app.netty_client.NettyClient;
import com.monitor_client.app.define.PortStat;
import com.monitor_client.app.define.TcpConnectionState;
import com.monitor_client.app.utils.Convert;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

@Component
public class Netstat {

    private Process netstatProcess;

    private BufferedReader output;

    static int count = 0;

    public void runNetstatTask(ArrayList<Integer> portList, NettyClient client, int interval) {

        Runnable runnable = new Runnable() {
            public void run() {
                ArrayList<PortStat> portStateList = new ArrayList<PortStat>();
                portStateList = getPortStatus(portList);

                /* Make Json data */
                JSONObject jsonObject = new JSONObject();
                JSONArray portArray = new JSONArray();
                portStateList.forEach((portStat) -> {
                    HashMap<String, Object> hashmap = new HashMap<String, Object>();
                    hashmap.put("number", portStat.getPort());
                    hashmap.put("state", portStat.getState() != null ? portStat.getState().getState() : "UNKNOWN");
                    JSONObject portInfo = new JSONObject(hashmap);
                    portArray.add(portInfo);
                    jsonObject.put("ports", portArray);
                });
                String jsonInfo = jsonObject.toJSONString();

                ByteBuf bbuf = Unpooled.buffer(jsonInfo.length() + 5);
                bbuf.writeByte(0x99); // start byte
                bbuf.writeInt(jsonInfo.length());
                bbuf.writeBytes(jsonInfo.getBytes());
                client.sendByteBuf(bbuf);
            }
        };

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, 0, interval, TimeUnit.SECONDS);
    }

    public ArrayList<PortStat> getPortStatus(ArrayList<Integer> portList) {

        ArrayList<PortStat> portStateList = new ArrayList<PortStat>();
        portList.forEach((port) -> {
            PortStat portStat = new PortStat();
            portStat.setPort(port);
            portStateList.add(portStat);
        });

        BufferedReader output = getNetstatOutput();

        /* Make port state list */
        output.lines()
                .filter(line -> line.contains("TCP"))
                .map(line -> line.split("\\s+")[2].split(":")[1] /* port */
                              + ":" + line.split("\\s+")[4] /* state */) // get Port number : Port state
                .filter(line -> portList.indexOf(Integer.parseInt(line.split(":")[0])) != -1) // filter desire port info
                .forEach(line -> {
                    String[] info = line.split(":");
                    portStateList.get(portList.indexOf(Integer.parseInt(info[0])))
                            .setState(TcpConnectionState.valueOf(info[1]));
                });

        waitFor();

        return portStateList;
    }

    public BufferedReader getNetstatOutput() {

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