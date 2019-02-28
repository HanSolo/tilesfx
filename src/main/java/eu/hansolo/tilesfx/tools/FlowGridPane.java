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

import javafx.beans.NamedArg;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;


/**
 * Created by hansolo on 10.02.17.
 */
public class FlowGridPane extends GridPane {
    private boolean         internalCall;
    private int             _noOfCols;
    private int             _noOfRows;
    private IntegerProperty noOfCols;
    private IntegerProperty noOfRows;


    // ******************** Constructors **************************************
    public FlowGridPane(final @NamedArg("NO_OF_COLS")int NO_OF_COLS, final @NamedArg("NO_OF_ROWS")int NO_OF_ROWS) {
        this(NO_OF_COLS, NO_OF_ROWS, null);
    }
    public FlowGridPane(final @NamedArg("NO_OF_COLS")int NO_OF_COLS, final @NamedArg("NO_OF_ROWS")int NO_OF_ROWS, final Node... NODES) {
        super();
        internalCall = false;
        _noOfCols    = NO_OF_COLS;
        _noOfRows    = NO_OF_ROWS;
        noOfCols     = new IntegerPropertyBase(NO_OF_COLS) {
            @Override protected void invalidated() {
                ObservableList<ColumnConstraints> constraints = getColumnConstraints();
                constraints.clear();
                int cols = get();
                for (int i = 0 ; i < cols ; ++i) {
                    ColumnConstraints c = new ColumnConstraints();
                    c.setHalignment(HPos.CENTER);
                    c.setHgrow(Priority.ALWAYS);
                    c.setMinWidth(60);
                    constraints.add(c);
                }
                set(cols);
                relayout();
                if (internalCall) return;
                _noOfCols = cols;
            }
            @Override public Object getBean() { return FlowGridPane.this; }
            @Override public String getName() { return "noOfCols"; }
        };
        noOfRows     = new IntegerPropertyBase(NO_OF_ROWS) {
            @Override protected void invalidated() {
                ObservableList<RowConstraints> constraints = getRowConstraints();
                constraints.clear();
                int rows = get();
                for (int i=0; i < rows; ++i) {
                    RowConstraints r = new RowConstraints();
                    r.setValignment(VPos.CENTER);
                    r.setVgrow(Priority.ALWAYS);
                    r.setMinHeight(20);
                    constraints.add(r);
                }
                set(rows);
                relayout();
                if (internalCall) return;
                _noOfRows = rows;
            }
            @Override public Object getBean() { return FlowGridPane.this; }
            @Override public String getName() { return "noOfRows"; }
        };
        getChildren().addListener((ListChangeListener<Node>) change -> relayout());
        registerListeners();
        if (null != NODES) { getChildren().setAll(NODES); }
    }

    private void registerListeners() {
        widthProperty().addListener(o -> checkAspectRatio());
        heightProperty().addListener(o -> checkAspectRatio());
    }


    // ******************** Methods *******************************************
    public Integer getNoOfCols() { return noOfCols.get(); }
    public void setNoOfCols(final Integer COLS) { noOfCols.set(COLS); }
    public IntegerProperty noOfColsProperty() { return noOfCols; }

    public Integer getNoOfRows() { return noOfRows.get(); }
    public void setNoOfRows(final Integer ROWS) { noOfRows.set(ROWS); }
    public IntegerProperty noOfRowsProperty() { return noOfRows; }

    public void setNoOfColsAndNoOfRows(final int COLS, final int ROWS) {
        setNoOfCols(COLS);
        setNoOfRows(ROWS);
    }

    private int coordsToOffset(final int COL, final int ROW) { return ROW * noOfCols.get() + COL; }
    private int offsetToCol(final int OFFSET) { return OFFSET % noOfCols.get(); }
    private int offsetToRow(final int OFFSET) { return OFFSET / noOfCols.get(); }

    private void checkAspectRatio() {
        internalCall = true;
        if (getWidth() < getHeight()) {
            setNoOfColsAndNoOfRows(_noOfRows, _noOfCols);
        } else {
            setNoOfColsAndNoOfRows(_noOfCols, _noOfRows);
        }
        internalCall = false;
        relayout();
    }

    private void relayout() {
        ObservableList<Node> children = getChildren();
        int    lastColSpan = 0;
        int    lastRowSpan = 0;
        for (Node child : children ) {
            int offs = children.indexOf(child);
            GridPane.setConstraints(child, offsetToCol(offs + lastColSpan), offsetToRow(offs + lastRowSpan));
            //lastColSpan = GridPane.getColumnSpan(child) == null ? 0 : GridPane.getColumnSpan(child);
            //lastRowSpan = GridPane.getRowSpan(child) == null ? 0 : GridPane.getRowSpan(child);
        }
    }
}