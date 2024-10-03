import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// Represents the light em all game world
class LightEmAll extends World {
  // a list of columns of GamePieces,
  // i.e., represents the board in column-major order
  ArrayList<ArrayList<GamePiece>> board;
  ArrayList<GamePiece> nodes; // a list of all nodes
  ArrayList<Edge> mst; // a list of edges of the minimum spanning tree
  // the width and height of the board
  int width;
  int height;
  // the current location of the power station,
  // as well as its effective radius
  int powerRow;
  int powerCol;
  int radius;
  boolean won; // has the player won the game
  int time; // keep track of time to complete the current game
  Random rand;

  // Constructor for LightEmAll
  LightEmAll(Random r) {
    this.board = new ArrayList<ArrayList<GamePiece>>();
    this.nodes = new ArrayList<GamePiece>();
    this.mst = new ArrayList<Edge>();
    this.width = IUtils.GAME_WIDTH;
    this.height = IUtils.GAME_HEIGHT;
    this.powerRow = 0;
    this.powerCol = 0;
    this.radius = 5;
    this.won = false;
    this.time = 0;
    this.rand = r;

    // Generate the game grid and randomize the board
    this.generateGrid();
    this.generateMst();
    this.randomizeBoard();

    // Set the power station at the origin
    this.board.get(powerCol).get(powerRow).updatePowerStation();
    this.bfs();
  }

  // Constructor for LightEmAll testing
  LightEmAll(Random r, int width, int height) {
    this.board = new ArrayList<ArrayList<GamePiece>>();
    this.nodes = new ArrayList<GamePiece>();
    this.mst = new ArrayList<Edge>();
    this.width = width;
    this.height = height;
    this.powerRow = 0;
    this.powerCol = 0;
    this.radius = 10;
    this.won = false;
    this.time = 0;
    this.rand = r;
  }

  // Render the game scene
  public WorldScene makeScene() {
    WorldScene ws = new WorldScene(IUtils.SCREEN_WIDTH, IUtils.SCREEN_HEIGHT);

    // Render Win
    if (this.won) {
      ws.placeImageXY(new TextImage("You Won", 50, Color.GREEN), IUtils.SCREEN_WIDTH / 2,
          IUtils.SCREEN_HEIGHT / 2);
      ws.placeImageXY(new TextImage("Press R to restart", 50, Color.BLACK), IUtils.SCREEN_WIDTH / 2,
          IUtils.SCREEN_HEIGHT / 2 + 55);
      ws.placeImageXY(new TextImage("Timer: " + this.time, 50, Color.BLACK),
          IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT / 2 + 110);
      return ws;
    }

    // Render each game piece on the scene
    for (int i = 0; i < this.board.size(); i++) {
      for (int j = 0; j < this.board.get(i).size(); j++) {
        GamePiece gp = this.board.get(i).get(j);
        int distanceFromPS = (int) Math.sqrt(
            Math.pow(Math.abs(i - this.powerRow), 2) + Math.pow(Math.abs(j - this.powerCol), 2));
        Color wireColor = new Color(255, 255 - 25 * distanceFromPS, 0);
        ws.placeImageXY(gp.draw(IUtils.TILE_SIZE, IUtils.WIRE_WIDTH, wireColor),
            IUtils.TILE_SIZE / 2 + IUtils.TILE_SIZE * i,
            IUtils.TILE_SIZE / 2 + IUtils.TILE_SIZE * j);
      }
    }

    // Place timer on screen
    ws.placeImageXY(new TextImage("Timer: " + this.time, 40, Color.BLACK), IUtils.SCREEN_WIDTH / 2,
        IUtils.SCREEN_HEIGHT - 45);

    return ws;

  }

  // Handle mouse clicks
  public void onMouseClicked(Posn posn, String button) {
    // Calculate the row and column of the clicked cell
    int clickedRow = posn.x / IUtils.TILE_SIZE;
    int clickedCol = posn.y / IUtils.TILE_SIZE;

    if (this.won) {
      return;
    }

    // Rotate the clicked cell if within game boundaries
    if (clickedRow >= 0 && clickedRow < this.height && clickedCol >= 0 && clickedCol < this.width) {
      this.board.get(clickedRow).get(clickedCol).rotate(1);
    }
    this.bfs();
  }

  // Handle key events (arrow keys to move the power station)
  public void onKeyEvent(String key) {
    if (this.won) {
      if (key.equals("r")) {
        this.board = new ArrayList<ArrayList<GamePiece>>();
        this.nodes = new ArrayList<GamePiece>();
        this.mst = new ArrayList<Edge>();
        this.powerRow = 0;
        this.powerCol = 0;
        this.won = false;
        this.time = 0;
        this.generateGrid();
        this.generateMst();
        this.randomizeBoard();
        this.board.get(powerCol).get(powerRow).updatePowerStation();
        this.bfs();
      }
      return;
    }

    int newRow = this.powerRow;
    int newCol = this.powerCol;

    if (key.equals("up")) {
      newCol -= 1;
    }
    else if (key.equals("down")) {
      newCol += 1;
    }
    else if (key.equals("left")) {
      newRow -= 1;
    }
    else if (key.equals("right")) {
      newRow += 1;
    }

    // Move the power station if within game boundaries and connected to the new
    // position
    if (newRow >= 0 && newRow < this.height && newCol >= 0 && newCol < this.width) {
      GamePiece newPiece = this.board.get(newRow).get(newCol);
      if (newPiece.isConnectedTo(this.board.get(powerRow).get(powerCol))) {
        this.board.get(powerRow).get(powerCol).updatePowerStation();
        newPiece.updatePowerStation();
        this.powerRow = newRow;
        this.powerCol = newCol;
      }
    }
    this.bfs();
  }

  // On tick method, update timer every tick if game is being played
  public void onTick() {
    if (!this.won) {
      this.time += 1;
    }
  }

  // Generate the game grid and add every node to this.nodes
  void generateGrid() {
    for (int i = 0; i < this.height; i++) {
      ArrayList<GamePiece> row = new ArrayList<GamePiece>();
      for (int j = 0; j < this.width; j++) {
        GamePiece gp = new GamePiece(j, i);
        row.add(gp);
        this.nodes.add(gp);
      }
      this.board.add(row);
    }
  }

  // Randomize the board by rotating pieces randomly
  void randomizeBoard() {
    for (ArrayList<GamePiece> row : this.board) {
      for (GamePiece piece : row) {
        piece.rotate(this.rand.nextInt(4));
      }
    }
  }

  // Breath First Search to light up the game pieces
  void bfs() {
    ArrayList<GamePiece> worklist = new ArrayList<GamePiece>();
    ArrayList<GamePiece> alreadySeen = new ArrayList<GamePiece>();

    for (int col = 0; col < this.board.size(); col++) {
      for (int row = 0; row < this.board.size(); row++) {
        this.board.get(col).get(row).powered = false;
      }
    }

    this.board.get(powerRow).get(powerCol).powered = true;
    worklist.add(this.board.get(powerRow).get(powerCol));

    // As long as the work list isn't empty...
    while (!worklist.isEmpty()) {
      GamePiece next = worklist.remove(0);

      // If piece isn't in radius of the power station abort the bfs
      int distanceFromPS = (int) Math.sqrt(Math.pow(Math.abs(next.row - this.powerRow), 2)
          + Math.pow(Math.abs(next.col - this.powerCol), 2));
      if (distanceFromPS > this.radius) {
        break;
      }

      if (next.row != 0) {
        GamePiece above = this.board.get(next.col).get(next.row - 1);
        if (next.top && above.bottom && !alreadySeen.contains(above)) {
          worklist.add(above);
          alreadySeen.add(above);
          above.powered = true;
        }
      }
      if (next.row != (this.board.size() - 1)) {
        GamePiece below = this.board.get(next.col).get(next.row + 1);
        if (next.bottom && below.top && !alreadySeen.contains(below)) {
          worklist.add(below);
          alreadySeen.add(below);
          below.powered = true;
        }
      }
      if (next.col != 0) {
        GamePiece left = this.board.get(next.col - 1).get(next.row);
        if (next.left && left.right && !alreadySeen.contains(left)) {
          worklist.add(left);
          alreadySeen.add(left);
          left.powered = true;
        }
      }
      if (next.col != (this.board.size() - 1)) {
        GamePiece right = this.board.get(next.col + 1).get(next.row);
        if (next.right && right.left && !alreadySeen.contains(right)) {
          worklist.add(right);
          alreadySeen.add(right);
          right.powered = true;
        }
      }
    }

    this.won = alreadySeen.size() == this.height * this.width;
  }

  // Generate minimum spanning tree and set it to this.mst
  void generateMst() {
    HashMap<GamePiece, GamePiece> representatives = new HashMap<GamePiece, GamePiece>();
    ArrayList<Edge> edgesInTree = new ArrayList<Edge>();
    ArrayList<Edge> worklist = new ArrayList<Edge>();

    // Set representatives
    for (GamePiece gp : this.nodes) {
      representatives.put(gp, gp);
    }

    // Generate edges and add them to the work list
    for (int col = 0; col < this.board.size(); col++) {
      for (int row = 0; row < this.board.get(col).size(); row++) {
        GamePiece current = this.board.get(col).get(row);
        // Check connections to the right and bottom
        if (col + 1 < this.width) {
          GamePiece right = this.board.get(col + 1).get(row);
          worklist.add(new Edge(current, right, rand.nextInt(100)));
        }
        if (row + 1 < this.height) {
          GamePiece bottom = this.board.get(col).get(row + 1);
          worklist.add(new Edge(current, bottom, rand.nextInt(100)));
        }
      }
    }

    // Generate MST
    while (!worklist.isEmpty() && representatives.size() > 1) {
      worklist.sort((e1, e2) -> e1.weight - e2.weight);
      Edge currentEdge = worklist.get(0);
      GamePiece from = currentEdge.fromNode;
      GamePiece to = currentEdge.toNode;
      if (!find(representatives, from).equals(find(representatives, to))) {
        edgesInTree.add(currentEdge);
        union(representatives, from, to);
        // Update the game piece direction booleans
        if (from.row < to.row) {
          from.bottom = true;
          to.top = true;
        }
        else if (from.row > to.row) {
          from.top = true;
          to.bottom = true;
        }
        else if (from.col < to.col) {
          from.right = true;
          to.left = true;
        }
        else if (from.col > to.col) {
          from.left = true;
          to.right = true;
        }
      }
      worklist.remove(0);
    }

    this.mst = edgesInTree;
  }

  // Union find find method
  GamePiece find(HashMap<GamePiece, GamePiece> representatives, GamePiece gp) {
    if (!representatives.get(gp).equals(gp)) {
      representatives.put(gp, find(representatives, representatives.get(gp)));
    }
    return representatives.get(gp);
  }

  // Union find union method
  void union(HashMap<GamePiece, GamePiece> representatives, GamePiece gp1, GamePiece gp2) {
    representatives.put(find(representatives, gp2), find(representatives, gp1));
  }
}

// Represents a single game piece on the board
class GamePiece {
  // in logical coordinates, with the origin
  // at the top-left corner of the screen
  int row;
  int col;
  // whether this GamePiece is connected to the
  // adjacent left, right, top, or bottom pieces
  boolean left;
  boolean right;
  boolean top;
  boolean bottom;
  // whether the power station is on this piece
  boolean powerStation;
  boolean powered;

  // Constructor for GamePiece
  GamePiece(int row, int col) {
    this.row = row;
    this.col = col;
    this.left = false;
    this.right = false;
    this.top = false;
    this.bottom = false;
    this.powerStation = false;
    this.powered = false;
  }

  // Generate an image of this, the given GamePiece.
  // - size: the size of the tile, in pixels
  // - wireWidth: the width of wires, in pixels
  // - poweredWireColor: the color of wire when powered
  WorldImage draw(int size, int wireWidth, Color poweredWireColor) {
    Color wireColor = Color.GRAY;
    if (this.powered) {
      wireColor = poweredWireColor;
    }

    WorldImage image = new OverlayImage(
        new RectangleImage(wireWidth, wireWidth, OutlineMode.SOLID, wireColor),
        new RectangleImage(size, size, OutlineMode.SOLID, Color.DARK_GRAY));
    WorldImage vWire = new RectangleImage(wireWidth, (size + 1) / 2, OutlineMode.SOLID, wireColor);
    WorldImage hWire = new RectangleImage((size + 1) / 2, wireWidth, OutlineMode.SOLID, wireColor);

    if (this.top) {
      image = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP, vWire, 0, 0, image);
    }
    if (this.right) {
      image = new OverlayOffsetAlign(AlignModeX.RIGHT, AlignModeY.MIDDLE, hWire, 0, 0, image);
    }
    if (this.bottom) {
      image = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM, vWire, 0, 0, image);
    }
    if (this.left) {
      image = new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.MIDDLE, hWire, 0, 0, image);
    }
    if (this.powerStation) {
      image = new OverlayImage(
          new OverlayImage(new StarImage(size / 3, 7, OutlineMode.OUTLINE, new Color(255, 128, 0)),
              new StarImage(size / 3, 7, OutlineMode.SOLID, new Color(0, 255, 255))),
          image);
    }
    return image;
  }

  // Update the power station status of this GamePiece
  void updatePowerStation() {
    this.powerStation = !this.powerStation;
  }

  // Rotate the GamePiece clockwise a certain number of times
  void rotate(int rotations) {
    for (int i = 0; i < rotations; i++) {
      boolean temp = this.top;
      this.top = this.left;
      this.left = this.bottom;
      this.bottom = this.right;
      this.right = temp;
    }
  }

  // Check if this GamePiece is connected to another piece
  boolean isConnectedTo(GamePiece other) {
    if (this.row == other.row && Math.abs(this.col - other.col) == 1) {
      if (this.col - other.col == 1) {
        return other.right && this.left;
      }
      return other.left && this.right;
    }
    if (this.col == other.col && Math.abs(this.row - other.row) == 1) {
      if (this.row - other.row == 1) {
        return other.bottom && this.top;
      }
      return other.top && this.bottom;
    }
    return false;
  }
}

// Represents an edge between two GamePieces
class Edge {
  GamePiece fromNode;
  GamePiece toNode;
  int weight;

  // Constructor for Edge
  Edge(GamePiece fn, GamePiece tn, int weight) {
    this.fromNode = fn;
    this.toNode = tn;
    this.weight = weight;
  }
}

// Interface containing constants used throughout the game
interface IUtils {
  int SCREEN_WIDTH = 480;
  int SCREEN_HEIGHT = 550;
  int TILE_SIZE = 60;
  int WIRE_WIDTH = 10;
  int GAME_WIDTH = 8;
  int GAME_HEIGHT = 8;
}

// Class containing test cases for LightEmAll
class ExamplesLightEmAll {
  LightEmAll game1;
  LightEmAll game2;
  LightEmAll game3;
  GamePiece gp1;
  GamePiece gp2;
  GamePiece gp3;

  // Initialize test conditions
  void initTestConditions() {
    this.game1 = new LightEmAll(new Random(1), 2, 2);
    this.game2 = new LightEmAll(new Random(1));
    this.game3 = new LightEmAll(new Random(1));
    this.gp1 = new GamePiece(0, 0);
    this.gp2 = new GamePiece(0, 0);
    this.gp3 = new GamePiece(0, 0);

    this.gp1.top = true;
    this.gp1.bottom = true;
    this.gp2.top = true;
    this.gp2.bottom = true;
    this.gp3.top = true;
    this.gp3.bottom = true;
  }

  // Test the game using BigBang
  void testBigBang(Tester t) {
    this.initTestConditions();
    this.game3.bigBang(IUtils.SCREEN_WIDTH, IUtils.SCREEN_HEIGHT, 1);
  }

  void testMakeScene(Tester t) {
    this.initTestConditions();
    this.game1.generateGrid();
    GamePiece topLeft = new GamePiece(0, 0);
    GamePiece topRight = new GamePiece(0, 1);
    GamePiece bottomLeft = new GamePiece(1, 0);
    GamePiece bottomRight = new GamePiece(1, 1);

    WorldScene ws = new WorldScene(IUtils.SCREEN_WIDTH, IUtils.SCREEN_HEIGHT);
    ws.placeImageXY(topLeft.draw(IUtils.TILE_SIZE, IUtils.WIRE_WIDTH, Color.GRAY),
        IUtils.TILE_SIZE / 2 + IUtils.TILE_SIZE * 0, IUtils.TILE_SIZE / 2 + IUtils.TILE_SIZE * 0);
    ws.placeImageXY(topRight.draw(IUtils.TILE_SIZE, IUtils.WIRE_WIDTH, Color.GRAY),
        IUtils.TILE_SIZE / 2 + IUtils.TILE_SIZE * 0, IUtils.TILE_SIZE / 2 + IUtils.TILE_SIZE * 1);
    ws.placeImageXY(bottomLeft.draw(IUtils.TILE_SIZE, IUtils.WIRE_WIDTH, Color.GRAY),
        IUtils.TILE_SIZE / 2 + IUtils.TILE_SIZE * 1, IUtils.TILE_SIZE / 2 + IUtils.TILE_SIZE * 0);
    ws.placeImageXY(bottomRight.draw(IUtils.TILE_SIZE, IUtils.WIRE_WIDTH, Color.GRAY),
        IUtils.TILE_SIZE / 2 + IUtils.TILE_SIZE * 1, IUtils.TILE_SIZE / 2 + IUtils.TILE_SIZE * 1);

    ws.placeImageXY(new TextImage("Timer: 0", 40, Color.BLACK), IUtils.SCREEN_WIDTH / 2,
        IUtils.SCREEN_HEIGHT - 45);

    t.checkExpect(this.game1.makeScene(), ws);
  }

  void testOnMouseClicked(Tester t) {
    this.initTestConditions();
    GamePiece piece = this.game2.board.get(0).get(0);
    t.checkExpect(piece.top, false);
    t.checkExpect(piece.bottom, false);
    t.checkExpect(piece.left, true);
    t.checkExpect(piece.right, false);

    this.game2.onMouseClicked(new Posn(25, 25), "LeftButton");
    t.checkExpect(piece.top, true);
    t.checkExpect(piece.bottom, false);
    t.checkExpect(piece.left, false);
    t.checkExpect(piece.right, false);
  }

  void testOnKeyEvent(Tester t) {
    this.initTestConditions();
    t.checkExpect(this.game2.powerCol, 0);
    this.game2.onKeyEvent("down");
    t.checkExpect(this.game2.powerCol, 0);
    this.game2.onKeyEvent("up");
    t.checkExpect(this.game2.powerCol, 0);
  }

  void testOnTick(Tester t) {
    this.initTestConditions();
    t.checkExpect(this.game1.time, 0);
    this.game1.onTick();
    t.checkExpect(this.game1.time, 1);
    this.game1.won = true;
    this.game1.onTick();
    t.checkExpect(this.game1.time, 1);
  }

  void testGenerateGrid(Tester t) {
    this.initTestConditions();
    t.checkExpect(this.game1.board, new ArrayList<ArrayList<GamePiece>>());
    this.game1.generateGrid();
    t.checkExpect(this.game1.board,
        new ArrayList<ArrayList<GamePiece>>(Arrays.asList(
            new ArrayList<GamePiece>(Arrays.asList(new GamePiece(0, 0), new GamePiece(1, 0))),
            new ArrayList<GamePiece>(Arrays.asList(new GamePiece(0, 1), new GamePiece(1, 1))))));
  }

  void testRandomizeBoard(Tester t) {
    this.initTestConditions();
    this.game1.generateGrid();
    GamePiece topLeft = new GamePiece(0, 0);
    GamePiece topRight = new GamePiece(0, 1);
    GamePiece bottomLeft = new GamePiece(1, 0);
    GamePiece bottomRight = new GamePiece(1, 1);

    t.checkExpect(this.game1.board,
        new ArrayList<ArrayList<GamePiece>>(
            Arrays.asList(new ArrayList<GamePiece>(Arrays.asList(topLeft, bottomLeft)),
                new ArrayList<GamePiece>(Arrays.asList(topRight, bottomRight)))));

    t.checkExpect(topRight.top, false);
    t.checkExpect(topRight.bottom, false);
    t.checkExpect(topRight.left, false);
    t.checkExpect(topRight.right, false);
    t.checkExpect(bottomRight.top, false);
    t.checkExpect(bottomRight.bottom, false);
    t.checkExpect(bottomRight.left, false);
    t.checkExpect(bottomRight.right, false);

    this.game1.randomizeBoard();
    topRight.rotate(1);
    bottomRight.rotate(1);

    t.checkExpect(this.game1.board,
        new ArrayList<ArrayList<GamePiece>>(
            Arrays.asList(new ArrayList<GamePiece>(Arrays.asList(topLeft, bottomLeft)),
                new ArrayList<GamePiece>(Arrays.asList(topRight, bottomRight)))));

    t.checkExpect(topRight.top, false);
    t.checkExpect(topRight.bottom, false);
    t.checkExpect(topRight.left, false);
    t.checkExpect(topRight.right, false);
    t.checkExpect(bottomRight.top, false);
    t.checkExpect(bottomRight.bottom, false);
    t.checkExpect(bottomRight.left, false);
    t.checkExpect(bottomRight.right, false);
  }

  void testBFS(Tester t) {
    this.initTestConditions();

    this.game2.bfs();
    t.checkExpect(this.game2.board.get(0).get(1).powered, false);
    this.game2.board.get(0).get(1).rotate(1);
    this.game2.bfs();
    t.checkExpect(this.game2.board.get(0).get(1).powered, false);
  }

  void testGenerateMst(Tester t) {
    this.initTestConditions();

    this.game1.generateGrid();
    t.checkExpect(this.game1.mst, new ArrayList<Edge>());
    t.checkExpect(this.game1.board.get(0).get(0).right, false);
    this.game1.generateMst();
    t.checkExpect(this.game1.board.get(0).get(0).right, true);
    t.checkExpect(this.game1.mst.isEmpty(), false);
  }

  void testFindAndUnion(Tester t) {
    this.initTestConditions();
    GamePiece gp1 = new GamePiece(0, 0);
    GamePiece gp2 = new GamePiece(0, 1);
    GamePiece gp3 = new GamePiece(1, 0);
    HashMap<GamePiece, GamePiece> representatives = new HashMap<>();
    representatives.put(gp1, gp1);
    representatives.put(gp2, gp2);
    representatives.put(gp3, gp3);
    t.checkExpect(this.game1.find(representatives, gp1), gp1);
    this.game1.union(representatives, gp1, gp2);
    t.checkExpect(this.game1.find(representatives, gp1), gp1);
    GamePiece gp4 = new GamePiece(1, 1);
    representatives.put(gp4, gp2);
    t.checkExpect(this.game1.find(representatives, gp4), gp1);
  }

  void testDraw(Tester t) {
    this.initTestConditions();
    this.gp2.rotate(1);
    this.gp3.powerStation = true;
    this.gp3.top = false;
    this.gp3.bottom = false;

    WorldImage image = new OverlayImage(
        new RectangleImage(IUtils.WIRE_WIDTH, IUtils.WIRE_WIDTH, OutlineMode.SOLID, Color.GRAY),
        new RectangleImage(IUtils.TILE_SIZE, IUtils.TILE_SIZE, OutlineMode.SOLID, Color.DARK_GRAY));
    WorldImage vWire = new RectangleImage(IUtils.WIRE_WIDTH, (IUtils.TILE_SIZE + 1) / 2,
        OutlineMode.SOLID, Color.GRAY);
    WorldImage hWire = new RectangleImage((IUtils.TILE_SIZE + 1) / 2, IUtils.WIRE_WIDTH,
        OutlineMode.SOLID, Color.GRAY);

    WorldImage topBot = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP, vWire, 0, 0,
        image);
    topBot = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM, vWire, 0, 0, topBot);

    WorldImage leftRight = new OverlayOffsetAlign(AlignModeX.RIGHT, AlignModeY.MIDDLE, hWire, 0, 0,
        image);
    leftRight = new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.MIDDLE, hWire, 0, 0, leftRight);

    t.checkExpect(this.gp1.draw(IUtils.TILE_SIZE, IUtils.WIRE_WIDTH, Color.GRAY), topBot);
    t.checkExpect(this.gp2.draw(IUtils.TILE_SIZE, IUtils.WIRE_WIDTH, Color.GRAY), leftRight);
    t.checkExpect(this.gp3.draw(IUtils.TILE_SIZE, IUtils.WIRE_WIDTH, Color.GRAY),
        new OverlayImage(
            new OverlayImage(
                new StarImage(IUtils.TILE_SIZE / 3, 7, OutlineMode.OUTLINE, new Color(255, 128, 0)),
                new StarImage(IUtils.TILE_SIZE / 3, 7, OutlineMode.SOLID, new Color(0, 255, 255))),
            image));
  }

  void testUpdatePowerStation(Tester t) {
    this.initTestConditions();
    t.checkExpect(this.gp1.powerStation, false);
    t.checkExpect(this.gp1.powered, false);

    this.gp1.updatePowerStation();
    t.checkExpect(this.gp1.powerStation, true);
    t.checkExpect(this.gp1.powered, false);

    this.gp1.updatePowerStation();
    t.checkExpect(this.gp1.powerStation, false);
    t.checkExpect(this.gp1.powered, false);
  }

  void testRotate(Tester t) {
    this.initTestConditions();
    t.checkExpect(this.gp1.top, true);
    t.checkExpect(this.gp1.bottom, true);
    t.checkExpect(this.gp1.left, false);
    t.checkExpect(this.gp1.right, false);

    this.gp1.rotate(1);
    t.checkExpect(this.gp1.top, false);
    t.checkExpect(this.gp1.bottom, false);
    t.checkExpect(this.gp1.left, true);
    t.checkExpect(this.gp1.right, true);
  }

  void testIsConnectedTo(Tester t) {
    this.initTestConditions();
    t.checkExpect(this.gp1.isConnectedTo(new GamePiece(1, 0)), false);
    t.checkExpect(this.gp1.isConnectedTo(new GamePiece(2, 2)), false);
  }

}