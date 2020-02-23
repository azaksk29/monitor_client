package com.monitor_client.app.config;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.springframework.stereotype.Component;

@Component
public class Config {
    private ArrayList<Integer> portList;

    public Config() {
        this.portList = new ArrayList<Integer>();
    }

    public ArrayList<Integer> getPortList() {
        return this.portList;
    }

    public void loadConfigFromFile(String filePath) {
        FileInputStream inputStream = null;

        try {
            inputStream = new FileInputStream(filePath);
            try (BufferedReader output = new BufferedReader(new InputStreamReader(inputStream))) {
                output.lines().forEach((line) -> {
                    if (line.contains("-")) {
                        String[] strPort = line.split("-");
                        int first = Integer.parseInt(strPort[0]);
                        int last = Integer.parseInt(strPort[1]);
                        for (int i = 0; i < last - first; i++)
                            portList.add(first + i);
                    } else if (line.isEmpty()) {
                    } else
                        portList.add(Integer.parseInt(line));
                });
            }
        } catch (Exception e) {
            System.out.println("Error File I/O !!" + e);
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                System.out.println("Can't close file !!" + e);
            }
        }
    }
}
