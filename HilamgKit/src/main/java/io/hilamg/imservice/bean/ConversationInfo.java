package io.hilamg.imservice.bean;

import java.io.Serializable;

public class ConversationInfo implements Serializable {
    //阅后即焚 时间
    private int messageBurnTime = -1;

    //截屏通知开关
    private int captureScreenEnabled = 0;

    public int getMessageBurnTime() {
        return messageBurnTime;
    }

    public void setMessageBurnTime(int messageBurnTime) {
        this.messageBurnTime = messageBurnTime;
    }

    public int getCaptureScreenEnabled() {
        return captureScreenEnabled;
    }

    public void setCaptureScreenEnabled(int captureScreenEnabled) {
        this.captureScreenEnabled = captureScreenEnabled;
    }

}
