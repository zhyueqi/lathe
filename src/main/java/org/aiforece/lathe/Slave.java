package org.aiforece.lathe;

import com.sun.istack.internal.logging.Logger;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;

public class Slave implements Runnable {
    private static final Logger logger = Logger.getLogger(Slave.class);
    private String slaveName;
    private SocketChannel client;
    private int clientIndex;
    private boolean closing;

    public Slave(int index) throws IOException {
        InetSocketAddress address = new InetSocketAddress( ServerConfig.SERVER_HOST, ServerConfig.SERVER_PORT);
        this.client = SocketChannel.open(address);
        this.clientIndex = index;
        this.slaveName = "slave[" + this.clientIndex + "]";
        this.closing = false;
    }
    @Override
    public void run() {
        logger.info("Slave start!");
        ByteBuffer buffer = ByteBuffer.allocate(Session.MAXPACKET);
        buffer.clear();
        // connect
        PacketResponse msg = new PacketResponse(PacketResponse.TASK_ACCQIRE, this.slaveName);
        try {
            this.client.write(msg.buffer);
        } catch (IOException e) {
            logger.warning("Regist slave failed !");
            return;
        }
        // wait command
        while(false == closing) {
            buffer.clear();
            try {
                this.client.read(buffer);
                PacketResponse  response = new PacketResponse(buffer);
                PacketResponse result = execTask(response);
                buffer.clear();
                this.client.write(result.buffer);
            }catch (InterruptedIOException e){
                closing = true;
                break;
            } catch (IOException e) {
                closing = true;
                break;
            }
        }
    }

    private PacketResponse execTask(PacketResponse response){
        logger.info("task type: " + response.taskType);
        logger.info("task body: " + response.body);

        return new PacketResponse(PacketResponse.TASK_COMPLETE, "I'm DONE!" );
    }

}
