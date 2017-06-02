import java.awt.*;
import javax.swing.*;
import javax.imageio.*;
import java.awt.image.*;
import java.util.*;
import java.io.*;

public class mapCreator {
  public static void main (String [] args) {
    Scanner scn = new Scanner (System.in);
    BufferedImage img = null;
    String strImgFileName;
    String strCSVFileName;
    String strImage;
    FileWriter fr = null;
    PrintWriter pr = null;
    
    System.out.println ("Image name: ");
    strImgFileName = scn.nextLine();
    try {
      img = ImageIO.read(new File (strImgFileName));
    }catch (IOException e) { 
    }
    
    System.out.println ("File name: ");
    strCSVFileName = scn.nextLine();
    try {
      fr = new FileWriter (strCSVFileName);
      pr = new PrintWriter (fr);
    }catch (IOException e) {
    }
    
    System.out.println ("imagefile: ");
    strImage = scn.nextLine();
    pr.println (img.getHeight() + "," + img.getWidth() + "," + strImage);
    
    for (int Count = 0; Count < img.getHeight(); Count ++) {
      String strRow = "";
      for (int Count1 = 0; Count1 < img.getWidth(); Count1 ++) {
        Color thisColor = new Color (img.getRGB(Count1, Count));
        
        if (thisColor.getGreen() == 255) {
          strRow += "t_0_2";
        }else {
          strRow += "f_" + thisColor.getRed()/50 + "_" + thisColor.getBlue()/50;
        }
        
        if (Count1 != img.getWidth() - 1) {
          strRow += ",";
        }
      }
      pr.println (strRow);
    }
    
    try{
      pr.close();
      fr.close();
    }catch (IOException e) {}
  }
}