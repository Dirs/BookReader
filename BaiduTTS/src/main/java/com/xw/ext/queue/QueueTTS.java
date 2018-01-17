package com.xw.ext.queue;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.xw.ext.BaiduTTS;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by XWCHQ on 2018/1/15-19:44
 * @author XWCHQ
 */

public class QueueTTS {
    private static QueueTTS instance = null;
    private final List<QueueItem> mQueueLinked;
    private WeakReference<QueueSpeechListener> weakListener;
    private final SpeechSynthesizerListener speechListener = new SpeechSynthesizerListener() {
        @Override
        public void onSynthesizeStart(String s) {

        }

        @Override
        public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {

        }

        @Override
        public void onSynthesizeFinish(String s) {

        }

        @Override
        public void onSpeechStart(String s) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_ON_SPEAK_START,s));
        }

        @Override
        public void onSpeechProgressChanged(String s, int i) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_ON_SPEAK_PROGRESS,i,0,s));
        }

        @Override
        public void onSpeechFinish(String s) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_ON_SPEAK_FINISH,s));
        }

        @Override
        public void onError(String s, SpeechError speechError) {

        }
    };

    private final int MSG_CHECK = 1;
    private final int MSG_SPEAK = 10;
    private final int MSG_ON_SPEAK_START = 2;
    private final int MSG_ON_SPEAK_PROGRESS = 3;
    private final int MSG_ON_SPEAK_FINISH = 4;
    private Handler mHandler;
    private volatile boolean isStarted;
    private int mCurrentIndex = 0;
    private int mMaxSize = 50;
    private final Handler.Callback mHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CHECK: {
                    if (!isStarted) {
                        return true;
                    }
                    if(mCurrentIndex < 0 || mCurrentIndex >= mQueueLinked.size()){
                        return true;
                    }
                    QueueItem poll = mQueueLinked.get(mCurrentIndex);
                    if (poll != null) {
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SPEAK, poll), poll.getDelay());
                    }
                }
                break;
                case MSG_SPEAK:
                    performSpeak((QueueItem) msg.obj);
                    break;
                case MSG_ON_SPEAK_START:
                    if(getQueueSpeachListener() != null){
                        getQueueSpeachListener().onSpeechStart((String) msg.obj,mCurrentIndex,mQueueLinked.size());
                    }
                    break;
                case MSG_ON_SPEAK_PROGRESS:
                    if(getQueueSpeachListener() != null){
                        getQueueSpeachListener().onSpeechProgressChanged((String) msg.obj,msg.arg1,mCurrentIndex,mQueueLinked.size());
                    }
                    break;
                case MSG_ON_SPEAK_FINISH:
                    if(getQueueSpeachListener() != null){
                        getQueueSpeachListener().onSpeechFinish((String) msg.obj,mCurrentIndex,mQueueLinked.size());
                    }
                    synchronized (mQueueLinked){
                        processMaxSize();
                    }
                    if(hasNext()) {
                        next();
                    }else {
                        stopQueue();
                    }
                    break;
                default:
            }
            return true;
        }
    };
    private QueueTTS() {
        BaiduTTS.getInstance().addSpeechSynthesizerListener(speechListener);
        mHandler = new Handler(Looper.getMainLooper(), mHandlerCallback);
        mQueueLinked = new ArrayList<>();
    }

    public static QueueTTS getInstance() {
        if (instance == null) {
            synchronized (QueueTTS.class) {
                if (instance == null) {
                    instance = new QueueTTS();
                }
            }
        }
        return instance;
    }

    public int getMaxSize() {
        return mMaxSize;
    }

    public void setMaxSize(int maxSize) {
        this.mMaxSize = maxSize;
    }

    private void performSpeak(QueueItem item) {
        BaiduTTS.getInstance().setSpeaker(item.getSpeaker());
        BaiduTTS.getInstance().speak(item.getText());
    }

    public void speak(String text) {
        speak(text, 0);
    }

    public void speak(String text, long delay) {
        speak(new QueueItem(text, delay));
    }

    public void speak(QueueItem queueItem) {
        addQueueItem(queueItem);
        speakQueue();
    }

    public void speakQueue() {
        if(!isStarted) {
            speakNext();
        }
    }

    public boolean isStarted(){
        return isStarted;
    }

    public void stopQueue() {
        isStarted = false;
    }

    public void clearQueue() {
        mQueueLinked.clear();
    }

    public void stopSpeak() {
        BaiduTTS.getInstance().stop();
    }

    public void stopAll(){
        stopQueue();
        stopSpeak();
    }

    public void stopAllAndClearQueue() {
        clearQueue();
        stopAll();
    }

    public boolean speakNext() {
        if(!hasNext()){
            return false;
        }
        mCurrentIndex++;
        if (!isStarted) {
            isStarted = true;
            mHandler.sendEmptyMessage(MSG_CHECK);
        }
        return true;
    }

    private void next(){
        mCurrentIndex++;
        mHandler.sendEmptyMessage(MSG_CHECK);
    }

    public boolean hasNext() {
        return mQueueLinked.size() > 0 && mCurrentIndex < mQueueLinked.size() - 1;
    }

    public boolean hasLast(){
        return mQueueLinked.size() > 0 && mCurrentIndex > 0;
    }

    public boolean speakLast(){
        if(!hasLast()){
            return false;
        }
        mCurrentIndex--;
        if(!isStarted) {
            isStarted = true;
            mHandler.sendEmptyMessage(MSG_CHECK);
        }
        return true;
    }

    public void addQueueItem(QueueItem queueItem) {
        synchronized (mQueueLinked) {
            mQueueLinked.add(queueItem);
            processMaxSize();
        }
    }

    public void addQueueItems(List<QueueItem> queueItemList) {
        synchronized (mQueueLinked) {
            mQueueLinked.addAll(queueItemList);
            processMaxSize();
        }
    }

    private void processMaxSize() {
        if(mQueueLinked.size() > mMaxSize){
            int startIndex = mQueueLinked.size() - mMaxSize;
            if(mCurrentIndex < startIndex){
                startIndex = mCurrentIndex;
            }
            if(startIndex == 0){
                return;
            }
            for(int i = startIndex - 1;i >= 0;i--){
                mQueueLinked.remove(i);
            }
            mCurrentIndex -= startIndex;
        }
    }

    public void setQueueSpeachListener(QueueSpeechListener listener){
        weakListener = new WeakReference<QueueSpeechListener>(listener);
    }

    public QueueSpeechListener getQueueSpeachListener(){
        if(weakListener != null) {
            return weakListener.get();
        }else {
            return null;
        }
    }
}
