/**
 * GraphNode.java
 * Kanka-quickstart-android
 * <p/>
 * Created by uepaa on 09/02/16.
 * <p/>
 * <p/>
 * Copyright (c) 2016 by Uepaa AG, Zürich, Switzerland.
 * All rights reserved.
 * <p/>
 * We reserve all rights in this document and in the information contained therein.
 * Reproduction, use, transmission, dissemination or disclosure of this document and/or
 * the information contained herein to third parties in part or in whole by any means
 * is strictly prohibited, unless prior written permission is obtained from Uepaa AG.
 */
package ch.uepaa.quickstart.graph;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.UUID;

/**
 * Graph node.
 * Created by uepaa on 09/02/16.
 */
public class GraphNode {

    private final UUID id;

    private float px;
    private float py;
    private float vx;
    private float vy;

    private float radius;
    private float mass;

    private boolean touched;

    private final Paint circlePaint;
    private final Paint strokePaint;
    private final Paint labelPaint;

    private String label;
    private boolean showStroke = false;

    public GraphNode(final UUID id, final float px, final float py, final float radius) {

        this.id = id;

        this.px = px;
        this.py = py;
        this.radius = radius;

        this.vx = 0;
        this.vy = 0;

        this.mass = 20.0f;

        this.touched = false;

        this.circlePaint = new Paint();
        this.circlePaint.setStyle(Paint.Style.FILL);
        this.circlePaint.setColor(Color.GRAY);

        this.strokePaint = new Paint();
        this.strokePaint.setStyle(Paint.Style.STROKE);
        this.strokePaint.setColor(Color.GRAY);
        this.strokePaint.setStrokeWidth(1f);

        this.labelPaint = new Paint();
        this.labelPaint.setStyle(Paint.Style.FILL);
        this.labelPaint.setColor(Color.WHITE);
        this.labelPaint.setTextSize(32);

        this.label = "";
    }

    public UUID getId() {
        return this.id;
    }

    public float getX() {
        return this.px;
    }

    public float getY() {
        return this.py;
    }

    public void setRadius(final float radius) {
        this.radius = radius;
        this.labelPaint.setTextSize(radius * 0.6f);
    }

    public float getRadius() {
        return this.radius;
    }

    public float getVelocityX() {
        return this.vx;
    }

    public float getVelocityY() {
        return this.vy;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public float getMass() {
        return this.mass;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public void setColor(final int colorCode) {
        this.circlePaint.setColor(colorCode);
    }

    public void draw(final Canvas canvas) {
        circlePaint.setAlpha(touched ? 240 : 255);
        canvas.drawCircle(px, py, radius, circlePaint);
        if (showStroke) {
            canvas.drawCircle(px, py, radius, strokePaint);
        }

        Rect bounds = new Rect();
        labelPaint.getTextBounds(label, 0, label.length(), bounds);
        canvas.drawText(label, px - bounds.width() / 2 - 2, py + bounds.height() / 2, labelPaint);
    }

    public void setPosition(final float x, final float y) {
        this.px = x;
        this.py = y;
    }

    public void setRandomPosition(final float width, final float height) {
        this.setPosition((float) (Math.random() * width), (float) (Math.random() * height));
    }

    public void setVelocity(float vx, float vy) {
        this.vx = vx;
        this.vy = vy;
    }

    public boolean isTouched(final float x, final float y) {
        double dist = Math.sqrt(Math.pow(x - this.px, 2) + Math.pow(y - this.py, 2));
        return dist < this.radius * 1.2;
    }

    public void setTouched(final boolean touched) {
        this.touched = touched;
    }

    public boolean isPositioned() {
        return (this.px > this.radius && this.py > this.radius);
    }

    public void setShowStroke(boolean showStroke) {
        this.showStroke = showStroke;
    }

    public void setStrokeColor(final int color) {
        this.strokePaint.setColor(color);
    }

    public void setStrokeWidth(final float width) {
        this.strokePaint.setStrokeWidth(width);
    }

    public boolean isShowStroke() {
        return this.showStroke;
    }
}
