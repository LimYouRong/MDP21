package com.example.mdpandroid;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

public class MazeView extends View {

    private static final int COLUMNS_SIZE = 15;  //Range of X-axis
    private static final int ROWS_SIZE = 20;     //Range of Y-axis
    private Cell[][] cells;
    private int[] robotCenter = {1, 1};// x,y
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
    public int [] getWaypoint(){
        return waypoint;
    }
    public void setWaypoint(int [] waypoint){
        this.waypoint = waypoint;
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
    public void turn(int leftright){
        if(leftright == 0){//turn left
            currentAngle -=90;
        }else if (leftright == 1){//turn right
            currentAngle +=90;
        }
        fixcurrentAngle();
    }
    private void fixcurrentAngle(){
        if(currentAngle<0){
            currentAngle +=360;
        }else if(currentAngle>270){
            currentAngle-=360;
        }
    }
    private void checkBounds(){
        if(robotCenter[0] == 0)
            robotCenter[0] =1;
        else if(robotCenter[0] == COLUMNS_SIZE - 1)
            robotCenter[0] = 13;
        else if(robotCenter[1] == 0)
            robotCenter[1] = 1;
        else if(robotCenter[1] == ROWS_SIZE - 1)
            robotCenter[1] = 18;
    }
    public void move(int updown){
        if(updown ==0){
//            robotCenter[0] = col;   //X coord
//            robotCenter[1] = row;   //Y coord
            switch (currentAngle){
                case 0://^
                    Log.d("@@@@@@@@@@@@@@@@@@@@@@","up is pressed");
                    robotFront[0] = robotCenter[0];
                    robotFront[1] = robotCenter[1] + 1;
                    robotCenter[1] = robotCenter[1]+1;
                    break;
                case 90://>
                    robotFront[0] = robotCenter[0] + 1;
                    robotFront[1] = robotCenter[1];
                    robotCenter[0] = robotCenter[0]+1;
                    break;
                case 180://v
                    robotFront[0] = robotCenter[0];
                    robotFront[1] = robotCenter[1] - 1;
                    robotCenter[1] = robotCenter[1] -1;
                    break;
                case 270://<
                    robotFront[0] = robotCenter[0] - 1;
                    robotFront[1] = robotCenter[1];
                    robotCenter[0] = robotCenter[0]-1;
                    break;
            }
        }else if(updown ==1){//down may not do

        }
        checkBounds();
    }
    public int[] getCurrentPosition(){
        Log.d("TAGGGGGGGGGGGGGGGGGGGGG","(X,Y) : direction as x:"+ robotCenter[0]+" y : "+ robotCenter[1]+" angle: "+ currentAngle);
        return robotCenter;
    }
    public void setCurrentPosition(int[] position){
        robotCenter[0] = position[0];
        robotCenter[1] = position[1];

    }

//    private void updateRobotPosition (int direction){
////        robotCenter[0]   //X coord
////        robotCenter[1]   //Y coord
//        switch (direction) {
//            case 0://^
//                robotFront[0] = robotCenter[0];
//                robotFront[1] = robotCenter[1] + 1;
//                break;
//            case 90://>
//                robotFront[0] = robotCenter[0] + 1;
//                robotFront[1] = robotCenter[1];
//                break;
//            case 180://v
//                robotFront[0] = robotCenter[0];
//                robotFront[1] = robotCenter[1] - 1;
//                break;
//            case 270://<
//                robotFront[0] = robotCenter[0] - 1;
//                robotFront[1] = robotCenter[1];
//                break;
//        }
//
//    }


}
