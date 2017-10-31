/*
 * Copyright (c) 2017 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.tilesfx.tools;

import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.events.TreeNodeEvent;
import eu.hansolo.tilesfx.events.TreeNodeEvent.EventType;
import eu.hansolo.tilesfx.events.TreeNodeEventListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class TreeNode {
    private final TreeNodeEvent PARENT_CHANGED   = new TreeNodeEvent(TreeNode.this, EventType.PARENT_CHANGED);
    private final TreeNodeEvent CHILDREN_CHANGED = new TreeNodeEvent(TreeNode.this, EventType.CHILDREN_CHANGED);
    private ChartData                   data;
    private TreeNode                    parent;
    private TreeNode                    myRoot;
    private TreeNode                    treeRoot;
    private int                         depth;
    private ObservableList<TreeNode>    children;
    private List<TreeNodeEventListener> listeners;


    // ******************** Constructors **************************************
    public TreeNode(final ChartData DATA) {
        this(DATA, null);
    }
    public TreeNode(final ChartData DATA, final TreeNode PARENT) {
        data      = DATA;
        parent    = PARENT;
        depth     = -1;
        children  = FXCollections.observableArrayList();
        listeners = new CopyOnWriteArrayList<>();
        init();
    }


    // ******************** Methods *******************************************
    private void init() {
        // Add this node to parents children
        if (null != parent) { parent.getChildren().add(this); }

        children.addListener((ListChangeListener<TreeNode>) c -> {
            while (c.next()) {
                if (c.wasRemoved()) { c.getRemoved().forEach(removedItem -> removedItem.removeAllTreeNodeEventListeners()); }
            }
            getTreeRoot().fireTreeNodeEvent(CHILDREN_CHANGED);
        });
    }

    public boolean isRoot() { return null == parent; }
    public boolean isLeaf() { return (null == children || children.isEmpty()); }
    public boolean hasParent() { return null != parent; }
    public void removeParent() {
        parent   = null;
        myRoot   = null;
        treeRoot = null;
        depth    = -1;
        getTreeRoot().fireTreeNodeEvent(PARENT_CHANGED);
    }

    public TreeNode getParent() { return parent; }
    public void setParent(final TreeNode PARENT) {
        if (null != PARENT) { PARENT.addNode(TreeNode.this); }
        parent   = PARENT;
        myRoot   = null;
        treeRoot = null;
        depth    = -1;
        getTreeRoot().fireTreeNodeEvent(PARENT_CHANGED);
    }

    public ChartData getData() { return data; }
    public void setData(final ChartData DATA) { data = DATA; }

    public List<TreeNode> getChildrenUnmodifiable() { return Collections.unmodifiableList(children); }
    public List<TreeNode> getChildren() { return children; }
    public void setChildren(final List<TreeNode> CHILDREN) { children.setAll(new LinkedHashSet<>(CHILDREN)); }

    public void addNode(final ChartData DATA) {
        TreeNode child = new TreeNode(DATA);
        child.setParent(this);
        children.add(child);
    }
    public void addNode(final TreeNode NODE) {
        if (children.contains(NODE)) { return; }
        NODE.setParent(this);
        children.add(NODE);
    }
    public void removeNode(final TreeNode NODE) { if (children.contains(NODE)) { children.remove(NODE); } }

    public void addNodes(final TreeNode... NODES) { addNodes(Arrays.asList(NODES)); }
    public void addNodes(final List<TreeNode> NODES) { NODES.forEach(node -> addNode(node)); }

    public void removeNodes(final TreeNode... NODES) { removeNodes(Arrays.asList(NODES)); }
    public void removeNodes(final List<TreeNode> NODES) { NODES.forEach(node -> removeNode(node)); }

    public void removeAllNodes() { children.clear(); }

    public Stream<TreeNode> stream() {
        if (isLeaf()) {
            return Stream.of(this);
        } else {
            return getChildren().stream()
                                .map(child -> child.stream())
                                .reduce(Stream.of(this), (s1, s2) -> Stream.concat(s1, s2));
        }
    }
    public Stream<TreeNode> lazyStream() {
        if (isLeaf()) {
            return Stream.of(this);
        } else {
            return Stream.concat(Stream.of(this), getChildren().stream().flatMap(TreeNode::stream));
        }
    }

    public Stream<TreeNode> flattened() { return Stream.concat(Stream.of(this), children.stream().flatMap(TreeNode::flattened)); }
    public List<TreeNode> getAll() { return flattened().collect(Collectors.toList()); }
    public List<ChartData> getAllData() { return flattened().map(TreeNode::getData).collect(Collectors.toList()); }

    public int getNoOfNodes() { return flattened().map(TreeNode::getData).collect(Collectors.toList()).size(); }
    public int getNoOfLeafNodes() { return flattened().filter(node -> node.isLeaf()).map(TreeNode::getData).collect(Collectors.toList()).size(); }

    public boolean contains(final TreeNode NODE) { return flattened().anyMatch(n -> n.equals(NODE)); }
    public boolean containsData(final ChartData DATA) { return flattened().anyMatch(n -> n.data.equals(DATA)); }

    public TreeNode getMyRoot() {
        if (null == myRoot) {
            if (null != getParent() && getParent().isRoot()) {
                myRoot = this;
            } else {
                myRoot = getMyRoot(getParent());
            }
        }
        return myRoot;
    }
    private TreeNode getMyRoot(final TreeNode NODE) {
        if (NODE.getParent().isRoot()) { return NODE; }
        return getMyRoot(NODE.getParent());
    }

    public TreeNode getTreeRoot() {
        if (null == treeRoot) {
            if (isRoot()) {
                treeRoot = this;
            } else {
                treeRoot = getTreeRoot(getParent());
            }
        }
        return treeRoot;
    }
    private TreeNode getTreeRoot(final TreeNode NODE) {
        if (NODE.isRoot()) { return NODE; }
        return getTreeRoot(NODE.getParent());
    }

    public int getDepth() {
        if (depth == -1) {
            if (isRoot()) {
                depth = 0;
            } else {
                depth = getDepth(getParent(), 0);
            }
        }
        return depth;
    }
    private int getDepth(final TreeNode NODE, int depth) {
        depth++;
        if (NODE.isRoot()) { return depth; }
        return getDepth(NODE.getParent(), depth);
    }

    public int getMaxLevel() { return getTreeRoot().stream().map(TreeNode::getDepth).max(Comparator.naturalOrder()).orElse(0); }

    public double getPercentage() {
        List<TreeNode> siblings   = getSiblings();
        double         sum        = siblings.stream().map(node -> node.getData()).mapToDouble(ChartData::getValue).sum();
        return Double.compare(sum, 0) == 0 ? 1.0 : getData().getValue() / sum;
    }

    public List<TreeNode> getSiblings() { return null == getParent() ? new ArrayList<>() : getParent().getChildren(); }

    public List<TreeNode> nodesAtSameLevel() {
        final int LEVEL = getDepth();
        return getTreeRoot().stream().filter(node -> node.getDepth() == LEVEL).collect(Collectors.toList());
    }

    public double getParentAngle() {
        List<TreeNode> parentList = new ArrayList<>();
        TreeNode node = TreeNode.this;
        while (!node.getParent().isRoot()) {
            node = node.getParent();
            parentList.add(node);
        }
        Collections.reverse(parentList);
        double parentAngle = 360.0;
        for (TreeNode n : parentList) { parentAngle = parentAngle * n.getPercentage(); }
        return parentAngle;
    }


    // ******************** Event handling ************************************
    public void setOnTreeNodeEvent(final TreeNodeEventListener LISTENER) { addTreeNodeEventListener(LISTENER); }
    public void addTreeNodeEventListener(final TreeNodeEventListener LISTENER) { if (!listeners.contains(LISTENER)) listeners.add(LISTENER); }
    public void removeTreeNodeEventListener(final TreeNodeEventListener LISTENER) { if (listeners.contains(LISTENER)) listeners.remove(LISTENER); }
    public void removeAllTreeNodeEventListeners() { listeners.clear(); }

    public void fireTreeNodeEvent(final TreeNodeEvent EVENT) {
        for (TreeNodeEventListener listener : listeners) { listener.onTreeNodeEvent(EVENT); }
    }

}
