package org.qxn.visual.gui;

import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;

public abstract class Component implements Serializable {

    protected boolean isGate = false;

    public int getSpan() {
        return span;
    }

    protected int span;

    public Component(int span) {
        this.span = span;
    }

    public boolean isGate() {
        return isGate;
    }

    public abstract void draw(double x, double y, GraphicsContext graphicsContext);
    public abstract void cleanUp();

}
