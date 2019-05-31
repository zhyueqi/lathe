package org.aiforece.lathe;

import java.nio.ByteBuffer;

/**
 * Created by zhyueqi on 2019/5/31.
 */
public class Packet {
    protected byte[] array;
    protected ByteBuffer buffer;
    public Packet(byte[] array){
        this.array = array;
        this.buffer = ByteBuffer.wrap(array);
        this.write(array.length);
    }

    public static String onStringMessage(byte[] buffer, int length) {
        return new String(buffer, 4, length);
    }

    public void write(int b){
        this.buffer.putInt(b);
    }

    public void write(String msg){
        this.buffer.put(msg.getBytes());
    }

}