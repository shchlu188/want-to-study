package com.thread;

/**
 * @pakage: com.thread
 * @author: scl
 * @create: 2020/3/20
 *
 *  继承{@link Thread}和实现@{@link Runnable}的比较
 *      优先选择{@link Runnable}
 *      没有类的单继承性的局限性
 *      实现的方式更加适合处理多线程有共享数据的情况
 *  联系：Thread也实现了Runnable 接口,都需要重写run()方法
 *
 */
public class ImplThreadDemo {
    public static void main(String[] args) {
        Ticket ticket = new Ticket(100);
        new Thread(ticket, "t1").start();
        new Thread(ticket, "t2").start();
        new Thread(ticket, "t3").start();
    }
}

class Ticket implements Runnable {
    private Integer count;

    public Ticket(int count) {
        this.count = count;
    }

    @Override
    public void run() {
        while (true) {
            if (count != 0) {
                try {
                    Thread.sleep(200);
                    sale();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }
    }

    private void sale() {
        if (count != 0) {
            System.out.println(Thread.currentThread().getName()+"\t"+count);
            this.count--;
        }
    }
}