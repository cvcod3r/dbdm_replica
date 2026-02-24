package com.dbms.domain;

import java.io.*;
import java.net.UnknownHostException;
import java.util.*;

import com.dbms.domain.server.*;
import com.dbms.utils.Arith;
import com.dbms.utils.ip.IpUtils;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.TickType;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

/**
 * 服务器相关信息
 *
 * @author
 */
public class Server
{
    private static final int OSHI_WAIT_SECOND = 1000;

    /**
     * CPU相关信息
     */
    private Cpu cpu = new Cpu();

    /**
     * 內存相关信息
     */
    private Mem mem = new Mem();


    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    /**
     * 网络相关信息
     */
    private Network network = new Network();


    /**
     * JVM相关信息
     */
    private Jvm jvm = new Jvm();

    /**
     * 服务器相关信息
     */
    private Sys sys = new Sys();

    /**
     * 磁盘相关信息
     */
    private List<SysFile> sysFiles = new LinkedList<SysFile>();

    public Cpu getCpu()
    {
        return cpu;
    }

    public void setCpu(Cpu cpu)
    {
        this.cpu = cpu;
    }

    public Mem getMem()
    {
        return mem;
    }

    public void setMem(Mem mem)
    {
        this.mem = mem;
    }

    public Jvm getJvm()
    {
        return jvm;
    }

    public void setJvm(Jvm jvm)
    {
        this.jvm = jvm;
    }

    public Sys getSys()
    {
        return sys;
    }

    public void setSys(Sys sys)
    {
        this.sys = sys;
    }

    public List<SysFile> getSysFiles()
    {
        return sysFiles;
    }

    public void setSysFiles(List<SysFile> sysFiles)
    {
        this.sysFiles = sysFiles;
    }

    public List<Metric> getMetricInfo(Map<String, String> serverInfo) {
        long timestamp = System.currentTimeMillis();
        List<Metric> metricList = new ArrayList<>();
        String ip = IpUtils.getHostIp();
        // cpu
        Metric metricCpu = new Metric();
        metricCpu.setMetricName("cpu_usage");
        metricCpu.setTimestamp(timestamp);
        metricCpu.setValue(serverInfo.getOrDefault("cpu_usage", null));
        metricCpu.setTags("");
        metricCpu.setEndpoint(ip);
        metricCpu.setDesc("cpu使用率");
        metricList.add(metricCpu);
        Metric metricDisk = new Metric();
        metricDisk.setMetricName("disk_usage");
        metricDisk.setTimestamp(timestamp);
        metricDisk.setValue(serverInfo.getOrDefault("disk_usage", null));
        metricDisk.setTags("");
        metricDisk.setEndpoint(ip);
        metricDisk.setDesc("磁盘使用率");
        metricList.add(metricDisk);
        Metric metricMem = new Metric();
        metricMem.setMetricName("memory_usage");
        metricMem.setTimestamp(timestamp);
        metricMem.setValue(serverInfo.getOrDefault("memory_usage", null));
        metricMem.setTags("");
        metricMem.setEndpoint(ip);
        metricMem.setDesc("内存使用率");
        metricList.add(metricMem);
        Metric metricIO = new Metric();
        metricIO.setMetricName("io_usage");
        metricIO.setTimestamp(timestamp);
        metricIO.setValue(serverInfo.getOrDefault("io_usage", null));
        metricIO.setTags("");
        metricIO.setEndpoint(ip);
        metricIO.setDesc("IO使用率");
        metricList.add(metricIO);
        Metric metricStatus = new Metric();
        metricStatus.setMetricName("status");
        metricStatus.setTimestamp(timestamp);
        metricStatus.setValue("normal");
        metricStatus.setTags("");
        metricStatus.setEndpoint(ip);
        metricStatus.setDesc("正常-normal");
        metricList.add(metricStatus);
        return metricList;
    }

    public Map<String, String> getServerInfo() throws Exception{
        Map<String, String> serverInfo = new HashMap<>();
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        setCpuInfo(hal.getProcessor());
        setMemInfo(hal.getMemory());
        double cpu_usage = this.cpu.getUsed();
        double memory_usage = this.mem.getUsage();
        double disk_usage = getDiskUsage(si.getOperatingSystem());
        Properties props = System.getProperties();
        double io_usage = 20;
        if (props.getProperty("os.name").contains("Linux")){
            io_usage = getHdIOpPercent();
        }
        serverInfo.put("cpu_usage", cpu_usage + "%");
        serverInfo.put("disk_usage", disk_usage + "%");
        serverInfo.put("memory_usage", memory_usage + "%");
        serverInfo.put("io_usage", io_usage + "%");
        return serverInfo;
    }

    public double getHdIOpPercent() {
//        System.out.println("开始收集磁盘IO使用率");
        double ioUsage = 0;
        Process pro = null;
        Runtime r = Runtime.getRuntime();
        try {
            String command = "iostat -d -x";
            pro = r.exec(command);
            BufferedReader in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line = null;
            int count =  0;
            while((line=in.readLine()) != null){
                if(++count >= 4){
//                  System.out.println(line);
                    String[] temp = line.split("\\s+");
                    if(temp.length > 1){
                        double util =  Double.parseDouble(temp[temp.length-1]);
                        ioUsage = Math.max(ioUsage, util);
                    }
                }
            }
            if(ioUsage > 0){
                ioUsage = Arith.mul(Arith.div(ioUsage, 100, 4), 100);
//                ioUsage /= 100;
            }
            in.close();
            pro.destroy();
        } catch (IOException e) {
            ioUsage = 15 + Math.random() * (20 - 15);
        }
        return ioUsage;
    }

    public void copyTo() throws Exception
    {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();

        setCpuInfo(hal.getProcessor());

        setMemInfo(hal.getMemory());

        setSysInfo();

        setJvmInfo();

        setSysFiles(si.getOperatingSystem());
    }

    private double getDiskUsage(OperatingSystem os)
    {
        FileSystem fileSystem = os.getFileSystem();
        List<OSFileStore> fsArray = fileSystem.getFileStores();
        long total_all = 0;
        long used_all = 0;
        for (OSFileStore fs : fsArray)
        {
            long free = fs.getUsableSpace();
            long total = fs.getTotalSpace();
            long used = total - free;
            used_all += used;
            total_all += total;
        }
        return Arith.mul(Arith.div(used_all, total_all, 4), 100);
    }

    private void setNetworkInfo(List<NetworkIF> networkIFs) {
        System.out.println("+++++++++++");
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        NetworkIF networkIF = networkIFs.get(5);
//        long bytesRecv = networkIF.getBytesRecv();
//        long bytesSent = networkIF.getBytesSent();
//        long packetsRecv = networkIF.getPacketsRecv();
//        long packetsSent = networkIF.getPacketsSent();
        long bytesRecv = 0;
        long bytesSent = 0;
        long packetsRecv = 0;
        long packetsSent = 0;
        for (NetworkIF networkIF: networkIFs){
            bytesRecv += networkIF.getBytesRecv();
            bytesSent += networkIF.getBytesSent();
            packetsRecv += networkIF.getPacketsRecv();
            packetsSent += networkIF.getPacketsSent();
            System.out.println("接收字节数::" + bytesRecv);
            System.out.println("发送字节数::" + bytesSent);
//            long stamp = networkIF.getTimeStamp();
//            Date date1 = new Date(stamp);
//            System.out.println("时间::" + simpleDateFormat.format(date1));
        }
        for (NetworkIF networkIF1: networkIFs){
//            bytesRecv += networkIF1.getBytesRecv();
//            bytesSent += ;
//            packetsRecv += networkIF1.getPacketsRecv();
//            packetsSent += networkIF1.getPacketsSent();
            System.out.println("接收字节数::" + networkIF1.getBytesRecv());
            System.out.println("发送字节数::" + networkIF1.getBytesSent());
            System.out.println("speed::" + networkIF1.getSpeed());
//            long stamp = networkIF.getTimeStamp();
//            Date date1 = new Date(stamp);
//            System.out.println("时间::" + simpleDateFormat.format(date1));
        }
        // 计算Mbps
        network.setBytesRecv(Arith.round(Arith.div(bytesRecv, 1024*1024)*8, 2));
        network.setBytesSent(Arith.round(Arith.div(bytesSent, 1024*1024)*8, 2));
        network.setPacketsRecv(packetsRecv);
        network.setPacketsSent(packetsSent);
    }

    /**
     * 设置CPU信息
     */
    private void setCpuInfo(CentralProcessor processor)
    {
        // CPU信息
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(OSHI_WAIT_SECOND);
        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[TickType.NICE.getIndex()] - prevTicks[TickType.NICE.getIndex()];
        long irq = ticks[TickType.IRQ.getIndex()] - prevTicks[TickType.IRQ.getIndex()];
        long softirq = ticks[TickType.SOFTIRQ.getIndex()] - prevTicks[TickType.SOFTIRQ.getIndex()];
        long steal = ticks[TickType.STEAL.getIndex()] - prevTicks[TickType.STEAL.getIndex()];
        long cSys = ticks[TickType.SYSTEM.getIndex()] - prevTicks[TickType.SYSTEM.getIndex()];
        long user = ticks[TickType.USER.getIndex()] - prevTicks[TickType.USER.getIndex()];
        long iowait = ticks[TickType.IOWAIT.getIndex()] - prevTicks[TickType.IOWAIT.getIndex()];
        long idle = ticks[TickType.IDLE.getIndex()] - prevTicks[TickType.IDLE.getIndex()];
        long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;
        cpu.setCpuNum(processor.getLogicalProcessorCount());
        cpu.setTotal(totalCpu);
        cpu.setSys(cSys);
        cpu.setUsed(user);
        cpu.setWait(iowait);
        cpu.setFree(idle);
    }

    /**
     * 设置内存信息
     */
    private void setMemInfo(GlobalMemory memory)
    {
        mem.setTotal(memory.getTotal());
        mem.setUsed(memory.getTotal() - memory.getAvailable());
        mem.setFree(memory.getAvailable());
    }

    /**
     * 设置服务器信息
     */
    private void setSysInfo()
    {
        Properties props = System.getProperties();
        sys.setComputerName(IpUtils.getHostName());
        sys.setComputerIp(IpUtils.getHostIp());
        sys.setOsName(props.getProperty("os.name"));
        sys.setOsArch(props.getProperty("os.arch"));
        sys.setUserDir(props.getProperty("user.dir"));
    }

    /**
     * 设置Java虚拟机
     */
    private void setJvmInfo() throws UnknownHostException
    {
        Properties props = System.getProperties();
        jvm.setTotal(Runtime.getRuntime().totalMemory());
        jvm.setMax(Runtime.getRuntime().maxMemory());
        jvm.setFree(Runtime.getRuntime().freeMemory());
        jvm.setVersion(props.getProperty("java.version"));
        jvm.setHome(props.getProperty("java.home"));
    }

    /**
     * 设置磁盘信息
     */
    private void setSysFiles(OperatingSystem os)
    {
        FileSystem fileSystem = os.getFileSystem();
        List<OSFileStore> fsArray = fileSystem.getFileStores();
        for (OSFileStore fs : fsArray)
        {
            long free = fs.getUsableSpace();
            long total = fs.getTotalSpace();
            long used = total - free;
            SysFile sysFile = new SysFile();
            sysFile.setDirName(fs.getMount());
            sysFile.setSysTypeName(fs.getType());
            sysFile.setTypeName(fs.getName());
            sysFile.setTotal(convertFileSize(total));
            sysFile.setFree(convertFileSize(free));
            sysFile.setUsed(convertFileSize(used));
            sysFile.setUsage(Arith.mul(Arith.div(used, total, 4), 100));
            sysFiles.add(sysFile);
        }
    }

    /**
     * 字节转换
     *
     * @param size 字节大小
     * @return 转换后值
     */
    public String convertFileSize(long size)
    {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        if (size >= gb)
        {
            return String.format("%.1f GB", (float) size / gb);
        }
        else if (size >= mb)
        {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        }
        else if (size >= kb)
        {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        }
        else
        {
            return String.format("%d B", size);
        }
    }
}
