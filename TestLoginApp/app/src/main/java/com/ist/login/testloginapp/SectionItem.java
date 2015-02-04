package com.ist.login.testloginapp;

/**
 * Created by Nouman on 1/22/2015.
 */
public class SectionItem implements Item {
    @Override
    public boolean isSection() {
        return true;
    }
    private final String title;

    public SectionItem(String title) {
        this.title = title;
    }

    public String getTitle(){
        return title;
    }
}
