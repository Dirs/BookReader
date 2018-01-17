package com.xw.ext.queue;

/**
 * Created by XWCHQ on 2018/1/16-11:11
 */

public interface QueueSpeechListener {
    void onSpeechStart(String text,int currentIndex,int total);
    void onSpeechFinish(String text,int currentIndex,int total);
    void onSpeechProgressChanged(String text,int progress,int currentIndex,int total);
}
