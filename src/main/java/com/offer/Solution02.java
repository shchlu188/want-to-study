package com.offer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * @pakage: com.offer
 * @author: Administrator
 * @create: 2020/3/22   最小的k个数 --29
 */
public class Solution02 {
    public static void main(String[] args) {
        Random random = new Random();
        int[] srcArr = new int[20];

        for (int i = 0; i < 20; i++) {
            srcArr[i] = random.nextInt(80);
        }
        System.out.println(Arrays.toString(srcArr));

        System.out.println(getLeastNumbers(srcArr,5));

    }

    /**
     * 方案1、
     * 对input数组排序，取前k个数
     * 方案2、
     * 使用优先队列{@link java.util.PriorityQueue} 是基于优先堆的一个无界队列，这个优先队列中的元素可以默认自然排序或者通过提供的
     * 方案3、
     * 使用堆排序:
     * 大顶堆
     * 小顶堆
     * 对于某个节点（index）：(从0开始)
     * 左孩子：2*index+1
     * 右孩子：2*index+2
     * 非叶子节点的下标: 节点个数/2 -1
     *
     * @param input 输入n个数
     * @param k     找出最小的个数
     * @return
     */
    public static ArrayList<Integer> getLeastNumbers(int[] input, int k) {
        // 判断k的值
        if (k > input.length || k == 0) {
            return new ArrayList<>();
        }
        // 存储k个元素
        int[] tmp = new int[k];
        // 拷贝
        System.arraycopy(input, 0, tmp, 0, k);
        // 维护堆排序
        for (int i = k / 2 - 1; i >= 0; i--) {
            // i 为维护的节点下标
            initiate(i, tmp, k);

        }
        // 遍历余下索引
        for (int i = k; i < input.length; i++) {
            if (input[i] < tmp[0]) {
                tmp[0] = input[i];
                initiate(0, tmp, k);
            }
        }
        // 将大顶堆的节点元素进行升序操作
        for (int i = k - 1; i >= 0; i--) {
            tmp[0] ^= tmp[i];
            tmp[i] ^= tmp[0];
            tmp[0] ^= tmp[i];
            // 维护堆
            initiate(0, tmp, i);

        }
        ArrayList<Integer> list = new ArrayList<>();
        for (int i : tmp) {
            list.add(i);
        }
        return list;
    }

    /**
     * @param i      下标
     * @param tmp    数组
     * @param length 堆的节点个数
     */
    private static void initiate(int i, int[] tmp, int length) {
        // 保存当前的节点的值
        int curValue = tmp[i];

        for (int k = i * 2 + 1; k < length; k = 2 * k + 1) {
            // 判断左右节点的大小
            if ((k + 1) < length && tmp[k + 1] > tmp[k]) {
                k++;
            }
            // 判断子节点与父节点的大小
            if (tmp[k] > curValue) {
                tmp[i] = tmp[k];
                // 把当前索引移到其左右节点的索引
                i = k;
                // 父节点比当前子节点都大，跳出循环
            } else {
                break;
            }
        }
        // 将索引i位置替换
        tmp[i] = curValue;

    }

}
