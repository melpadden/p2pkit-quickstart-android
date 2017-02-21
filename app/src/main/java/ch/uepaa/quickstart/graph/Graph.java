/**
 * Graph.java
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
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import ch.uepaa.quickstart.R;
import ch.uepaa.quickstart.utils.DroidUtils;

/**
 * Graph.
 * Created by uepaa on 09/02/16.
 * <p/>
 * Note: Graph layout algorithm based on zshiba's processing implementation.
 *
 * @link https://github.com/zshiba/visualization/tree/master/src/force_directed_graph
 */
public class Graph {

    private final String TAG = "Graph";

    private int width;
    private int height;

    private final Map<UUID, GraphNode> graphNodes;
    private final Map<UUID, GraphEdge> graphEdges;

    private UUID ownNodeId;
    private GraphNode selectedNode;

    private final float boundary;
    private final float nodeRadius;
    private final float nodeMass;
    private final String nodeOwnText;

    private final float springConstant;
    private final float coulombConstant;
    private final float dampingCoefficient;
    private final float timeStep;

    private final Paint paintBg;
    private final int colorNode;
    private final int colorEdge;
    private final int colorActive;
    private final float strokeEdge;

    public Graph(final Context context) {
        Resources res = context.getResources();

        this.width = 1;
        this.height = 1;

        this.graphNodes = new ConcurrentHashMap<>();
        this.graphEdges = new ConcurrentHashMap<>();

        this.ownNodeId = UUID.randomUUID();
        this.selectedNode = null;

        this.springConstant = DroidUtils.getFloatConstant(res, R.dimen.graph_spring_constant);
        this.coulombConstant = DroidUtils.getFloatConstant(res, R.dimen.graph_coulomb_constant);
        this.dampingCoefficient = DroidUtils.getFloatConstant(res, R.dimen.graph_damping_coefficient);
        this.timeStep = DroidUtils.getFloatConstant(res, R.dimen.graph_time_step);

        this.boundary = (float) (res.getDimension(R.dimen.activity_horizontal_margin) * 0.5);
        this.nodeRadius = res.getDimension(R.dimen.graph_node_radius);
        this.nodeMass = DroidUtils.getFloatConstant(res, R.dimen.graph_node_mass);
        this.nodeOwnText = res.getString(R.string.node_me);

        this.paintBg = new Paint();
        this.paintBg.setColor(res.getColor(R.color.graph_bg));
        this.paintBg.setStyle(Paint.Style.FILL);

        this.colorNode = res.getColor(R.color.graph_node);
        this.colorEdge = res.getColor(R.color.graph_edge);
        this.colorActive = res.getColor(R.color.graph_active);

        this.strokeEdge = res.getDimension(R.dimen.graph_edge_stroke);
    }

    public void setup(final UUID ownNodeId) {
        Log.i(TAG, "setup | ownNodeId = " + ownNodeId);

        this.ownNodeId = ownNodeId;

        clear();
    }

    public void addNode(final UUID id) {
        Log.i(TAG, "addNode | id = " + id);

        if (id != null && !graphNodes.containsKey(id)) {

            GraphNode node = new GraphNode(id, 0, 0, nodeRadius);
            node.setRandomPosition(width, height);
            node.setMass(nodeMass);
            node.setColor(colorNode);
            node.setStrokeWidth(2 * strokeEdge);
            node.setStrokeColor(colorActive);
            node.setShowStroke(false);

            graphNodes.put(id, node);

            if (id.equals(ownNodeId)) {

                node.setLabel(nodeOwnText);
                node.setRadius(nodeRadius * 1.2f);
                node.setMass(nodeMass * 1.2f);

            } else {

                GraphNode ownNode = findNodeById(ownNodeId);
                if (ownNode != null) {

                    GraphEdge edge = new GraphEdge(ownNode, node);
                    edge.setColor(colorEdge);
                    edge.setStroke(strokeEdge);
                    edge.setMaxLength(Math.min(width - 2 * boundary, height - 2 * boundary));

                    graphEdges.put(id, edge);
                }
            }
        }
    }

    public void removeNode(final UUID id) {
        Log.i(TAG, "removeNode | id = " + id);

        if (graphNodes.containsKey(id)) {

            graphNodes.remove(id);

            List<GraphEdge> nodeEdges = findEdgesById(id);
            graphEdges.values().removeAll(nodeEdges);
        }
    }

    public void setEdgeStrength(final UUID id, final float strength) {
        GraphEdge edge = findEdgeById(id);
        if (edge != null) {
            edge.setStrength(strength);
            edge.setDashed(strength < 0);
        }
    }

    public void setNodeColor(final UUID id, final int colorCode) {
        Log.v(TAG, "setNodeColor | id = " + id);

        GraphNode node = findNodeById(id);
        if (node != null) {
            node.setColor(colorCode);
        }
    }

    public void setHighlighted(final UUID id, final boolean highlighted) {
        setNodeHighlighted(id, highlighted);
        setEdgeHighlighted(id, highlighted);

        updateOwnNode();
    }

    private void setEdgeHighlighted(final UUID id, final boolean highlighted) {
        GraphEdge edge = findEdgeById(id);
        if (edge != null) {
            edge.setStrokeWidth(highlighted ? strokeEdge * 2.0f : strokeEdge);
            edge.setColor(highlighted ? colorActive : colorEdge);
        }
    }

    private void setNodeHighlighted(final UUID id, final boolean highlighted) {
        GraphNode node = findNodeById(id);
        if (node != null) {
            node.setShowStroke(highlighted);
        }
    }

    public void updateOwnNode() {
        boolean hasStrokedNode = false;
        for (GraphNode node : graphNodes.values()) {
            if (!node.getId().equals(ownNodeId) && node.isShowStroke()) {
                hasStrokedNode = true;
                break;
            }
        }

        setNodeHighlighted(ownNodeId, hasStrokedNode);
    }

    public void reset() {
        Log.i(TAG, "reset");

        clear();
    }

    private void clear() {
        graphEdges.clear();
        graphNodes.clear();
    }

    public void update() {
        Collection<GraphNode> nodes = getNodeList();
        for (GraphNode target : nodes) {

            if (target == selectedNode) {
                continue;
            }

            // Coulomb's law (repulse)
            float forceX = 0.0f;
            float forceY = 0.0f;
            for (GraphNode node : nodes) {

                if (!node.equals(target)) {
                    float dx = target.getX() - node.getX();
                    float dy = target.getY() - node.getY();
                    float rSquared = dx * dx + dy * dy + 0.0001f; // to avoid zero deviation
                    float coulombForceX = coulombConstant * dx / rSquared;
                    float coulombForceY = coulombConstant * dy / rSquared;
                    forceX += coulombForceX;
                    forceY += coulombForceY;
                }
            }

            // Hooke's law (attract)
            // todo consider using only one edge in case of memory/performance issues: GraphEdge edge = findEdgeById(target.getId());
            List<GraphEdge> adjacent = findEdgesById(target.getId());
            for (int j = 0; j < adjacent.size(); j++) {
                GraphEdge edge = adjacent.get(j);

                GraphNode node = edge.getAdjacent(target);

                float springLength = edge.getLength();
                float dx = node.getX() - target.getX();
                float dy = node.getY() - target.getY();

                float l = (float) Math.sqrt(dx * dx + dy * dy) + 0.0001f; //to avoid zero deviation
                float springLengthX = springLength * dx / l;
                float springLengthY = springLength * dy / l;
                float springForceX = springConstant * (dx - springLengthX);
                float springForceY = springConstant * (dy - springLengthY);

                forceX += springForceX;
                forceY += springForceY;
            }

            float accelerationX = forceX / target.getMass();
            float accelerationY = forceY / target.getMass();

            float velocityX = (target.getVelocityX() + timeStep * accelerationX) * dampingCoefficient;
            float velocityY = (target.getVelocityY() + timeStep * accelerationY) * dampingCoefficient;

            float x = (float) (target.getX() + timeStep * velocityX + accelerationX * Math.pow(timeStep, 2.0f) / 2.0f);
            float y = (float) (target.getY() + timeStep * velocityY + accelerationY * Math.pow(timeStep, 2.0f) / 2.0f);

            // boundary check
            float radius = target.getRadius();
            if (x < radius + boundary) {
                x = radius + boundary;
            } else if (x > width - boundary - radius) {
                x = width - boundary - radius;
            }
            if (y < radius + boundary) {
                y = radius + boundary;
            } else if (y > height - boundary - radius) {
                y = height - boundary - radius;
            }

            target.setPosition(x, y);
            target.setVelocity(velocityX, velocityY);
        }
    }

    public void draw(final Canvas canvas) {
        canvas.drawRect(0, 0, width, height, paintBg);

        for (GraphEdge edge : graphEdges.values()) {
            edge.draw(canvas);
        }

        for (GraphNode node : graphNodes.values()) {
            node.draw(canvas);
        }
    }

    public void setSize(final int width, final int height) {
        this.width = width;
        this.height = height;

        for (GraphNode node : graphNodes.values()) {
            if (!node.isPositioned()) {
                node.setRandomPosition(width, height);
            }
        }

        for (GraphEdge edge : graphEdges.values()) {
            edge.setMaxLength(Math.min(width - 2 * boundary, height - 2 * boundary));
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                selectedNode = findTouchedNode(event.getX(), event.getY());
                if (selectedNode != null) {
                    selectedNode.setTouched(true);
                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (selectedNode != null) {
                    selectedNode.setPosition(event.getX(), event.getY());
                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (selectedNode != null) {
                    selectedNode.setTouched(false);
                    selectedNode = null;
                    return true;
                }
                break;
            }
        }

        return false;
    }

    private Collection<GraphNode> getNodeList() {
        return this.graphNodes.values();
    }

    private GraphNode findNodeById(final UUID id) {
        return graphNodes.get(id);
    }

    private GraphEdge findEdgeById(final UUID id) {
        return graphEdges.get(id);
    }

    private List<GraphEdge> findEdgesById(final UUID id) {
        List<GraphEdge> matching = new ArrayList<>();

        for (GraphEdge edge : graphEdges.values()) {
            if (edge.matchesNodeId(id)) {
                matching.add(edge);
            }
        }

        return matching;
    }

    private GraphNode findTouchedNode(final float x, final float y) {
        for (GraphNode node : graphNodes.values()) {
            if (node.isTouched(x, y)) {
                return node;
            }
        }

        return null;
    }

    @SuppressWarnings("unused")
    public void debug() {
        Log.i(TAG, "debug");

        UUID ownId = UUID.randomUUID();
        setup(ownId);

        addNode(ownId);
        assertGraph(1, 0, "Node own should be added.");

        UUID node1Id = UUID.randomUUID();
        addNode(node1Id);
        setEdgeStrength(node1Id, 1.0f);
        assertGraph(2, 1, "Node 1 should be added.");

        UUID node2Id = UUID.randomUUID();
        addNode(node2Id);
        setEdgeStrength(node2Id, 0.0f);
        assertGraph(3, 2, "Node 2 should be added.");

        addNode(node2Id);
        assertGraph(3, 2, "Node 2 dublicate should not be added.");

        UUID node3Id = UUID.randomUUID();
        addNode(node3Id);
        assertGraph(4, 3, "Node 3 should be added.");

        removeNode(node3Id);
        assertGraph(3, 2, "Node 3 should be removed.");

        UUID node4Id = UUID.randomUUID();
        addNode(node4Id);
        setEdgeStrength(node4Id, 0.5f);
        assertGraph(4, 3, "Node 4 should be removed.");

        UUID node5Id = UUID.randomUUID();
        addNode(node5Id);
        setEdgeStrength(node5Id, 0.8f);
        assertGraph(5, 4, "Node 5 should be removed.");

        UUID node6Id = UUID.randomUUID();
        addNode(node6Id);
        assertGraph(6, 5, "Node 6 should be removed.");
    }

    private void assertGraph(final int nbOfNodes, final int nbOfEdges, final String msg) {
        if (graphNodes.size() != nbOfNodes || graphEdges.size() != nbOfEdges) {
            throw new AssertionError(msg);
        }
    }
}
