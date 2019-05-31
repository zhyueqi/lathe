package org.aiforece.lathe;

import com.sun.istack.internal.logging.Logger;

import java.nio.ByteBuffer;

public class PacketResponse extends  Packet{
    private static final Logger logger = Logger.getLogger(PacketResponse.class);
    public final static int TASK_DISPATCH = 1;
    public final static int TASK_EXECUTE = 2;
    public final static int TASK_COMPLETE = 3;
    public final static int TASK_ACCQIRE = 4;
    public int taskType;
    public String body;
    public PacketResponse(int taskType, String body){
        this.taskType = taskType;
        this.body = body;
        int length = 4 + 4 + body.getBytes().length;
        this.buffer = ByteBuffer.allocate(length);
        this.buffer.clear();
        this.buffer.putInt(length);
        this.buffer.putInt(taskType);
        this.buffer.put(body.getBytes());
        this.buffer.flip();
    }

    public PacketResponse(ByteBuffer buffer){
        buffer.flip();
        int length = buffer.getInt();
        this.taskType = buffer.getInt();
        this.body = new String(buffer.array(), 0, buffer.position());
        logger.info("length: " + length);
        logger.info("taskType: "+ taskType);
    }


}
