package com.dbms.domain.server;

import java.util.Date;

public class Net {

    //@ApiModelProperty("当前时间")
    private Date currentTime;

    //("当前接收包裹数")
    private Long currentRxPackets;
    //("当前发送包裹数")
    private Long currentTxPackets;
    //("当前接收字节数")
    private Long currentRxBytes;
    //("当前发送字节数")
    private Long currentTxBytes;

    //("接收总包裹数")
    private Long rxPackets;
    //("发送总包裹数")
    private Long txPackets;
    //("接收总字节数")
    private Long rxBytes;

    //("发送总字节数")
    private Long txBytes;

    public Date getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
    }

    public Long getCurrentRxPackets() {
        return currentRxPackets;
    }

    public void setCurrentRxPackets(Long currentRxPackets) {
        this.currentRxPackets = currentRxPackets;
    }

    public Long getCurrentTxPackets() {
        return currentTxPackets;
    }

    public void setCurrentTxPackets(Long currentTxPackets) {
        this.currentTxPackets = currentTxPackets;
    }

    public Long getCurrentRxBytes() {
        return currentRxBytes;
    }

    public void setCurrentRxBytes(Long currentRxBytes) {
        this.currentRxBytes = currentRxBytes;
    }

    public Long getCurrentTxBytes() {
        return currentTxBytes;
    }

    public void setCurrentTxBytes(Long currentTxBytes) {
        this.currentTxBytes = currentTxBytes;
    }

    public Long getRxPackets() {
        return rxPackets;
    }

    public void setRxPackets(Long rxPackets) {
        this.rxPackets = rxPackets;
    }

    public Long getTxPackets() {
        return txPackets;
    }

    public void setTxPackets(Long txPackets) {
        this.txPackets = txPackets;
    }

    public Long getRxBytes() {
        return rxBytes;
    }

    public void setRxBytes(Long rxBytes) {
        this.rxBytes = rxBytes;
    }

    public Long getTxBytes() {
        return txBytes;
    }

    public void setTxBytes(Long txBytes) {
        this.txBytes = txBytes;
    }



}
