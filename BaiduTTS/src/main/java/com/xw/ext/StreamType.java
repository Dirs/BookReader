package com.xw.ext;

import android.media.AudioManager;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by XWCHQ on 2017/4/11-11:19
 */
@IntDef(value = {AudioManager.STREAM_ALARM,AudioManager.STREAM_DTMF,AudioManager.STREAM_MUSIC,AudioManager.STREAM_RING,AudioManager.STREAM_VOICE_CALL,AudioManager.STREAM_NOTIFICATION,AudioManager.STREAM_SYSTEM})
@Retention(RetentionPolicy.SOURCE)
public @interface StreamType {
}
