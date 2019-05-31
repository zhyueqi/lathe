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
    private  byte[] buffer;
    private int packetSize;
    private int bytesReaded;

    public Session(SocketChannel socketChannel, int SelectionKey, Selector selector){
        this.socketChannel = socketChannel;
        this.eventsToBe = SelectionKey;
        this.selector = selector;
        this.buffer = new byte[MAXPACKET];
        this.packetSize = 0;
        this.bytesReaded = 0;
    }

    public void process() throws IOException {

        if((eventsReady & READABLE) != 0){
            sessionRead();
        }

        if ((eventsReady & WRITEABLE) != 0){
            sessionWrite();
        }
    }

    protected void sessionWrite() {
        String responseMsg = "it's from server";
        byte[] output = new byte[responseMsg.getBytes().length + 4];
        Packet packet = new Packet(output);
        packet.write(responseMsg);
        
    }

    protected void sessionRead() throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.wrap(this.buffer);
        if(packetSize == 0){
            // 未读读取到消息头
            int readed = this.socketChannel.read(byteBuffer);
            if(readed >=0) bytesReaded += readed;
            if(bytesReaded < 4 && readed == -1){
                close();
                return;
            }
            this.packetSize = fromByteArray(this.buffer);
            if(this.packetSize > MAXPACKET) {
                close();
                return;
            }
        }
        // 直到读取完整
        int readed = this.socketChannel.read(byteBuffer);
        if(readed >=0) bytesReaded += readed;
        if((bytesReaded < this.packetSize)){
            if(readed < 0)
                close();
            return;
        }

        // 解析报文、处理
        String message = Packet.onStringMessage(buffer, byteBuffer.position());
        byteBuffer.clear();
        logger.info("报文内容：" + message);
        // 监听写出
        this.socketChannel.keyFor(selector).interestOps(WRITEABLE);
        selector.wakeup();

    }

    public void close(){
        if(this.socketChannel.isOpen()){
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
