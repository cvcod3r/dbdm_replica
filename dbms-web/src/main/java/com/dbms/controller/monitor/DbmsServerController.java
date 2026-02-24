package com.dbms.controller.monitor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.core.AjaxResult;
import com.dbms.domain.Server;
import com.dbms.domain.server.Network;
import com.dbms.entity.ReportMonitorInfoEntity;
import com.dbms.service.ReportMonitorInfoService;
import com.dbms.utils.Arith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

import java.math.BigDecimal;
import java.util.List;


@RestController
@RequestMapping("/monitor")
public class DbmsServerController
{

    @Autowired
    private ReportMonitorInfoService reportMonitorInfoService;

    @GetMapping("/server")
    public AjaxResult getInfo() throws Exception {
        Server server = new Server();
        server.copyTo();
        return AjaxResult.success(server);
    }

    @GetMapping("/server/network")
    public AjaxResult getNetworkInfo() throws Exception{
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        List<NetworkIF> networkIFs = hal.getNetworkIFs();
        long bytesRecv = 0;
        long bytesSent = 0;
        long packetsRecv = 0;
        long packetsSent = 0;
        long timeStamp = 0;
        for (NetworkIF networkIF: networkIFs){
            bytesRecv += networkIF.getBytesRecv();
            bytesSent += networkIF.getBytesSent();
            packetsRecv += networkIF.getPacketsRecv();
            packetsSent += networkIF.getPacketsSent();
//            System.out.println("接收字节数::" + bytesRecv);
//            System.out.println("发送字节数::" + bytesSent);
            timeStamp = networkIF.getTimeStamp();
        }
        long bytesRecv1 = 0;
        long bytesSent1 = 0;
        long packetsRecv1 = 0;
        long packetsSent1 = 0;
        long timeStamp1 = 0;
        Thread.sleep(2000);
        List<NetworkIF> networkIFs2 = hal.getNetworkIFs();
        for (NetworkIF networkIF: networkIFs2){
            bytesRecv1 += networkIF.getBytesRecv();
            bytesSent1 += networkIF.getBytesSent();
            packetsRecv1 += networkIF.getPacketsRecv();
            packetsSent1 += networkIF.getPacketsSent();
//            System.out.println("接收字节数::" + bytesRecv);
//            System.out.println("发送字节数::" + bytesSent);
            timeStamp1 = networkIF.getTimeStamp();
//            Date date1 = new Date(stamp);
//            System.out.println("时间::" + simpleDateFormat.format(date1));
        }
        long bytesRecvDiff = bytesRecv1 - bytesRecv;
        long bytesSentDiff = bytesSent1 - bytesSent;
        long packetsRecvDiff = packetsRecv1 - packetsRecv;
        long packetsSentDiff = packetsSent1 - packetsSent;
        // 计算Mbps
        Network network = new Network();
        network.setBytesRecv(Arith.round(Arith.div(bytesRecvDiff, 1024*2)*8, 2));
        network.setBytesSent(Arith.round(Arith.div(bytesSentDiff, 1024*2)*8, 2));
        network.setPacketsRecv(packetsRecvDiff/2);
        network.setPacketsSent(packetsSentDiff/2);
        network.setTimeStamp(timeStamp1 - timeStamp);
        return AjaxResult.success(network);
    }

    @PostMapping("/report-monitor-info")
    public AjaxResult reportMonitorInfo(@RequestBody ReportMonitorInfoEntity reportMonitorInfo){
        reportMonitorInfo.setInfoId(1);
        if (reportMonitorInfoService.saveOrUpdate(reportMonitorInfo)){
            return new AjaxResult(0, "请求成功");
        }else {
            return new AjaxResult(1, "请求失败");
        }
    }

    @GetMapping("/get-report-monitor-info")
    public AjaxResult getReportMonitorInfo(){
//        QueryWrapper<ReportMonitorInfoEntity> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq(ReportMonitorInfoEntity.INFO_ID, 1);
        return AjaxResult.success(reportMonitorInfoService.getById(1));
    }

}

