package Ciscos.delta.model;

import java.util.ArrayList;
import java.util.HashMap;

public class Cisco {
    private String name;
    private String ip;
    private HashMap<String, String> macIface;       //  11:66:55:dd:00:aa		9
    private HashMap<String, String> ipMac;          //  11:66:55:dd:00:aa	10.1.72.118
    private HashMap<String, Integer> uniqueMap;     //  9		1
    private ArrayList<String> uniqueList;           //  9	11:66:55:dd:00:aa	10.1.72.118

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, String> getMacIface() {
        return macIface;
    }

    public void setMacIface(HashMap<String, String> macIface) {
        this.macIface = macIface;
    }

    public HashMap<String, String> getIpMac() {
        return ipMac;
    }

    public void setIpMac(HashMap<String, String> ipMac) {
        this.ipMac = ipMac;
    }

    public HashMap<String, Integer> getUniqueMap() {
        return uniqueMap;
    }

    public void setUniqueMap(HashMap<String, Integer> uniqueMap) {
        this.uniqueMap = uniqueMap;
    }

    public ArrayList<String> getUniqueList() {
        return uniqueList;
    }

    public void setUniqueList(ArrayList<String> uniqueList) {
        this.uniqueList = uniqueList;
    }

    public Cisco(String cisco_ip) {
        this.ip = cisco_ip;
    }

}