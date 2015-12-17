package org.ifollowyou.saber.model;

public class JarFileItem {
    private String jarPath;
    private String itemPath;
    private String itemName;
    private String itemContent;

    public JarFileItem(String jarPath, String itemPath) {
        this.jarPath = jarPath;
        this.itemPath = itemPath;
    }

    public String getJarPath() {
        return jarPath;
    }

    public String getItemPath() {
        return itemPath;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemContent() {
        return itemContent;
    }

}