package org.aiforece.lathe;

public class PacketSlave extends Packet{
    public final static int REGIST = 21;
    public final static String EXIT = "EXIT";
    public String slaveName;
    public String command;

    public PacketSlave(String name, String command){
        this.slaveName = name;
        this.command = command;
        String msg = command + ";" + name;
        this.init(msg.getBytes());
    }
}
