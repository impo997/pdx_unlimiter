package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import javafx.scene.image.Image;

public class Eu4StabilityComp extends SimpleInfoComp {

    private int stability;

    @Override
    protected String getDisplayValue() {
        return (stability > 0 ? "+" : (stability < 0 ? "-" : "")) + stability;
    }

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        stability = (int) content.get().getNodeForKey("countries")
                .getNodeForKey(data.eu4().getTagName())
                .getNodeForKey("stability").getDouble();
    }

    @Override
    protected Image getImage() {
        return GameImage.EU4_ICON_STABILITY;
    }

    @Override
    protected String getTooltip() {
        return PdxuI18n.get("STABILITY");
    }
}
