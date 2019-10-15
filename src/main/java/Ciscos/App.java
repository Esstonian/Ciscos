package Ciscos;


import Ciscos.delta.controller.Session;
import Ciscos.delta.model.Mac;

public class App {
    public static void main(String[] args) {
        new Mac();
        Session.getCiscoIPList().forEach(Session::newSession);
//        Session.test();
        Session.outUniqueList();
        System.exit(0); // ERROR: JDWP Unable to get JNI 1.2 environment, jvm->GetEnv() return code = -2
    }
}
