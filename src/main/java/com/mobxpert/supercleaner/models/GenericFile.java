package com.mobxpert.supercleaner.models;

import java.io.File;

public class GenericFile {
    private File file;
    private boolean isSelected = false;

    public File getFile() {
        return this.file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }
}
