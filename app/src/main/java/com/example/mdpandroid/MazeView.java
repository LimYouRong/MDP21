package com.example.mdpandroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.jetbrains.annotations.Nullable;

public class MazeView extends View {

    private static final int COLUMNS_SIZE = 15;  //Range of X-axis
    private static final int ROWS_SIZE = 20;     //Range of Y-axis
    private static final float SPACE_WIDTH = 4;
    private Cell[][] cells;
    private int[] robotCenter = {1, 1};// x,y
    private int[] waypoint = {1, 1};
    private Paint lightBluePaint;
    private Paint darkBluePaint;
    private Paint whitePaint;
    private Paint orangePaint;
    private Paint backgroundPaint;
    private int cellWidth;
    private int cellHeight;
    MainActivity activityMain = (MainActivity) getContext();


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
        lightBluePaint = new Paint();
        darkBluePaint = new Paint();
        whitePaint = new Paint();
        orangePaint = new Paint();
        backgroundPaint = new Paint();

        //setting color for all color

        lightBluePaint.setColor(Color.parseColor("#E4EDF2"));
        darkBluePaint.setColor(Color.parseColor("#4E7D96"));
        whitePaint.setColor(Color.WHITE);
        orangePaint.setColor(Color.parseColor("#FF844B"));
        backgroundPaint.setColor(Color.parseColor("#F4F5FB"));
        backgroundPaint.setStrokeWidth(SPACE_WIDTH);

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

    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight)
    {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        //calculate the cellsize based on the canvas
        cellWidth = getWidth() / COLUMNS_SIZE;
        cellHeight = getHeight() / ROWS_SIZE;

        if (cellWidth > cellHeight) {
            cellWidth = cellHeight;
        } else {
            cellHeight = cellWidth;
        }
        this.setLayoutParams(new LinearLayout.LayoutParams(cellWidth * COLUMNS_SIZE, cellHeight * ROWS_SIZE));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        canvas.drawColor(0xE4EDF2);

        //Plot empty Map
        for (int i=0;i<COLUMNS_SIZE;i++){
            for(int j=0;j<ROWS_SIZE;j++){
                canvas.drawRect(i*cellWidth,(ROWS_SIZE-1-j)*cellHeight,(i+1)*cellWidth,(ROWS_SIZE-j)*cellHeight,lightBluePaint);
            }
        }

        //startZone
        for(int i=0;i<=2;i++)
            for(int j=0;j<=2;j++)
                canvas.drawRect(i * cellWidth, (ROWS_SIZE - 1 - j) * cellHeight,
                        (i + 1) * cellWidth, (ROWS_SIZE - j) * cellHeight, whitePaint);

        //goalZone
        for(int i=12;i<=14;i++)
            for(int j=17;j<=19;j++)
                canvas.drawRect(i * cellWidth, (ROWS_SIZE - 1 - j) * cellHeight,
                        (i + 1) * cellWidth, (ROWS_SIZE - j) * cellHeight, orangePaint);
        //Grid drawing
        for (int c = 0; c < COLUMNS_SIZE + 1; c++) {
            canvas.drawLine(c * cellWidth, 0, c * cellWidth, ROWS_SIZE * cellHeight, backgroundPaint);
        }

        for (int r = 0; r < ROWS_SIZE + 1; r++) {
            canvas.drawLine(0, r * cellHeight, COLUMNS_SIZE * cellWidth, r * cellHeight, backgroundPaint);
        }

        //displaying robot position when user taps
        if (robotCenter[0] >= 0) {
            canvas.drawCircle(robotCenter[0] * cellWidth + cellWidth / 2,
                    (ROWS_SIZE - robotCenter[1]) * cellHeight - cellHeight / 2, 1.3f * cellWidth, darkBluePaint);
        }
        if (robotFront[0] >= 0) {
            canvas.drawCircle(robotFront[0] * cellWidth + cellWidth / 2,
                    (ROWS_SIZE - robotFront[1]) * cellHeight - cellHeight / 2, 0.3f * cellWidth, whitePaint);
        }



    }

    public void turn(int leftright){
        if(leftright == 0){//turn left
            currentAngle -=90;
        }else if (leftright == 1){//turn right
            currentAngle +=90;
        }
        fixcurrentAngle();
        updateMap();
    }
    private void fixcurrentAngle(){
        if(currentAngle<0){
            currentAngle +=360;
        }else if(currentAngle>270){
            currentAngle-=360;
        }
        switch(currentAngle){
            case 0:
                robotFront[0] = robotCenter[0];
                robotFront[1] = robotCenter[1] + 1;
                break;
            case 90:
                robotFront[0] = robotCenter[0]+1;
                robotFront[1] = robotCenter[1];
                break;
            case 180:
                robotFront[0] = robotCenter[0];
                robotFront[1] = robotCenter[1] - 1;
                break;
            case 270:
                robotFront[0] = robotCenter[0] - 1;
                robotFront[1] = robotCenter[1];
                break;
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

        updateMap();
    }
    public void move(int updown){
        if(updown ==0){
//            robotCenter[0] = col;   //X coord
//            robotCenter[1] = row;   //Y coord
            switch (currentAngle){
                case 0://^
                    Log.d("@@@@@@@@@@@@@@@@@@@@@@","up is pressed");
                    robotFront[0] = robotCenter[0];
                    robotCenter[1] = robotCenter[1]+1;
                    robotFront[1] = robotCenter[1] + 1;
                    break;
                case 90://>
                    robotFront[1] = robotCenter[1];
                    robotCenter[0] = robotCenter[0]+1;
                    robotFront[0] = robotCenter[0] + 1;
                    break;
                case 180://v
                    robotFront[0] = robotCenter[0];
                    robotCenter[1] = robotCenter[1] -1;
                    robotFront[1] = robotCenter[1] - 1;
                    break;
                case 270://<
                    robotFront[1] = robotCenter[1];
                    robotCenter[0] = robotCenter[0]-1;
                    robotFront[0] = robotCenter[0] - 1;
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
        updateMap();
    }
    private void updateMap(){
        if(activityMain.autoUpdate){
            invalidate();
        }
    }



}
