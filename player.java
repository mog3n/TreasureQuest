import java.io.*;
import java.awt.Color;

public class player {
  //properties
  static String strType;
  int intSpeed = 5;
  static int X = 0;
  static int SpawnX = 0;
  static int SpawnY = 0;
  static int Y = 0;
  int MouseX = 0;
  int MouseY = 0;
  double dblAngle = 0;
  int degreesAngle = 0;
  
  boolean blnUp = false;
  boolean blnDown = false;
  boolean blnLeft = false;
  boolean blnRight = false;
  int intMoveState = 0;
  
  boolean isAlive = false;
  static int intHealth = 100;
  static int maxHealth = 100;
  static long deathTime = 0;
  
  Map currentMap;
  
  //methods
  public void create () {
    //File IO
    //Init: speed, strength, mana, max health, defense, attacks
  }
  public int move () {
    
    //Moves player using blnDirections
    //Adjust angle
    //decrease cooldown
    intMoveState = 0;
    int pixel1;
    int pixel2;
    
    if (blnUp) {
      pixel1 = getPixelRGB (X - 25, Y + 10, "r");
      pixel2 = getPixelRGB (X + 25, Y + 10, "r");
      
      if (pixel1 != 0 && pixel2 != 0) { 
        Y -= intSpeed;
        intMoveState -= 1;
      }
    }
    if (blnDown) {
      pixel1 = getPixelRGB (X - 25, Y + 46, "r");
      pixel2 = getPixelRGB (X + 25, Y + 46, "r");
      
      if (pixel1 != 0 && pixel2 != 0) { 
        Y += intSpeed;
        intMoveState += 1;
      }
    }
    if (blnLeft) {
      pixel1 = getPixelRGB (X - 30, Y + 15, "r");
      pixel2 = getPixelRGB (X - 30, Y + 30, "r");
      
      if (pixel1 != 0 && pixel2 != 0) { 
        X -= intSpeed;
        intMoveState -= 2;
      }
    }
    if (blnRight) {
      pixel1 = getPixelRGB (X + 30, Y + 15, "r");
      pixel2 = getPixelRGB (X + 30, Y + 30, "r");
      
      if (pixel1 != 0 && pixel2 != 0) { 
        X += intSpeed;
        intMoveState += 2;
      }
    }
    
    try {
      dblAngle = Math.atan2((MouseY - Y),(MouseX - X));
      if (dblAngle < - Math.PI/4) {
        dblAngle = 2 * Math.PI + dblAngle;
      }
    }catch (ArithmeticException e) {
      if (MouseY < Y) {
        dblAngle = 3 * Math.PI/2;
      }else {
        dblAngle = Math.PI/2;
      }
    }
    degreesAngle = (int) (Math.toDegrees(dblAngle));  
    
     if(intHealth < 0 && !UserInterface.deathScreenVisible){
      UserInterface.deathScreenVisible = true;
      deathTime = System.nanoTime();
      ClientMain.ssm.sendText("player," + ClientMain.intPlayerNumber + ",iamdeadlol");
    }
    
    return getPixelRGB (X, Y + 46, "g");
  }
  
  public void attack () {
    
  }
  
  public int getPixelRGB (int pixelX, int pixelY, String strRGB) {
    Color pixelColor = new Color(0,0,0,0);
    if(currentMap != null){
      pixelColor = new Color (currentMap.MapBoundariesImg.getRGB(pixelX, pixelY));
    }
    
    if (strRGB.equals ("r")) {
      return pixelColor.getRed();
    }else if (strRGB.equals ("g")) {
      return pixelColor.getGreen();
    }else if (strRGB.equals ("b")) {
      return pixelColor.getBlue();
    }else return 0;
  }
  
  //constructor
  public player (String strType, int intX, int intY, String strMap) {
    //init
    this.X = intX;
    SpawnX = intX;
    SpawnY = intY;
    this.Y = intY;
    this.strType = strType;
    
    currentMap = new Map (strMap + ".csv");
    System.out.println("PLAYER" + strMap);
    try {
      currentMap.renderBoundMap();
    }catch (IOException e) {
      System.out.println("error making boundary");
    }
    
    create();
    //call create
  }
}