public class GameLogic {
    private static final int col = 7;
    private static final int row = 6;
    int[][] Grid;


    public void fillBoard(){
        Grid = new int[6][7];
        for(int x = 0; x < row; ++x){
            for(int y = 0; y < col; ++y){
                Grid[x][y] = -1;
            }
        }
    }
    public boolean canPlace(int xCoor){
        if(Grid == null){
            fillBoard();
        }

        return false;
    }
}
