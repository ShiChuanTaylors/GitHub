package com.speed.traquer.app;

/**
 * Created by ting on 12/8/14.
 */
public class SuggestGetSet {

    private String keyWord;
    public SuggestGetSet(String keyWord){
        this.setLocation(keyWord);
    }

    public String getLocation() {
        return keyWord;
    }

    public void setLocation(String keyWord) {
        this.keyWord = keyWord;
    }

}
