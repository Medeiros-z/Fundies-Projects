import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// Represents the MineSweeper game
class MinesweeperWorld extends World {
  ArrayList<ArrayList<Cell>> cells;
  Random rand;
  int flags; // Amount of flags the user has placed
  int timer; // Amount of time taken to complete the puzzle
  boolean startGame; // Is this game on the menu or has it started?
  boolean hasWon; // True if user reveals all non-mine cells
  boolean hasLost; // True if the user has hit a mine

  // Initial game constructor
  MinesweeperWorld(Random rand) {
    this.cells = new ArrayList<ArrayList<Cell>>();
    this.rand = rand;
    this.flags = 0;
    this.timer = 0;
    this.startGame = false;
    this.hasWon = false;
    this.hasLost = false;
  }

  // Generate game grid
  // EFFECT: update this.cells to add cells
  void generateGrid(int rows, int columns) {
    for (int i = 0; i < rows; i++) {
      this.cells.add(new ArrayList<Cell>());
      for (int j = 0; j < columns; j++) {
        this.cells.get(i).add(new Cell());
      }
    }
  }

  // Generate a variable amount of mines in this.cells
  // Effect: update cells in this.cells to have mines
  void generateMines(int startingMines) {
    int randRow;
    int randCol;
    while (this.countMines() < startingMines) {
      randRow = this.rand.nextInt(this.cells.size());
      randCol = this.rand.nextInt(this.cells.get(randRow).size());
      this.cells.get(randRow).get(randCol).addMine();
    }
  }

  // Count mines in this.cells
  int countMines() {
    int totalMines = 0;
    for (ArrayList<Cell> row : this.cells) {
      for (Cell cell : row) {
        // Note: Per piazza, accessing this is not mutating any data, just seeing a
        // boolean
        if (cell.hasMine()) {
          totalMines += 1;
        }
      }
    }
    return totalMines;
  }

  // Creates a list for each cells neighboring cells
  // EFFECT: for each cell in this.cells calculate and set the cells neighbors
  void calculateNeighbors() {
    for (int i = 0; i < this.cells.size(); i++) {
      for (int j = 0; j < this.cells.get(i).size(); j++) {
        Cell thisCell = this.cells.get(i).get(j);
        thisCell.addNeighbors(this.getNeighbors(i, j));
      }
    }
  }

  // Using the given x, y position grab the neighbors of the given position
  ArrayList<Cell> getNeighbors(int x, int y) {
    ArrayList<Cell> neighbors = new ArrayList<Cell>();
    ArrayList<Integer> xRange = new ArrayList<Integer>(Arrays.asList(x - 1, x, x + 1));
    ArrayList<Integer> yRange = new ArrayList<Integer>(Arrays.asList(y - 1, y, y + 1));

    for (int thisX : xRange) {
      for (int thisY : yRange) {
        // Validate that the x, y positions are valid in this game
        boolean sameXY = (x == thisX && y == thisY);
        boolean naturalXY = (thisX >= 0 && thisY >= 0);
        boolean goodXY = thisX < this.cells.size() && thisY < this.cells.get(x).size();

        if (!sameXY && naturalXY && goodXY) {
          neighbors.add(this.cells.get(thisX).get(thisY));
        }
      }
    }

    return neighbors;
  }

  // Returns true if the user has won by revealing all non-mine cells
  boolean hasWon() {
    boolean won = true;
    for (ArrayList<Cell> row : this.cells) {
      for (Cell cell : row) {
        won = won && cell.hasWonHelper();
      }
    }
    return won;
  }

  // Make Scene for Mine Sweeper
  public WorldScene makeScene() {
    WorldScene ws = new WorldScene(IUtils.SCREEN_WIDTH, IUtils.SCREEN_HEIGHT);

    ws.placeImageXY(
        new TextImage("Timer: " + this.timer + " | Flags: " + this.flags, 30, Color.BLACK),
        IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT - 2 * IUtils.FONT_SIZE);

    // Initialize new game scene
    if (!this.startGame) {
      ws.placeImageXY(new TextImage("MineSweeper", 40, Color.BLACK), IUtils.SCREEN_WIDTH / 2,
          IUtils.SCREEN_HEIGHT / 10);
      ws.placeImageXY(new TextImage("Follow the directions below to start a new game.",
          IUtils.FONT_SIZE, Color.BLACK), IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT / 5);
      ws.placeImageXY(new TextImage("Press 1 to play in easy mode (10 mines in a 9 x 9 board)",
          IUtils.FONT_SIZE, Color.BLACK), IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT / 3);
      ws.placeImageXY(
          new TextImage("Press 2 to play in medium mode (40 mines in a 16 x 16 board)",
              IUtils.FONT_SIZE, Color.BLACK),
          IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT / 3 + 2 * IUtils.FONT_SIZE);
      ws.placeImageXY(
          new TextImage("Press 3 to play in hard mode (99 mines in a 30 x 16 board)",
              IUtils.FONT_SIZE, Color.BLACK),
          IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT / 3 + 4 * IUtils.FONT_SIZE);

      // Add lose message if user loses
      if (this.hasLost) {
        ws.placeImageXY(new TextImage("You Lost!", 40, Color.RED), IUtils.SCREEN_WIDTH / 2,
            IUtils.SCREEN_HEIGHT - 6 * IUtils.FONT_SIZE);
      }

      // Add win message if user wins
      if (this.hasWon) {
        ws.placeImageXY(new TextImage("You Win!", 40, Color.GREEN), IUtils.SCREEN_WIDTH / 2,
            IUtils.SCREEN_HEIGHT - 6 * IUtils.FONT_SIZE);
      }

      return ws;
    }

    for (int i = 0; i < this.cells.size(); i++) {
      for (int j = 0; j < this.cells.get(i).size(); j++) {
        Cell cell = this.cells.get(i).get(j);
        ws.placeImageXY(cell.drawCell(), IUtils.CELL_SIZE + IUtils.CELL_SIZE * i,
            IUtils.CELL_SIZE + IUtils.CELL_SIZE * j);
      }
    }

    return ws;

  }

  // OnMouseClicked event handler, handles mouse click and does corresponding
  // actions
  // EFFECT: Reveals cells on a left click, or places a flag on a right click
  public void onMouseClicked(Posn posn, String button) {
    int x = (posn.x - IUtils.CELL_SIZE / 2) / IUtils.CELL_SIZE;
    int y = (posn.y - IUtils.CELL_SIZE / 2) / IUtils.CELL_SIZE;

    // Check if the position clicked is a cell
    if (x >= this.cells.size() || y >= this.cells.get(0).size()) {
      return;
    }

    Cell clickedCell = this.cells.get(x).get(y);

    if (button.equals("LeftButton")) {
      clickedCell.revealCell();
      // Calculate win/lose conditions and end game if either are true
      this.hasLost = clickedCell.hasLost();
      this.hasWon = this.hasWon();
      this.startGame = !this.hasLost && !this.hasWon;
    }
    else if (button.equals("RightButton")) {
      this.flags += clickedCell.updateFlag();
    }

  }

  // On key handler, checks if key can be used to further the game and does so if
  // possible
  // EFFECT: Starts the game with a difficulty based on what key is pressed
  public void onKeyEvent(String key) {
    if (this.startGame) {
      return;
    }

    this.cells = new ArrayList<ArrayList<Cell>>();
    this.timer = 0;
    this.hasWon = false;
    this.hasLost = false;

    if (key.equals("1")) {
      this.startGame = true;
      this.flags = 10;
      this.generateGrid(9, 9);
      this.generateMines(10);
      this.calculateNeighbors();
    }
    else if (key.equals("2")) {
      this.startGame = true;
      this.flags = 40;
      this.generateGrid(16, 16);
      this.generateMines(40);
      this.calculateNeighbors();
    }
    else if (key.equals("3")) {
      this.startGame = true;
      this.flags = 99;
      this.generateGrid(30, 16);
      this.generateMines(99);
      this.calculateNeighbors();
    }
  }

  // OnTick handler, every tick increment timer by one if the game has started
  // EFFECT: increment timer by one if the game has started
  public void onTick() {
    if (this.startGame) {
      this.timer += 1;
    }
  }
}

// Represents a cell in MineSweeper
class Cell {
  ArrayList<Cell> neighbors; // Neighbors to the cells
  boolean hasMine; // Does this cell have a mine in it?
  boolean hasFlag; // Does this cell have a flag over it?
  boolean isRevealed; // Has this cell been clicked/revealed to user
  int neighboringMines; // Number of neighboring mines

  Cell() {
    this.neighbors = new ArrayList<Cell>();
    this.hasMine = false;
    this.hasFlag = false;
    this.isRevealed = false;
  }

  // Set the this.neighbor to the given list and
  // uses it to see how many mines are near this cell
  // EFFECT: Add neighbors to this.neighbor and calculate this.neighboringMines
  void addNeighbors(ArrayList<Cell> neighbors) {
    this.neighbors = neighbors;
    this.neighboringMines = this.countNeighboringMines();
  }

  // Return the amount of mines in neighboring cells
  int countNeighboringMines() {
    int neighboringMines = 0;
    for (Cell cell : this.neighbors) {
      // Note: Per piazza, accessing this is not mutating any data, just seeing a
      // boolean
      if (cell.hasMine()) {
        neighboringMines += 1;
      }
    }
    return neighboringMines;
  }

  // EFFECT: Adds a mine to this cell, turning the this.hasMine field true
  void addMine() {
    this.hasMine = true;
  }

  // Does this cell have a mine in it?
  boolean hasMine() {
    return this.hasMine;
  }

  // Returns -1 if a flag was added, 1 if one was removed, and 0 if no flag was
  // placed
  // EFFECT: Updates flag on this cell, turning the this.hasFlag reversing the
  // boolean
  int updateFlag() {
    if (!this.isRevealed) {
      this.hasFlag = !this.hasFlag;
      if (this.hasFlag) {
        return -1;
      }
      return 1;
    }
    return 0;
  }

  // Reveals this cell, if there is no mines near this cell, reveal its neighbors
  // too
  // Don't reveal if its already revealed or it has a flag on it
  // EFFECT: Reveal this.cell
  void revealCell() {
    if (this.hasMine && !this.hasFlag) {
      this.isRevealed = true;
      return;
    }
    if (!this.isRevealed && !this.hasFlag) {
      this.isRevealed = true;
      if (this.neighboringMines == 0) {
        for (Cell cell : this.neighbors) {
          cell.revealCell();
        }
      }
    }
  }

  // Returns true if the user has lost, i.e. if the cell is revealed and has a
  // mine
  boolean hasLost() {
    return this.hasMine && this.isRevealed;
  }

  // Helper for hasWon, checks if this cell has been revealed if not a mine, or
  // not revealed if mine
  boolean hasWonHelper() {
    return (this.isRevealed && !this.hasMine) || (this.hasMine && !this.isRevealed);
  }

  // Draws cell, with all its features, on the grid
  WorldImage drawCell() {
    WorldImage insideImage = new RectangleImage(IUtils.CELL_SIZE, IUtils.CELL_SIZE, "solid",
        Color.CYAN);
    ArrayList<Color> colors = new ArrayList<Color>(
        Arrays.asList(Color.BLACK, Color.BLUE, Color.GREEN, Color.RED, Color.DARK_GRAY,
            Color.MAGENTA, Color.ORANGE, Color.PINK, Color.YELLOW));

    if (this.hasMine && this.isRevealed) {
      insideImage = new OverlayImage(new CircleImage(IUtils.MINE_SIZE, "solid", Color.RED),
          new RectangleImage(IUtils.CELL_SIZE, IUtils.CELL_SIZE, "solid", Color.WHITE));
    }
    else if (this.hasFlag) {
      insideImage = new OverlayImage(
          new EquilateralTriangleImage(IUtils.FLAG_SIZE, "solid", Color.YELLOW), insideImage);
    }
    else if (this.isRevealed) {
      String cellString = "";
      if (this.neighboringMines > 0) {
        cellString = Integer.toString(this.neighboringMines);
      }
      insideImage = new OverlayImage(new TextImage(cellString, colors.get(neighboringMines)),
          new RectangleImage(IUtils.CELL_SIZE, IUtils.CELL_SIZE, "solid", Color.WHITE));
    }

    return new FrameImage(insideImage, Color.BLACK);
  }
}

// Constants for the game
interface IUtils {
  int SCREEN_WIDTH = 800;
  int SCREEN_HEIGHT = 500;
  int FONT_SIZE = 25;
  int CELL_SIZE = 25; // Cell Size
  int MINE_SIZE = 10;
  int FLAG_SIZE = 15;
}

// Examples class for MineSweeper
class ExamplesMineSweeper {

  MinesweeperWorld game1;
  MinesweeperWorld game2;
  Cell cell1;
  Cell cell2;

  void initTestConditions() {
    this.game1 = new MinesweeperWorld(new Random(1));
    this.game2 = new MinesweeperWorld(new Random(2));
    this.cell1 = new Cell();
    this.cell2 = new Cell();
  }

  void testBigBang(Tester t) {
    this.initTestConditions();
    this.game1.bigBang(IUtils.SCREEN_WIDTH, IUtils.SCREEN_HEIGHT, 1);
  }

  // test the method generateGrid in the MinesweeperWorld class
  void testGenerateGrid(Tester t) {
    this.initTestConditions();
    t.checkExpect(this.game1.cells, new ArrayList<ArrayList<Cell>>());
    t.checkExpect(this.game2.cells, new ArrayList<ArrayList<Cell>>());

    this.game1.generateGrid(2, 2);
    this.game2.generateGrid(4, 3);
    t.checkExpect(this.game1.cells,
        new ArrayList<ArrayList<Cell>>(
            Arrays.asList(new ArrayList<Cell>(Arrays.asList(new Cell(), new Cell())),
                new ArrayList<Cell>(Arrays.asList(new Cell(), new Cell())))));
    t.checkExpect(this.game2.cells,
        new ArrayList<ArrayList<Cell>>(
            Arrays.asList(new ArrayList<Cell>(Arrays.asList(new Cell(), new Cell(), new Cell())),
                new ArrayList<Cell>(Arrays.asList(new Cell(), new Cell(), new Cell())),
                new ArrayList<Cell>(Arrays.asList(new Cell(), new Cell(), new Cell())),
                new ArrayList<Cell>(Arrays.asList(new Cell(), new Cell(), new Cell())))));
  }

  // test the method generateMines in the MinesweeperWorld class
  void testGenerateMines(Tester t) {
    this.initTestConditions();
    Cell mineCell = new Cell();
    mineCell.addMine();

    this.game1.generateGrid(2, 2);
    this.game2.generateGrid(4, 3);
    t.checkExpect(this.game1.cells,
        new ArrayList<ArrayList<Cell>>(
            Arrays.asList(new ArrayList<Cell>(Arrays.asList(new Cell(), new Cell())),
                new ArrayList<Cell>(Arrays.asList(new Cell(), new Cell())))));
    t.checkExpect(this.game2.cells,
        new ArrayList<ArrayList<Cell>>(
            Arrays.asList(new ArrayList<Cell>(Arrays.asList(new Cell(), new Cell(), new Cell())),
                new ArrayList<Cell>(Arrays.asList(new Cell(), new Cell(), new Cell())),
                new ArrayList<Cell>(Arrays.asList(new Cell(), new Cell(), new Cell())),
                new ArrayList<Cell>(Arrays.asList(new Cell(), new Cell(), new Cell())))));

    this.game1.generateMines(2);
    this.game2.generateMines(4);
    t.checkExpect(this.game1.cells,
        new ArrayList<ArrayList<Cell>>(
            Arrays.asList(new ArrayList<Cell>(Arrays.asList(mineCell, new Cell())),
                new ArrayList<Cell>(Arrays.asList(mineCell, new Cell())))));
    t.checkExpect(this.game2.cells,
        new ArrayList<ArrayList<Cell>>(
            Arrays.asList(new ArrayList<Cell>(Arrays.asList(new Cell(), new Cell(), new Cell())),
                new ArrayList<Cell>(Arrays.asList(mineCell, new Cell(), new Cell())),
                new ArrayList<Cell>(Arrays.asList(mineCell, new Cell(), new Cell())),
                new ArrayList<Cell>(Arrays.asList(mineCell, mineCell, new Cell())))));
  }

  // test the method countMines in the MinesweeperWorld class
  void testCountMines(Tester t) {
    this.initTestConditions();

    this.game1.generateGrid(2, 2);
    this.game2.generateGrid(4, 3);

    t.checkExpect(this.game1.countMines(), 0);
    t.checkExpect(this.game2.countMines(), 0);

    this.game1.generateMines(2);
    this.game2.generateMines(4);

    t.checkExpect(this.game1.countMines(), 2);
    t.checkExpect(this.game2.countMines(), 4);
  }

  // test the method calculateNeighbors in the MinesweeperWorld class
  void testCalculateNeighbors(Tester t) {
    this.initTestConditions();

    this.game1.generateGrid(2, 2);
    this.game2.generateGrid(4, 3);

    t.checkExpect(this.game1.cells.get(0).get(0).neighbors, new ArrayList<Cell>());
    t.checkExpect(this.game2.cells.get(1).get(1).neighbors, new ArrayList<Cell>());

    this.game1.calculateNeighbors();
    this.game2.calculateNeighbors();

    t.checkExpect(this.game1.cells.get(0).get(0).neighbors,
        new ArrayList<Cell>(Arrays.asList(this.game1.cells.get(0).get(1),
            this.game1.cells.get(1).get(0), this.game1.cells.get(1).get(1))));

    t.checkExpect(this.game2.cells.get(1).get(1).neighbors,
        new ArrayList<Cell>(
            Arrays.asList(this.game2.cells.get(0).get(0), this.game2.cells.get(0).get(1),
                this.game2.cells.get(0).get(2), this.game2.cells.get(1).get(0),
                this.game2.cells.get(1).get(2), this.game2.cells.get(2).get(0),
                this.game2.cells.get(2).get(1), this.game2.cells.get(2).get(2))));
  }

  // test the method getNeighbors in the MinesweeperWorld class
  void testGetNeighbors(Tester t) {
    this.initTestConditions();

    t.checkExpect(this.game1.getNeighbors(1, 1), new ArrayList<Cell>());
    t.checkExpect(this.game2.getNeighbors(2, 2), new ArrayList<Cell>());

    this.game1.generateGrid(2, 2);
    this.game2.generateGrid(3, 3);

    t.checkExpect(this.game1.getNeighbors(1, 1),
        new ArrayList<Cell>(Arrays.asList(new Cell(), new Cell(), new Cell())));
    t.checkExpect(this.game2.getNeighbors(1, 1), new ArrayList<Cell>(Arrays.asList(new Cell(),
        new Cell(), new Cell(), new Cell(), new Cell(), new Cell(), new Cell(), new Cell())));
  }

  // test the method hasWon in the MinesweeperWorld class
  void testHasWon(Tester t) {
    this.initTestConditions();

    this.game1.generateGrid(2, 2);
    this.game1.cells.get(0).get(0).addMine();

    t.checkExpect(this.game1.hasWon(), false);

    this.game1.cells.get(0).get(1).revealCell();
    this.game1.cells.get(1).get(0).revealCell();
    this.game1.cells.get(1).get(1).revealCell();

    t.checkExpect(this.game1.hasWon(), true);

    this.game1.cells.get(0).get(0).revealCell();

    t.checkExpect(this.game1.hasWon(), false);

    this.game2.generateGrid(3, 3);
    this.game2.generateMines(8);

    t.checkExpect(this.game2.hasWon(), false);

    this.game2.cells.get(0).get(2).revealCell();

    t.checkExpect(this.game2.hasWon(), true);
  }

//test the method makeScene in the MinesweeperWorld class
  void testMakeScene(Tester t) {
    this.initTestConditions();

    WorldScene homeScreen = new WorldScene(IUtils.SCREEN_WIDTH, IUtils.SCREEN_HEIGHT);
    homeScreen.placeImageXY(new TextImage("Timer: 0 | Flags: 0", 30, Color.BLACK),
        IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT - 2 * IUtils.FONT_SIZE);
    homeScreen.placeImageXY(new TextImage("MineSweeper", 40, Color.BLACK), IUtils.SCREEN_WIDTH / 2,
        IUtils.SCREEN_HEIGHT / 10);
    homeScreen.placeImageXY(new TextImage("Follow the directions below to start a new game.",
        IUtils.FONT_SIZE, Color.BLACK), IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT / 5);
    homeScreen
        .placeImageXY(new TextImage("Press 1 to play in easy mode (10 mines in a 9 x 9 board)",
            IUtils.FONT_SIZE, Color.BLACK), IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT / 3);
    homeScreen.placeImageXY(
        new TextImage("Press 2 to play in medium mode (40 mines in a 16 x 16 board)",
            IUtils.FONT_SIZE, Color.BLACK),
        IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT / 3 + 2 * IUtils.FONT_SIZE);
    homeScreen.placeImageXY(
        new TextImage("Press 3 to play in hard mode (99 mines in a 30 x 16 board)",
            IUtils.FONT_SIZE, Color.BLACK),
        IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT / 3 + 4 * IUtils.FONT_SIZE);

    WorldScene winHomeScreen = new WorldScene(IUtils.SCREEN_WIDTH, IUtils.SCREEN_HEIGHT);
    winHomeScreen.placeImageXY(new TextImage("Timer: 0 | Flags: 0", 30, Color.BLACK),
        IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT - 2 * IUtils.FONT_SIZE);
    winHomeScreen.placeImageXY(new TextImage("MineSweeper", 40, Color.BLACK),
        IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT / 10);
    winHomeScreen.placeImageXY(new TextImage("Follow the directions below to start a new game.",
        IUtils.FONT_SIZE, Color.BLACK), IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT / 5);
    winHomeScreen
        .placeImageXY(new TextImage("Press 1 to play in easy mode (10 mines in a 9 x 9 board)",
            IUtils.FONT_SIZE, Color.BLACK), IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT / 3);
    winHomeScreen.placeImageXY(
        new TextImage("Press 2 to play in medium mode (40 mines in a 16 x 16 board)",
            IUtils.FONT_SIZE, Color.BLACK),
        IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT / 3 + 2 * IUtils.FONT_SIZE);
    winHomeScreen.placeImageXY(
        new TextImage("Press 3 to play in hard mode (99 mines in a 30 x 16 board)",
            IUtils.FONT_SIZE, Color.BLACK),
        IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT / 3 + 4 * IUtils.FONT_SIZE);
    winHomeScreen.placeImageXY(new TextImage("You Win!", 40, Color.GREEN), IUtils.SCREEN_WIDTH / 2,
        IUtils.SCREEN_HEIGHT - 6 * IUtils.FONT_SIZE);

    WorldScene loseHomeScreen = new WorldScene(IUtils.SCREEN_WIDTH, IUtils.SCREEN_HEIGHT);
    loseHomeScreen.placeImageXY(new TextImage("Timer: 0 | Flags: 0", 30, Color.BLACK),
        IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT - 2 * IUtils.FONT_SIZE);
    loseHomeScreen.placeImageXY(new TextImage("MineSweeper", 40, Color.BLACK),
        IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT / 10);
    loseHomeScreen.placeImageXY(new TextImage("Follow the directions below to start a new game.",
        IUtils.FONT_SIZE, Color.BLACK), IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT / 5);
    loseHomeScreen
        .placeImageXY(new TextImage("Press 1 to play in easy mode (10 mines in a 9 x 9 board)",
            IUtils.FONT_SIZE, Color.BLACK), IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT / 3);
    loseHomeScreen.placeImageXY(
        new TextImage("Press 2 to play in medium mode (40 mines in a 16 x 16 board)",
            IUtils.FONT_SIZE, Color.BLACK),
        IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT / 3 + 2 * IUtils.FONT_SIZE);
    loseHomeScreen.placeImageXY(
        new TextImage("Press 3 to play in hard mode (99 mines in a 30 x 16 board)",
            IUtils.FONT_SIZE, Color.BLACK),
        IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT / 3 + 4 * IUtils.FONT_SIZE);
    loseHomeScreen.placeImageXY(new TextImage("You Lost!", 40, Color.RED), IUtils.SCREEN_WIDTH / 2,
        IUtils.SCREEN_HEIGHT - 6 * IUtils.FONT_SIZE);

    t.checkExpect(this.game1.makeScene(), homeScreen);
    this.game1.hasWon = true;
    t.checkExpect(this.game1.makeScene(), winHomeScreen);
    this.game1.hasLost = true;
    this.game1.hasWon = false;
    t.checkExpect(this.game1.makeScene(), loseHomeScreen);

    this.game2.generateGrid(3, 3);
    this.game2.startGame = true;
    this.game2.timer = 10;
    this.game2.flags = 5;

    WorldScene unrevealedGrid = new WorldScene(IUtils.SCREEN_WIDTH, IUtils.SCREEN_HEIGHT);
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        WorldImage cell = new FrameImage(
            new RectangleImage(IUtils.CELL_SIZE, IUtils.CELL_SIZE, "solid", Color.CYAN),
            Color.BLACK);
        unrevealedGrid.placeImageXY(cell, IUtils.CELL_SIZE + IUtils.CELL_SIZE * i,
            IUtils.CELL_SIZE + IUtils.CELL_SIZE * j);
      }
    }
    unrevealedGrid.placeImageXY(new TextImage("Timer: 10 | Flags: 5", 30, Color.BLACK),
        IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT - 2 * IUtils.FONT_SIZE);

    t.checkExpect(this.game2.makeScene(), unrevealedGrid);

    WorldScene revealedGridFlags = new WorldScene(IUtils.SCREEN_WIDTH, IUtils.SCREEN_HEIGHT);

    this.game2.cells.get(2).get(0).updateFlag();
    this.game2.cells.get(2).get(1).revealCell();
    this.game2.cells.get(2).get(2).addMine();
    this.game2.cells.get(2).get(2).revealCell();
    this.game2.calculateNeighbors();

    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 3; j++) {
        WorldImage cell = new FrameImage(
            new RectangleImage(IUtils.CELL_SIZE, IUtils.CELL_SIZE, "solid", Color.CYAN),
            Color.BLACK);
        revealedGridFlags.placeImageXY(cell, IUtils.CELL_SIZE + IUtils.CELL_SIZE * i,
            IUtils.CELL_SIZE + IUtils.CELL_SIZE * j);
      }
    }
    WorldImage flagCell = new FrameImage(
        new OverlayImage(new EquilateralTriangleImage(IUtils.FLAG_SIZE, "solid", Color.YELLOW),
            new RectangleImage(IUtils.CELL_SIZE, IUtils.CELL_SIZE, "solid", Color.CYAN)),
        Color.BLACK);
    WorldImage revealedCell = new FrameImage(
        new OverlayImage(new TextImage("1", Color.BLUE),
            new RectangleImage(IUtils.CELL_SIZE, IUtils.CELL_SIZE, "solid", Color.WHITE)),
        Color.BLACK);

    WorldImage revealedMineCell = new FrameImage(
        new OverlayImage(new CircleImage(IUtils.MINE_SIZE, "solid", Color.RED),
            new RectangleImage(IUtils.CELL_SIZE, IUtils.CELL_SIZE, "solid", Color.WHITE)),
        Color.BLACK);
    revealedGridFlags.placeImageXY(flagCell, IUtils.CELL_SIZE + IUtils.CELL_SIZE * 2,
        IUtils.CELL_SIZE + IUtils.CELL_SIZE * 0);
    revealedGridFlags.placeImageXY(revealedCell, IUtils.CELL_SIZE + IUtils.CELL_SIZE * 2,
        IUtils.CELL_SIZE + IUtils.CELL_SIZE * 1);
    revealedGridFlags.placeImageXY(revealedMineCell, IUtils.CELL_SIZE + IUtils.CELL_SIZE * 2,
        IUtils.CELL_SIZE + IUtils.CELL_SIZE * 2);
    revealedGridFlags.placeImageXY(new TextImage("Timer: 10 | Flags: 5", 30, Color.BLACK),
        IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT - 2 * IUtils.FONT_SIZE);

    t.checkExpect(this.game2.makeScene(), revealedGridFlags);
  }

  // test the method onMouseClicked in the MinesweeperWorld class
  void testOnMouseClicked(Tester t) {
    this.initTestConditions();

    // Generate First Game
    this.game1.generateGrid(3, 3);
    this.game1.startGame = true;
    this.game1.cells.get(0).get(0).addMine();

    // Assert the base cases
    t.checkExpect(this.game1.cells.get(0).get(0).isRevealed, false);
    t.checkExpect(this.game1.hasLost, false);
    t.checkExpect(this.game1.startGame, true);

    // Test Unknown Clicks / Clicks not Left or Right Button
    this.game1.onMouseClicked(new Posn(10, 10), "UnknownButton");

    t.checkExpect(this.game1.cells.get(0).get(0).isRevealed, false);
    t.checkExpect(this.game1.hasLost, false);
    t.checkExpect(this.game1.startGame, true);

    // Test Left Button on mines loses game and ends game
    this.game1.onMouseClicked(new Posn(10, 10), "LeftButton");

    t.checkExpect(this.game1.cells.get(0).get(0).isRevealed, true);
    t.checkExpect(this.game1.hasLost, true);
    t.checkExpect(this.game1.startGame, false);

    // Generate Second Game
    this.game2.generateGrid(2, 2);
    this.game2.startGame = true;
    this.game2.cells.get(0).get(0).updateFlag();

    // Assert Base Cases
    t.checkExpect(this.game2.cells.get(0).get(0).hasFlag, true);
    t.checkExpect(this.game2.cells.get(0).get(0).isRevealed, false);

    // Test Left Button on flag cells don't change cells
    this.game2.onMouseClicked(new Posn(10, 10), "LeftButton");

    t.checkExpect(this.game2.cells.get(0).get(0).hasFlag, true);
    t.checkExpect(this.game2.cells.get(0).get(0).isRevealed, false);

    // Test Right Button on flag cells revert flag status
    this.game2.onMouseClicked(new Posn(10, 10), "RightButton");

    t.checkExpect(this.game2.cells.get(0).get(0).hasFlag, false);
    t.checkExpect(this.game2.cells.get(0).get(0).isRevealed, false);

    this.game2.onMouseClicked(new Posn(10, 10), "RightButton");

    t.checkExpect(this.game2.cells.get(0).get(0).hasFlag, true);
    t.checkExpect(this.game2.cells.get(0).get(0).isRevealed, false);

    this.game2.onMouseClicked(new Posn(10, 10), "RightButton");

    t.checkExpect(this.game2.cells.get(0).get(0).hasFlag, false);
    t.checkExpect(this.game2.cells.get(0).get(0).isRevealed, false);

    // Check Left Button on normal cells reveal them
    t.checkExpect(this.game2.cells.get(0).get(1).isRevealed, false);

    this.game2.onMouseClicked(new Posn(10, 50), "LeftButton");

    t.checkExpect(this.game2.cells.get(0).get(1).isRevealed, true);

    // Check revealing all cells wins the game and ends the game
    t.checkExpect(this.game2.hasWon, false);
    t.checkExpect(this.game2.startGame, true);

    this.game2.onMouseClicked(new Posn(10, 10), "LeftButton");
    this.game2.onMouseClicked(new Posn(50, 10), "LeftButton");
    this.game2.onMouseClicked(new Posn(50, 50), "LeftButton");

    t.checkExpect(this.game2.hasWon, true);
    t.checkExpect(this.game2.startGame, false);

  }

  // test the method onKeyEvent in the MinesweeperWorld class
  void testOnKeyEvent(Tester t) {
    this.initTestConditions();

    t.checkExpect(this.game1.cells.size(), 0);
    t.checkExpect(this.game1.flags, 0);
    t.checkExpect(this.game1.startGame, false);
    t.checkExpect(this.game1.countMines(), 0);

    this.game1.onKeyEvent("1");

    t.checkExpect(this.game1.flags, 10);
    t.checkExpect(this.game1.startGame, true);
    t.checkExpect(this.game1.cells.size(), 9);
    t.checkExpect(this.game1.cells.get(0).size(), 9);
    t.checkExpect(this.game1.countMines(), 10);

    this.initTestConditions();
    this.game1.onKeyEvent("2");

    t.checkExpect(this.game1.flags, 40);
    t.checkExpect(this.game1.startGame, true);
    t.checkExpect(this.game1.cells.size(), 16);
    t.checkExpect(this.game1.cells.get(0).size(), 16);
    t.checkExpect(this.game1.countMines(), 40);

    this.initTestConditions();
    this.game1.onKeyEvent("3");

    t.checkExpect(this.game1.flags, 99);
    t.checkExpect(this.game1.startGame, true);
    t.checkExpect(this.game1.cells.size(), 30);
    t.checkExpect(this.game1.cells.get(0).size(), 16);
    t.checkExpect(this.game1.countMines(), 99);
  }

  // test the method onTick in the MinesweeperWorld class
  void testOnTick(Tester t) {
    this.initTestConditions();

    t.checkExpect(this.game1.timer, 0);
    t.checkExpect(this.game2.timer, 0);

    this.game1.onTick();

    t.checkExpect(this.game1.timer, 0);

    this.game1.onKeyEvent("1");
    this.game1.onTick();

    this.game2.onKeyEvent("1");
    this.game2.onTick();
    this.game2.onTick();
    this.game2.onTick();
    this.game2.onTick();
    this.game2.onTick();

    t.checkExpect(this.game1.timer, 1);
    t.checkExpect(this.game2.timer, 5);
  }

  // test the method addNeighbors in the Cell class
  void testAddNeighbors(Tester t) {
    this.initTestConditions();

    this.cell2.addMine();

    t.checkExpect(this.cell1.neighbors, new ArrayList<Cell>());
    t.checkExpect(this.cell1.neighboringMines, 0);

    this.cell1.addNeighbors(new ArrayList<Cell>(Arrays.asList(new Cell(), new Cell(), new Cell())));

    t.checkExpect(this.cell1.neighbors,
        new ArrayList<Cell>(Arrays.asList(new Cell(), new Cell(), new Cell())));
    t.checkExpect(this.cell1.neighboringMines, 0);

    this.cell1.addNeighbors(new ArrayList<Cell>(Arrays.asList(new Cell(), this.cell2, new Cell())));

    t.checkExpect(this.cell1.neighbors,
        new ArrayList<Cell>(Arrays.asList(new Cell(), this.cell2, new Cell())));
    t.checkExpect(this.cell1.neighboringMines, 1);
  }

  // test the method countNeighboringMines in Cell class
  void testCountNeighboringMine(Tester t) {
    this.initTestConditions();

    Cell theMineCell = new Cell();
    theMineCell.addMine();

    t.checkExpect(this.cell1.countNeighboringMines(), 0);
    t.checkExpect(this.cell2.countNeighboringMines(), 0);

    this.cell1.neighbors = new ArrayList<Cell>(Arrays.asList(new Cell(), new Cell(), new Cell()));
    this.cell2.neighbors = new ArrayList<Cell>(
        Arrays.asList(theMineCell, theMineCell, new Cell(), new Cell(), theMineCell));

    t.checkExpect(this.cell1.countNeighboringMines(), 0);
    t.checkExpect(this.cell2.countNeighboringMines(), 3);
  }

  // test the method addMine in Cell class
  void testAddMine(Tester t) {
    this.initTestConditions();
    t.checkExpect(this.cell1.hasMine, false);

    this.cell1.addMine();

    t.checkExpect(this.cell1.hasMine, true);

    this.cell1.addMine();

    t.checkExpect(this.cell1.hasMine, true);
  }

  // test the method hasMine in the Cell class
  void testHasMine(Tester t) {
    this.initTestConditions();
    t.checkExpect(this.cell1.hasMine(), false);

    this.cell1.addMine();

    t.checkExpect(this.cell1.hasMine(), true);

    this.cell1.addMine();

    t.checkExpect(this.cell1.hasMine(), true);
  }

  // test the method updateFlag in the Cell class
  void testUpdateFlag(Tester t) {
    this.initTestConditions();
    t.checkExpect(this.cell2.hasFlag, false);

    t.checkExpect(this.cell2.updateFlag(), -1);
    t.checkExpect(this.cell2.hasFlag, true);

    t.checkExpect(this.cell2.updateFlag(), 1);
    t.checkExpect(this.cell2.hasFlag, false);

    t.checkExpect(this.cell1.hasFlag, false);

    this.cell1.revealCell();

    t.checkExpect(this.cell1.updateFlag(), 0);
    t.checkExpect(this.cell1.hasFlag, false);
  }

  // test the method revealCell in the Cell class
  void testRevealCell(Tester t) {
    this.initTestConditions();
    t.checkExpect(this.cell1.isRevealed, false);

    this.cell1.revealCell();
    t.checkExpect(this.cell1.isRevealed, true);

    this.cell1.revealCell();
    t.checkExpect(this.cell1.isRevealed, true);
  }

  // test the method hasLost in the Cell class
  void testHasLost(Tester t) {
    this.initTestConditions();
    t.checkExpect(this.cell1.hasLost(), false);
    t.checkExpect(this.cell2.hasLost(), false);

    this.cell1.addMine();
    this.cell2.revealCell();
    t.checkExpect(this.cell1.hasLost(), false);
    t.checkExpect(this.cell2.hasLost(), false);

    this.cell2.addMine();
    this.cell1.revealCell();
    t.checkExpect(this.cell1.hasLost(), true);
    t.checkExpect(this.cell2.hasLost(), true);
  }

  // test the method hasWonHelper in the Cell class
  void testHasWonHelper(Tester t) {
    this.initTestConditions();
    t.checkExpect(this.cell1.hasMine, false);
    t.checkExpect(this.cell1.isRevealed, false);
    t.checkExpect(this.cell2.hasMine, false);
    t.checkExpect(this.cell2.isRevealed, false);
    t.checkExpect(this.cell1.hasWonHelper(), false);
    t.checkExpect(this.cell2.hasWonHelper(), false);

    this.cell1.revealCell();
    t.checkExpect(this.cell1.hasMine, false);
    t.checkExpect(this.cell1.isRevealed, true);
    t.checkExpect(this.cell1.hasWonHelper(), true);

    this.cell2.addMine();
    t.checkExpect(this.cell2.hasMine, true);
    t.checkExpect(this.cell2.isRevealed, false);
    t.checkExpect(this.cell2.hasWonHelper(), true);
  }

  // test the method DrawCell in the Cell class
  void testDrawCell(Tester t) {
    this.initTestConditions();
    WorldImage cellImage = new FrameImage(
        new RectangleImage(IUtils.CELL_SIZE, IUtils.CELL_SIZE, "solid", Color.CYAN));
    ArrayList<Color> colors = new ArrayList<Color>(
        Arrays.asList(Color.BLACK, Color.BLUE, Color.GREEN, Color.RED, Color.DARK_GRAY,
            Color.MAGENTA, Color.ORANGE, Color.PINK, Color.YELLOW));
    WorldImage mineImage = new FrameImage(
        new OverlayImage(new CircleImage(IUtils.MINE_SIZE, "solid", Color.RED),
            new RectangleImage(IUtils.CELL_SIZE, IUtils.CELL_SIZE, "solid", Color.WHITE)));
    WorldImage flagImage = new FrameImage(
        new OverlayImage(new EquilateralTriangleImage(IUtils.FLAG_SIZE, "solid", Color.YELLOW),
            new RectangleImage(IUtils.CELL_SIZE, IUtils.CELL_SIZE, "solid", Color.CYAN)));
    WorldImage revealedImage = new FrameImage(new OverlayImage(new TextImage("", colors.get(0)),
        new RectangleImage(IUtils.CELL_SIZE, IUtils.CELL_SIZE, "solid", Color.WHITE)));

    t.checkExpect(this.cell1.drawCell(), cellImage);
    t.checkExpect(this.cell2.drawCell(), cellImage);

    this.cell1.updateFlag();
    this.cell2.addMine();
    this.cell2.revealCell();

    t.checkExpect(this.cell1.drawCell(), flagImage);
    t.checkExpect(this.cell2.drawCell(), mineImage);

    this.cell1.updateFlag();
    this.cell1.revealCell();

    t.checkExpect(this.cell1.drawCell(), revealedImage);
  }

}
