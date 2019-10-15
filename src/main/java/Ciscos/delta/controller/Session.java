package Ciscos.delta.controller;

import Ciscos.delta.model.Cisco;
import Ciscos.delta.view.ExcelOut;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Session {
private static ArrayList<Cisco> ciscos = new ArrayList<>();

    public static void newSession(String ip) {
        SnmpSession snmpSession = new SnmpSession(ip);//context.getBean("SnmpSessionImpl", SnmpSessionImpl.class);
        Cisco cisco = snmpSession.getCisco();
        new ExcelOut(cisco);
//        showCisco(cisco);
        ciscos.add(cisco);
    }

    public static void outUniqueList(){
        ExcelOut.writeUniqueSheet(ciscos);
    }

    private static void showCisco(Cisco cisco) {
//        for (Map.Entry<String, String> entry : cisco.getMacIface().entrySet())
//            System.out.println(entry.getKey() + "\t\t" + entry.getValue());
//        System.out.println();
        for (Map.Entry<String, String> entry : cisco.getIpMac().entrySet())
            System.out.println(entry.getKey() + "\t\t" + entry.getValue());
        System.out.println();
//        for (Map.Entry<String, Integer> entry : cisco.getUniqueMap().entrySet())
//            System.out.println(entry.getKey() + "\t\t" + entry.getValue());
//        System.out.println();
        for (String s : cisco.getUniqueList())
            System.out.println(s);
        System.out.println(cisco.getName());
//        System.out.println(cisco.getMacIface().size());
    }

//    public static void test() {
//        RecordsServiceImpl recordsRepository = context
//                .getBean("recordsService", RecordsServiceImpl.class);
//        System.out.println(recordsRepository.
//                findByCiscoEm(recordsRepository.findCisco("10.64.144.10")));
//    }

    public static List<String> getCiscoIPList() {
        String matchIP = "((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)";
        List<String> ipList = null;
        try (Stream<String> stream = Files.lines(Paths.get("cisco.ip"), Charset.defaultCharset())) {
            if (stream != null) {
                ipList = stream
                        .filter(s -> s.matches(matchIP))
                        .filter(Session::checkIp)
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            System.err.println("Not found cisco.ip");
        }
        assert ipList != null;
        return ipList;
    }

    private static boolean checkIp(String ip) {
        boolean b = false;
        Socket socket = new Socket();
        SocketAddress address = new InetSocketAddress(ip, 23); //telnet
        try {
            socket.connect(address);
        } catch (IOException e) {
            System.err.println("timeout " + ip);
        }
        if (socket.isConnected()) {
            b = true;
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
        return b;
    }
}