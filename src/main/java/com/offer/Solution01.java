package com.offer;

/**
 * @pakage: com.offer
 * @author: Administrator
 * @create: 2020/3/22   二位数组
 */
public class Solution01 {
    /**
     * @param target 查找的目标值
     * @param arr    二位数组
     * @return
     */
    public static boolean find(int target, int[][] arr) {
        // 获取第一行最右边的数，其特点是：行最大，列最小
        int x = arr.length - 1;
        int y = 0;
        int tmp = arr[x][0];
        // 默认不存在
        boolean flag = false;
        while (x >= 0 && x < arr.length && y >= 0 && y < arr.length) {
            if (tmp > target){
                y++;
            }else if (tmp<target){
                x--;
            }else {
                flag =true;
                break;
            }

        }
        return flag;
    }
}
