package com.zebra.jamesswinton.scantocloudwedge.data;

public class ScanEvent {

    public enum PostRequestState { COMPLETE, IN_PROGRESS, FAILED }

    private String source;
    private String labelType;
    private String dataString;
    private long timeStamp;
    private PostRequestState postRequestState;

    public ScanEvent(String dataString, String labelType, String source, long timeStamp,
                     PostRequestState postRequestState) {
        this.source = source;
        this.labelType = labelType;
        this.dataString = dataString;
        this.timeStamp = timeStamp;
        this.postRequestState = postRequestState;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getLabelType() {
        return labelType;
    }

    public void setLabelType(String labelType) {
        this.labelType = labelType;
    }

    public String getDataString() {
        return dataString;
    }

    public void setDataString(String dataString) {
        this.dataString = dataString;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public PostRequestState getPostRequestState() {
        return postRequestState;
    }

    public void setPostRequestState(PostRequestState postRequestState) {
        this.postRequestState = postRequestState;
    }
}
