/**
 * GraphView.java
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

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Graph view.
 * Created by uepaa on 09/02/16.
 */
public class GraphView extends SurfaceView implements SurfaceHolder.Callback {

    private GraphThread thread;

    private final Graph graph;

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);

        graph = new Graph(context);

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        setWillNotDraw(false);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (thread == null) {

            thread = new GraphThread(holder, graph);
            thread.setRunning(true);
            thread.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        graph.setSize(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        thread.setRunning(false);

        while (retry) {
            try {
                thread.join();
                retry = false;
                thread = null;
            } catch (InterruptedException e) {
                Log.d("GraphView", "Interrupted Exception", e);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return graph.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        graph.draw(canvas);
    }

    public Graph getGraph() {
        return graph;
    }

    private class GraphThread extends Thread {

        private final SurfaceHolder holder;
        private final Graph graph;

        private boolean running = false;

        public GraphThread(SurfaceHolder holder, Graph graph) {
            this.holder = holder;
            this.graph = graph;
        }

        @Override
        public void run() {
            Canvas canvas = null;
            while (running) {
                graph.update();
                try {
                    canvas = holder.lockCanvas(null);
                    synchronized (holder) {
                        postInvalidate();
                    }
                } finally {
                    if (canvas != null) {
                        holder.unlockCanvasAndPost(canvas);
                    }
                }
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    // ignore
                }
            }

        }

        public void setRunning(boolean b) {
            running = b;
        }
    }

}
