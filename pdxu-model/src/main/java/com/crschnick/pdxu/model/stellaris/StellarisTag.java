package com.crschnick.pdxu.model.stellaris;

import com.crschnick.pdxu.io.node.Node;
import lombok.Setter;

public class StellarisTag {

    @Setter
    private String name;

    private String iconCategory;
    private String iconFile;

    private String backgroundCategory;
    private String backgroundFile;
    private String backgroundPrimaryColor;
    private String backgroundSecondaryColor;

    public StellarisTag(String name, String iconCategory, String iconFile, String backgroundCategory, String backgroundFile, String backgroundPrimaryColor, String backgroundSecondaryColor) {
        this.name = name;
        this.iconCategory = iconCategory;
        this.iconFile = iconFile;
        this.backgroundCategory = backgroundCategory;
        this.backgroundFile = backgroundFile;
        this.backgroundPrimaryColor = backgroundPrimaryColor;
        this.backgroundSecondaryColor = backgroundSecondaryColor;
    }

    public StellarisTag() {
    }

    public static StellarisTag fromNode(Node node) {
        Node flagNode = node.getNodeForKey("flag");
        StellarisTag tag = new StellarisTag();
        tag.name = "Unknown";

        Node icon = flagNode.getNodeForKey("icon");
        tag.iconCategory = icon.getNodeForKey("category").getString();
        tag.iconFile = icon.getNodeForKey("file").getString();

        Node bg = flagNode.getNodeForKey("background");
        tag.backgroundCategory = bg.getNodeForKey("category").getString();
        tag.backgroundFile = bg.getNodeForKey("file").getString();

        var colors = flagNode.getNodeForKey("colors").getNodeArray();
        tag.backgroundPrimaryColor = colors.get(0).getString();
        tag.backgroundSecondaryColor = colors.get(1).getString();
        return tag;
    }

    public String getName() {
        return name;
    }

    public String getIconCategory() {
        return iconCategory;
    }

    public String getIconFile() {
        return iconFile;
    }

    public String getBackgroundCategory() {
        return backgroundCategory;
    }

    public String getBackgroundFile() {
        return backgroundFile;
    }

    public String getBackgroundPrimaryColor() {
        return backgroundPrimaryColor;
    }

    public String getBackgroundSecondaryColor() {
        return backgroundSecondaryColor;
    }
}
