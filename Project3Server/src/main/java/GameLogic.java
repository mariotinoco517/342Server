public class GameLogic {
    private static final int col = 7;
    private static final int row = 6;
    int[][] Grid;
    int counter = 0;
    int playerToPlace = 1;

    public GameLogic() {
        fillInitialBoard();
    }
    //I made it an int so we can represent... 0 = invalid move ... 1 = valid move ... 2 = won game
    public int makeMove(int colCoor, int playerNum){
        if(playerNum != playerToPlace){
            System.err.println("ERROR IN PLAYERTOPALCE VALIDATION");
            return 0;
        }
        if(colCoor > 6 || colCoor < 0){ // this should never be the case but just in case
            System.err.println("ERROR IN COL VALIDATION");
            return 0;
        }
        if(!canPlace(colCoor)){
            System.err.println("CANNOT PLACE IN THE COLUMN");
            return 0;
        }
        boolean placed = false;
        for(int x = 0; x < row; ++x){
            if(Grid[x][colCoor] == -1){
                Grid[x][colCoor] = playerNum;
                placed = true;
                if(playerToPlace == 1){
                    playerToPlace = 2;
                }else{
                    playerToPlace = 1;
                }
                break;
            }
        }
        if(!placed){
            return 0;
        }
        if(ifWin(playerNum)){
            return 2;
        }
        return 1; //I cant think of another case sooooo if you do just add it
    }
    public void fillInitialBoard(){
        Grid = new int[6][7];
        for(int x = 0; x < row; ++x){
            for(int y = 0; y < col; ++y){
                Grid[x][y] = -1;
            }
        }
    }
    public boolean canPlace(int colCoor){
        if(Grid == null){
            fillInitialBoard();
        }
        for(int x = 0; x < row; ++x){
            for(int y = 0; y < col; ++y){
                if(Grid[x][y] == -1){
                    return true;
                }
            }
        }
        return false;
    }
    public boolean ifWin(int playerNum){
        if(Grid == null){
            fillInitialBoard();
        }
        if(checkDiagonal(playerNum)){
            return true;
        }
        else if(checkHorizontal(playerNum)){
            return true;
        }
        else if(checkVertical(playerNum)){
            return true;
        }
        return false;
    }
    //x represents row number and y represents column number because it is a 6x7 grid
    public boolean checkDiagonal(int playerNum){
        //bottom left to top right diagonal
        for(int x = 3; x < row; ++x) {
            for(int y = 0; y < col-3; ++y){
                if(Grid[x][y] == playerNum &&
                        Grid[x-1][y+1] == playerNum &&
                        Grid[x-2][y+2] == playerNum &&
                        Grid[x-3][y+3] == playerNum){
                    return true;
                }
            }
        }
        //top left to bottom right diagonal
        for(int x = 0; x < row-3; ++x) {
            for(int y = 0; y < col-3; ++y){
                if(Grid[x][y] == playerNum &&
                        Grid[x+1][y+1] == playerNum &&
                        Grid[x+2][y+2] == playerNum &&
                        Grid[x+3][y+3] == playerNum){
                    return true;
                }
            }
        }
        return false;
    }
    public boolean checkVertical(int playerNum){
        for(int x = 0; x < row; ++x){
            for(int y = 0; y < col; ++y){
                if(Grid[x][y] == playerNum){
                    ++counter;
                }
                else{
                    counter = 0;
                }
                if(counter == 4){
                    counter = 0;
                    return true;
                }
            }
        }
        counter = 0;
        return false;
    }
    public boolean checkHorizontal(int playerNum){
        for(int y = 0; y < col; ++y){
            for(int x = 0; x < row; ++x){
                if(Grid[x][y] == playerNum){
                    ++counter;
                }
                else{
                    counter = 0;
                }
                if(counter == 4){
                    counter = 0;
                    return true;
                }
            }
        }
        counter = 0;
        return false;
    }

    public void clearGame(){
        fillInitialBoard();
        playerToPlace = 1;
    }
}
