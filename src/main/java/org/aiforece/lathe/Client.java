package org.aiforece.lathe;

import com.sun.istack.internal.logging.Logger;

import java.io.IOException;

public class Client  {
    private static final Logger logger = Logger.getLogger(Client.class);
    public static void main(String[] args) throws IOException {
        Slave slave  = new Slave(1);
        Thread thread = new Thread(slave);
        thread.start();
    }
}
