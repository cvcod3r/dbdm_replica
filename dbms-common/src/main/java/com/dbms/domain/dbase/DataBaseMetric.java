package com.dbms.domain.dbase;


import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DataBaseMetric {

    /**
     * cpu利用率
     */
    private String cpuUsage;
    /**
     * 内存使用量
     */
    private Map<String, String> memoryUsage;
    /**
     * 公网带宽
     */
    private Map<String, String> networkInfo;
    /**
     * 磁盘容量
     */
    private Map<String, String> diskUsage;


    public static boolean checkSSH(String host, String username, String password) throws Exception {
        // 创建会话
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, 22);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        // 连接远程主机
        session.connect();
        return session.isConnected();
    }


    public static String getCpuUsage(String host, String username, String password) throws Exception {
        String result = null;
        // 创建会话
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, 22);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        // 连接远程主机
        session.connect();

        // 执行 top 命令
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand("top -bn1 | grep 'Cpu(s)' | awk '{print $8}'");
        channel.setInputStream(null);
        channel.setErrStream(System.err);
        channel.connect();
        InputStream in = channel.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String cpuIdle = reader.readLine();
        double idle = Double.parseDouble(cpuIdle);
        double cpuUsage = 100 - idle;
        DecimalFormat df = new DecimalFormat("#.00"); // 创建DecimalFormat对象，指定保留两位小数
        result = df.format(cpuUsage); // 格式化num并转换为字符串
//        System.out.println("CPU utilization: " + result + "%");
        // 关闭连接
        channel.disconnect();
        session.disconnect();
        return result;
    }

    public static Map<String, String> getMemUsage(String host, String username, String password) throws Exception {
        Map<String, String> result = new HashMap<>();
        // 创建会话
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, 22);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        // 连接远程主机
        session.connect();

        // 执行 free 命令
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand("free -m | grep 'Mem' | awk '{print $2, $3}'");
        channel.setInputStream(null);
        channel.setErrStream(System.err);
        channel.connect();
        InputStream in = channel.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String memInfo = reader.readLine();
        String[] memStats = memInfo.split("\\s+");
        String totalMem = memStats[0];
        String usedMem = memStats[1];
        double usedMemPercent = Double.parseDouble(usedMem) / Double.parseDouble(totalMem) * 100;
        DecimalFormat df = new DecimalFormat("#.00"); // 创建DecimalFormat对象，指定保留两位小数
        result.put("totalMemory", totalMem);
        result.put("usedMemory", usedMem);
        result.put("usedMemoryPercent", df.format(usedMemPercent));
        channel.disconnect();
        session.disconnect();
        return result;
    }

    // 获取硬盘信息
    public static Map<String, String> getDiskUsage(String host, String username, String password) throws Exception {
        Map<String, String> result = new HashMap<>();
        // 创建会话
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, 22);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        // 连接远程主机
        session.connect();

        // 执行 df 命令
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand("df -h | grep '/dev/vda' | awk '{print $2, $3, $5}'");
        channel.setInputStream(null);
        channel.setErrStream(System.err);
        channel.connect();
        InputStream in = channel.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String diskInfo = reader.readLine();
        String[] diskStats = diskInfo.split("\\s+");
        String totalDisk = diskStats[0];
        String usedDisk = diskStats[1];
        String usedDiskPercent = diskStats[2];
        result.put("totalDisk", totalDisk.replace("G", ""));
        result.put("usedDisk", usedDisk.replace("G", ""));
        result.put("usedDiskPercent", usedDiskPercent.replace("%", ""));
        channel.disconnect();
        session.disconnect();
        return result;
    }

    // 获取网络信息
    public static Map<String, String> getNetworkBytes(String host, String username, String password) throws Exception {
        Map<String, String> result = new HashMap<>();
        // 创建会话
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, 22);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        // 连接远程主机
        session.connect();
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand("ifconfig eth0 | grep 'bytes' | awk '{print $5}'");
        channel.setInputStream(null);
        channel.setErrStream(System.err);
        channel.connect();
        InputStream in = channel.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String receivedBytes = reader.readLine();
        System.out.println(receivedBytes);
        String sentBytes = reader.readLine();
        System.out.println(sentBytes);
        result.put("receiveBytes", receivedBytes);
        result.put("transmitBytes", sentBytes);
        channel.disconnect();
        session.disconnect();
        return result;
    }
}
