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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class TreeNode<T> {
    private T                 data;
    private TreeNode<T>       parent;
    private List<TreeNode<T>> children;


    // ******************** Constructors **************************************
    public TreeNode(final T DATA) {
        this(DATA, null);
    }
    public TreeNode(final T DATA, final TreeNode<T> PARENT) {
        data     = DATA;
        parent   = PARENT;
        children = new ArrayList<>();
        init();
    }


    // ******************** Methods *******************************************
    private void init() {
        // Add this node to parents children
        if (null != parent) { parent.getChildren().add(this); }
    }

    public boolean isRoot() { return null == parent; }
    public boolean isLeaf() { return (null == children || children.isEmpty()); }
    public boolean hasParent() { return null != parent; }
    public void removeParent() { parent = null; }

    public TreeNode<T> getParent() { return parent; }
    public void setParent(final TreeNode<T> PARENT) {
        if (null != PARENT) { PARENT.addNode(TreeNode.this); }
        parent = PARENT;
    }

    public T getData() { return data; }
    public void setData(final T DATA) { data = DATA; }

    public List<TreeNode<T>> getChildrenUnmodifiable() { return Collections.unmodifiableList(children); }
    public List<TreeNode<T>> getChildren() { return children; }
    public void setChildren(final List<TreeNode<T>> CHILDREN) { children = new ArrayList<>(new LinkedHashSet<>(CHILDREN)); }

    public void addNode(final T DATA) {
        TreeNode<T> child = new TreeNode<T>(DATA);
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
    public List<T> getAll() { return flattened().map(TreeNode::getData).collect(Collectors.toList()); }

    public int getNoOfNodes() { return flattened().map(TreeNode::getData).collect(Collectors.toList()).size(); }
    public int getNoOfLeafNodes() { return flattened().filter(node -> node.isLeaf()).map(TreeNode::getData).collect(Collectors.toList()).size(); }

    public boolean contains(final TreeNode<T> NODE) { return flattened().anyMatch(n -> n.equals(NODE)); }
    public boolean containsContext(final T DATA) { return flattened().anyMatch(n -> n.data.equals(DATA)); }

    public TreeNode<T> getRoot() {
        if (isRoot()) { return this; }
        return findRoot(getParent());
    }
    private TreeNode<T> findRoot(final TreeNode<T> NODE) {
        if (NODE.isRoot()) { return NODE; }
        return findRoot(NODE.getParent());
    }

    public int getLevel() {
        if (isRoot()) { return 0; }
        return getLevel(getParent(), 0);
    }
    private int getLevel(final TreeNode<T> NODE, int level) {
        level++;
        if (NODE.isRoot()) { return level; }
        return getLevel(NODE.getParent(), level);
    }

    public int getMaxLevel() { return getRoot().stream().map(TreeNode::getLevel).max(Comparator.naturalOrder()).orElse(0); }
}
