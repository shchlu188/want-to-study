####谈谈对程序(Program)、进程(Process)、线程(Thread)的理解？
    ·程序：是为完成特定任务、用某种语言编写的一组指令集合
    ·进程：就是程序执行的过程，是资源分配的单位
    ·线程：作为进程的调度和执行的单位，每个线程拥有自己独立的栈和程序计数器
####代码完成继承Thread的方式创建线程，并遍历100以内的自然数
    `class MyThread extends Thread{
         public static int count =100;
         @Override
         public void run() {
             while (true){
                 if (count>0){
                     count--;
                 }
             }
         }
     }`
####代码完成实现Runnable的方式创建线程，并遍历100以内的自然数

####对比两种创建方式

####IDEA中Project和Module的理解

####线程的生命周期
    ·新建：
    ·就绪：
    ·阻塞：
    ·运行：
    ·销毁：
####解决线程安全
    ·同步代码块
        synchronized(同步监视器){操作资源的代码}
        要求多线程必须要共用同意把锁
    ·同步方法
        在方法上加上synchronized关键字
    
 
        
