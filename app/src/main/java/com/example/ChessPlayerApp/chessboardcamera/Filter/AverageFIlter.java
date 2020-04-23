package com.example.ChessPlayerApp.chessboardcamera.Filter;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class AverageFIlter {
    Queue<Integer>[][] history;

    public AverageFIlter(int bufferSize){
        history = new Queue[8][8];
        for (int i = 0; i < 8 ; i ++)
            for (int j = 0; j < 8; j ++) {
                history[i][j] = new LinkedList<>();
                for (int k = 0; k < bufferSize; k++)
                    history[i][j].add(0);
            }

    }
    public void update(int[][] newData){
        for (int i = 0; i < 8 ; i ++)
            for (int j = 0; j < 8; j ++){
                history[i][j].poll();
                history[i][j].add(newData[i][j]);
            }
    }

    public int[][] getAvg(){
        int[][] mu = new int[8][8];
        for (int i = 0; i < 8 ; i ++)
            for (int j = 0; j < 8; j ++){
                int count = 0;
                for (int val: history[i][j]){
                    count += val;
                }
                mu[i][j] = count/history[i][j].size();
            }
        return mu;
    }

    public int getValue(int i, int j){
        int count = 0;
        for (int val: history[i][j]){
            count += val;
        }
        return count/history[i][j].size();
    }
}
