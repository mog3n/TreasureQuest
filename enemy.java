import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;

public class enemy {
  //properties
  String strType;
  int intX = 0;
  int intY = 0;
  int intAttack = 0;
  int intSpeed = 0;
  boolean alive = true;
  boolean justKilled = false;
  boolean attacking = false;
  int intHealth = 100;
  
  double dblAngle = 0;
  boolean playerPresent [] = {false, false, false, false};
  double dblDistance [] = {10000, 10000, 10000, 10000};
  int Target = 10;
  boolean targetting = false;
  long timeSinceAttacked = 0;
  int CoolDown;
  
  //methods
  public void findRange (int PX, int PY, int intPlayer) {
    dblDistance [intPlayer] = Math.sqrt(Math.pow(intX - PX, 2) + Math.pow(intY - PY, 2));
    playerPresent [intPlayer] = true;
    if (CoolDown > 0 && intPlayer == Target) {
      CoolDown --;
    }
  }
  
  public void attemptActivate () {
    for (int Count = 0; Count < 4; Count ++) {
      if (Target == 10) {
        if (dblDistance [Count] < 400 && dblDistance [Count] > 0) {
          Target = Count;
          targetting = true;
        }
      }else {
        if (Math.min(dblDistance [Count], dblDistance [Target]) == dblDistance [Count]) {
          Target = Count;
          targetting = true;
        }
      }
    }
  }
  
  public void move (int PX, int PY) {
    dblAngle = findAngle (PX, PY);
    
    if (dblDistance [Target] > 70) {
      Color movementPixel = new Color (ServerMain.currentMap.MapBoundariesImg.getRGB(intX + (int) (40 * Math.cos(dblAngle)), intY + (int) (40 * Math.sin(dblAngle))));
      int intRed = movementPixel.getRed();
      
      if (intRed == 255) {
        intX += (int) (intSpeed * Math.cos (dblAngle));
        intY += (int) (intSpeed * Math.sin (dblAngle));
      }else {
        dblAngle = dblAngle - dblAngle % Math.PI/2;
        if (dblAngle % Math.PI/2 < Math.PI/4) {
          movementPixel = new Color (ServerMain.currentMap.MapBoundariesImg.getRGB(intX + (int) (40 * Math.cos(dblAngle)), intY + (int) (40 * Math.sin(dblAngle))));
          intRed = movementPixel.getRed();
          if (intRed == 255) {
            intX += (int) (intSpeed * Math.cos (dblAngle));
            intY += (int) (intSpeed * Math.sin (dblAngle));
          }
          
          dblAngle += Math.PI/2;
          movementPixel = new Color (ServerMain.currentMap.MapBoundariesImg.getRGB(intX + (int) (40 * Math.cos(dblAngle)), intY + (int) (40 * Math.sin(dblAngle))));
          intRed = movementPixel.getRed();
          if (intRed == 255) {
            intX += (int) (intSpeed * Math.cos (dblAngle));
            intY += (int) (intSpeed * Math.sin (dblAngle));
          }
        }else {
          dblAngle += Math.PI/2;
          movementPixel = new Color (ServerMain.currentMap.MapBoundariesImg.getRGB(intX + (int) (40 * Math.cos(dblAngle)), intY + (int) (40 * Math.sin(dblAngle))));
          intRed = movementPixel.getRed();
          if (intRed == 255) {
            intX += (int) (intSpeed * Math.cos (dblAngle));
            intY += (int) (intSpeed * Math.sin (dblAngle));
          }
          
          dblAngle -= Math.PI/2;
          movementPixel = new Color (ServerMain.currentMap.MapBoundariesImg.getRGB(intX + (int) (40 * Math.cos(dblAngle)), intY + (int) (40 * Math.sin(dblAngle))));
          intRed = movementPixel.getRed();
          if (intRed == 255) {
            intX += (int) (intSpeed * Math.cos (dblAngle));
            intY += (int) (intSpeed * Math.sin (dblAngle));
          }
        }
        
      }
    }
    if (dblDistance [Target] < 125 && CoolDown == 0) {
      CoolDown = 45;
      attacking = true;
    }
  }
  
  public void kill () {
    justKilled = true;
    alive = false;
  }
  
  public double findAngle (int PX, int PY) {
    double angle;
    
    try {
      angle = Math.atan2((PY - intY),(PX - intX));
      if (angle < 0) {
        angle = 2 * Math.PI + angle;
      }
    }catch (ArithmeticException e) {
      if (PY < intY) {
        angle = 3 * Math.PI/2;
      }else {
        angle = Math.PI/2;
      }
    }
    
    return angle;
  }
  
  //constructor
  public enemy (int intX, int intY, String strType, int intAttack, int intSpeed) {
    this.intX = intX * 60;
    this.intY = intY * 60;
    this.strType = strType;
    this.intAttack = intAttack;
    this.intSpeed = intSpeed;
    System.out.println(strType);
  }
  
  public static void main (String [] args) {
    enemy E = new enemy (0, 0, "shark", 10, 10);
    
    int X;
    int Y;
    
    //for (int Count = 0; Count <= 360; Count += 10) {
    X = (int) (1000 * Math.cos(Math.toRadians(45)));
    Y = (int) (1000 * Math.sin(Math.toRadians(45)));
    
    System.out.println (Math.toDegrees(E.findAngle (X, Y)));
    //}
  }
}