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

package eu.hansolo.tilesfx.events;

import eu.hansolo.tilesfx.tools.TreeNode;


public class TreeNodeEvent<T> {
    public enum EventType { PARENT_CHANGED, CHILDREN_CHANGED, NODE_SELECTED }

    private final TreeNode<T>  SRC;
    private final EventType TYPE;


    // ******************** Constructors **************************************
    public TreeNodeEvent(final TreeNode<T> SRC, final EventType TYPE) {
        this.SRC  = SRC;
        this.TYPE = TYPE;
    }


    // ******************** Methods *******************************************
    public TreeNode<T> getSource() { return SRC; }

    public EventType getType() { return TYPE; }
}
