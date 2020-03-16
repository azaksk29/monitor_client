package com.monitor_client.app.capture;

import com.monitor_client.app.netty_client.NettyClient;
import com.monitor_client.app.utils.ByteUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.extern.slf4j.Slf4j;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.packet.format.FormatUtils;
import org.jnetpcap.protocol.lan.Ethernet;
import org.jnetpcap.protocol.tcpip.Tcp;

import java.nio.charset.StandardCharsets;

@Slf4j
public class CaptureHandler implements PcapPacketHandler<String> {


    NettyClient client;

    static final byte[] carraige = { (byte)0x0D, (byte)0x0A};

    CaptureHandler(NettyClient client) {
        this.client = client;
    }

    @Override
    public void nextPacket(PcapPacket packet, String user) {

        final Tcp tcp = new Tcp();
        if (false == packet.hasHeader(tcp))
            return; // Not IP packet

        if (0 == tcp.getPayloadLength())
            return;

        packet.getHeader(tcp);

        Ethernet eth = new Ethernet();
        if (packet.hasHeader(eth)) {
            System.out.println(
                    "SRC MAC : " + ByteUtils.toHexString(eth.source())+ "," +FormatUtils.mac(eth.source()) + "," +
                            " DST MAC : " + FormatUtils.mac(eth.destination()) +
                            " ( " + tcp.getPayloadLength() + " ) ");
            }

//


        //final Ip4 ip = new Ip4();
        //packet.getHeader(ip);
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
        // TODO : Unpooled ??
        //ByteBuf bbuf = Unpooled.buffer(payload.length + 1);



        String mac = ByteUtils.toHexString(eth.source());
        byte[] macBytes = mac.getBytes(StandardCharsets.US_ASCII);
        ByteBuf bbuf = PooledByteBufAllocator.DEFAULT.buffer(2+macBytes.length+2);
        bbuf.writeByte((byte)0x77); // start byte
        bbuf.writeByte((byte)(macBytes.length+2));
        bbuf.writeBytes(macBytes);
        bbuf.writeBytes(carraige);
        client.send(bbuf);

        bbuf = PooledByteBufAllocator.DEFAULT.buffer(payload.length);
        //bbuf.writeByte(0x77); // start byte
        //bbuf.writeInt(payload.length);
        bbuf.writeBytes(payload);
        //NettyClient.send(NettyClient.channelMap.get(tcp.destination()), bbuf);
        client.send(bbuf);

//        byte[] bytes = new byte[bbuf.readableBytes()];
//        bbuf.getBytes(0, bytes);
//        System.out.println("capture ..... " + ByteUtils.toHexString(  bytes  ) );
//        bbuf.release();

        //final String s = new String(payload);

        // final Payload hexPayload = new Payload();
        // packet.hasHeader(hexPayload);
        // System.out.print(hexPayload.toHexdump());

    }
}