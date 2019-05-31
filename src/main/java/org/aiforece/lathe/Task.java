package org.aiforece.lathe;

import java.util.logging.Logger;

public  class Task implements Runnable{
    protected String name;
    public Task(String name){
        this.name = name;
    }

    @Override
    public void run(){
        System.out.println("TASK EXEC " + this.name);
    }
}
