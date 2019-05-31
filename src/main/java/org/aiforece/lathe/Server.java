package org.aiforece.lathe;

import com.sun.istack.internal.logging.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

/**
 * Created by zhyueqi on 2019/5/29.
 */
public class Server extends Thread{
    private static final Logger logger = Logger.getLogger(Server.class);
    private boolean accept;
    private int port;
    private String hostAddress;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ConcurrentLinkedQueue<Session> sessionQueue;
    private ConcurrentLinkedQueue<Plan> plans;

    public Server(ServerConfig config) throws IOException {
        this.sessionQueue = new ConcurrentLinkedQueue<>();
        this.plans = new ConcurrentLinkedQueue<>();
        this.port = config.SERVER_PORT;
        this.hostAddress = config.SERVER_HOST;
        this.accept = true;
        this.serverSocketChannel = ServerSocketChannel.open();

        InetSocketAddress address = new InetSocketAddress(hostAddress, port);
        this.serverSocketChannel.bind(address);
        this.serverSocketChannel.configureBlocking(false);
        logger.log(Level.INFO, "ServerNIO is binding on: "
                + serverSocketChannel.getLocalAddress());

        this.selector = Selector.open();
        this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }
    @Override
    public void run(){
        System.out.println("RUN!");
        while(accept){
            try {
                //方便查看日志过程
                sleep(2000);
                for(Session session; (session = sessionQueue.poll())!=null;){
                    session.socketChannel.register(this.selector, session.eventsToBe, session);
                }
                this.selector.select();
                Set<SelectionKey> comingKeys = this.selector.selectedKeys();
                // 测试是否闲置循环
                if(comingKeys.size() == 0){
                    try {
                        logger.warning("ServerNIO Free LOOP");
                        sleep(50);
                        continue;
                    } catch (InterruptedException e) {
                        accept = false;
                        break;
                    }
                }
                Iterator<SelectionKey> comingKeyIterator = comingKeys.iterator();
                while(comingKeyIterator.hasNext()){
                    SelectionKey currentKey = comingKeyIterator.next();
                    handleCurrentKey(currentKey);
                    comingKeyIterator.remove();
                }
            }catch (ClosedSelectorException e){
                logger.warning("SELECTOR is Closed");
            }
            catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void handleCurrentKey(SelectionKey currentKey){
        // 建立连接
        if(!currentKey.isValid()){
            return;
        }
        try {
            if(currentKey.isAcceptable()){
                SocketChannel incomingChannel = serverSocketChannel.accept();
                incomingChannel.configureBlocking(false);
                Session session = new Session(incomingChannel, Session.READABLE, this.selector, this.plans);
                sessionQueue.add(session);
                logger.info("Connection Accepted from: " + incomingChannel.getLocalAddress());
            }else {
                Session session = (Session) currentKey.attachment();
                session.eventsReady = currentKey.readyOps();
                session.process();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {
        ServerConfig serverConfig = new ServerConfig();
        Server server = new Server(serverConfig);
        server.plans.add(new Plan("2", Plan.PLAN_GEN));
        server.start();
    }
}


