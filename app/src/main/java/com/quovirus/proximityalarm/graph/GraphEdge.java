/**
 * GraphEdge.java
 * Kanka-quickstart-android
 * <p/>
 * Created by uepaa on 10/02/16.
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
package com.quovirus.proximityalarm.graph;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.UUID;

/**
 * Graph edge.
 * Created by uepaa on 10/02/16.
 */
public class GraphEdge {

    private final GraphNode node1;
    private final GraphNode node2;
    private final Path path;

    private float length;
    private float strength;

    private final Paint paint;

    public GraphEdge(final GraphNode node1, final GraphNode node2) {
        this.node1 = node1;
        this.node2 = node2;
        this.length = 240;
        this.strength = 1.0f;
        this.path = new Path();

        this.paint = new Paint();
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setColor(Color.GRAY);
    }

    public void setColor(final int colorCode) {
        this.paint.setColor(colorCode);
    }

    public void setStroke(final float width) {
        this.paint.setStrokeWidth(width);
    }

    public void setDashed(final boolean dashed) {
        float dash = paint.getStrokeWidth() * 2;
        paint.setPathEffect(dashed ? new DashPathEffect(new float[]{dash, dash}, 0) : null);
    }

    public void draw(final Canvas canvas) {
        path.reset();
        path.moveTo(node1.getX(), node1.getY());
        path.lineTo(node2.getX(), node2.getY());

        canvas.drawPath(path, paint);
    }

    public float getLength() {
        return this.length * this.strength + (node1.getRadius() + node2.getRadius());
    }

    public void setMaxLength(final float size) {
        this.length = (size * 0.5f - 1.2f * (node1.getRadius() + node2.getRadius()));
    }

    public void setStrength(float strength) {
        this.strength = 1.0f - (float) Math.max(Math.min(strength, 1.0), 0);
    }

    public void setStrokeWidth(final float width) {
        paint.setStrokeWidth(width);
    }

    public GraphNode getAdjacent(final GraphNode node) {
        return node1.equals(node) ? node2 : node1;
    }

    public boolean matchesNodeId(final UUID id) {
        return (node1.getId().equals(id) || node2.getId().equals(id));
    }
}
