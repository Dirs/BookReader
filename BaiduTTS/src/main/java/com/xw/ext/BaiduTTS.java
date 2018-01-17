package com.xw.ext;

import android.content.Context;
import android.media.AudioManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.SynthesizerTool;
import com.baidu.tts.client.TtsMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by XWCHQ on 2016/12/26-14:52.
 */
public class BaiduTTS implements SpeechSynthesizerListener {
    private static final String TAG = "BaiduTTS";
    private static final String SAMPLE_DIR_NAME = "baiduTTS";
    private static final String SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female.dat";
    private static final String SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male.dat";
    private static final String TEXT_MODEL_NAME = "bd_etts_text.dat";
    private static final String LICENSE_FILE_NAME = "temp_license";
    private static final String ENGLISH_SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female_en.dat";
    private static final String ENGLISH_SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male_en.dat";
    private static final String ENGLISH_TEXT_MODEL_NAME = "bd_etts_text_en.dat";
    private static BaiduTTS instance;
    private SpeechSynthesizer mSpeechSynthesizer;
    private String mSampleDirPath;
    private SpeechSynthesizerListener speechSynthesizerListener = new SpeechSynthesizerListener() {
        @Override
        public void onSynthesizeStart(String s) {
            for (WeakReference<SpeechSynthesizerListener> weakListener : weakListeners) {
                SpeechSynthesizerListener listener = weakListener.get();
                if(listener != null){
                    listener.onSynthesizeStart(s);
                }
            }
        }

        @Override
        public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {
            for (WeakReference<SpeechSynthesizerListener> weakListener : weakListeners) {
                SpeechSynthesizerListener listener = weakListener.get();
                if(listener != null){
                    listener.onSynthesizeDataArrived(s,bytes,i);
                }
            }
        }

        @Override
        public void onSynthesizeFinish(String s) {
            for (WeakReference<SpeechSynthesizerListener> weakListener : weakListeners) {
                SpeechSynthesizerListener listener = weakListener.get();
                if(listener != null){
                    listener.onSynthesizeFinish(s);
                }
            }
        }

        @Override
        public void onSpeechStart(String s) {
            for (WeakReference<SpeechSynthesizerListener> weakListener : weakListeners) {
                SpeechSynthesizerListener listener = weakListener.get();
                if(listener != null){
                    listener.onSpeechStart(s);
                }
            }
        }

        @Override
        public void onSpeechProgressChanged(String s, int i) {
            for (WeakReference<SpeechSynthesizerListener> weakListener : weakListeners) {
                SpeechSynthesizerListener listener = weakListener.get();
                if(listener != null){
                    listener.onSpeechProgressChanged(s,i);
                }
            }
        }

        @Override
        public void onSpeechFinish(String s) {
            for (WeakReference<SpeechSynthesizerListener> weakListener : weakListeners) {
                SpeechSynthesizerListener listener = weakListener.get();
                if(listener != null){
                    listener.onSpeechFinish(s);
                }
            }
        }

        @Override
        public void onError(String s, SpeechError speechError) {
            for (WeakReference<SpeechSynthesizerListener> weakListener : weakListeners) {
                SpeechSynthesizerListener listener = weakListener.get();
                if(listener != null){
                    listener.onError(s,speechError);
                }
            }
        }
    };
    private List<WeakReference<SpeechSynthesizerListener>> weakListeners = new ArrayList<>();
    private TtsMode ttsMode = TtsMode.MIX;

    private BaiduTTS() {
        mSpeechSynthesizer = SpeechSynthesizer.getInstance();
    }

    public static BaiduTTS getInstance() {
        if (instance == null) {
            synchronized (BaiduTTS.class) {
                if (instance == null) {
                    instance = new BaiduTTS();
                }
            }
        }
        return instance;
    }

    /**
     * @see #init(Context, ApiInfo, TtsMode)
     */
    public void init(@NonNull Context context, @NonNull String appId, @NonNull String apiKey, @NonNull String secretKey) {
        init(context, new ApiInfo(appId, apiKey, secretKey), TtsMode.MIX);
    }

    public void init(@NonNull Context context, @NonNull ApiInfo apiInfo, @Nullable TtsMode mode) {
        initialEnv(context.getApplicationContext());
        initialSynthesizer(context.getApplicationContext(), apiInfo.getAppId(), apiInfo.getApiKey(), apiInfo.getSecretKey());
        if (mode != null) {
            this.ttsMode = mode;
        }
    }

    private void initialSynthesizer(Context context, String appId, String apiKey, String secretKey) {
        this.mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        this.mSpeechSynthesizer.setContext(context);
        this.mSpeechSynthesizer.setSpeechSynthesizerListener(this);
        // 文本模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, mSampleDirPath + "/"
                + TEXT_MODEL_NAME);
        // 声学模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, mSampleDirPath + "/"
                + SPEECH_FEMALE_MODEL_NAME);
        // 本地授权文件路径,如未设置将使用默认路径.设置临时授权文件路径，LICENCE_FILE_NAME请替换成临时授权文件的实际路径，仅在使用临时license文件时需要进行设置，如果在[应用管理]中开通了正式离线授权，不需要设置该参数，建议将该行代码删除（离线引擎）
        // 如果合成结果出现临时授权文件将要到期的提示，说明使用了临时授权文件，请删除临时授权即可。
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_LICENCE_FILE, mSampleDirPath + "/"
                + LICENSE_FILE_NAME);
        // 请替换为语音开发者平台上注册应用得到的App ID (离线授权)
        this.mSpeechSynthesizer.setAppId(appId/*APPID,请替换成自己的id。*/);
        // 请替换为语音开发者平台注册应用得到的apikey和secretkey (在线授权)
        this.mSpeechSynthesizer.setApiKey(apiKey,
                secretKey/*自己的APIKey*/);
        // 发音人（在线引擎），可用参数为0,1,2,3。。。（服务器端会动态增加，各值含义参考文档，以文档说明为准。0--普通女声，1--普通男声，2--特别男声，3--情感男声。。。）
        setSpeaker(0);
        // 设置Mix模式的合成策略
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        setVolume(9);
        setAudioStreamType(AudioManager.STREAM_MUSIC);
        // 授权检测接口(只是通过AuthInfo进行检验授权是否成功。)
        // AuthInfo接口用于测试开发者是否成功申请了在线或者离线授权，如果测试授权成功了，可以删除AuthInfo部分的代码（该接口首次验证时比较耗时），不会影响正常使用（合成使用时SDK内部会自动验证授权）
        AuthInfo authInfo = this.mSpeechSynthesizer.auth(ttsMode);

        if (authInfo.isSuccess()) {
            Log.d(TAG, "auth success");
        } else {
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            Log.d(TAG, "auth failed errorMsg=" + errorMsg);
        }

        // 初始化tts
        mSpeechSynthesizer.initTts(ttsMode);
        // 加载离线英文资源（提供离线英文合成功能）
        int result =
                mSpeechSynthesizer.loadEnglishModel(mSampleDirPath + "/" + ENGLISH_TEXT_MODEL_NAME, mSampleDirPath
                        + "/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
        Log.d(TAG, "loadEnglishModel result=" + result);

        //打印引擎信息和model基本信息
        printEngineInfo();
    }

    /**
     * @param type AudioManager.Stream_
     */
    public void setAudioStreamType(@StreamType int type) {
        if (this.mSpeechSynthesizer != null) {
            this.mSpeechSynthesizer.setAudioStreamType(type);
        }
    }

    /**
     *没有效果，不开放
     * @param volume 0-9
     */
    private void setVolume(int volume) {
        if (this.mSpeechSynthesizer != null) {
            this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, String.valueOf(volume));
        }
    }

    /**
     * 联网模式可用，MIX模式在有网络时有效
     * @param speaker {@link Speaker}
     */
    public void setSpeaker(int speaker) {
        if (this.mSpeechSynthesizer != null) {
            this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, String.valueOf(speaker));
        }
    }

    /**Deprecated,
     * use {@link #addSpeechSynthesizerListener(SpeechSynthesizerListener)}
     * @param listener
     */
    @Deprecated
    public void setSpeechSynthesizerListener(SpeechSynthesizerListener listener) {
        addSpeechSynthesizerListener(listener);
    }

    public void addSpeechSynthesizerListener(SpeechSynthesizerListener listener){
        weakListeners.add(new WeakReference<SpeechSynthesizerListener>(listener));
    }

    private void initialEnv(Context context) {
        if (mSampleDirPath == null) {
            String sdcardPath = Environment.getExternalStorageDirectory().toString();
            mSampleDirPath = sdcardPath + "/" + SAMPLE_DIR_NAME;
        }
        boolean result = makeDir(mSampleDirPath);
        if (!result) {
            return;
        }
        copyFromAssetsToSdcard(context, false, SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/" + SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(context, false, SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/" + SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(context, false, TEXT_MODEL_NAME, mSampleDirPath + "/" + TEXT_MODEL_NAME);
        copyFromAssetsToSdcard(context, false, LICENSE_FILE_NAME, mSampleDirPath + "/" + LICENSE_FILE_NAME);
        copyFromAssetsToSdcard(context, false, "english/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(context, false, "english/" + ENGLISH_SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(context, false, "english/" + ENGLISH_TEXT_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_TEXT_MODEL_NAME);
    }

    /**
     * 将sample工程需要的资源文件拷贝到SD卡中使用（授权文件为临时授权文件，请注册正式授权）
     *
     * @param isCover 是否覆盖已存在的目标文件
     * @param source
     * @param dest
     */
    private void copyFromAssetsToSdcard(Context context, boolean isCover, String source, String dest) {
        File file = new File(dest);
        if (isCover || (!file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = context.getResources().getAssets().open(source);
                fos = new FileOutputStream(dest);
                byte[] buffer = new byte[1024];
                int size;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean makeDir(String dirPath) {
        File file = new File(dirPath);
        return file.exists() || file.mkdirs();
    }

    /**
     * 打印引擎so库版本号及基本信息和model文件的基本信息
     */
    private void printEngineInfo() {
        Log.d(TAG, "EngineVersioin=" + SynthesizerTool.getEngineVersion());
        Log.d(TAG, "EngineInfo=" + SynthesizerTool.getEngineInfo());
        String textModelInfo = SynthesizerTool.getModelInfo(mSampleDirPath + "/" + TEXT_MODEL_NAME);
        Log.d(TAG, "textModelInfo=" + textModelInfo);
        String speechModelInfo = SynthesizerTool.getModelInfo(mSampleDirPath + "/" + SPEECH_FEMALE_MODEL_NAME);
        Log.d(TAG, "speechModelInfo=" + speechModelInfo);
    }

    /**
     * 初始化成功后才有效
     */
    public int speak(String text) {
        if (this.mSpeechSynthesizer != null) {
            int result = this.mSpeechSynthesizer.speak(text);
            if (result < 0) {
                Log.e(TAG, "error" + result + ",please look up error code in doc or URL:http://yuyin.baidu.com/docs/tts/122 ");
            }
            return result;
        } else {
            Log.e(TAG, "error" + ",tts has not init");
            return -1;
        }
    }

    public void pause() {
        if (this.mSpeechSynthesizer != null) {
            this.mSpeechSynthesizer.pause();
        } else {
            Log.e(TAG, "pause error" + ",tts has not init");
        }
    }

    public void resume() {
        if (this.mSpeechSynthesizer != null) {
            this.mSpeechSynthesizer.resume();
        } else {
            Log.e(TAG, "resume error" + ",tts has not init");
        }
    }

    public void stop() {
        if (this.mSpeechSynthesizer != null) {
            this.mSpeechSynthesizer.stop();
        } else {
            Log.e(TAG, "stop error" + ",tts has not init");
        }
    }

    @Override
    public void onSynthesizeStart(String s) {
        Log.d(TAG, "Synthesize Start:" + s);
        if (speechSynthesizerListener != null) {
            speechSynthesizerListener.onSynthesizeStart(s);
        }
    }

    @Override
    public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {
        if (speechSynthesizerListener != null) {
            speechSynthesizerListener.onSynthesizeDataArrived(s, bytes, i);
        }
    }

    @Override
    public void onSynthesizeFinish(String s) {
        Log.d(TAG, "Synthesize Finish:" + s);
        if (speechSynthesizerListener != null) {
            speechSynthesizerListener.onSynthesizeFinish(s);
        }
    }

    @Override
    public void onSpeechStart(String s) {
        Log.d(TAG, "speak start:" + s);
        if (speechSynthesizerListener != null) {
            speechSynthesizerListener.onSpeechStart(s);
        }
    }

    @Override
    public void onSpeechProgressChanged(String s, int i) {
        if (speechSynthesizerListener != null) {
            speechSynthesizerListener.onSpeechProgressChanged(s, i);
        }
    }

    @Override
    public void onSpeechFinish(String s) {
        Log.d(TAG, "Speech Finish:" + s);
        if (speechSynthesizerListener != null) {
            speechSynthesizerListener.onSpeechFinish(s);
        }
    }

    @Override
    public void onError(String s, SpeechError speechError) {
        Log.e(TAG, "Error:" + s + ",code:" + speechError.code + ",description:" + speechError.description);
        if (speechSynthesizerListener != null) {
            speechSynthesizerListener.onError(s, speechError);
        }
    }
}
