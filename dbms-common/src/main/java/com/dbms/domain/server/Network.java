package com.dbms.domain.server;

import lombok.Data;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

@Data
public class Network {

//    networkIF.getBytesRecv();
//            networkIF.getBytesSent();
//            networkIF.getPacketsRecv();
//            networkIF.getPacketsSent();
    private double bytesRecv;

    private double bytesSent;


    private long packetsRecv;

    private long PacketsSent;

    private long timeStamp;

//    private void copyTo(){
//
//
//    }

}
