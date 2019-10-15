package Ciscos.delta.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Mac {
    private static HashMap<String, String> arpMac = new HashMap<>();

    public static HashMap<String, String> getArpMac() {
        return arpMac;
    }

    public Mac() {
        fillArp();
        showArp();
    }

    private void fillArp(){
        System.out.print("Collect arp data");
        String firstOctets = "10.64.";
        for (int i = 144; i < 157; i++) {
            for (int j = 1; j < 256; j++) {
                ping(firstOctets + i + "." + j);
            }
            try {
                Thread.sleep(200);
                probeArp();
            } catch (InterruptedException ignored) {
            }
            System.out.print(".");
        }
    }

    private void ping(String ip){
        Runnable r = () -> {
            try {
//                Runtime.getRuntime().exec("ping -c 2 -b 255.255.255.255");
                Runtime.getRuntime().exec("ping -c 2 " + ip);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        new Thread(r).start();
    }

    private void probeArp(){
        try {
            Process process = Runtime.getRuntime().exec("arp -a");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            reader.lines().forEach(s -> {
                String[] line = s.replace("(", "").replace(")", "").split(" ");
                arpMac.put(line[3], line[1] + " " + line[0]);   // 11:66:55:dd:00:aa	10.1.72.118 smr-ws0000.
            });

        } catch (IOException ignored) {
        }
    }

    private void showArp() {
//        for (Map.Entry<String, String> entry : arpMac.entrySet()) {
//            System.out.println(entry.getKey() + "\t" + entry.getValue());
//        }
        System.out.println("\nCollected " + arpMac.size() + " pairs");
    }
}
