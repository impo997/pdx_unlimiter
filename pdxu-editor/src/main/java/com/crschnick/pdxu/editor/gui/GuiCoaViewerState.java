package com.crschnick.pdxu.editor.gui;


import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.editor.EditorState;
import com.crschnick.pdxu.editor.node.EditorRealNode;
import com.crschnick.pdxu.io.node.LinkedArrayNode;
import com.crschnick.pdxu.io.node.NodeEvaluator;
import com.crschnick.pdxu.model.CoatOfArms;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;


public abstract class GuiCoaViewerState<T extends GuiCoaDisplayType> {

    public static class Ck3GuiCoaViewerState extends GuiCoaViewerState<GuiCk3CoaDisplayType> {

        public Ck3GuiCoaViewerState(EditorState state, EditorRealNode editorNode) {
            super(state, editorNode, GuiCk3CoaDisplayType.REALM);
        }

        @Override
        protected void setup(HBox box) {
            GuiCk3CoaDisplayType.init(this, box);
        }
    }

    public static class Vic3GuiCoaViewerState extends GuiCoaViewerState<GuiVic3CoaDisplayType> {

        public Vic3GuiCoaViewerState(EditorState state, EditorRealNode editorNode) {
            super(state, editorNode, GuiVic3CoaDisplayType.NONE);
        }

        @Override
        protected void setup(HBox box) {
            GuiVic3CoaDisplayType.init(this, box);
        }
    }

    private ObjectProperty<T> displayType;
    protected EditorState state;
    private EditorRealNode editorNode;
    private ObjectProperty<CoatOfArms> parsedCoa;
    private ObjectProperty<Image> image;

    GuiCoaViewerState(EditorState state, EditorRealNode editorNode, T initial) {
        this.state = state;
        this.editorNode = editorNode;
        this.displayType = new SimpleObjectProperty<>(initial);
        this.image = new SimpleObjectProperty<>(ImageHelper.DEFAULT_IMAGE);
        this.parsedCoa = new SimpleObjectProperty<>(createCoatOfArms());
    }

    void init(HBox box) {
        setup(box);
        refresh();
    }

    protected abstract void setup(HBox box);

    private CoatOfArms createCoatOfArms() {
        // Only evaluate for game date are files
        var node = new LinkedArrayNode(
                state.isSavegame() ?
                        state.getRootNodes().values().stream().map(editorRootNode -> editorRootNode.getBackingNode().getArrayNode()).toList() :
                        state.getRootNodes().values().stream().map(editorRootNode -> editorRootNode.getBackingNode().copy().getArrayNode()).peek(
                                NodeEvaluator::evaluateArrayNode).toList());
        var coatOfArmsNode = editorNode.getBackingNode().copy();
        NodeEvaluator.evaluateArrayNode(coatOfArmsNode.getArrayNode());
        return CoatOfArms.fromNode(coatOfArmsNode, s -> node.getNodeForKeyIfExistent(s).orElse(null));
    }

    void refresh() {
        // The data might not be valid anymore
        if (!editorNode.isValid()) {
            return;
        }

        parsedCoa.set(createCoatOfArms());
        updateImage();
    }

    void updateImage() {
        if (parsedCoa.get() == null) {
            image.set(ImageHelper.DEFAULT_IMAGE);
        } else {
            image.set(displayType.get().render(parsedCoa.get(), state.getFileContext()));
        }
    }

    public Image getImage() {
        return image.get();
    }

    public ObjectProperty<Image> imageProperty() {
        return image;
    }

    public T getDisplayType() {
        return displayType.get();
    }

    public ObjectProperty<T> displayTypeProperty() {
        return displayType;
    }
}
