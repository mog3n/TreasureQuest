import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.*;
import javax.imageio.*;
import java.awt.image.*;

public class Map {
  //properties
  //======================================================================================================================================================
  boolean RenderedMap = false;
  boolean BoundMap = false;
  
  String [] mapData;
  String [][] mapArray;
  String [][] interactions;
  String strMapFile;
  BufferedImage MapImg;
  BufferedImage MapBoundariesImg;
  BufferedImage MinimapImg;
  BufferedImage MinimapImgFullscreen;
  BufferedImage MinimapImgCropped;
  BufferedImage MMmage;
  BufferedImage MMbuff;
  BufferedImage MMthief;
  BufferedImage MMdoctor;
  BufferedImage tileImg;
  int miniMapScale = 15;
  
  int intPlayerOnMapX;
  int intPlayerOnMapY;
  
  //methods
  //======================================================================================================================================================
  public void readData () throws IOException {
    FileReader file = new FileReader (strMapFile);
    BufferedReader reader = new BufferedReader (file);
    
    mapData = reader.readLine().split(",");
    mapArray = new String[Integer.parseInt(mapData[1])][Integer.parseInt(mapData[0])];
    tileImg = ImageIO.read(new File(mapData[2] + ".png"));
    
    reader.close();
    file.close();
  }
  
  public void renderMap() throws IOException {
    RenderedMap = true;
    FileReader file = new FileReader(strMapFile);
    BufferedReader reader = new BufferedReader(file);
    reader.readLine();
    
    //CREATE BLANK BUFFEREDIMAGE, based on the mapSize variable
    BufferedImage canvas = new BufferedImage(Integer.parseInt(mapData[1]) * 60, Integer.parseInt(mapData[0]) * 60, BufferedImage.TYPE_INT_ARGB);
    Graphics2D pane = canvas.createGraphics();
    
    for(int row = 0; row < Integer.parseInt(mapData[0]); row ++){
      String rowData [] = null;
      
      rowData = reader.readLine().split(",");
      
      for (int col = 0; col < Integer.parseInt(mapData[1]); col++) {
        mapArray[row][col] = rowData[col];
        String tile [] = rowData [col].split("_");
        int tileX = col * 60;
        int tileY = row * 60;
        int tileImgX = Integer.parseInt(tile [1]) * 60;
        int tileImgY = Integer.parseInt(tile [2]) * 60;
        
        
        pane.drawImage (tileImg, tileX, tileY, tileX + 60, tileY + 60, tileImgX, tileImgY, tileImgX + 60, tileImgY + 60, null);
        if (tile [0].equals("m")) {
          pane.setColor (new Color (0, 0, 255, 100));
          pane.fillRect(tileX, tileY, tileX + 60, tileY + 60);
        }
      }
    }
    
    reader.close();
    file.close();
    MapImg = canvas;
  }
  
  //------------------------------------------------------------------------------------------------------------------------------------------------------
  public void renderBoundMap() throws IOException {
    BoundMap = true;
    FileReader file = new FileReader(strMapFile);
    BufferedReader reader = new BufferedReader(file);
    reader.readLine();
    
    interactions = new String[Integer.parseInt(mapData[1])][Integer.parseInt(mapData[0])];
    
    //CREATE BLANK BUFFEREDIMAGE, based on the mapSize variable
    BufferedImage boundariesCanvas = new BufferedImage(Integer.parseInt(mapData[1]) * 60, Integer.parseInt(mapData[0]) * 60, BufferedImage.TYPE_INT_ARGB);
    Graphics2D boundariesPane = boundariesCanvas.createGraphics();
    
    for(int row = 0; row < Integer.parseInt(mapData[0]); row ++){
      String rowData [] = null;
      
      rowData = reader.readLine().split(",");
      
      for (int col = 0; col < Integer.parseInt(mapData[1]); col++) {
        mapArray[row][col] = rowData[col];
        String tile [] = rowData [col].split("_");
        int tileX = col * 60;
        int tileY = row * 60;
        
        if (tile [0].equals ("t") || tile [0].equals("a")) {
          boundariesPane.setColor (Color.RED);
          boundariesPane.fillRect (tileX, tileY, 60, 60);
          
          if (tile [0].equals ("t")) {
            interactions [row][col] = "x_0";
          }else if (tile [0].equals ("a")) {
            interactions [row][col] = "a_0";
          }
        }else if (tile [0].equals("f") || tile [0].equals("g")) {
          boundariesPane.setColor (Color.BLACK);
          boundariesPane.fillRect (tileX, tileY, 60, 60);
          
          if (tile [0].equals ("f")) {
            interactions [row][col] = "x_0";
          }else if (tile [0].equals ("g")) {
            interactions [row][col] = "g_0";
          }
        }else if (tile [0].equals ("m")) {
          boundariesPane.setColor (new Color (255, 100, 0));
          boundariesPane.fillRect (tileX, tileY, 60, 60);
          
          interactions [row][col] = "m_0";
        }
      }
    }
    
    reader.close();
    file.close();
    MapBoundariesImg = boundariesCanvas;
  }
  
  //------------------------------------------------------------------------------------------------------------------------------------------------------
  public void updateInteractions (int row, int col, int intState) {
    String [] Tile = interactions [row][col].split("_");
    Tile [1] = intState + "";
    
    interactions [row][col] = Tile[0] + "_" + Tile[1];
    
    if (Tile [0].equals ("g")) {
      if (BoundMap) {
        Graphics2D bound = MapBoundariesImg.createGraphics();
        if (Tile [1].equals ("1")) {
          bound.setColor (Color.RED);
        }else if (Tile [1].equals("0")) {
          bound.setColor (Color.BLACK);
        }
        bound.fillRect (col * 60, row * 60, 60, 60);
      }
      
      if (RenderedMap) {
        Graphics2D renderedmap = MapImg.createGraphics();
        if (Tile [1].equals ("1")) {
          renderedmap.drawImage (tileImg, col * 60, row * 60, col * 60 + 60, row * 60 + 60, 180, 120, 240, 180, null);
        }else if (Tile [1].equals ("0")) {
          renderedmap.drawImage (tileImg, col * 60, row * 60, col * 60 + 60, row * 60 + 60, 180, 180, 240, 240, null);
        }
      }
    }else if (Tile [0].equals ("a")) {
      if (BoundMap) {
        Graphics2D bound = MapBoundariesImg.createGraphics();
        if (Tile [1].equals ("1")) {
          bound.setColor (new Color (255, 255, 0));
        }else if (Tile [1].equals("0")) {
          bound.setColor (Color.RED);
        }
        bound.fillRect (col * 60, row * 60, 60, 60);
      }
      
      if (RenderedMap) {
        Graphics2D renderedmap = MapImg.createGraphics();
        if (Tile [1].equals ("1")) {
          renderedmap.drawImage (tileImg, col * 60, row * 60, col * 60 + 60, row * 60 + 60, 0, 180, 60, 240, null);
        }else if (Tile [1].equals ("0")) {
          renderedmap.drawImage (tileImg, col * 60, row * 60, col * 60 + 60, row * 60 + 60, 60, 180, 120, 240, null);
        }
      }
    }
  }
  
  //------------------------------------------------------------------------------------------------------------------------------------------------------
  public void renderMinimap() throws IOException {
    int tile_size_px = miniMapScale;
    BufferedImage minimap = new BufferedImage(mapArray.length * tile_size_px,mapArray[0].length * tile_size_px, BufferedImage.TYPE_INT_ARGB);
    Graphics2D pane = minimap.createGraphics();
    
    for(int row = 0; row < mapArray.length; row ++){
      
      for (int col = 0; col < mapArray[0].length; col++) {
        int tileX = col * tile_size_px;
        int tileY = row * tile_size_px;
        String tile[] = mapArray[row][col].split("_");

        if (tile [0].equals ("t")) {
          pane.setColor (new Color (255, 255, 255, 175));
          pane.fillRect (tileX, tileY, tile_size_px, tile_size_px);
        }
      }
    }
    MinimapImg = minimap;
    //File outputfile = new File("minimap_render.png");
    //ImageIO.write(minimap, "png", outputfile);
  }
  
  //------------------------------------------------------------------------------------------------------------------------------------------------------
  public void updateMinimap(int intPlayerData [][], String playerClass [], int intPlayerNumber){
    BufferedImage temp = new BufferedImage(mapArray.length * miniMapScale,mapArray[0].length * miniMapScale, BufferedImage.TYPE_INT_ARGB);
    Graphics2D temp_gfx = temp.createGraphics();
    temp_gfx.drawImage(MinimapImg, 0, 0, null);

    //PLOT PLAYER LOCATIONS
    for(int player = 0; player < 4; player++){
      int x_pos = Math.round(intPlayerData[player][0] / (60/miniMapScale)) - Math.round(miniMapScale/2);
      int y_pos = Math.round(intPlayerData[player][1] / (60/miniMapScale)) - Math.round(miniMapScale/2);
      
      if (playerClass [player].equals ("mage")) {
        temp_gfx.drawImage(MMmage, x_pos, y_pos, null);
      }else if (playerClass [player].equals ("buff")) {
        temp_gfx.drawImage(MMbuff, x_pos, y_pos, null);
      }else if (playerClass [player].equals ("thief")) {
        temp_gfx.drawImage(MMthief, x_pos, y_pos, null);
      }else if (playerClass [player].equals ("doctor")) {
        temp_gfx.drawImage(MMdoctor, x_pos, y_pos, null);
      }
    }

    //PLOT ENEMY LOCATIONS ON MINIMAP
    for(int enemy=0; enemy<ClientMain.longEnemyData.length; enemy++){
      int x_pos = Math.round(ClientMain.longEnemyData[enemy][0] / (60/miniMapScale)) - Math.round(miniMapScale/2);
      int y_pos = Math.round(ClientMain.longEnemyData[enemy][1] / (60/miniMapScale)) - Math.round(miniMapScale/2);

      temp_gfx.setColor(Color.RED);
      temp_gfx.fillRect(x_pos, y_pos, miniMapScale, miniMapScale);
    }

    MinimapImgFullscreen = temp;
    //CROP
    int minimap_x = 200;
    int minimap_y = 200;
    BufferedImage minimap = new BufferedImage(minimap_x, minimap_y, BufferedImage.TYPE_INT_ARGB);
    Graphics2D gfx = minimap.createGraphics();
    intPlayerOnMapX = Math.round(intPlayerData[intPlayerNumber][0] / (60/miniMapScale)) - Math.round(miniMapScale/2); //player coordinates relative to minimap
    intPlayerOnMapY = Math.round(intPlayerData[intPlayerNumber][1] / (60/miniMapScale)) + Math.round(miniMapScale/2);
    //System.out.println("Minimap: px: " + intPlayerOnMapX + " py: " + intPlayerOnMapY);
    gfx.setColor(new Color(50, 50, 50, 110));
    gfx.fillRect(0,0,minimap_x,minimap_y);
    gfx.drawImage(temp, 0, 0, minimap_x, minimap_y, (intPlayerOnMapX - Math.round(minimap_x/2) + (Math.round(miniMapScale/2) )) - Math.round(miniMapScale/2),
                                                    (intPlayerOnMapY - Math.round(minimap_y/2) ) - Math.round(miniMapScale/2),
                                                    (intPlayerOnMapX + Math.round(minimap_x/2) ), (intPlayerOnMapY + Math.round(minimap_y/2) ), null);
    MinimapImgCropped = minimap;
  }
  
  //constructor
  //======================================================================================================================================================
  public Map(String strMapFile) {
    this.strMapFile = strMapFile;
    
    try {
      MMmage = ImageIO.read(new File("minimap_mage.png"));
      MMbuff = ImageIO.read(new File("buff minimap.png"));
      MMthief = ImageIO.read(new File("minimap_thief.png"));
      MMdoctor = ImageIO.read(new File("minimap_doctor.png"));
      
      readData();
    }catch (IOException e) {
    }
  }
}