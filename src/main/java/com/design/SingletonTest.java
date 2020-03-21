package com.design;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @pakage: com.design
 * @author: Administrator
 * @create: 2020/3/21
 * 字节码流程
 * JIT、CPU对指令重排序，导致使用到尚未初始化的实例,可以使用volatile关键字进行修饰
 * 1、分配空间
 * 2、引用赋值
 * 3、初始化
 * 注意：2和3的顺序不确定。
 */
public class SingletonTest {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        serialSingleton();

    }

    // 序列化单例
    private static void serialSingleton() throws IOException, ClassNotFoundException {
        LazyStaticSerialSingleton singleton = LazyStaticSerialSingleton.getInstance();
        ObjectOutput oos = new ObjectOutputStream(new FileOutputStream("testSerial"));
        oos.writeObject(singleton);
        oos.close();
        ObjectInput ois = new ObjectInputStream(new FileInputStream("testSerial"));
        LazyStaticSerialSingleton serialSingleton = (LazyStaticSerialSingleton) ois.readObject();
        System.out.println(serialSingleton == singleton);
    }

    /**
     * 通过反射实例化Singleton
     *
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private void reflectSingleton() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

        Constructor<LazyStaticSingleton> constructor = LazyStaticSingleton.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        LazyStaticSingleton staticSingleton = constructor.newInstance();
        LazyStaticSingleton lazyStaticSingleton = LazyStaticSingleton.getInstance();
        System.out.println(staticSingleton == lazyStaticSingleton);
    }
}

/**
 * 饿汉式
 * 类加载过程：
 * 1、加载二进制数据到内存中，生成对应的Class数据结构
 * 2、连接： a、检验；b、准备（给类的静态成员变量赋默认值）；c、解析
 * 3、初始化: 给类的静态变量赋初值
 * 初始化的条件：
 * 直接进行new操作
 * 访问静态属性
 * 访问静态方法
 * 用反射访问类
 * 初始化子类
 */
class HungrySingleton {
    private static final HungrySingleton INSTANCE = new HungrySingleton();

    private HungrySingleton() {
    }

    ;

    public static HungrySingleton getInstance() {
        return INSTANCE;
    }
}

/**
 * 懒汉式
 */
class LazyStaticSingleton {
    private static class InnerClassHolder {
        private static final LazyStaticSingleton INSTANCE = new LazyStaticSingleton();
    }

    private LazyStaticSingleton() {
        if (InnerClassHolder.INSTANCE != null) {
            throw new IllegalArgumentException("Cannot reflectively create  objects");
        }
    }

    ;

    public static LazyStaticSingleton getInstance() {
        return InnerClassHolder.INSTANCE;
    }
}

class LazyStaticSerialSingleton implements Serializable {
    // 可以通过版本号兼容
    private static class InnerClassHolder {
        private static final LazyStaticSerialSingleton INSTANCE = new LazyStaticSerialSingleton();
    }

    private LazyStaticSerialSingleton() {
        if (InnerClassHolder.INSTANCE != null) {
            throw new IllegalArgumentException("Cannot reflectively create  objects");
        }
    }

    ;

    public static LazyStaticSerialSingleton getInstance() {
        return InnerClassHolder.INSTANCE;
    }

    // 解决序列化
    private Object readResolve() throws ObjectStreamException {
        return InnerClassHolder.INSTANCE;
    }
}

class LazySingleton {
    private static LazySingleton instance;

    private LazySingleton() {

    }

    ;

    public static LazySingleton getInstance() {
        if (instance == null) {
            synchronized (LazySingleton.class) {
                if (instance == null) {
                    instance = new LazySingleton();

                }
            }
        }
        return instance;
    }
}

/**
 * 枚举创建
 */
enum EnumSingleton {
    INSTANCE;

    public void print() {
        System.out.println("enum Singleton");
    }
}

