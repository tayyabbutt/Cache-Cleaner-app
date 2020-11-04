package com.mobxpert.supercleaner.managers;


import com.mobxpert.supercleaner.models.GenericFile;

import java.util.ArrayList;

public class ContentManager {
    private static ContentManager manager;
    private ArrayList<GenericFile> content;
    private ContentType contentType = ContentType.DEFAULT;

    public enum ContentType {
        DEFAULT,
        IMAGES,
        AUDIOS,
        VIDEOS,
        DOCUMENTS,
        OTHERS
    }

    private ContentManager() {
    }

    public static ContentManager getInstance() {
        if (manager == null) {
            manager = new ContentManager();
        }
        return manager;
    }

    public void resetContentList() {
        if (this.content != null) {
            this.content.clear();
        }
        this.content = null;
    }

    public void setGenericFiles(ArrayList<GenericFile> content) {
        if (this.content != null) {
            this.content.clear();
        }
        this.content = content;
    }

    public ArrayList<GenericFile> getContent() {
        return this.content;
    }

    public ContentType getContentType() {
        return this.contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }
}
