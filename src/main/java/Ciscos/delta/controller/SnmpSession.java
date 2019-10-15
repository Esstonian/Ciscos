package Ciscos.delta.controller;

import Ciscos.delta.model.Cisco;
import Ciscos.delta.model.Mac;
import org.snmp4j.CommunityTarget;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;


class SnmpSession {
    private Cisco cisco;
    private static Snmp snmp;
    private static OID oid;
    private static CommunityTarget target;
    private static final String macOID = "1.3.6.1.2.1.17.4.3.1.1."; // #show mac address table
    private static final String intOID = "1.3.6.1.2.1.17.4.3.1.2."; // #show int
    private static final String ipmOID = "1.3.6.1.2.1.3.1.1.2.1.1."; // #show ip
    private static final String nameOID = "1.3.6.1.2.1.1.5.";

    SnmpSession(String ip) {
        cisco = new Cisco(ip);
        oid = getSNMP(ip);
        getCiscoRecords();
        cisco.setIp(ip);
        cisco.setName(getCiscoName());
        closeSnmp();
    }
    //        "1.3.6.1.4.1.9.9.68.1.2.2.1.2" // распределение access-портов по vlan

    Cisco getCisco() {
        return cisco;
    }

    private String getCiscoName() {
        oid.setValue(nameOID);
        HashMap<String, String> nameMap = getMap(oid); // map{oid, name}
        return nameMap.get(nameOID + "0");
    }

    private void getCiscoRecords() {
        HashMap<String, String> macIface = new HashMap<>();
        HashMap<String, String> ipMac = new HashMap<>();
        HashMap<String, String> macIp = Mac.getArpMac();

        oid.setValue(macOID);
        HashMap<String, String> macMap = getMap(oid); // map{oid+mac d, mac address h}
        oid.setValue(intOID);
        HashMap<String, String> ifaceMap = getMap(oid); // map{oid+mac d, int number h}
        oid.setValue(ipmOID);
        HashMap<String, String> ipMap = getMap(oid); // map{oid+ip d, mac address h}

        macMap.forEach((s1, s2) -> macIface.put(s2, ifaceMap.get(intOID + s1.split(macOID)[1])));
        ipMap.forEach((s1, s2) -> ipMac.put(s2, s1.split(ipmOID)[1]));

        HashMap<String, Integer> uniqueMap = new HashMap<>();
        macIface.forEach((macName, intNum) -> { //count interfaces
            if (uniqueMap.containsKey(intNum))
                uniqueMap.put(intNum, uniqueMap.get(intNum) + 1);
            else uniqueMap.put(intNum, 1);
        });

        ArrayList<String> uniqueList = new ArrayList<>();
        uniqueMap.forEach((macNum, intName) -> {
            if (intName == 1) {
                final String[] mac = new String[1];
                macIface.forEach((s1, s2) -> {
                    if (Integer.valueOf(macIface.get(s1)).equals(Integer.valueOf(macNum))) mac[0] = s1;
                });
                String ip = ipMac.get(mac[0]);
                String addIp;
                if (ip == null) {
                    addIp = "\t ";
                    if (macIp.get(mac[0]) != null) {
                        uniqueList.add(macNum + addIp + mac[0] + addIp + macIp.get(mac[0]).split(" ")[0] + "\t" + macIp.get(mac[0]).split(" ")[1]);
                    } else {
                        uniqueList.add(macNum + addIp + mac[0] + addIp + addIp);
                    }
                } else {
                    addIp = "\t" + ip;
                    uniqueList.add(macNum + "\t" + mac[0] + addIp + "\t" + getByName(ip));
                }
            }
        });

        uniqueList.sort((o1, o2) -> {
            int i1 = Integer.valueOf(o1.split("\t")[0]);
            int i2 = Integer.valueOf(o2.split("\t")[0]);
            if (i1 == i2) return 0;
            return i1 < i2 ? -1 : 1;
        });
        cisco.setIpMac(ipMac);
        cisco.setMacIface(macIface);
        cisco.setUniqueMap(uniqueMap);
        cisco.setUniqueList(uniqueList);
    }

    private String getByName(String mac) {
        String ip = "";
        try {
            ip = InetAddress.getByName(mac).getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return ip;
    }

    private void closeSnmp() {
        try {
            snmp.close();
        } catch (IOException e) {
            System.err.println("Snmp stop session error");
        }
    }

    private OID getSNMP(String ciscoIP) {
        String agentAddress = "udp:" + ciscoIP + "/161";
        try {
            newSnmpSession(agentAddress);
        } catch (IOException e) {
            System.err.println("New snmp session error");
        }
        return (new OID());
    }

    private HashMap<String, String> getMap(OID oid) {
        TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
        treeUtils.setMaxRepetitions(10);
        List<TreeEvent> list = treeUtils.getSubtree(target, oid);
        HashMap<String, String> map = new HashMap<>();

        list.forEach(event -> {
            if (event.isError()) {
                System.err.println("oid [" + oid + "] " + event.getErrorMessage());
            }
            VariableBinding[] varBindings = event.getVariableBindings();
            if (varBindings != null)
                for (VariableBinding varBinding : varBindings) {
                    map.put(varBinding.getOid().toString(), varBinding.getVariable().toString());
//                    log.Log.getLogger().info(varBinding.getOid() + " : " + varBinding.getVariable());
                }
            else
                System.err.println("request timed out");
        });
        return map;
    }

    private void newSnmpSession(String AgentAddress) throws IOException {
        Address targetAddress = GenericAddress.parse(AgentAddress);
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        snmp = new Snmp(transport);
        transport.listen();
        target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(targetAddress);
        target.setRetries(2);
        target.setTimeout(1000);
        target.setVersion(SnmpConstants.version2c);
/*        switch (version) {
            case 1:
                target.setVersion(SnmpConstants.version1);   //  OID
            case 2:
                target.setVersion(SnmpConstants.version2c);  //  MIB
            case 3:
                target.setVersion(SnmpConstants.version3);
            default:
                target.setVersion(SnmpConstants.version1);
        }*/
    }
}