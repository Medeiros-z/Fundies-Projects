import tester.Tester;

import java.awt.Color;

import javalib.funworld.*;
import javalib.worldimages.*;

//represents a list of words
interface ILoWord {
  // adds a word to the end of the list
  ILoWord addToEnd(IWord w);

  // filter out words with empty strings from a list
  ILoWord filterOutEmpties();

  // draw list of words onto a world scene
  WorldScene draw(WorldScene ws);

  // moves this list of words
  ILoWord move();

  // get the first word in list that starts with given character
  IWord getWordStartsWith(String s);

  // remove given IWord from ILoWord if it exists inside
  ILoWord removeCurrentWord(IWord w);

  // return true if any words have a y value equal to given value
  boolean isGameOver(int screenHeight);

  // is this list empty?
  boolean isMt();
}

//represents an empty list of words
class MtLoWord implements ILoWord {

  /* TEMPLATE:
   * Methods:
   *  this.addToEnd(IWord) ... ILoWord
   *  this.filterOutEmpties() ... ILoWord
   *  this.draw(WorldScene) ... WorldScene
   *  this.move() ... ILoWord
   *  this.getWordStartsWith(String) ... IWord
   *  this.removeCurrentWord(IWord) ... ILoWord
   *  this.isGameOver(int) .. boolean
   *  this.isMt() ... boolean
   * */

  // adds a word to the end of the list
  public ILoWord addToEnd(IWord w) {
    /* TEMPLATE for this method
     * EVERYTHING from our class-wide template...
     * Methods of parameters:
     *  w.startsWith(String) ... boolean
     *  w.checkAndReduce(String) ... IWord
     *  w.isEmpty() ... boolean
     *  w.placeImage(WorldScene) ... WorldScene
     *  w.move() ... IWord
     *  w.equals(IWord) ... boolean
     *  w.sameActiveWord(ActiveWord) ... boolean
     *  w.sameInactiveWord(InactiveWord) ... boolean
     *  w.switchActivity() ... IWord
     *  w.checkYPos(int) ... boolean
     * */
    return new ConsLoWord(w, this);
  }

  // filter out words with empty strings from a list
  public ILoWord filterOutEmpties() {
    return this;
  }

  // draw list of words onto a world scene
  public WorldScene draw(WorldScene ws) {
    return ws;
  }

  // moves the words in this empty list
  public ILoWord move() {
    return this;
  }

  // get the first word in list that starts with given character
  public IWord getWordStartsWith(String s) {
    return new InactiveWord("", 0, 0);
  }

  // remove given IWord from ILoWord if it exists inside
  public ILoWord removeCurrentWord(IWord w) {
    /* TEMPLATE for this method
     * EVERYTHING from our class-wide template...
     * Methods of parameters:
     *  w.startsWith(String) ... boolean
     *  w.checkAndReduce(String) ... IWord
     *  w.isEmpty() ... boolean
     *  w.placeImage(WorldScene) ... WorldScene
     *  w.move() ... IWord
     *  w.equals(IWord) ... boolean
     *  w.sameActiveWord(ActiveWord) ... boolean
     *  w.sameInactiveWord(InactiveWord) ... boolean
     *  w.switchActivity() ... IWord
     *  w.checkYPos(int) ... boolean
     * */
    return this;
  }

  // return true if any words have a y value equal to given value
  public boolean isGameOver(int screenHeight) {
    return false;
  }

  // is this list empty?
  public boolean isMt() {
    return true;
  }
}

class ConsLoWord implements ILoWord {
  IWord first;
  ILoWord rest;

  // the constructor
  ConsLoWord(IWord first, ILoWord rest) {
    this.first = first;
    this.rest = rest;
  }

  /* TEMPLATE:
   * Fields:
   *  this.first ... IWord
   *  this.rest ... ILoWord
   * Methods:
   *  this.addToEnd(IWord) ... ILoWord
   *  this.filterOutEmpties() ... ILoWord
   *  this.draw(WorldScene) ... WorldScene
   *  this.move() ... ILoWord
   *  this.getWordStartsWith(String) ... IWord
   *  this.removeCurrentWord(IWord) ... ILoWord
   *  this.isGameOver(int) ... boolean
   *  this.isMt() ... boolean
   * Methods of Fields:
   *  this.first.startsWith(String) ... boolean
   *  this.first.checkAndReduce(String) ... IWord
   *  this.first.isEmpty() ... boolean
   *  this.first.placeImage(WorldScene) ... WorldScene
   *  this.first.move() ... IWord
   *  this.first.equals(IWord) ... boolean
   *  this.first.sameActiveWord(ActiveWord) ... boolean
   *  this.first.sameInactiveWord(InactiveWord) ... boolean
   *  this.first.switchActivity() ... IWord
   *  this.first.checkYPos(int) ... boolean
   *  
   *  this.rest.addToEnd(IWord) ... ILoWord
   *  this.rest.filterOutEmpties() ... ILoWord
   *  this.rest.draw(WorldScene) ... WorldScene
   *  this.rest.move() ... ILoWord
   *  this.rest.getWordStartsWith(String) ... IWord
   *  this.rest.removeCurrentWord(IWord) ... ILoWord
   *  this.rest.isGameOver(int) ... boolean
   *  this.rest.isMt() ... boolean
   * */

  // adds a word to the end of the list
  public ILoWord addToEnd(IWord w) {
    /* TEMPLATE for this method
     * EVERYTHING from our class-wide template...
     * Methods of parameters:
     *  w.startsWith(String) ... boolean
     *  w.checkAndReduce(String) ... IWord
     *  w.isEmpty() ... boolean
     *  w.placeImage(WorldScene) ... WorldScene
     *  w.move() ... IWord
     *  w.equals(IWord) ... boolean
     *  w.sameActiveWord(ActiveWord) ... boolean
     *  w.sameInactiveWord(InactiveWord) ... boolean
     *  w.switchActivity() ... IWord
     *  w.checkYPos(int) ... boolean
     * */
    return new ConsLoWord(this.first, this.rest.addToEnd(w));
  }

  // filter out words with empty strings from a list
  public ILoWord filterOutEmpties() {
    if (this.first.isEmpty()) {
      return this.rest.filterOutEmpties();
    }
    else {
      return new ConsLoWord(this.first, this.rest.filterOutEmpties());
    }
  }

  // draw list of words onto a world scene
  public WorldScene draw(WorldScene ws) {
    return this.rest.draw(this.first.placeImage(ws));
  }

  // move words in this list of words
  public ILoWord move() {
    return new ConsLoWord(this.first.move(), this.rest.move());
  }

  // get the first word in list that starts with given character and makes it active
  public IWord getWordStartsWith(String s) {
    if (this.first.startsWith(s) && !s.isEmpty()) {
      return this.first;
    }
    else {
      return this.rest.getWordStartsWith(s);
    }
  }

  // remove given IWord from ILoWord if it exists inside
  public ILoWord removeCurrentWord(IWord w) {
    /* TEMPLATE for this method
     * EVERYTHING from our class-wide template...
     * Methods of parameters:
     *  w.startsWith(String) ... boolean
     *  w.checkAndReduce(String) ... IWord
     *  w.isEmpty() ... boolean
     *  w.placeImage(WorldScene) ... WorldScene
     *  w.move() ... IWord
     *  w.equals(IWord) ... boolean
     *  w.sameActiveWord(ActiveWord) ... boolean
     *  w.sameInactiveWord(InactiveWord) ... boolean
     *  w.switchActivity() ... IWord
     *  w.checkYPos(int) ... boolean
     * */
    if (this.first.equals(w)) {
      return this.rest;
    }
    else {
      return new ConsLoWord(this.first, this.rest.removeCurrentWord(w));
    }
  }

  // return true if any words have a y value equal to given value
  public boolean isGameOver(int screenHeight) {
    return this.first.checkYPos(screenHeight) || this.rest.isGameOver(screenHeight);
  }

  // is this list empty?
  public boolean isMt() {
    return false;
  }
}

//represents a word in the ZType game
interface IWord {
  // removes first letter of given string if its equal to char input, else return string
  IWord checkAndReduce(String s);

  // returns true if word string is empty
  boolean isEmpty();

  // place word text onto world scene
  WorldScene placeImage(WorldScene ws);

  // move this word
  IWord move();

  // does this.word start with given character
  boolean startsWith(String s);

  // does this word have the same word and x position as that word
  boolean equals(IWord w);

  // does this word equal that active word
  boolean sameActiveWord(ActiveWord that);

  // does this word equal that inactive word
  boolean sameInactiveWord(InactiveWord that);

  // turns a IWord into an ActiveWord
  IWord switchActivity();

  // return true if this.y is equal to given value
  boolean checkYPos(int screenHeight);
}

// represents a word 
abstract class AWord implements IWord {
  String word;
  int x;
  int y;

  // the constructor
  AWord(String word, int x, int y) {
    this.word = word;
    this.x = x;
    this.y = y;
  }

  /* TEMPLATE:
   * Fields:
   *  this.word ... String
   *  this.x ... integer
   *  this.y ... integer
   * Methods:
   *  this.startsWith(String) ... boolean
   *  this.checkAndReduce(String) ... IWord
   *  this.isEmpty() ... boolean
   *  this.placeImage(WorldScene) ... WorldScene
   *  this.move() ... IWord
   *  this.equals(IWord) ... boolean
   *  this.sameActiveWord(ActiveWord) ... boolean
   *  this.sameInactiveWord(InactiveWord) ... boolean
   *  this.switchActivity() ... IWord
   *  this.checkYPos(int) ... boolean
   * */

  // does this.word start with given character
  public boolean startsWith(String s) {
    return this.word.startsWith(s);
  }

  // removes first letter of given string if its equal to char input, else return string
  public abstract IWord checkAndReduce(String s);

  // returns true if word string is empty
  public boolean isEmpty() {
    return this.word.equals("");
  }

  // place word text onto world scene
  public abstract WorldScene placeImage(WorldScene ws);

  // move this word
  public abstract IWord move();

  // does this word equal that word
  public abstract boolean equals(IWord w);

  // does this word equal that active word
  public boolean sameActiveWord(ActiveWord that) {
    /* TEMPLATE for this method
     * EVERYTHING from our class-wide template...
     * Methods of parameters:
     *  that.startsWith(String) ... boolean
     *  that.checkAndReduce(String) ... IWord
     *  that.isEmpty() ... boolean
     *  that.placeImage(WorldScene) ... WorldScene
     *  that.move() ... IWord
     *  that.equals(IWord) ... boolean
     *  that.sameActiveWord(ActiveWord) ... boolean
     *  that.sameInactiveWord(InactiveWord) ... boolean
     *  that.switchActivity() ... IWord
     *  that.checkYPos(int) ... boolean
     * */
    return false;
  }

  // does this word equal that inactive word
  public boolean sameInactiveWord(InactiveWord that) {
    /* TEMPLATE for this method
     * EVERYTHING from our class-wide template...
     * Methods of parameters:
     *  that.startsWith(String) ... boolean
     *  that.checkAndReduce(String) ... IWord
     *  that.isEmpty() ... boolean
     *  that.placeImage(WorldScene) ... WorldScene
     *  that.move() ... IWord
     *  that.equals(IWord) ... boolean
     *  that.sameActiveWord(ActiveWord) ... boolean
     *  that.sameInactiveWord(InactiveWord) ... boolean
     *  that.switchActivity() ... IWord
     *  that.checkYPos(int) ... boolean
     * */
    return false;
  }

  // switches the activity of a IWord
  public abstract IWord switchActivity();

  // return true if this.y is greater than or equal to the given value
  public boolean checkYPos(int screenHeight) {
    return this.y >= screenHeight;
  }
}

//represents an active word in the ZType game
class ActiveWord extends AWord {

  // the constructor
  ActiveWord(String word, int x, int y) {
    super(word, x, y);
  }

  /* TEMPLATE:
   * Fields:
   *  this.word ... String
   *  this.x ... integer
   *  this.y ... integer
   * Methods:
   *  this.startsWith(String) ... boolean
   *  this.checkAndReduce(String) ... IWord
   *  this.isEmpty() ... boolean
   *  this.placeImage(WorldScene) ... WorldScene
   *  this.move() ... IWord
   *  this.equals(IWord) ... boolean
   *  this.sameActiveWord(ActiveWord) ... boolean
   *  this.sameInactiveWord(InactiveWord) ... boolean
   *  this.switchActivity() ... IWord
   *  this.checkYPos(int) ... boolean
   * */

  // removes first letter of given string if its equal to char input, else return string
  public IWord checkAndReduce(String s) {
    if (this.word.length() > 0 && this.startsWith(s)) {
      return new ActiveWord(this.word.substring(1), this.x, this.y);
    }
    else {
      return this;
    }
  }

  // place word text onto world scene
  public WorldScene placeImage(WorldScene ws) {
    return ws.placeImageXY(
        new TextImage(this.word, IUtils.FONT_SIZE - 4, FontStyle.BOLD, Color.BLUE), this.x, this.y);
  }

  // move this word
  public IWord move() {
    return new ActiveWord(this.word, this.x, this.y + 3);
  }

  // does this word equal that word
  public boolean equals(IWord w) {
    /* TEMPLATE for this method
     * EVERYTHING from our class-wide template...
     * Methods of parameters:
     *  w.startsWith(String) ... boolean
     *  w.checkAndReduce(String) ... IWord
     *  w.isEmpty() ... boolean
     *  w.placeImage(WorldScene) ... WorldScene
     *  w.move() ... IWord
     *  w.equals(IWord) ... boolean
     *  w.sameActiveWord(ActiveWord) ... boolean
     *  w.sameInactiveWord(InactiveWord) ... boolean
     *  w.switchActivity() ... IWord
     *  w.checkYPos(int) ... boolean
     * */
    return w.sameActiveWord(this);
  }

  // does this word equal that active word
  public boolean sameActiveWord(ActiveWord that) {
    /* TEMPLATE for this method
     * EVERYTHING from our class-wide template...
     * Methods of parameters:
     *  that.startsWith(String) ... boolean
     *  that.checkAndReduce(String) ... IWord
     *  that.isEmpty() ... boolean
     *  that.placeImage(WorldScene) ... WorldScene
     *  that.move() ... IWord
     *  that.equals(IWord) ... boolean
     *  that.sameActiveWord(ActiveWord) ... boolean
     *  that.sameInactiveWord(InactiveWord) ... boolean
     *  that.switchActivity() ... IWord
     *  that.checkYPos(int) ... boolean
     * */
    return this.word.equals(that.word) && this.x == that.x && this.y == that.y;
  }

  public IWord switchActivity() {
    return new InactiveWord(this.word, this.x, this.y);
  }
}

//represents an inactive word in the ZType game
class InactiveWord extends AWord {

  // the constructor
  InactiveWord(String word, int x, int y) {
    super(word, x, y);
  }

  /* TEMPLATE
   * Fields:
   *  this.word ... String
   *  this.x ... integer
   *  this.y ... integer
   * Methods:
   *  this.startsWith(String) ... boolean
   *  this.checkAndReduce(String) ... IWord
   *  this.isEmpty() ... boolean
   *  this.placeImage(WorldScene) ... WorldScene
   *  this.move() ... IWord
   *  this.equals(IWord) ... boolean
   *  this.sameActiveWord(ActiveWord) ... boolean
   *  this.sameInactiveWord(InactiveWord) ... boolean
   *  this.switchActivity() ... IWord
   *  this.checkYPos(int) ... boolean
   * */

  // removes first letter of given string if its equal to char input, else return string
  public IWord checkAndReduce(String s) {
    return this;
  }

  // place word text onto world scene
  public WorldScene placeImage(WorldScene ws) {
    return ws.placeImageXY(
        new TextImage(this.word, IUtils.FONT_SIZE - 4, FontStyle.BOLD, Color.RED), this.x, this.y);
  }

  // move this word
  public IWord move() {
    return new InactiveWord(this.word, this.x, this.y + 3);
  }

  // does this word equal that word
  public boolean equals(IWord w) {
    /* TEMPLATE for this method
     * EVERYTHING from our class-wide template...
     * Methods of parameters:
     *  w.startsWith(String) ... boolean
     *  w.checkAndReduce(String) ... IWord
     *  w.isEmpty() ... boolean
     *  w.placeImage(WorldScene) ... WorldScene
     *  w.move() ... IWord
     *  w.equals(IWord) ... boolean
     *  w.sameActiveWord(ActiveWord) ... boolean
     *  w.sameInactiveWord(InactiveWord) ... boolean
     *  w.switchActivity() ... IWord
     *  w.checkYPos(int) ... boolean
     * */
    return w.sameInactiveWord(this);
  }

  // does this word equal that inactive word
  public boolean sameInactiveWord(InactiveWord that) {
    /* TEMPLATE for this method
     * EVERYTHING from our class-wide template...
     * Methods of parameters:
     *  that.startsWith(String) ... boolean
     *  that.checkAndReduce(String) ... IWord
     *  that.isEmpty() ... boolean
     *  that.placeImage(WorldScene) ... WorldScene
     *  that.move() ... IWord
     *  that.equals(IWord) ... boolean
     *  that.sameActiveWord(ActiveWord) ... boolean
     *  that.sameInactiveWord(InactiveWord) ... boolean
     *  that.switchActivity() ... IWord
     *  that.checkYPos(int) ... boolean
     * */
    return this.word.equals(that.word) && this.x == that.x && this.y == that.y;
  }

  public IWord switchActivity() {
    return new ActiveWord(this.word, this.x, this.y);
  }
}

//all examples and tests for ILoWord
class ExamplesWordLists {
  // IWord examples
  IWord apple = new ActiveWord("apple", 10, 10);
  IWord apples = new ActiveWord("apples", 25, 70);
  IWord pple = new ActiveWord("pple", 10, 10);
  IWord banana = new ActiveWord("baNANa", 180, 240);
  IWord cake = new InactiveWord("cake", 300, 300);
  IWord dip = new InactiveWord("DIP", 450, 500);
  IWord mtWord = new InactiveWord("", 0, 0);

  // ILoWord examples
  ILoWord mt = new MtLoWord();
  ILoWord one = new ConsLoWord(this.apple, this.mt);
  ILoWord two = new ConsLoWord(this.cake, this.one);
  ILoWord three = new ConsLoWord(this.banana, this.two);
  ILoWord four = new ConsLoWord(this.apples, one);
  ILoWord five = new ConsLoWord(this.mtWord, new ConsLoWord(this.mtWord, this.mt));
  ILoWord six = new ConsLoWord(this.apple, this.five);
  ILoWord seven = new ConsLoWord(this.banana, new ConsLoWord(this.mtWord, this.six));
  ILoWord eight = new ConsLoWord(this.apple, new ConsLoWord(this.dip, this.mt));

  // WorldScene Example
  WorldScene ws = new WorldScene(500, 500)
      .placeImageXY(new RectangleImage(500, 500, "solid", Color.BLACK), 250, 250);

  // test for ILoWord addToEnd()
  boolean testAddToEnd(Tester t) {
    // test addToEnd with mt list
    return t.checkExpect(this.mt.addToEnd(this.apple), this.one)
        // test addToEnd with ConsLoList
        && t.checkExpect(this.one.addToEnd(this.apple), new ConsLoWord(this.apple, this.one))
        && t.checkExpect(this.two.addToEnd(this.dip),
            new ConsLoWord(this.cake,
                new ConsLoWord(this.apple, new ConsLoWord(this.dip, this.mt))))
        && t.checkExpect(this.three.addToEnd(this.banana),
            new ConsLoWord(this.banana,
                new ConsLoWord(this.cake,
                    new ConsLoWord(this.apple, new ConsLoWord(this.banana, this.mt)))))
        && t.checkExpect(this.four.addToEnd(this.apple),
            new ConsLoWord(this.apples, new ConsLoWord(this.apple, this.one)))
        && t.checkExpect(this.five.addToEnd(this.mtWord), new ConsLoWord(this.mtWord, this.five));
  }

  // test for ILoWord filterOutEmpties()
  boolean testFilterOutEmpties(Tester t) {
    // test filterOutEmptoes with mt list
    return t.checkExpect(this.mt.filterOutEmpties(), this.mt)
        // test filterOutEmpties with ConsLoList
        && t.checkExpect(this.one.filterOutEmpties(), this.one)
        && t.checkExpect(this.three.filterOutEmpties(), this.three)
        // test filterOutEmpties with list of empty words
        && t.checkExpect(this.five.filterOutEmpties(), this.mt)
        // test filterOurEmpties with list of empty and nonempty words
        && t.checkExpect(this.six.filterOutEmpties(), this.one)
        && t.checkExpect(this.seven.filterOutEmpties(),
            new ConsLoWord(this.banana, new ConsLoWord(this.apple, this.mt)));
  }

  // test for ILoWord draw()
  boolean testDraw(Tester t) {
    // test draw with mt list
    return t.checkExpect(this.mt.draw(this.ws), this.ws)
        // test draw with mt with world scene
        && t.checkExpect(
            this.mt.draw(ws.placeImageXY(
                new TextImage("apple", IUtils.FONT_SIZE - 4, FontStyle.BOLD, Color.BLUE), 10, 10)),
            ws.placeImageXY(
                new TextImage("apple", IUtils.FONT_SIZE - 4, FontStyle.BOLD, Color.BLUE), 10, 10))
        // test draw with ConsLoList
        && t.checkExpect(this.one.draw(this.ws),
            this.ws.placeImageXY(
                new TextImage("apple", IUtils.FONT_SIZE - 4, FontStyle.BOLD, Color.BLUE), 10, 10))
        && t.checkExpect(this.two.draw(this.ws), this.ws
            .placeImageXY(new TextImage("apple", IUtils.FONT_SIZE - 4, FontStyle.BOLD, Color.BLUE),
                10, 10)
            .placeImageXY(new TextImage("cake", IUtils.FONT_SIZE - 4, FontStyle.BOLD, Color.RED),
                300, 300))
        // test draw with ConsLoList with multiple words
        && t.checkExpect(this.three.draw(this.ws), this.ws
            .placeImageXY(new TextImage("apple", IUtils.FONT_SIZE - 4, FontStyle.BOLD, Color.BLUE),
                10, 10)
            .placeImageXY(new TextImage("cake", IUtils.FONT_SIZE - 4, FontStyle.BOLD, Color.RED),
                300, 300)
            .placeImageXY(new TextImage("baNANa", IUtils.FONT_SIZE - 4, FontStyle.BOLD, Color.BLUE),
                180, 240))
        && t.checkExpect(this.five.draw(this.ws), this.ws)
        && t.checkExpect(this.six.draw(this.ws), this.ws.placeImageXY(
            new TextImage("apple", IUtils.FONT_SIZE - 4, FontStyle.BOLD, Color.BLUE), 10, 10));
  }

  // test for ILoWord move()
  boolean testMove(Tester t) {
    return t.checkExpect(this.mt.move(), this.mt)
        && t.checkExpect(this.one.move(), new ConsLoWord(new ActiveWord("apple", 10, 13), this.mt))
        && t.checkExpect(this.two.move(),
            new ConsLoWord(new InactiveWord("cake", 300, 303),
                new ConsLoWord(new ActiveWord("apple", 10, 13), this.mt)))
        && t.checkExpect(this.six.filterOutEmpties().move(),
            new ConsLoWord(this.apple.move(), this.mt));
  }

  // test for ILoWord getWordStartsWith()
  boolean testGetWordStartsWith(Tester t) {
    return t.checkExpect(this.mt.getWordStartsWith("a"), this.mtWord)
        && t.checkExpect(this.one.getWordStartsWith("a"), this.apple)
        && t.checkExpect(this.one.getWordStartsWith("b"), this.mtWord)
        && t.checkExpect(this.three.getWordStartsWith("c"), this.cake)
        && t.checkExpect(this.six.getWordStartsWith(""), this.mtWord)
        && t.checkExpect(this.six.getWordStartsWith("a"), this.apple);
  }

  // test for ILoWord removeCurrentWord()
  boolean testRemoveCurrentWord(Tester t) {
    return t.checkExpect(this.mt.removeCurrentWord(this.mtWord), this.mt)
        && t.checkExpect(this.mt.removeCurrentWord(this.apple), this.mt)
        && t.checkExpect(this.one.removeCurrentWord(this.mtWord), this.one)
        && t.checkExpect(this.one.removeCurrentWord(this.apple), this.mt)
        && t.checkExpect(this.three.removeCurrentWord(this.cake),
            new ConsLoWord(this.banana, this.one))
        && t.checkExpect(this.five.removeCurrentWord(this.mtWord),
            new ConsLoWord(this.mtWord, this.mt));
  }

  // test for ILoWord isGameOver()
  boolean testIsGameOver(Tester t) {
    return t.checkExpect(this.mt.isGameOver(400), false)
        && t.checkExpect(this.one.isGameOver(400), false)
        && t.checkExpect(this.three.isGameOver(1), true)
        && t.checkExpect(this.six.isGameOver(200), false)
        && t.checkExpect(this.eight.isGameOver(400), true);
  }

  // test for ILoWord isMt()
  boolean testIsMt(Tester t) {
    return t.checkExpect(this.mt.isMt(), true)
        && t.checkExpect(this.one.isMt(), false)
        && t.checkExpect(this.three.isMt(), false)
        && t.checkExpect(this.six.isMt(), false);
  }

  // test for IWord checkAndReduce()
  boolean testCheckAndReduceIWord(Tester t) {
    // test checkAndReduce with word with letter
    return t.checkExpect(this.apple.checkAndReduce("a"), new ActiveWord("pple", 10, 10))
        // test checkAndReduce with word without letter
        && t.checkExpect(this.apple.checkAndReduce("b"), new ActiveWord("apple", 10, 10))
        && t.checkExpect(this.banana.checkAndReduce("c"), this.banana)
        && t.checkExpect(this.cake.checkAndReduce("z"), this.cake)
        && t.checkExpect(this.cake.checkAndReduce("c"), this.cake)
        // test checkAndReduce with mtWord
        && t.checkExpect(this.mtWord.checkAndReduce("p"), this.mtWord)
        // test checkAndReduce on case sensitive word
        && t.checkExpect(this.dip.checkAndReduce("d"), this.dip)
        // test checkAndReduce on case sensitive word
        && t.checkExpect(this.dip.checkAndReduce("D"), this.dip);
  }

  // test for IWord isEmpty()
  boolean testIsEmpty(Tester t) {
    // test isEmpty on nonempty word
    return t.checkExpect(this.apple.isEmpty(), false) && t.checkExpect(this.banana.isEmpty(), false)
        && t.checkExpect(this.cake.isEmpty(), false)
        // test isEmpty on empty word
        && t.checkExpect(this.mtWord.isEmpty(), true);
  }

  // test for IWord placeImage()
  boolean testPlaceImage(Tester t) {
    // test placeImage on nonempty word
    return t.checkExpect(this.apple.placeImage(this.ws),
        this.ws.placeImageXY(
            new TextImage("apple", IUtils.FONT_SIZE - 4, FontStyle.BOLD, Color.BLUE), 10, 10))
        && t.checkExpect(this.banana.placeImage(this.ws), this.ws.placeImageXY(
            new TextImage("baNANa", IUtils.FONT_SIZE - 4, FontStyle.BOLD, Color.BLUE), 180, 240))
        && t.checkExpect(this.cake.placeImage(this.ws), this.ws.placeImageXY(
            new TextImage("cake", IUtils.FONT_SIZE - 4, FontStyle.BOLD, Color.RED), 300, 300))
        // test placeImage on mtWord
        && t.checkExpect(this.mtWord.placeImage(this.ws), this.ws.placeImageXY(
            new TextImage("", IUtils.FONT_SIZE - 4, FontStyle.BOLD, Color.RED), 200, 400));
  }

  // test for IWord move()
  boolean testMoveIWord(Tester t) {
    // test move on ActiveWord
    return t.checkExpect(this.apple.move(), new ActiveWord("apple", 10, 13))
        && t.checkExpect(this.banana.move(), new ActiveWord("baNANa", 180, 243))
        // test move on InactiveWord
        && t.checkExpect(this.cake.move(), new InactiveWord("cake", 300, 303))
        && t.checkExpect(this.dip.move(), new InactiveWord("DIP", 450, 500));
  }

  // test for IWord startsWith()
  boolean testStartsWith(Tester t) {
    // test startsWith on ActiveWord
    return t.checkExpect(this.apple.startsWith("a"), true)
        && t.checkExpect(this.banana.startsWith("a"), false)
        // test startsWith on InactiveWord
        && t.checkExpect(this.cake.startsWith("c"), true)
        && t.checkExpect(this.dip.startsWith("c"), false);
  }

  // test for IWord equals()
  boolean testEquals(Tester t) {
    // test equals on ActiveWord
    return t.checkExpect(this.apple.equals(this.apple), true)
        && t.checkExpect(this.apple.equals(this.banana), false)
        && t.checkExpect(this.banana.equals(this.apple), false)
        // test Equals on InactiveWord
        && t.checkExpect(this.dip.equals(this.dip), true)
        && t.checkExpect(this.dip.equals(this.cake), false)
        && t.checkExpect(this.cake.equals(this.dip), false)
        // test equals on combination
        && t.checkExpect(this.apple.equals(this.cake), false)
        && t.checkExpect(this.cake.equals(this.apple), false);
  }

  // test for IWord sameActiveWord()
  boolean testSameActiveWord(Tester t) {
    // test on ActiveWord
    return t.checkExpect(this.apple.sameActiveWord(new ActiveWord("apple", 10, 10)), true)
        && t.checkExpect(this.apple.sameActiveWord(new ActiveWord("apple", 100, 100)), false)
        // test on InactiveWord
        && t.checkExpect(this.dip.sameActiveWord(new ActiveWord("DIP", 450, 500)), false);
  }

  // test for IWord sameInactiveWord()
  boolean testSameInactiveWord(Tester t) {
    // test on InactiveWord
    return t.checkExpect(this.cake.sameInactiveWord(new InactiveWord("cake", 300, 300)), true)
        && t.checkExpect(this.cake.sameInactiveWord(new InactiveWord("CAKE", 300, 300)), false)
        // test on ActiveWord
        && t.checkExpect(this.apple.sameInactiveWord(new InactiveWord("apple", 10, 10)), false);
  }
  
  // test for IWord switchActivity()
  boolean testSwitchActivity(Tester t) {
    // test on ActiveWord
    return t.checkExpect(this.apple.switchActivity(), new InactiveWord("apple", 10, 10))
        && t.checkExpect(this.banana.switchActivity(), new InactiveWord("baNANa", 180, 240))
        // test on InactiveWord
        && t.checkExpect(this.cake.switchActivity(), new ActiveWord("cake", 300, 300))
        && t.checkExpect(this.dip.switchActivity(), new ActiveWord("DIP", 450, 500));
  }

  // test for IWord checkYPos()
  boolean testCheckYPos(Tester t) {
    // test on ActiveWord
    return t.checkExpect(this.apple.checkYPos(12), false)
        && t.checkExpect(this.apple.checkYPos(9), true)
        // test on InactiveWord
        && t.checkExpect(this.cake.checkYPos(400), false)
        && t.checkExpect(this.cake.checkYPos(200), true);
  }
}