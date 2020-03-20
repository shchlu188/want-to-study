package com.thread;

import java.util.concurrent.TimeUnit;

/**
 * @pakage: com.thread
 * @author: Administrator
 * @create: 2020/3/20
 * @Descript: 线程的生命周期
 */
public class ThreadLifeDemo {
    public static void main(String[] args) {
        LifeTicket ticket = new LifeTicket();
        new Thread(ticket).start();
        new Thread(ticket).start();
        new Thread(ticket).start();
        new Thread(ticket).start();
    }
}

class LifeTicket implements Runnable {
    private int ticket = 100;

    @Override
    public void run() {
        while (true) {
           synchronized (this){
               try {
                   TimeUnit.SECONDS.sleep(2);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }

               if (ticket > 0) {
                   ticket--;
                   System.out.println(Thread.currentThread().getName() + "\t 还剩下：" + ticket);
               }else {
                   break;
               }
           }
        }

    }
}