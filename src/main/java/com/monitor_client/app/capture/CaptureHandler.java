package com.monitor_client.app.capture;

import java.util.Date;

import com.monitor_client.app.netty_client.NettyClient;

import org.jnetpcap.packet.Payload;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.packet.format.FormatUtils;
import org.jnetpcap.protocol.lan.Ethernet;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.tcpip.Tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class CaptureHandler implements PcapPacketHandler<String> {

    private NettyClient client;

    public CaptureHandler(NettyClient client) {
        this.client = client;
    }

    @Override
    public void nextPacket(PcapPacket packet, String user) {

        final Tcp tcp = new Tcp();
        if (packet.hasHeader(tcp) == false)
            return; // Not IP packet

        if (0 == tcp.getPayloadLength())
            return;

        packet.getHeader(tcp);

        final Ethernet eth = new Ethernet();
        if (packet.hasHeader(eth)) {
            System.out.println(
                    "SRC MAC : " + FormatUtils.mac(eth.source()) + "\nDST MAC : " + FormatUtils.mac(eth.destination()));
        }

        // final Ip4 ip = new Ip4();
        // packet.getHeader(ip);
        // final String sourceIP = FormatUtils.ip(ip.source());
        // final String destinationIP = FormatUtils.ip(ip.destination());
        // System.out.print(" source ip = " + sourceIP);
        // System.out.print(" destination ip = " + destinationIP);
        // System.out.println(" src port = " + tcp.source() + ", dst port = " +
        // tcp.destination());
        // System.out.print(" destination ip = " + destinationIP + "\n");

        // final int captureLength = packet.getCaptureHeader().caplen();
        // System.out.printf("%s, length:%d\n", new
        // Date(packet.getCaptureHeader().timestampInMillis()), captureLength);

        final byte[] payload = tcp.getPayload();
        ByteBuf bbuf = Unpooled.buffer(payload.length + 1);
        bbuf.writeByte(0x77); // start byte
        bbuf.writeInt(payload.length); 
        bbuf.writeBytes(payload);
        client.sendByteBuf(bbuf);

        // System.out.println(payload.toString() + "(" + payload.length + ")");

        final String s = new String(payload);
        System.out.println(s);

        // final Payload hexPayload = new Payload();
        // packet.hasHeader(hexPayload);
        // System.out.print(hexPayload.toHexdump());

    }
}