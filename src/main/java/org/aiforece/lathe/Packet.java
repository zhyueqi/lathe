package org.aiforece.lathe;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by zhyueqi on 2019/5/31.
 */
public class Packet {
    protected byte[] array;
    protected ByteBuffer buffer;


    public Packet(){}

    public Packet(byte[] array){
        init(array);
    }

    protected void init(byte[] array){
        this.array = array;
        this.buffer = ByteBuffer.allocate(array.length + 4);
        this.buffer.clear();
        this.buffer.putInt(array.length);
        this.buffer.put(array);
        this.buffer.flip();
    }

    public static String onStringMessage(byte[] buffer, int length) {
        return new String(buffer, 4, length - 4);
    }



}