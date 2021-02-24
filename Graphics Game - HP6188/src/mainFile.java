import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.Scanner;

public class mainFile extends Application {

    GraphicsContext gc;
    Square[][] grid;
    int moveCounter;
    boolean dead;
    static int mineNumber;
    boolean firstMove = true;
    Image mine,flag;
    static int flagNum;

    public static void main(String[] args)
    {
        Scanner keyboard = new Scanner(System.in);

        System.out.println("**Minesweeper Rules**");
        System.out.println("To play, click a random square, the grid will be generated based on your intial click");
        System.out.println("After you click, the mines will be randomly placed, hidden around you");
        System.out.println("Your only clue is the number present on each square once a mine is clicked");
        System.out.println("This number represents the number of mines surrounding a square in all 8 surrounding directions");
        System.out.println("You win by clearing all the squares with no mines");
        System.out.println("If you click a mine, you lose and all the mines will appear");
        System.out.println("You can place flags by right clicking and remove them by right clicking, these are markers for possible mines");
        System.out.println("Have Fun and Be Safe!");
        System.out.println("");

        do{
            System.out.println("Enter mine number 1 - 90: ");
            mineNumber = keyboard.nextInt();
        }while(mineNumber<1||mineNumber>90);

        flagNum = mineNumber;

        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        mine = new Image("File:Images/mine.jpg");
        flag = new Image("File:Images/flagUse.png");

        grid = new Square[10][10];
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                grid[row][col] = new Square();
            }
        }
        moveCounter = 0;
        dead = false;

        stage.setTitle("Minesweeper");
        Group group = new Group();
        Canvas canvas = new Canvas(400, 500);
        group.getChildren().add(canvas);
        Scene scene = new Scene(group);
        stage.setScene(scene);
        gc = canvas.getGraphicsContext2D();

        canvas.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                double x = mouseEvent.getX();
                double y = mouseEvent.getY();
                int col = (int) x / 40;
                int row = (int) y / 40;

                if(x<=400&&y<=400&& !win(grid)&& !dead&&mouseEvent.getButton().name().equals("PRIMARY")&&!grid[row][col].isMarked()) {

                    if(firstMove)
                    {
                        generateGrid(row, col);
                        //gridMap();
                        select(row,col);
                        flagChecker();
                        firstMove = false;
                    }

                    //grid[row][col].setVisited(true);
                    select(row,col);
                    flagChecker();
                    moveCounter++;

                    if(grid[row][col].isMine()) {
                        dead = true;
                        removeFlags();
                        showMines();
                    }
                }

                else if(x<=400&&y<=400&& !win(grid)&& !dead&&mouseEvent.getButton().name().equals("SECONDARY"))
                {
                    if(!grid[row][col].isVisited()&&grid[row][col].isMarked()) {
                        grid[row][col].setMarked(false);
                        flagNum++;
                    }
                    else if(!grid[row][col].isVisited()&&!grid[row][col].isMarked()&&flagNum>0)
                    {
                        grid[row][col].setMarked(true);
                        flagNum--;
                    }
                }

                else if(x<=100&&x>=25&&y<=460&&y>=410&&(win(grid)||dead))
                    reset();
                draw(gc);
            }
        });


        draw(gc);
        canvas.requestFocus();
        stage.show();
    }

    public void draw(GraphicsContext gc)
    {

        gc.setFill(Color.WHITE);
        gc.fillRect(0,0,400,500);

        if(dead) {
            gc.setFill(Color.RED);
            gc.fillText("Game Over: You Hit a Mine!", 250, 450);
        }

        if(win(grid))
        {
            gc.setFill(Color.RED);
            gc.fillText("You Won in "+moveCounter+" moves!", 250, 450);
        }

        int xDraw = 0, yDraw = 0;

        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if(grid[row][col].isVisited()&&!grid[row][col].isMine())
                {
                    gc.setFill(Color.WHITESMOKE);
                    gc.fillRect(xDraw, yDraw, 40,40);
                    gc.strokeRect(xDraw, yDraw, 40,40);
                    gc.setFill(Color.BLACK);
                    if(mineAround(grid, row, col)!=0)
                        gc.fillText(""+mineAround(grid, row, col), xDraw+17, yDraw+23);
                }
                else if(grid[row][col].isVisited()&&grid[row][col].isMine())
                {
                    gc.setFill(Color.WHITESMOKE);
                    gc.fillRect(xDraw, yDraw, 40,40);
                    gc.strokeRect(xDraw, yDraw, 40,40);
                    gc.drawImage(mine, xDraw, yDraw);
                    gc.strokeRect(xDraw, yDraw, 40,40);
                }
                else
                {
                    gc.setFill(Color.GRAY);
                    gc.fillRect(xDraw, yDraw, 40,40);
                    gc.strokeRect(xDraw, yDraw, 40,40);
                }
                xDraw += 40;
            }
            xDraw = 0;
            yDraw +=40;
        }

        gc.setFill(Color.RED);
        gc.fillRect(25, 410, 75,50);
        gc.setFill(Color.BLACK);
        gc.fillText("Reset", 47, 437);

        gc.setFill(Color.BLUE);
        gc.fillText("Move Counter: "+moveCounter, 150, 425);
        gc.fillText("Flag Counter: "+flagNum, 150, 440);


        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if(grid[row][col].isMarked()) {
                    gc.drawImage(flag , col*40, row*40);
                }

            }
        }


    }

    public boolean win(Square[][] grid)
    {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if(grid[row][col].isVisited()==false&&grid[row][col].isMine()==false)
                    return false;
            }
        }
        return true;
    }

    public void reset()
    {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                grid[row][col].setVisited(false);
                grid[row][col].setMine(false);
            }
        }

        flagNum = mineNumber;
        moveCounter = 0;
        dead = false;
        firstMove = true;
        removeFlags();
    }

    public int mineAround(Square[][] grid, int row, int col)
    {
        int mine = 0;

        if(row-1>=0&&grid[row-1][col].isMine()) {
            mine++;
        }
        if(col-1>=0&&grid[row][col-1].isMine()) {
            mine++;
        }
        if(col-1>=0&&row-1>=0&&grid[row-1][col-1].isMine()) {
            mine++;
        }
        if(row-1>=0&&col+1<10&&grid[row-1][col+1].isMine()) {
            mine++;
        }
        if(col+1<10&&grid[row][col+1].isMine()) {
            mine++;
        }
        if(row+1<10&&grid[row+1][col].isMine()) {
            mine++;
        }
        if(row+1<10&&col-1>=0&&grid[row+1][col-1].isMine()) {
            mine++;
        }
        if(row+1<10&&col+1<10&&grid[row+1][col+1].isMine()) {
            mine++;
        }
        return mine;
    }

    public void generateGrid(int playerRow, int playerCol)
    {
        for (int mines = 0; mines < mineNumber;) {
            int row = (int)(Math.random()*10);
            int col = (int)(Math.random()*10);

            if((playerRow==row&&playerCol==col)||grid[row][col].isMine())
                continue;
            grid[row][col].setMine(true);
            mines++;
        }
    }

    public void gridMap()
    {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if(grid[row][col].isMine()&&col==9)
                    System.out.print("M");
                else if(grid[row][col].isMine())
                    System.out.print("M, ");
                else if(col==9)
                    System.out.print("0");
                else
                    System.out.print("0, ");
            }
            System.out.println("");
        }
    }

    public void showMines()
    {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if(grid[row][col].isMine())
                    grid[row][col].setVisited(true);
            }
        }
    }

    public void select(int row, int col)
    {
        if (row < 0 || row >= 10 || col < 0 || col >= 10)
            return;
        if (!grid[row][col].isMine() && !grid[row][col].isVisited()) {
            grid[row][col].setVisited(true);
            select( row+1, col );
            select( row-1, col );
            select( row, col-1 );
            select( row, col+1 );
        }
        else
            return;

    }

    public void flagChecker()
    {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if(grid[row][col].isVisited()&&grid[row][col].isMarked())
                    grid[row][col].setMarked(false);

            }
        }
    }

    public void removeFlags()
    {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                grid[row][col].setMarked(false);
            }
        }
    }

}
