package org.aiforece.lathe;

import com.sun.istack.internal.logging.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by zhyueqi on 2019/5/30.
 */
public class Session {
    private static final Logger logger = Logger.getLogger(Session.class);
    public static final int READABLE = SelectionKey.OP_READ;
    public static final int WRITEABLE = SelectionKey.OP_WRITE;
    public static final int CLOSE = -1;
    public static final int MAXPACKET = 32 * 1024;
    protected SocketChannel socketChannel;
    protected Selector selector;
    protected int eventsToBe;
    protected int eventsReady;
    private  ByteBuffer readBuffer;
    private int packetSize;
    private int bytesReaded;

    private Packet toWrite;

    private int writed;
    private boolean closing;

    public Session(SocketChannel socketChannel, int SelectionKey, Selector selector){
        this.socketChannel = socketChannel;
        this.eventsToBe = SelectionKey;
        this.selector = selector;
        this.readBuffer = ByteBuffer.allocate(MAXPACKET);
        this.packetSize = 0;
        this.bytesReaded = 0;
        this.writed = 0;
        this.closing = false;
    }

    public void process() throws IOException {
        if((eventsReady & READABLE) != 0){
            sessionRead();
        }

        if ((eventsReady & WRITEABLE) != 0){
            sessionWrite();
        }
    }

    protected void sessionWrite() throws IOException {
        logger.info("sessionWrite");
        if(this.socketChannel.keyFor(selector).isValid() == false){
            logger.info("session write not valid");
            return;
        }

        if(this.socketChannel.isOpen() ==  false){
            logger.info("client has been closed!");
        }

        if(this.toWrite != null){
            transport(this.toWrite);
        }
    }

    public void writeToClient(){
        String msg = "response to client!";
        this.toWrite = new Packet(msg.getBytes());
    }


    private void transport(Packet packet) throws IOException {
        int length = this.socketChannel.write(packet.buffer);
        if(length > 0) writed+= length;
        if(false == packet.buffer.hasRemaining()){
            logger.info("RESPONSE COMPLETE!");
            this.toWrite = null;
            socketChannel.keyFor(selector).interestOps(READABLE);
            selector.wakeup();
        }
    }

    protected void sessionRead() throws IOException {

        if(packetSize == 0){
            // 未读读取到消息头
            int readed = this.socketChannel.read(readBuffer);
            if(readed >=0) bytesReaded += readed;
            if(bytesReaded < 4 && readed == -1){
                close();
                return;
            }
            this.packetSize = fromByteArray(this.readBuffer.array());
            if(this.packetSize > MAXPACKET) {
                close();
                return;
            }
        }
        // 直到读取完整
        int readed = this.socketChannel.read(readBuffer);
        if(readed >=0) bytesReaded += readed;
        if((bytesReaded < this.packetSize)){
            if(readed < 0)
                close();
            return;
        }

        // 解析报文、处理

        PacketResponse response = new PacketResponse(readBuffer);
        String message = response.body;
        readBuffer.clear();
        this.bytesReaded = 0;
        logger.info("报文内容：" + message);

        writeToClient();
        // 监听写出
        this.socketChannel.keyFor(selector).interestOps(WRITEABLE);
        selector.wakeup();

    }

    public void close(){
        if(this.socketChannel.isOpen()){
            this.closing = true;
            SelectionKey key = this.socketChannel.keyFor(this.selector);
            if(key != null)
                key.cancel();
            this.selector = null;
        }
    }

    // packing an array of 4 bytes to an int, big endian
    private int fromByteArray(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

}
