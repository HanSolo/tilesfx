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


public class Tree<T> {
    private T                 data;
    private Tree<T>       parent;
    private List<Tree<T>> children;


    // ******************** Constructors **************************************
    public Tree(final T DATA) {
        this(DATA, null);
    }
    public Tree(final T DATA, final Tree<T> PARENT) {
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

    public Tree<T> getParent() { return parent; }
    public void setParent(final Tree<T> PARENT) {
        if (null != PARENT) { PARENT.addNode(Tree.this); }
        parent = PARENT;
    }

    public T getData() { return data; }
    public void setData(final T DATA) { data = DATA; }

    public List<Tree<T>> getChildrenUnmodifiable() { return Collections.unmodifiableList(children); }
    public List<Tree<T>> getChildren() { return children; }
    public void setChildren(final List<Tree<T>> CHILDREN) { children = new ArrayList<>(new LinkedHashSet<>(CHILDREN)); }

    public void addNode(final T DATA) {
        Tree<T> child = new Tree<T>(DATA);
        child.setParent(this);
        children.add(child);
    }
    public void addNode(final Tree<T> NODE) {
        if (children.contains(NODE)) { return; }
        NODE.setParent(this);
        children.add(NODE);
    }
    public void removeNode(final Tree<T> NODE) { if (children.contains(NODE)) { children.remove(NODE); } }

    public void addNodes(final Tree<T>... NODES) { addNodes(Arrays.asList(NODES)); }
    public void addNodes(final List<Tree<T>> NODES) { NODES.forEach(node -> addNode(node)); }

    public void removeNodes(final Tree<T>... NODES) { removeNodes(Arrays.asList(NODES)); }
    public void removeNodes(final List<Tree<T>> NODES) { NODES.forEach(node -> removeNode(node)); }

    public void removeAllNodes() { children.clear(); }

    public Stream<Tree<T>> stream() {
        if (isLeaf()) {
            return Stream.of(this);
        } else {
            return getChildren().stream()
                                .map(child -> child.stream())
                                .reduce(Stream.of(this), (s1, s2) -> Stream.concat(s1, s2));
        }
    }
    public Stream<Tree<T>> lazyStream() {
        if (isLeaf()) {
            return Stream.of(this);
        } else {
            return Stream.concat(Stream.of(this), getChildren().stream().flatMap(Tree::stream));
        }
    }

    public Stream<Tree<T>> flattened() { return Stream.concat(Stream.of(this), children.stream().flatMap(Tree::flattened)); }
    public List<T> getAll() { return flattened().map(Tree::getData).collect(Collectors.toList()); }

    public int getNoOfNodes() { return flattened().map(Tree::getData).collect(Collectors.toList()).size(); }
    public int getNoOfLeafNodes() { return flattened().filter(node -> node.isLeaf()).map(Tree::getData).collect(Collectors.toList()).size(); }

    public boolean contains(final Tree<T> NODE) { return flattened().anyMatch(n -> n.equals(NODE)); }
    public boolean containsData(final T DATA) { return flattened().anyMatch(n -> n.data.equals(DATA)); }

    public Tree<T> getMyRoot() {
        if (getParent().isRoot()) { return this; }
        return findMyRoot(getParent());
    }
    private Tree<T> findMyRoot(final Tree<T> NODE) {
        if (NODE.getParent().isRoot()) { return NODE; }
        return findMyRoot(NODE.getParent());
    }

    public Tree<T> getTreeRoot() {
        if (isRoot()) { return this; }
        return findTreeRoot(getParent());
    }
    private Tree<T> findTreeRoot(final Tree<T> NODE) {
        if (NODE.isRoot()) { return NODE; }
        return findTreeRoot(NODE.getParent());
    }

    public int getLevel() {
        if (isRoot()) { return 0; }
        return getLevel(getParent(), 0);
    }
    private int getLevel(final Tree<T> NODE, int level) {
        level++;
        if (NODE.isRoot()) { return level; }
        return getLevel(NODE.getParent(), level);
    }

    public int getMaxLevel() { return getTreeRoot().stream().map(Tree::getLevel).max(Comparator.naturalOrder()).orElse(0); }

    public List<Tree<T>> getSiblings() { return null == getParent() ? new ArrayList<>() : getParent().getChildren(); }

    public List<Tree<T>> nodesAtSameLevel() {
        final int LEVEL = getLevel();
        return getTreeRoot().stream().filter(node -> node.getLevel() == LEVEL).collect(Collectors.toList());
    }
}