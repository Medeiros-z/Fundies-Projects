import tester.*;
import javalib.worldimages.*;
import javalib.funworld.*;
import java.awt.Color;
import java.util.Random;

// Represents a world class for the ZTypeWorld game
class ZTypeWorld extends World {
  ILoWord words; // List of inactive words
  IWord currentWord; // Current word player is typing
  int newWordTimer; // Timer until a new word is added to this.words
  int score; // Score of the game
  Random rand;

  // New Game Constructor
  ZTypeWorld() {
    this.words = IUtils.U.randomLoWord(IUtils.STARTING_WORDS_LENGTH, new Random());
    this.currentWord = new InactiveWord("", 0, 0);
    this.newWordTimer = IUtils.TICKS_PER_WORD;
    this.score = 0;
    this.rand = new Random();
  }

  // Test Game Constructor
  ZTypeWorld(Random rand) {
    this.words = IUtils.U.randomLoWord(IUtils.STARTING_WORDS_LENGTH, rand);
    this.currentWord = new InactiveWord("", 0, 0);
    this.newWordTimer = IUtils.TICKS_PER_WORD;
    this.score = 0;
    this.rand = rand;
  }

  // Constructor
  ZTypeWorld(ILoWord words, IWord currentWord, int newWordTimer, int score, Random rand) {
    this.words = words;
    this.currentWord = currentWord;
    this.newWordTimer = newWordTimer;
    this.score = score;
    this.rand = rand;
  }

  /* TEMPLATE:
   * Fields:
   *  this.words ... ILoWord
   *  this.currentWord ... IWord
   *  this.newWordTimer ... int
   *  this.score ... int
   *  this.rand ... Random
   * Methods:
   *  this.makeScene() ... WorldScene
   *  this.lastScene() ... WorldScene
   *  this.onTick() ... World
   *  this.onKeyEvent(String) ... World
   *  this.worldEnds() ... WorldEnd
   * Methods of Fields:
   *  this.words.addToEnd(IWord) ... ILoWord
   *  this.wordsfilterOutEmpties() ... ILoWord
   *  this.words.draw(WorldScene) ... WorldScene
   *  this.words.move() ... ILoWord
   *  this.words.getWordStartsWith(String) ... IWord
   *  this.words.removeCurrentWord(IWord) ... ILoWord
   *  this.words.isGameOver(int) .. boolean
   *  this.words.isMt() ... boolean
   *  
   *  this.currentWord.startsWith(String) ... boolean
   *  this.currentWord.checkAndReduce(String) ... IWord
   *  this.currentWord.isEmpty() ... boolean
   *  this.currentWord.placeImage(WorldScene) ... WorldScene
   *  this.currentWord.move() ... IWord
   *  this.currentWord.equals(IWord) ... boolean
   *  this.currentWord.sameActiveWord(ActiveWord) ... boolean
   *  this.currentWord.sameInactiveWord(InactiveWord) ... boolean
   *  this.currentWord.switchActivity() ... IWord
   *  this.currentWord.checkYPos(int) ... boolean
   * */

  // Draw scene with all words from this.words
  public WorldScene makeScene() {
    return this.words
        .draw(IUtils.BLANK.placeImageXY(
            new TextImage("Score: " + Integer.toString(this.score), IUtils.FONT_SIZE,
                FontStyle.BOLD, Color.GREEN),
            IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT - IUtils.FONT_SIZE));
  }

  // Draw last scene after win/lose condition is triggered
  public WorldScene lastScene(String msg) {
    return IUtils.BLANK.placeImageXY(
        new TextImage(msg, IUtils.FONT_SIZE, FontStyle.BOLD, Color.GREEN), IUtils.SCREEN_WIDTH / 2,
        IUtils.SCREEN_HEIGHT / 2);
  }

  // Move all words in this.words down every tick and make new word every x ticks
  public World onTick() {
    int newTimer = this.newWordTimer - 1;
    if (newTimer == 0) {
      this.words = this.words.addToEnd(IUtils.U.randomInactiveWord(this.rand));
      newTimer = IUtils.TICKS_PER_WORD;
    }
    return new ZTypeWorld(this.words.filterOutEmpties().move(), this.currentWord.move(), newTimer,
        this.score, this.rand);
  }

  // On key handler, checks if key can be used to further the game and does so if possible
  public World onKeyEvent(String key) {
    if (this.currentWord.isEmpty()) {
      IWord word = this.words.getWordStartsWith(key);
      if (!word.isEmpty()) {
        IWord newCurrentWord = word.switchActivity().checkAndReduce(key);
        return new ZTypeWorld(this.words.removeCurrentWord(word).addToEnd(newCurrentWord),
            newCurrentWord, this.newWordTimer, this.score + 1, this.rand);
      }
      return this;
    }
    else if (key.equals("backspace")) {
      return new ZTypeWorld(
          this.words.removeCurrentWord(this.currentWord)
              .addToEnd(this.currentWord.switchActivity()),
          new InactiveWord("", 0, 0), this.newWordTimer, this.score, this.rand);
    }
    else {
      IWord newCurrentWord = this.currentWord.checkAndReduce(key);
      if (!this.currentWord.equals(newCurrentWord)) {
        this.score = this.score + 1;
      }
      return new ZTypeWorld(this.words.removeCurrentWord(this.currentWord).addToEnd(newCurrentWord),
          newCurrentWord, this.newWordTimer, this.score, this.rand);
    }
  }

  // World Ends handler, ends game if a word reaches the bottom of the screen or player wins
  public WorldEnd worldEnds() {
    if (this.words.isGameOver(IUtils.SCREEN_HEIGHT)) {
      return new WorldEnd(true, this.lastScene("You Lose"));
    }
    else if (this.words.filterOutEmpties().isMt()) {
      return new WorldEnd(true, this.lastScene("You Win"));
    }
    else {
      return new WorldEnd(false, this.makeScene());
    }
  }
}

interface IUtils {
  int SCREEN_WIDTH = 600; // Screen width
  int SCREEN_HEIGHT = 400; // Screen height
  int WORD_LENGTH = 6; // Max length of words in the game
  int STARTING_WORDS_LENGTH = 1; // Number of words when game starts
  int TICKS_PER_WORD = 10; // Amount of ticks before spawning a new word
  int FONT_SIZE = 24;
  double TICK_RATE = .3;
  String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
  Utils U = new Utils();
  WorldScene BLANK = new WorldScene(SCREEN_WIDTH, SCREEN_HEIGHT); // Blank WorldScene
}

class Utils implements IUtils {
  /* TEMPLATE:
   * Methods:
   *  this.randomCharacter() ... String
   *  this.randomWord(int) ... String
   *  this.randomInactiveWord() ... IWord
   *  this.randomLoWord(int) ... ILoWord
   * */

  // Generates a random character
  String randomCharacter(Random rand) {
    int index = rand.nextInt(26);
    return IUtils.ALPHABET.substring(index, index + 1);
  }

  // Generates a random word of up to x length
  String randomWord(int length, Random rand) {
    if (length <= 0) {
      return "";
    }
    return this.randomCharacter(rand) + this.randomWord(length - 1, rand);
  }

  // Generates a random inactive word
  IWord randomInactiveWord(Random rand) {
    // Randomizes word length : TAS: PART 2, can unrandomize for Part 1 by removing randomness here
    // in randomLength -> int randomLength = IUtils.WORD_LENGTH
    int randomLength = rand.nextInt(IUtils.WORD_LENGTH) + 1;
    return new InactiveWord(this.randomWord(randomLength, rand), rand.nextInt(550) + 25,
        rand.nextInt(20) * -1);
  }

  // Generates a random list of inactive words of x length
  ILoWord randomLoWord(int length, Random rand) {
    if (length == 0) {
      return new MtLoWord();
    }
    else {
      return new ConsLoWord(this.randomInactiveWord(rand), this.randomLoWord(length - 1, rand));
    }
  }
}

class ExamplesZType {
  Utils u = new Utils();

  // IWords generated with Random(1)
  IWord rand_1_1 = new InactiveWord("ahjm", 379, -14);
  IWord rand_1_2 = new InactiveWord("wkrxn", 38, -2);
  IWord rand_1_3 = new InactiveWord("geebe", 257, -10);
  IWord rand_1_4 = new InactiveWord("ezsdzs", 280, -14);
  IWord rand_1_5 = new InactiveWord("c", 388, -15);
  IWord rand_1_6 = new InactiveWord("gdyx", 485, -17);

  // IWords generated with Random(2)
  IWord rand_2_1 = new InactiveWord("gavre", 31, -19);
  IWord rand_2_2 = new InactiveWord("ec", 461, -14);

  // IWords generated with Random(10)
  IWord rand_10_1 = new InactiveWord("glye", 131, -17);

  // ILoWord examples rand_{seed}_{length}_words
  ILoWord mt = new MtLoWord();
  ILoWord rand_1_6_words = new ConsLoWord(this.rand_1_1,
      new ConsLoWord(this.rand_1_2, new ConsLoWord(this.rand_1_3, new ConsLoWord(this.rand_1_4,
          new ConsLoWord(this.rand_1_5, new ConsLoWord(this.rand_1_6, this.mt))))));
  ILoWord rand_2_2_words = new ConsLoWord(this.rand_2_1, new ConsLoWord(this.rand_2_2, this.mt));
  ILoWord rand_10_1_words = new ConsLoWord(this.rand_10_1, this.mt);

  // ZTypeWorld examples
  ZTypeWorld w1 = new ZTypeWorld(this.mt, new InactiveWord("", 0, 0), 10, 0, new Random(1));
  ZTypeWorld w2 = new ZTypeWorld(new Random(1));
  ZTypeWorld w3 = new ZTypeWorld(
      new ConsLoWord(new InactiveWord("ahjm", 379, 100),
          new ConsLoWord(new InactiveWord("wkrxn", 38, 60),
              new ConsLoWord(new ActiveWord("geebe", 257, 30), this.mt))),
      new ActiveWord("geebe", 257, 30), 1, 0, new Random(1));
  ZTypeWorld w4 = new ZTypeWorld(new ConsLoWord(new ActiveWord("test", 100, 100), this.mt),
      new ActiveWord("test", 100, 100), 7, 0, new Random(1));
  ZTypeWorld w5 = new ZTypeWorld(new ConsLoWord(new ActiveWord("test", 100, 600), this.mt),
      new ActiveWord("test", 100, 100), 7, 0, new Random(1));
  // Test for makeScene() in ZTypeWorld
  boolean testMakeScene(Tester t) {
    return t.checkExpect(this.w1.makeScene(),
        IUtils.BLANK.placeImageXY(
            new TextImage("Score: 0", IUtils.FONT_SIZE, FontStyle.BOLD, Color.GREEN),
            IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT - IUtils.FONT_SIZE))
        && t.checkExpect(this.w2.makeScene(), IUtils.BLANK
            .placeImageXY(new TextImage("Score: 0", IUtils.FONT_SIZE, FontStyle.BOLD, Color.GREEN),
                IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT - IUtils.FONT_SIZE)
            .placeImageXY(new TextImage("ahjm", Color.RED), 379, -14))
        && t.checkExpect(this.w3.makeScene(), IUtils.BLANK
            .placeImageXY(new TextImage("Score: 0", IUtils.FONT_SIZE, FontStyle.BOLD, Color.GREEN),
                IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT - IUtils.FONT_SIZE)
            .placeImageXY(new TextImage("ahjm", IUtils.FONT_SIZE - 4, FontStyle.BOLD, Color.RED),
                379, 100)
            .placeImageXY(new TextImage("wkrxn", IUtils.FONT_SIZE - 4, FontStyle.BOLD, Color.RED),
                38, 60)
            .placeImageXY(new TextImage("geebe", IUtils.FONT_SIZE - 4, FontStyle.BOLD, Color.BLUE),
                257, 30));
  }

  // Test for lastScene() in ZTypeWorld
  boolean testLastScene(Tester t) {
    return t.checkExpect(this.w1.lastScene("You Lose"),
        IUtils.BLANK.placeImageXY(
            new TextImage("You Lose", IUtils.FONT_SIZE, FontStyle.BOLD, Color.GREEN),
            IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT / 2))
        && t.checkExpect(this.w3.lastScene("You Win"),
            IUtils.BLANK.placeImageXY(
                new TextImage("You Win", IUtils.FONT_SIZE, FontStyle.BOLD, Color.GREEN),
                IUtils.SCREEN_WIDTH / 2, IUtils.SCREEN_HEIGHT / 2));
  }

  // Test for onTick() in ZTypeWorld
  boolean testOnTick(Tester t) {
    return t.checkExpect(this.w1.onTick(),
        new ZTypeWorld(this.mt, new InactiveWord("", 0, 3), 9, 0, new Random(1)))
        && t.checkExpect(this.w1.onTick().onTick(),
            new ZTypeWorld(this.mt, new InactiveWord("", 0, 6), 8, 0, new Random(1)))
        && t.checkExpect(this.w2.onTick(),
            new ZTypeWorld(new ConsLoWord(this.rand_1_1.move(), this.mt),
                new InactiveWord("", 0, 3), 9, 0, new Random(1)))
        && t.checkExpect(this.w3.onTick(),
            new ZTypeWorld(
                new ConsLoWord(new InactiveWord("ahjm", 379, 100).move(),
                    new ConsLoWord(new InactiveWord("wkrxn", 38, 60).move(),
                        new ConsLoWord(new ActiveWord("geebe", 257, 30).move(),
                            new ConsLoWord(new InactiveWord("ahjm", 379, -11), this.mt)))),
                new ActiveWord("geebe", 257, 30).move(), 10, 0, new Random(1)));
  }

  // Test for onKeyEvent() in ZTypeWorld
  boolean testOnKeyEvent(Tester t) {
    return t.checkExpect(this.w1.onKeyEvent("a"), this.w1)
        && t.checkExpect(this.w2.onKeyEvent("b"), this.w2) && t
            .checkExpect(this.w2.onKeyEvent("a"),
                new ZTypeWorld(new ConsLoWord(new ActiveWord("hjm", 379, -14), this.mt),
                    new ActiveWord("hjm", 379, -14), 10, 1, new Random(1)))
        && t.checkExpect(this.w4.onKeyEvent("backspace"),
            new ZTypeWorld(new ConsLoWord(new InactiveWord("test", 100, 100), this.mt),
                new InactiveWord("", 0, 0), 7, 0, new Random(1)))
        && t.checkExpect(this.w4.onKeyEvent("t"),
            new ZTypeWorld(new ConsLoWord(new ActiveWord("est", 100, 100), this.mt),
                new ActiveWord("est", 100, 100), 7, 1, new Random(1)));
  }
  
  // Test for worldEnds() in ZTypeWorld
  boolean testWorldEnds(Tester t) {
    return t.checkExpect(this.w1.worldEnds(), new WorldEnd(true, this.w1.lastScene("You Win")))
        && t.checkExpect(this.w3.worldEnds(), new WorldEnd(false, this.w3.makeScene()))
        && t.checkExpect(this.w5.worldEnds(), new WorldEnd(true, this.w5.lastScene("You Lose")));
  }

  // Test for randomCharacter() in Utils
  boolean testRandomCharacter(Tester t) {
    return t.checkExpect(this.u.randomCharacter(new Random(1)), "r")
        && t.checkExpect(this.u.randomCharacter(new Random(2)), "s")
        && t.checkExpect(this.u.randomCharacter(new Random(10)), "p");
  }

  // Test for randomWord() in Utils
  boolean testRandomWord(Tester t) {
    return t.checkExpect(this.u.randomWord(0, new Random(1)), "")
        && t.checkExpect(this.u.randomWord(1, new Random(1)), "r")
        && t.checkExpect(this.u.randomWord(5, new Random(1)), "rahjm")
        && t.checkExpect(this.u.randomWord(6, new Random(2)), "sgavre");
  }

  // Test for randomInactiveWord() in Utils
  boolean testRandomInactiveWord(Tester t) {
    return t.checkExpect(this.u.randomInactiveWord(new Random(1)), this.rand_1_1)
        && t.checkExpect(this.u.randomInactiveWord(new Random(2)), this.rand_2_1)
        && t.checkExpect(this.u.randomInactiveWord(new Random(10)), this.rand_10_1);
  }

  // Test for randomLoWord in Utils
  boolean testRandomLoWord(Tester t) {
    return t.checkExpect(this.u.randomLoWord(0, new Random(1)), this.mt)
        && t.checkExpect(this.u.randomLoWord(1, new Random(10)), this.rand_10_1_words)
        && t.checkExpect(this.u.randomLoWord(2, new Random(2)), this.rand_2_2_words)
        && t.checkExpect(this.u.randomLoWord(6, new Random(1)), this.rand_1_6_words);
  }

  boolean testBigBang(Tester t) {
    ZTypeWorld world = new ZTypeWorld(new Random(1));
    return world.bigBang(IUtils.SCREEN_WIDTH, IUtils.SCREEN_HEIGHT, IUtils.TICK_RATE);
  }

}