package com.example.mdpandroid;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class MazeView extends View {

    private static final int COLUMNS_SIZE = 15;  //Range of X-axis
    private static final int ROWS_SIZE = 20;     //Range of Y-axis
    private Cell[][] cells;
    public int[] robotCenter = {1, 1};// x,y
    private int[] waypoint = {1, 1};
    //    // robot starting coordinates
    private int[] robotFront = {1, 2}; //x,y
    //currentAngle will change based on control of robot.
    private int currentAngle =0;
    private class Cell{
        int col,row;
        public Cell(int col,int row){
            this.col = col;
            this.row = row;
        }
    }

    public MazeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initalizeMaze();
    }



    private void initalizeMaze() {
        cells = new Cell[COLUMNS_SIZE][ROWS_SIZE];

        for(int i=0;i<COLUMNS_SIZE;i++){
            for(int j=0;j<ROWS_SIZE;j++){
                cells[i][j]= new Cell(i,j);
            }
        }

    }


}
