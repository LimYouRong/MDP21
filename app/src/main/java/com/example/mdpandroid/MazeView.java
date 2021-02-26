package com.example.mdpandroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.List;

public class MazeView extends View {

    private static final int COLUMNS_SIZE = 15;  //Range of X-axis
    private static final int ROWS_SIZE = 20;     //Range of Y-axis
    private static final float SPACE_WIDTH = 4;
    private Cell[][] cells;
    private int[] robotCenter = {1, 1};// x,y
    private int[] waypoint = new int[2];
    private int[] touchPos = new int[2];
    private int[][] obstacle = new int[ROWS_SIZE][COLUMNS_SIZE];
    private int[][] explored = new int[ROWS_SIZE][COLUMNS_SIZE];
    private Paint lightBluePaint = new Paint();
    private Paint notLightBluePaint = new Paint();
    private Paint bluePaint = new Paint();
    private Paint darkBluePaint = new Paint();
    private Paint whitePaint = new Paint();
    private Paint orangePaint = new Paint();
    private Paint backgroundPaint = new Paint();
    private Paint yellowPaint = new Paint();
    private int cellWidth;
    private int cellHeight;
    private String[][] numberGrid = new String[ROWS_SIZE][COLUMNS_SIZE];
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
        Log.d("wp","wpX: "+waypoint[0]+" wpY: "+waypoint[1]);
        return waypoint;
    }
    public void setWaypoint(int [] waypoint){
        this.waypoint = waypoint;
    }

    public MazeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        //setting color for all color
        lightBluePaint.setColor(Color.parseColor("#E4EDF2"));
        notLightBluePaint.setColor(Color.parseColor("#C9E2F0"));
        bluePaint.setColor(Color.parseColor("#6699CC"));
        darkBluePaint.setColor(Color.parseColor("#4E7D96"));
        whitePaint.setColor(Color.WHITE);
        orangePaint.setColor(Color.parseColor("#FF844B"));
        yellowPaint.setColor(Color.YELLOW);
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

        //Plot empty Map
        for (int i=0;i<COLUMNS_SIZE;i++){
            for(int j=0;j<ROWS_SIZE;j++){
                canvas.drawRect(i*cellWidth,(ROWS_SIZE-1-j)*cellHeight,(i+1)*cellWidth,(ROWS_SIZE-j)*cellHeight,lightBluePaint);
            }
        }

        //touchPos
        canvas.drawRect(touchPos[0]*cellWidth,(ROWS_SIZE-1-touchPos[1])*cellHeight,
                (touchPos[0]+1)*cellWidth,(ROWS_SIZE-touchPos[1])*cellHeight,bluePaint);

        //waypoint
        canvas.drawRect(waypoint[0]*cellWidth, (ROWS_SIZE-1-waypoint[1])*cellHeight,
                (waypoint[0] + 1) * cellWidth, (ROWS_SIZE-waypoint[1])*cellHeight, yellowPaint);

        //Obstacle
        for (int y = 0; y < ROWS_SIZE; y++) {
            for (int x = 0; x < COLUMNS_SIZE; x++) {
                if (obstacle != null && obstacle[y][x] == 1) {
                    canvas.drawRect(x * cellWidth, (ROWS_SIZE - 1 - y) * cellHeight,
                            (x + 1) * cellWidth, (ROWS_SIZE - y) * cellHeight, darkBluePaint);
                }
            }
        }

        //Numberid drawings on obstacle
        for (int y = 0; y < ROWS_SIZE; y++) {
            for (int x = 0; x < COLUMNS_SIZE; x++) {
                if(numberGrid[y][x]!=null){
                    String item = numberGrid[y][x];
                    if(Integer.parseInt(item)<10&&Integer.parseInt(item)>0)
                        canvas.drawText(item,(x-1)*cellWidth+9,(ROWS_SIZE-y+1)*cellHeight-7,whitePaint);
                    else if(Integer.parseInt(item)>9&&Integer.parseInt(item)<16)
                        canvas.drawText(item,(x-1)*cellWidth+6,(ROWS_SIZE-y+1)*cellHeight-7,whitePaint);
                }
            }
        }

        //EXPLORATION MODE CODE
        for (int y = 0; y < ROWS_SIZE; y++) {
            for (int x = 0; x < COLUMNS_SIZE; x++) {

                //when explored then draw obstacle if any
                if (explored != null && explored[y][x] == 1) {
                    canvas.drawRect(y * cellWidth, (ROWS_SIZE - 1 - x) * cellHeight,
                            (y + 1) * cellWidth, (ROWS_SIZE - x) * cellHeight,  notLightBluePaint);
                    if (obstacle != null && obstacle[y][x] == 1) {
                        canvas.drawRect(y * cellWidth, (ROWS_SIZE - 1 - x) * cellHeight,
                                (y + 1) * cellWidth, (ROWS_SIZE - x) * cellHeight, darkBluePaint);
                    }
                }
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

    public boolean onTouchEvent(MotionEvent event){
        if (event.getAction() != MotionEvent.ACTION_DOWN)
            return true;
        int posX = (int)event.getX()/cellWidth;
        int posY = ROWS_SIZE-1-(int)event.getY()/cellWidth;
        touchPos[0]=posX;
        touchPos[1]=posY;
        activityMain.updateCoord();
        Log.d("PPPPPPPPPPPPPPPPPPPP",touchPos[0]+" AND "+touchPos[1]);
        if(obstacle[posX][posY]==1){

        }

        invalidate();
        return true;
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
    public void realTimeObstacleCheck(String message){
//        message = "2 2 2 4 2 0";
        String[] splitStr = message.split("\\s+");
        Log.d("TTTTTTTTTTTTTTTTTTTTT0",splitStr[0]);
        Log.d("TTTTTTTTTTTTTTTTTTTTT1",splitStr[1]);
        Log.d("TTTTTTTTTTTTTTTTTTTTT2",splitStr[2]);
        Log.d("TTTTTTTTTTTTTTTTTTTTT3",splitStr[3]);
        Log.d("TTTTTTTTTTTTTTTTTTTTT4",splitStr[4]);
        Log.d("TTTTTTTTTTTTTTTTTTTTT5",splitStr[5]);
        //x = robotCenter[0] y = robotCenter[1]
        Log.d("TAGGGGGGGGGGGGGGGGGGGGG","(X,Y) : direction as x:"+ robotCenter[0]+" y : "+ robotCenter[1]+" angle: "+ currentAngle);

        int num0 = Integer.parseInt(splitStr[0]);
        int num1 = Integer.parseInt(splitStr[1]);
        int num2 = Integer.parseInt(splitStr[2]);
        int num3 = Integer.parseInt(splitStr[3]);
        int num4 = Integer.parseInt(splitStr[4]);
        int num5 = Integer.parseInt(splitStr[5]);

        if (num0 != 2){
            Log.d("oooooooooooooooooo","First Sensor");
                        FirstSensor(num0);
        }
        if(num1 !=2){
            Log.d("oooooooooooooooooo","Second Sensor");
            SecondSensor(num1);
        }
        if(num2 !=2){
            Log.d("oooooooooooooooooo","Third Sensor");
            ThirdSensor(num2);
        }
        if(num3 !=4){
            Log.d("oooooooooooooooooo","Fourth Sensor");
            FourthSensor(num3);
        }
        if (num4 !=2){
            Log.d("oooooooooooooooooo","Fifth Sensor");
            FifthSensor(num4);
        }
        if(num5 != 2){
            Log.d("oooooooooooooooooo","Sixth Sensor");
            SixthSensor(num5);
        }

    }
    private void FirstSensor(int num0){
        if (currentAngle == 0){
            setObstacle(robotCenter[0]-1, (robotCenter[1]+num0+2));
        }else if(currentAngle == 90){
            setObstacle(robotCenter[0]+num0+2,robotCenter[1]+1);
        }else if(currentAngle == 180){
            setObstacle(robotCenter[0]+1,robotCenter[1]-(num0+2));
        }else{//270
            setObstacle(robotCenter[0]-(num0+2),robotCenter[1]-1);
        }
    }
    private void SecondSensor(int num1){
        if (currentAngle == 0){
            setObstacle(robotCenter[0], (robotCenter[1]+num1+2));
        }else if(currentAngle == 90){
            setObstacle(robotCenter[0]+num1+2,robotCenter[1]);
        }else if(currentAngle == 180){
            setObstacle(robotCenter[0],robotCenter[1]-(num1+2));
        }else{//270
            setObstacle(robotCenter[0]-(num1+2),robotCenter[1]);
        }
    }
    private void ThirdSensor(int num2){
        if (currentAngle == 0){
            setObstacle(robotCenter[0]+1, (robotCenter[1]+num2+2));
        }else if(currentAngle == 90){
            setObstacle(robotCenter[0]+num2+2,robotCenter[1]-1);
        }else if(currentAngle == 180){
            setObstacle(robotCenter[0]-1,robotCenter[1]-(num2+2));
        }else{//270
            setObstacle(robotCenter[0]-(num2+2),robotCenter[1]+1);
        }
    }
    private void FourthSensor(int num3){
//        if(splitStr[3]!="4"){
            if (currentAngle == 0){
                setObstacle(robotCenter[0]+2+num3,robotCenter[1]+1);
            }else if(currentAngle == 90){
                setObstacle(robotCenter[0]+1,robotCenter[1]-(2+num3));
            }else if(currentAngle == 180){
                setObstacle(robotCenter[0]-(2+num3),robotCenter[1]-1);
            }else{//270
                setObstacle(robotCenter[0]-1,robotCenter[1]+2+num3);
            }
    }
    private void FifthSensor(int num4){
        if (currentAngle ==0){
            setObstacle(robotCenter[0]-(2+num4),robotCenter[1]+1);
        }else if(currentAngle == 90){
            setObstacle(robotCenter[0]+1,robotCenter[1]+2+num4);
        }else if(currentAngle == 180){
            setObstacle(robotCenter[0]+2+num4,robotCenter[1]-1);
        }else{
            setObstacle(robotCenter[0]-1,robotCenter[1]-(2+num4));
        }
    }
    private void SixthSensor(int num5){
        if (currentAngle ==0){
            setObstacle(robotCenter[0]-(2+num5),robotCenter[1]-1);
        }else if(currentAngle == 90){
            setObstacle(robotCenter[0]-1,robotCenter[1]+2+num5);
        }else if(currentAngle == 180){
            setObstacle(robotCenter[0]+2+num5,robotCenter[1]+1);
        }else{
            setObstacle(robotCenter[0]+1,robotCenter[1]-(2+num5));
        }
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

        fixcurrentAngle();
        updateMap();
    }
    private void checkObstacle(){
        fixcurrentAngle();
        switch(currentAngle){
            case 0:
                if (obstacle[robotCenter[1]+1][robotCenter[0]]==1 || obstacle[robotCenter[1]+1][robotCenter[0]-1]==1 || obstacle[robotCenter[1]+1][robotCenter[0]+1]==1){
                    robotCenter[1]-=1;
                }
                break;
            case 90:
                if (obstacle[robotCenter[1]][robotCenter[0]+1]==1 || obstacle[robotCenter[1]+1][robotCenter[0]+1]==1 || obstacle[robotCenter[1]-1][robotCenter[0]+1]==1){
                    robotCenter[0]-=1;
                }
                break;
            case 180:
                if (obstacle[robotCenter[1]-1][robotCenter[0]]==1 || obstacle[robotCenter[1]-1][robotCenter[0]+1]==1 || obstacle[robotCenter[1]-1][robotCenter[0]-1]==1){
                    robotCenter[1]+=1;
                }
                break;
            case 270:
                if (obstacle[robotCenter[1]][robotCenter[0]-1]==1 || obstacle[robotCenter[1]-1][robotCenter[0]-1]==1 || obstacle[robotCenter[1]+1][robotCenter[0]-1]==1){
                    robotCenter[0]+=1;
                }
                break;
        }
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
        checkObstacle();
        fixcurrentAngle();
    }
    public int[] getTouchPos(){
        int[] temp=touchPos.clone();
        return temp;
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
    public void setObstacle(int y, int x){
        if (x>=0 && x<COLUMNS_SIZE && y>=0 && y<ROWS_SIZE){
            obstacle[x][y]=1;
            updateMap();
        }else{
            Log.d("QQQQQQQQQQQQQQQQQ","OBS outside of cells");
        }


    }

    public void setExplored(int y, int x){
        if (x>=0 && x<COLUMNS_SIZE && y>=0 && y<ROWS_SIZE){
            explored[x][y]=1;
            updateMap();
        }else{
            Log.d("QQQQQQQQQQQQQQQQQ","explored outside of cells");
        }


    }

    public void setUnexplored(int y, int x){
        if (x>=0 && x<COLUMNS_SIZE && y>=0 && y<ROWS_SIZE){
            explored[x][y]=0;
            updateMap();
        }else{
            Log.d("QQQQQQQQQQQQQQQQQ","unexplored outside of cells");
        }


    }
    public void resetMap(){
        robotCenter[0]=1;
        robotCenter[1]=1;
        robotFront[0]=1;
        robotFront[1]=2;
        currentAngle=0;
        waypoint[0]=1;
        waypoint[1]=1;
        for(int x=0;x<ROWS_SIZE;x++){
            for(int y=0;y<COLUMNS_SIZE;y++){
                explored[x][y]=0;
                obstacle[x][y]=0;
            }
        }
        invalidate();
    }



}
