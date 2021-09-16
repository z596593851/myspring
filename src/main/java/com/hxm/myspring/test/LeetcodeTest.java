package com.hxm.myspring.test;

import java.util.*;

public class LeetcodeTest {
    public int findMaximizedCapital(int k, int w, int[] profits, int[] capital) {
        int n = profits.length;
        List<int[]> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(new int[]{capital[i], profits[i]});
        }
        list.sort(Comparator.comparingInt(a -> a[0]));
        PriorityQueue<Integer> queue = new PriorityQueue<>((a, b)->b-a);
        int count=0;
        int index=0;
        while(index<n && w<=list.get(index)[0] && count<k){
            queue.add(list.get(index)[1]);
            w+=list.get(index)[1];
            index++;
        }
        while(index<n){
            if(w>=list.get(index)[0]){
                if(count<k){
                    queue.add(list.get(index)[1]);
                    w+=list.get(index)[1];
                }else if(w-queue.peek()>=list.get(index)[0]){
                    queue.poll();
                    queue.add(list.get(index)[1]);
                    w+=list.get(index)[1];
                }
                index++;
            }
        }
        return w;
    }
}
