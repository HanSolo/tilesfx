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


public class TreeNode<T> {
    private final TreeNodeEvent         PARENT_CHANGED   = new TreeNodeEvent(TreeNode.this, EventType.PARENT_CHANGED);
    private final TreeNodeEvent         CHILDREN_CHANGED = new TreeNodeEvent(TreeNode.this, EventType.CHILDREN_CHANGED);
    private T                           item;
    private TreeNode                    parent;
    private TreeNode                    myRoot;
    private TreeNode                    treeRoot;
    private int                         depth;
    private ObservableList<TreeNode<T>> children;
    private List<TreeNodeEventListener> listeners;


    // ******************** Constructors **************************************
    public TreeNode(final T ITEM) {
        this(ITEM, null);
    }
    public TreeNode(final T ITEM, final TreeNode<T> PARENT) {
        item      = ITEM;
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

    public TreeNode<T> getParent() { return parent; }
    public void setParent(final TreeNode<T> PARENT) {
        if (null != PARENT) { PARENT.addNode(TreeNode.this); }
        parent   = PARENT;
        myRoot   = null;
        treeRoot = null;
        depth    = -1;
        getTreeRoot().fireTreeNodeEvent(PARENT_CHANGED);
    }

    public T getItem() { return item; }
    public void setItem(final T ITEM) { item = ITEM; }

    public List<TreeNode<T>> getChildrenUnmodifiable() { return Collections.unmodifiableList(children); }
    public List<TreeNode<T>> getChildren() { return children; }
    public void setChildren(final List<TreeNode<T>> CHILDREN) { children.setAll(new LinkedHashSet<>(CHILDREN)); }

    public void addNode(final T ITEM) {
        TreeNode<T> child = new TreeNode<>(ITEM);
        child.setParent(this);
        children.add(child);
    }
    public void addNode(final TreeNode<T> NODE) {
        if (children.contains(NODE)) { return; }
        NODE.setParent(this);
        children.add(NODE);
    }
    public void removeNode(final TreeNode<T> NODE) { if (children.contains(NODE)) { children.remove(NODE); } }

    public void addNodes(final TreeNode<T>... NODES) { addNodes(Arrays.asList(NODES)); }
    public void addNodes(final List<TreeNode<T>> NODES) { NODES.forEach(node -> addNode(node)); }

    public void removeNodes(final TreeNode<T>... NODES) { removeNodes(Arrays.asList(NODES)); }
    public void removeNodes(final List<TreeNode<T>> NODES) { NODES.forEach(node -> removeNode(node)); }

    public void removeAllNodes() { children.clear(); }

    public Stream<TreeNode<T>> stream() {
        if (isLeaf()) {
            return Stream.of(this);
        } else {
            return getChildren().stream()
                                .map(child -> child.stream())
                                .reduce(Stream.of(this), (s1, s2) -> Stream.concat(s1, s2));
        }
    }
    public Stream<TreeNode<T>> lazyStream() {
        if (isLeaf()) {
            return Stream.of(this);
        } else {
            return Stream.concat(Stream.of(this), getChildren().stream().flatMap(TreeNode::stream));
        }
    }

    public Stream<TreeNode<T>> flattened() { return Stream.concat(Stream.of(this), children.stream().flatMap(TreeNode::flattened)); }
    public List<TreeNode<T>> getAll() { return flattened().collect(Collectors.toList()); }
    public List<T> getAllItems() { return flattened().map(TreeNode::getItem).collect(Collectors.toList()); }

    public int getNoOfNodes() { return flattened().map(TreeNode::getItem).collect(Collectors.toList()).size(); }
    public int getNoOfLeafNodes() { return flattened().filter(node -> node.isLeaf()).map(TreeNode::getItem).collect(Collectors.toList()).size(); }

    public boolean contains(final TreeNode<T> NODE) { return flattened().anyMatch(n -> n.equals(NODE)); }
    public boolean containsData(final T ITEM) { return flattened().anyMatch(n -> n.item.equals(ITEM)); }

    public TreeNode<T> getMyRoot() {
        if (null == myRoot) {
            if (null != getParent() && getParent().isRoot()) {
                myRoot = this;
            } else {
                myRoot = getMyRoot(getParent());
            }
        }
        return myRoot;
    }
    private TreeNode<T> getMyRoot(final TreeNode<T> NODE) {
        if (NODE.getParent().isRoot()) { return NODE; }
        return getMyRoot(NODE.getParent());
    }

    public TreeNode<T> getTreeRoot() {
        if (null == treeRoot) {
            if (isRoot()) {
                treeRoot = this;
            } else {
                treeRoot = getTreeRoot(getParent());
            }
        }
        return treeRoot;
    }
    private TreeNode<T> getTreeRoot(final TreeNode<T> NODE) {
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
    private int getDepth(final TreeNode<T> NODE, int depth) {
        depth++;
        if (NODE.isRoot()) { return depth; }
        return getDepth(NODE.getParent(), depth);
    }

    public int getMaxLevel() { return getTreeRoot().stream().map(TreeNode::getDepth).max(Comparator.naturalOrder()).orElse(0); }

    public List<TreeNode<T>> getSiblings() { return null == getParent() ? new ArrayList<>() : getParent().getChildren(); }

    public List<TreeNode<T>> nodesAtSameLevel() {
        final int LEVEL = getDepth();
        return getTreeRoot().stream().filter(node -> node.getDepth() == LEVEL).collect(Collectors.toList());
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
