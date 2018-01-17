package com.xw.ext;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class ApiInfo implements Serializable,Parcelable{

    private String appId;
    private String apiKey;
    private String secretKey;

    protected ApiInfo(Parcel in) {
        appId = in.readString();
        apiKey = in.readString();
        secretKey = in.readString();
    }

    public static final Creator<ApiInfo> CREATOR = new Creator<ApiInfo>() {
        @Override
        public ApiInfo createFromParcel(Parcel in) {
            return new ApiInfo(in);
        }

        @Override
        public ApiInfo[] newArray(int size) {
            return new ApiInfo[size];
        }
    };

    public String getAppId() {
        return appId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public ApiInfo(String appId, String apiKey, String secretKey) {
        this.appId = appId;
        this.apiKey = apiKey;
        this.secretKey = secretKey;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(appId);
        dest.writeString(apiKey);
        dest.writeString(secretKey);
    }
}