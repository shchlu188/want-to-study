package com.thread;

/**
 * @pakage: com.thread
 * @author: Administrator
 * @create: 2020/3/20
 */
public class ExtendThreadDemo {
    public static void main(String[] args) {
        new MyThread().start();
        new MyThread().start();
        new MyThread().start();
    }
}

class MyThread extends Thread{
    public static int count =100;
    @Override
    public void run() {
        while (true){
            if (count>0){
                count--;
                System.out.println(Thread.currentThread().getName() + "\t sale ticket: \t" + count);
            }else {
                break;
            }
        }
    }
}
class MyImp implements Runnable{

    @Override
    public void run() {

    }
}
class Win extends Thread {
    public static int ticket = 100;

    @Override
    public void run() {
        while (true) {
            if (ticket >0) {
                ticket--;
                System.out.println(Thread.currentThread().getName() + "\t sale ticket: \t" + ticket);
            } else {
                break;
            }
        }
    }
}
