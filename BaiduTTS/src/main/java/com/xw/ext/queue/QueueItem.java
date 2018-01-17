package com.xw.ext.queue;

import com.xw.ext.Speaker;

/**
 * Created by XWCHQ on 2018/1/15-19:48
 */

public class QueueItem {
    private String text = "";
    private long delay = 0;
    private int speaker = Speaker.FAMALE_NORMAL;

    public QueueItem(String text) {
        this(text,0);
    }

    public QueueItem(String text, long delay) {
        this(text,delay,Speaker.FAMALE_NORMAL);
    }

    public QueueItem(String text, long delay, int speaker) {
        this.text = text;
        this.delay = delay;
        this.speaker = speaker;
    }

    public String getText() {
        return text;
    }

    public QueueItem setText(String text) {
        this.text = text;
        return this;
    }

    public long getDelay() {
        return delay;
    }

    public QueueItem setDelay(long delay) {
        this.delay = delay;
        return this;
    }

    public int getSpeaker() {
        return speaker;
    }

    public QueueItem setSpeaker(int speaker) {
        this.speaker = speaker;
        return this;
    }
}
