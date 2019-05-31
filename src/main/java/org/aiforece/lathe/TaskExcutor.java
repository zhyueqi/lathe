package org.aiforece.lathe;

import com.sun.istack.internal.logging.Logger;

import java.util.concurrent.BlockingQueue;

public class TaskExcutor implements Runnable {
    private static final Logger logger = Logger.getLogger(TaskExcutor.class);
    private BlockingQueue<Task> queue;

    public TaskExcutor(BlockingQueue<Task> queue){
        this.queue = queue;
    }

    @Override
    public void run() {
    //获取消息，收到exit后退出
        try {
            for(Task task = queue.take(); task.name != "EXIT";) {
                logger.info("Task " + task.name + " execute");
                Thread.sleep(20);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
