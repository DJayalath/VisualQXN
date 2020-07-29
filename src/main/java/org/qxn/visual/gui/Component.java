package org.qxn.visual.gui;

import javafx.scene.canvas.GraphicsContext;

public abstract class Component {

    protected int row;
    protected int col;

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getSpan() {
        return span;
    }

    protected int span;

    public Component(int row, int col, int span) {
        this.row = row;
        this.col = col;
        this.span = span;
    }

    public abstract void draw(GraphicsContext graphicsContext);

}
