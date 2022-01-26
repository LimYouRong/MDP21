package com.example.mdpandroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

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
    private int[][] arrow = new int[10][3]; //max 5 arrows with 3 attri,[x,y,dir]
    private Paint lightBluePaint = new Paint();
    private Paint aviaryBlue = new Paint();
    private Paint bluePaint = new Paint();
    private Paint darkBluePaint = new Paint();
    private Paint whitePaint = new Paint();
    private Paint orangePaint = new Paint();
    private Paint backgroundPaint = new Paint();
    private Paint yellowPaint = new Paint();
    private Paint redPaint = new Paint();
    private int cellWidth;
    private int cellHeight;
    private String[][] obstacleNumberGrid = new String[ROWS_SIZE][COLUMNS_SIZE];
    MainActivity activityMain = (MainActivity) getContext();
    private HashMap<String, String> imageMap;
    private ArrayList<ArrayList<Integer>> arrowIcons;

    //robot starting coordinates
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
    public MazeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        //setting color for all color
        lightBluePaint.setColor(Color.parseColor("#E4EDF2"));
        aviaryBlue.setColor(Color.parseColor("#C9E2F0"));
        bluePaint.setColor(Color.parseColor("#6699CC"));
        darkBluePaint.setColor(Color.parseColor("#4E7D96"));
        whitePaint.setColor(Color.WHITE);
        orangePaint.setColor(Color.parseColor("#FF844B"));
        yellowPaint.setColor(Color.YELLOW);
        redPaint.setColor(Color.RED);
        backgroundPaint.setColor(Color.parseColor("#F4F5FB"));
        backgroundPaint.setStrokeWidth(SPACE_WIDTH);

        arrowIcons = new ArrayList<ArrayList<Integer>>();
        initalizeMaze();
        initalizeImageLUT();
    }

    public void setWaypoint(int[] waypoint) {
        this.waypoint = waypoint;
        //updateMap(); Either call updateMap here or invalidate on mainActivity
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
        //EXPLORATION MODE CODE
        for (int y = 0; y < ROWS_SIZE; y++) {
            for (int x = 0; x < COLUMNS_SIZE; x++) {
                //when explored then draw obstacle if any
                if (explored != null && explored[y][x] == 1) {
                    canvas.drawRect(x * cellWidth, (ROWS_SIZE - 1 - y) * cellHeight,
                            (x + 1) * cellWidth, (ROWS_SIZE - y) * cellHeight, aviaryBlue);
                    if (obstacle != null && obstacle[y][x] == 1) {
                        canvas.drawRect(x * cellWidth, (ROWS_SIZE - 1 - y) * cellHeight,
                                (x + 1) * cellWidth, (ROWS_SIZE - y) * cellHeight, darkBluePaint);
                    }
                }
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
        whitePaint.setTextSize(cellWidth-8);
        whitePaint.setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
        for (int y = 0; y < ROWS_SIZE; y++) {
            for (int x = 0; x < COLUMNS_SIZE; x++) {
                if (obstacleNumberGrid[y][x] != null) {
                    String item = obstacleNumberGrid[y][x];
                    if(Integer.parseInt(item)<10&&Integer.parseInt(item)>0)
                        canvas.drawText(item,(x)*cellWidth+9,(ROWS_SIZE-y)*cellHeight-7,whitePaint);
                    else if(Integer.parseInt(item)>9&&Integer.parseInt(item)<16)
                        canvas.drawText(item,(x)*cellWidth+4,(ROWS_SIZE-y)*cellHeight-7,whitePaint);
                }
            }
        }

        //Arrow
//        for (int i = 0; i < 9; i++) {
//            if (arrow[i][0] != 0 || arrow[i][1] != 0 || arrow[i][2] != 0) {
//                Log.d("runnning make arrow!", ":" + i + "  " + arrow[i][0] + " " + arrow[i][1] + "  " + arrow[i][2]);
//                Path[] path = makeArrow(arrow[i][0], arrow[i][1], arrow[i][2]).clone();
//                canvas.drawPath(path[0], redPaint);//draw straight line
//                canvas.drawPath(path[1], redPaint);//draw triangle
//            }
//        }
        //Arrow v2
        for (ArrayList<Integer> li : arrowIcons) {
            Log.d("runnning make arrow!", ":" + li + "  " + li.get(0) + " " + li.get(1) + "  " + li.get(2));
            Path[] path = makeArrow(li.get(0), li.get(1), li.get(2)).clone();
            canvas.drawPath(path[0], redPaint);//draw straight line
            canvas.drawPath(path[1], redPaint);//draw triangle
        }
        //Arrow with a arraylist
//        for (int i = 0; i < 9; i++) {
//            if (arrow[i][0] != 0 || arrow[i][1] != 0 || arrow[i][2] != 0) {
//                Log.d("runnning make arrow!", ":" + i + "  " + arrow[i][0] + " " + arrow[i][1] + "  " + arrow[i][2]);
//                Path[] path = makeArrow(arrow[i][0], arrow[i][1], arrow[i][2]).clone();
//                canvas.drawPath(path[0], redPaint);//draw straight line
//                canvas.drawPath(path[1], redPaint);//draw triangle
//            }
//        }


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
        int posY = ROWS_SIZE - 1 - (int) event.getY() / cellHeight;

        touchPos[0]=posX;
        touchPos[1]=posY;
        activityMain.updateCoord();
        Log.d("MAP PRESSED WITH X AND Y",touchPos[0]+" AND "+touchPos[1]);

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

    //ALL 6 sensor methods are not used as we are updating robot using PC's updated mdf string.
    //This is an alternate method to update obstacle directly from sensor taken by Auduino.
    public void realTimeObstacleCheck(String message){
//        message = "2 2 2 4 2 2";
        String[] splitStr = message.split("\\s+");
//        Log.d("TTTTTTTTTTTTTTTTTTTTT0",splitStr[0]);
//        Log.d("TTTTTTTTTTTTTTTTTTTTT1",splitStr[1]);
//        Log.d("TTTTTTTTTTTTTTTTTTTTT2",splitStr[2]);
//        Log.d("TTTTTTTTTTTTTTTTTTTTT3",splitStr[3]);
//        Log.d("TTTTTTTTTTTTTTTTTTTTT4",splitStr[4]);
//        Log.d("TTTTTTTTTTTTTTTTTTTTT5",splitStr[5]);
//        Log.d("TAGGGGGGGGGGGGGGGGGGGGG","(X,Y) : direction as x:"+ robotCenter[0]+" y : "+ robotCenter[1]+" angle: "+ currentAngle);

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

    //This method is used instead of modulo to prevent potential overflowing
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

    //This method is used to ensure the robot is within the arena.
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
    public void setCurrentPosition(int x,int y){
        robotCenter[0] = x;
        robotCenter[1] = y;
        updateMap();
    }
    private void updateMap(){
        if(activityMain.autoUpdate){
            invalidate();
        }
    }
    public void setObstacle(int y, int x){
        if (x>=0 && x<COLUMNS_SIZE && y>=0 && y<ROWS_SIZE){
            obstacle[y][x]=1;
            updateMap();
        }else{
            Log.d("QQQQQQQQQQQQQQQQQ","OBS outside of cells");
        }
    }

    public boolean isObstacle(int x, int y) {
        if (x >= 0 && x < COLUMNS_SIZE && y >= 0 && y < ROWS_SIZE) {
            return obstacle[y][x] != 0;
        }
        obstacle[y][x] = 1;
        Log.d("QQQQQQQQQQQQQQQQQ", "OBS outside of cells");
        return false;
    }

    public void findBestObstacle(ArrayList li) {

        int x = Integer.parseInt(li.get(0).toString());//x is current_x
        int y = Integer.parseInt(li.get(1).toString());//y is current_y
//        int cur_x = Integer.parseInt(li.get(2).toString());//We dont need this
//        int cur_y = Integer.parseInt(li.get(3).toString());//we dont need this
        int dir = Integer.parseInt(li.get(2).toString()) * 90;

//        setNumberGrid("404", x - 2, y + 1);
//        dir = 0 * 90;

        //mazeView.setArrow(Integer.parseInt(li.get(2).toString()),Integer.parseInt(li.get(3).toString()),Integer.parseInt(li.get(4).toString()));
        String tag = li.get(3).toString();

        //if(tag in set):
        //  return;
        //else:
        //  do fuzzy
        //add item to set

        //mazeView.setArrow(Integer.parseInt(li.get(2).toString()),Integer.parseInt(li.get(3).toString()),Integer.parseInt(li.get(4).toString()));
        Log.d("111111111111111111111111111", "inside findBestObs method");
        Log.d("(((((((((((((((((((())))))))))))))))))", x + " and " + y + " and " + dir + " and tag " + tag);
        if (dir == 0) {
            Log.d("111111111111111111111111111", "pinside 1");
//            setObstacle(robotCenter[0]-(2+num4),robotCenter[1]+1);
            if (isObstacle(x - 2, y + 1)) {//c => a
                setNumberGrid(tag, x - 2, y + 1);
                setArrow(x, y + 2, 3);
                Log.d("111111111111111111111111111", "TESTpinside a");
            } else if (isObstacle(x - 2, y)) {//a =>b
                setNumberGrid(tag, x - 2, y);
                setArrow(x, y + 1, 3);
                Log.d("111111111111111111111111111", "pinside b");
            } else if (isObstacle(x - 3, y + 1)) {//e =>c
                setNumberGrid(tag, x - 3, y + 1);
                setArrow(x-1, y + 2, 3);
                Log.d("111111111111111111111111111", "pinside c");
            } else if (isObstacle(x - 3, y)) {//b =>d
                setNumberGrid(tag, x - 3, y);
                setArrow(x-1, y + 1, 3);
                Log.d("111111111111111111111111111", "pinside d");
            } else if (isObstacle(x - 2, y - 1)) {//d=>e
                setNumberGrid(tag, x - 2, y - 1);
                setArrow(x, y , 3);
                Log.d("111111111111111111111111111", "pinside e");
            } else if (isObstacle(x - 3, y - 1)) {//f
                setNumberGrid(tag, x - 3, y - 1);
                setArrow(x-1, y, 3);
                Log.d("111111111111111111111111111", "pinside f");
            } else {
                Log.d("OOOOOOOOOOOOOOOOOOOO", "no closest obs");
                setObstacle(y, x);
                setNumberGrid(tag, x, y);
            }
        } else if (dir == 90) {
            Log.d("111111111111111111111111111", "inside 1");
            if (isObstacle(x + 1, y + 2)) {//c =>a
                setNumberGrid(tag, x + 1, y + 2);
                setArrow(x+2, y + 2, 0);
                Log.d("111111111111111111111111111", "inside a");
            } else if (isObstacle(x, y + 2)) {//a =>b
                setNumberGrid(tag, x, y + 2);
                setArrow(x+1, y + 2, 0);
                Log.d("111111111111111111111111111", "inside b");
            } else if (isObstacle(x + 1, y + 3)) {//e => c
                setNumberGrid(tag, x + 1, y + 3);
                setArrow(x+2, y + 3, 0);
                Log.d("111111111111111111111111111", "inside c");
            } else if (isObstacle(x, y + 3)) {//b=>d
                setNumberGrid(tag, x, y + 3);
                setArrow(x+1, y + 3, 0);
                Log.d("111111111111111111111111111", "inside d");
            } else if (isObstacle(x - 1, y + 2)) {//d=>e
                setNumberGrid(tag, x - 1, y + 2);
                setArrow(x, y + 2, 0);
                Log.d("111111111111111111111111111", "inside e");
            } else if (isObstacle(x - 1, y + 3)) {//f problem, test again
                setNumberGrid(tag, x - 1, y + 3);
                setArrow(x, y + 3, 0);
                Log.d("111111111111111111111111111", "inside f");
            } else {
                Log.d("OOOOOOOOOOOOOOOOOOOO", "no closest obs");
                setObstacle(y, x);
                setNumberGrid(tag, x, y);
            }
        } else if (dir == 180) {
            if (isObstacle(x + 2, y - 1)) {//c =>a
                setNumberGrid(tag, x + 2, y - 1);
                setArrow(x+2, y, 1);
                Log.d("111111111111111111111111111", "inside a");
            } else if (isObstacle(x + 2, y)) {//a=>b
                setNumberGrid(tag, x + 2, y);
                setArrow(x+2, y+1, 1);
                Log.d("111111111111111111111111111", "inside b");
            } else if (isObstacle(x + 3, y - 1)) {//e=>c
                setNumberGrid(tag, x + 3, y - 1);
                setArrow(x+3, y, 1);
                Log.d("111111111111111111111111111", "inside c");
            } else if (isObstacle(x + 3, y)) {//b=>d
                setNumberGrid(tag, x + 3, y);
                setArrow(x+3, y+1, 1);
                Log.d("111111111111111111111111111", "inside d");
            } else if (isObstacle(x + 2, y + 1)) {//d=>e
                setNumberGrid(tag, x + 2, y + 1);
                setArrow(x+2, y+2, 1);
                Log.d("111111111111111111111111111", "inside e");
            } else if (isObstacle(x + 3, y + 1)) {//f
                setNumberGrid(tag, x + 3, y + 1);
                setArrow(x+3, y+2, 1);
                Log.d("111111111111111111111111111", "inside f");
            } else {
                Log.d("OOOOOOOOOOOOOOOOOOOO", "no closest obs");
                setObstacle(y, x);
                setNumberGrid(tag, x, y);
            }
        } else if (dir == 270) {
            if (isObstacle(x - 1, y - 2)) {//c=>a
                setNumberGrid(tag, x - 1, y - 2);
                setArrow(x, y, 2);
                Log.d("111111111111111111111111111", "inside a");
            } else if (isObstacle(x, y - 2)) {//a=>b
                setNumberGrid(tag, x, y - 2);
                setArrow(x+1, y, 2);
                Log.d("111111111111111111111111111", "inside b");
            } else if (isObstacle(x - 1, y - 3)) {//e=>c
                setNumberGrid(tag, x - 1, y - 3);
                setArrow(x, y-1, 2);
                Log.d("111111111111111111111111111", "inside c");
            } else if (isObstacle(x, y - 3)) {//b=>d
                setNumberGrid(tag, x, y - 3);
                setArrow(x+1, y-1, 2);
                Log.d("111111111111111111111111111", "inside d");
            } else if (isObstacle(x + 1, y - 2)) {//d=>e
                setNumberGrid(tag, x + 1, y - 2);
                setArrow(x+2, y, 2);
                Log.d("111111111111111111111111111", "inside e");
            } else if (isObstacle(x + 1, y - 3)) {//f
                setNumberGrid(tag, x + 1, y - 3);
                setArrow(x+2, y-1, 2);
                Log.d("111111111111111111111111111", "inside f");
            } else {
                Log.d("OOOOOOOOOOOOOOOOOOOO", "no closest obs");
                setObstacle(y, x);
                setNumberGrid(tag, x, y);
            }

        }
        invalidate();
    }
    public void refreshExploration() {
        for (int x = 0; x < ROWS_SIZE; x++) {
            for (int y = 0; y < COLUMNS_SIZE; y++) {
                explored[x][y] = 0;
                obstacle[x][y] = 0;
                obstacleNumberGrid[x][y] = null;
            }
        }
    }

    public void setExplored(int y, int x){
        if (x>=0 && x<COLUMNS_SIZE && y>=0 && y<ROWS_SIZE){
            explored[y][x]=1;
            updateMap();
        }else{
            Log.d("QQQQQQQQQQQQQQQQQ","explored outside of cells");
        }
    }

    public void setUnexplored(int y, int x){
        if (x>=0 && x<COLUMNS_SIZE && y>=0 && y<ROWS_SIZE){
            explored[y][x]=0;
            updateMap();
        }else{
            Log.d("QQQQQQQQQQQQQQQQQ","unexplored outside of cells");
        }
    }

    private void initalizeImageLUT() {
        imageMap = new HashMap<String, String>();
//        imageMap.put("0", "down");
//        imageMap.put("1", "left");
//        imageMap.put("2", "right");
//        imageMap.put("3", "up");
//        imageMap.put("4", "circle");
//        imageMap.put("5", "eight");
//        imageMap.put("6", "nine");
//        imageMap.put("7", "seven");
//        imageMap.put("8", "six");
//        imageMap.put("9", "v");
//        imageMap.put("10", "w");
//        imageMap.put("11", "x");
//        imageMap.put("12", "y");
//        imageMap.put("13", "z");
//        imageMap.put("14", "zero");
        imageMap.put("0", "2");
        imageMap.put("1", "4");
        imageMap.put("2", "3");
        imageMap.put("3", "1");
        imageMap.put("4", "5");
        imageMap.put("5", "8");
        imageMap.put("6", "9");
        imageMap.put("7", "7");
        imageMap.put("8", "6");
        imageMap.put("9", "11");
        imageMap.put("10", "12");
        imageMap.put("11", "13");
        imageMap.put("12", "14");
        imageMap.put("13", "15");
        imageMap.put("14", "10");
    }

    public void setNumberGrid(String item, int x, int y) {
        if (x >= 0 && x < COLUMNS_SIZE && y >= 0 && y < ROWS_SIZE) {

            if (obstacle[y][x] == 1) {
                obstacleNumberGrid[y][x] = imageMap.get(item);
                Log.d("{{{{{{{{{{{{{{{{{{{{{{{{{", "x: " + (x) + " y: " + (y));
            }else{
                Log.d("Not obstacle so no number grid!", "x: " + (x) + " y: " + (y));
            }
        }else{
            Log.d("QQQQQQQQQQQQQQQQQ","numbergrid outside of cells");
        }
    }

    public void setCurrentAngle(int ang){
        currentAngle=ang;
        fixcurrentAngle();
        updateMap();
    }

    public Path[] makeArrow(int i, int j, int dir) {
        //these is location of the base vertices of triangle in arrow
        float tri1x = 0, tri1y = 0, tri2x = 0, tri2y = 0;

        //this is location of straight line of arrow
        float sx = 0, sy = 0, fx = 0, fy = 0;
        switch (dir) {
            case 0:
                sx = (float) (i * cellWidth - 0.5 * cellHeight);
                sy = (ROWS_SIZE + 1 - j) * cellHeight;
                fx = (float) (i * cellWidth - 0.5 * cellHeight);
                fy = (ROWS_SIZE - j) * cellHeight;
                tri1x = (float) (fx - 0.5 * cellHeight);
                tri1y = (float) (fy + 0.5 * cellHeight);
                tri2x = (float) (fx + 0.5 * cellHeight);
                tri2y = (float) (fy + 0.5 * cellHeight);
                break;
            case 1:
                sx = (i - 1) * cellWidth;
                sy = (float) ((ROWS_SIZE - j) * cellHeight + 0.5 * cellHeight);
                fx = (i) * cellWidth;
                fy = (float) ((ROWS_SIZE - j) * cellHeight + 0.5 * cellHeight);
                tri1x = (float) (fx - 0.5 * cellHeight);
                tri1y = (float) (fy - 0.5 * cellHeight);
                tri2x = (float) (fx - 0.5 * cellHeight);
                tri2y = (float) (fy + 0.5 * cellHeight);
                break;
            case 2:
                fx = (float) (i * cellWidth - 0.5 * cellHeight);
                fy = (ROWS_SIZE + 1 - j) * cellHeight;
                sx = (float) ((i) * cellWidth - 0.5 * cellHeight);
                sy = (ROWS_SIZE - j) * cellHeight;
                tri1x = (float) (fx - 0.5 * cellHeight);
                tri1y = (float) (fy - 0.5 * cellHeight);
                tri2x = (float) (fx + 0.5 * cellHeight);
                tri2y = (float) (fy - 0.5 * cellHeight);
                break;
            case 3:
                fx = (i - 1) * cellWidth;
                fy = (float) ((ROWS_SIZE - j) * cellHeight + 0.5 * cellHeight);
                sx = (i) * cellWidth;
                sy = (float) ((ROWS_SIZE - j) * cellHeight + 0.5 * cellHeight);
                tri1x = (float) (fx + 0.5 * cellHeight);
                tri1y = (float) (fy - 0.5 * cellHeight);
                tri2x = (float) (fx + 0.5 * cellHeight);
                tri2y = (float) (fy + 0.5 * cellHeight);
                break;
            default:
                break;
        }
        Log.d("sx", ": " + sx);
        Log.d("sy", ": " + sy);
        Log.d("fx", ": " + fx);
        Log.d("fy", ": " + fy);
        Log.d("tri1x", ": " + tri1x);
        Log.d("tri1y", ": " + tri1y);
        Log.d("tri2x", ": " + tri2x);
        Log.d("tri2y", ": " + tri2y);

        Path[] ret = new Path[2];
        //draw Straight line for arrow
        Path linePath = new Path();
        linePath.moveTo(sx - 2, sy - 2);
        linePath.lineTo(fx - 2, fy - 2);
        linePath.lineTo(fx + 2, fy + 2);
        linePath.lineTo(sx + 2, sy + 2);
        linePath.close();
        ret[0] = linePath;

        //draw triangle for arrow
        Path triangle = new Path();
        triangle.moveTo(fx, fy);
        triangle.lineTo(tri1x, tri1y);
        triangle.lineTo(tri2x, tri2y);
        triangle.close();
        ret[1] = triangle;

        return ret;
    }

    public void setArrow(int x, int y, int dir) {
        Log.d("%%%%%%%%%%%%%%%%%%%%", "SET ARROW METHOD");
//        for (int i = 0; i < arrow.length; i++) {
//            if (arrow[i] == null) {
//                arrow[i][1] = x;
//                arrow[i][2] = y;
//                arrow[i][3] = dir;
//            }
//        }
        ArrayList<Integer> arr = new ArrayList<Integer>();
        arr.add(x);
        arr.add(y);
        arr.add(dir);
        arrowIcons.add(arr);
        invalidate();
    }

    public void resetMap(){
        robotCenter[0]=1;
        robotCenter[1]=1;
        robotFront[0]=1;
        robotFront[1]=2;
        currentAngle=0;
        waypoint[0]=1;
        waypoint[1]=1;
        arrowIcons.clear();
        refreshExploration();
        invalidate();
    }



}
