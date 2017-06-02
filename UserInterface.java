import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.*;
import javax.imageio.*;
import java.awt.image.*;

class UserInterface implements Runnable, ActionListener{
  
  //****************************************
  //Mr. Cadawas || ICS4U1 CPT
  //Mogen Cheng, Mark Abadir, Ross Baumgartner
  //2017-01-22
  //****************************************
  //
  //FILE: UserInterface
  //DESCRIPTION: A thread that renders in-game User Interface elements.
  //
  //             When run by ClientAnimation, this class will render a static variable,
  //             BufferedImage -> 'UIRender'. paintComponent in ClientAnimation will then
  //             draw this buffered image at the end of the method.
  //
  //REASON:      A seperate thread will increase gameplay performance as opposed to
  //             drawing and processing all the data within ClientAnimation's "paintComponent"
  //
  //****************************************
  
  
  //main bufferedImage
  static BufferedImage UIRender = new BufferedImage(1300, 720, BufferedImage.TYPE_INT_ARGB);
  Graphics2D g = UIRender.createGraphics();
  
//ATTACK DATA
  String atk1;
  String atk2;
  long longcooldownatk1 = 0;
  long longcooldownatk2 = 0;
  boolean attack1HasCooldown = false;
  boolean attack2HasCooldown = false;
  static boolean deathScreenVisible = false;
  
  int intCharacter = 0;
  int intPlayerNumber = ClientMain.intPlayerNumber;
  int characterSelected = 0;
  //--ATTACKS & COOLDOWNS--
  String strDataStats[][] = new String[4][5]; //variable for holding attack names, cooldowns, etc.
  String [] playerClass = {"NA", "NA", "NA", "NA"};
  
  //timers
  static Timer timerRefreshUI;
  static Timer dismissChatTimer;
  
  //Interface Buffered Images
  BufferedImage Crosshair = null;
  BufferedImage UI_Mouse = null;
  BufferedImage UI_PauseButtons = null;
  BufferedImage UI_Home = null;
  BufferedImage UI_Esc = null;
  BufferedImage UI_Help = null;
  BufferedImage UI_Hearts = null;
  BufferedImage UI_Respawn = null;
  int mouseButton = 0;
  int keyButton = 0;
  static String chatMessage = "";
  static String chatMessages[] = new String[15];
  
  //Fonts
  Font fontMedium = new Font("Arial", Font.ITALIC, 22);
  Font fontMenu = new Font("Arial", Font.PLAIN, 12);
  Font selectCharFont = new Font("Arial", Font.PLAIN, 60);
  Font mouseLabelFont = new Font("Arial", Font.PLAIN, 18);
  
  //Colors
  Color colorNoClickBg = new Color(30,30,30, 220);
  Color colorNoClickLabel = new Color(255, 255, 255, 210);
  Color colorClickBg = new Color(255,189,0, 220);
  Color colorClickLabel = new Color(255, 255, 255, 210);
  Color colorCooldownBg = new Color(244, 66, 66, 210);
  
  //UI Boolean Options
  static boolean bigMapVisible = false;
  static boolean pauseScreenVisible = false;
  static boolean chooseCharacterMenuVisible = true;
  static boolean helpScreenVisible = false;
  static boolean chatVisible = false;
  static boolean recievedMessage = false;
  
  //CONSTRUCTOR FOR THREAD
  public void run(){
    //Add Anti Aliasing
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); 
    
    timerRefreshUI = new Timer(1000/60, this);
    dismissChatTimer = new Timer(6500, this); //messages recieved and sent will temporarily show on screen for 6.5 sec.
    
    timerRefreshUI.start();
    dismissChatTimer.start();
    try{
      FileReader in = new FileReader("data_attacks.txt");
      BufferedReader bf = new BufferedReader(in);
      //load attack data
      for(int line=0; line<4;line++){
        String strLine[] = bf.readLine().split(";");
        for(int segment=0;segment<5;segment++){
          strDataStats[line][segment] = strLine[segment];
        }
      }
    }catch(Exception e){
    }
    
    try{
      
      //UI Elements
      Crosshair = ImageIO.read(new File("UI_crosshair.png"));
      UI_Mouse = ImageIO.read(new File("UI_mouse.png"));
      UI_PauseButtons = ImageIO.read(new File("UI_pausebtn.png"));
      UI_Home = ImageIO.read(new File("UI_home.png"));
      UI_Esc = ImageIO.read(new File("UI_esc.png"));
      UI_Help = ImageIO.read(new File("UI_help.png"));
      UI_Hearts = ImageIO.read(new File("UI_hearts.png"));
      UI_Respawn = ImageIO.read(new File("UI_respawn.png"));
    }catch(Exception e){
      System.out.println("Reading UI elements failed: " + e);
    }
    
    
    //fills in the chat array with empty strings
    for(int message=0;message<chatMessages.length;message++){
      switch(message){
        case 13:
          if(ClientMain.sm_thread != null && ClientMain.ssm != null){
            chatMessages[message] = "Your IP: " + ClientMain.ssm.getMyAddress() + " (LAN)";
          }
          break;
        case 14:
        if(ClientMain.sm_thread != null && ClientMain.ssm != null){
            chatMessages[message] = "Welcome to Treasure Quest!";
          }
          break;
        default:
          chatMessages[message] = "";
          break;
          
      }
    }
    
  }
  
  
  public void actionPerformed(ActionEvent e){
    if(e.getSource() == dismissChatTimer){
      recievedMessage = false;
      dismissChatTimer.stop();
    }
    if(e.getSource() == timerRefreshUI){
      g.setBackground(new Color(0, 0, 0, 0));
      g.clearRect(0, 0, 1300, 720);
      //load variables from ClientAnimation
      this.playerClass = ClientAnimation.playerClass;
      //======================================
      //                        DRAW USER INTERFACE INTO UIRender buffered image
      //======================================
      // --MINIMAP--
      if(bigMapVisible){
        Color opacity = new Color(0, 0, 0, 255);
        g.setColor(opacity);
        g.fillRect(0, 0, 1280, 720);
        g.drawImage(ClientAnimation.currentMap.MinimapImgFullscreen, 630-ClientAnimation.currentMap.intPlayerOnMapX, 330-ClientAnimation.currentMap.intPlayerOnMapY, null);
        g.drawImage(UI_Esc, 0, 0, null);
      }else{
        g.drawImage (ClientAnimation.currentMap.MinimapImgCropped, 40, 500, null);
      }
      
      //==================================
      //--CHATBOX--
      
      int intChatYPos = 350;
      int intChatXPos = 930;
      
      if( (recievedMessage || chatVisible) && !chooseCharacterMenuVisible && chatMessages != null){
        //draw border
        g.setColor(new Color(0, 0, 0, 60));
        g.fillRect(intChatXPos - 10, intChatYPos - 10, 300, (16*chatMessages.length)+10);
        
        //if a message has been sent to the player, display message without allowing keyboard input.
        g.setFont(fontMenu);
        g.setColor(new Color(255, 255, 255, 255));
        for(int message=0;message<chatMessages.length; message++){
          if(chatMessages != null && chatMessages[message] != null){
            g.drawString(chatMessages[message], intChatXPos, intChatYPos);
          }
          intChatYPos+= 16;
        }
      }
      
      //player has pressed [ENTER], will allow to type in messages
      if(chatVisible){
        Color chatBg = new Color(255, 255, 255, 255);
        g.setColor(chatBg);
        g.fillRect(intChatXPos-10, intChatYPos, 300, 30);
        g.setColor(new Color(0, 0, 0, 255));
        //g.drawRect(250, 668, 300, 30);
        //chatField.setVisible(true);
        g.setFont(fontMenu);
        if(chatMessage != ""){
          g.drawString(chatMessage, intChatXPos, intChatYPos+20);
        }else{
          g.drawString("Type your message here", intChatXPos, intChatYPos+20);
        }
      }
      
      //==================================
      //--Draw Mouse Image--
      
      int UI_Mouse_PosX = 620;
      int UI_Mouse_PosY = 600;
      if(!chooseCharacterMenuVisible && !pauseScreenVisible && !bigMapVisible){ //show only when these UI scerens are hidden
        
        //var for boxes
        int x1 = 460;
        int x2 = 690;
        int opacity1 = 130;
        int opacity2 = 50;
        
        //draw  Attack string
        double timeSinceAtk1_ms = (System.nanoTime()-longcooldownatk1)/1e6;
        double timeSinceAtk2_ms = (System.nanoTime()-longcooldownatk2)/1e6;

        //CHANGE ATTACK STRINGS TO COOLDOWN IF difference between last attack is greater than that declared in data_attacks.txt
        if(timeSinceAtk1_ms > Double.parseDouble(strDataStats[characterSelected][2])){
          atk1 = strDataStats[characterSelected][1];
          attack1HasCooldown = false;
        }else{
          atk1 = "Cooldown";
          attack1HasCooldown = true;
        }
        if(timeSinceAtk2_ms > Double.parseDouble(strDataStats[characterSelected][4])){
          atk2 = strDataStats[characterSelected][3];
          attack2HasCooldown = false;
        }else{
          atk2 = "Cooldown";
          attack2HasCooldown = true;
        }

        //draw strings
        g.setFont(mouseLabelFont);
        
        switch(mouseButton){
          //CASE 1: LEFT CLICK
          case 1: g.drawImage(UI_Mouse, UI_Mouse_PosX,UI_Mouse_PosY, UI_Mouse_PosX + 60, UI_Mouse_PosY + 100, 60, 0, 120, 100, null);
          if(!attack1HasCooldown){
            g.setColor(colorClickBg);
            g.fillRect(x1, 630, 150, 30);
          }else{
            g.setColor(colorCooldownBg);
            g.fillRect(x1, 630, 150, 30);
          }
          if(!attack2HasCooldown){
            g.setColor(colorNoClickBg);
            g.fillRect(x2, 630, 150, 30);
          }else{
            g.setColor(colorCooldownBg);
            g.fillRect(x2, 630, 150, 30);
          }
          //strings
          g.setColor(colorClickLabel);
          g.drawString(atk1, x1+15, 650);
          g.setColor(colorNoClickLabel);
          g.drawString(atk2, x2+15, 650);
          break;
          //CASE 3: RIGHT CLICK
          case 3: g.drawImage(UI_Mouse, UI_Mouse_PosX,UI_Mouse_PosY, UI_Mouse_PosX + 60, UI_Mouse_PosY + 100, 120, 0, 180, 100, null);
          if(!attack1HasCooldown){
            g.setColor(colorNoClickBg);
            g.fillRect(x1, 630, 150, 30);
          }else{
            g.setColor(colorCooldownBg);
            g.fillRect(x1, 630, 150, 30);
          }
          if(!attack2HasCooldown){
            g.setColor(colorClickBg);
            g.fillRect(x2, 630, 150, 30);
          }else{
            g.setColor(colorCooldownBg);
            g.fillRect(x2, 630, 150, 30);
          }
          //strings
          g.setColor(colorNoClickLabel);
          g.drawString(atk1, x1+15, 650);
          g.setColor(colorClickLabel);
          g.drawString(atk2, x2+15, 650);
          break;
          //NO CLICK
          default: g.drawImage(UI_Mouse, UI_Mouse_PosX,UI_Mouse_PosY, UI_Mouse_PosX + 60, UI_Mouse_PosY + 100, 0, 0, 60, 100, null);
          //make label background red if cooldown is happening
          if(attack1HasCooldown){
            g.setColor(colorCooldownBg);
            g.fillRect(x1, 630, 150, 30);
          }else{
            g.setColor(colorNoClickBg);
          g.fillRect(x1, 630, 150, 30);
          }
          if(attack2HasCooldown){
            g.setColor(colorCooldownBg);
            g.fillRect(x2, 630, 150, 30);
          }else{
            g.setColor(colorNoClickBg);
          g.fillRect(x2, 630, 150, 30);
          }

          //strings
          g.setColor(colorNoClickLabel);
          g.drawString(atk1, x1+15, 650);
          g.drawString(atk2, x2+15, 650);
          break;
        }
      }
      
      //================================
      //DRAW USER HEALTH
      for (int Count = 0; Count < (player.intHealth - player.intHealth % 10)/10; Count ++) {
        g.drawImage (UI_Hearts, 60 + 30 * Count, 60, 80 + 30 * Count, 80, 0, 0, 20, 20, null);
      }
      for (int Count = (player.intHealth - player.intHealth % 10)/10; Count < player.maxHealth/10; Count ++) {
        g.drawImage (UI_Hearts, 60 + 30 * Count, 60, 80 + 30 * Count, 80, 20, 0, 40, 20, null);
      }

      
      //==================================
      //--SHOW CHARACETR SELECTION--
      if(chooseCharacterMenuVisible){
        
        Color opacity = new Color(0, 0, 0, 200);
        g.setColor(opacity);
        g.fillRect(0, 0, 1300, 800);
        
        //draw title
        g.setFont(selectCharFont);
        g.setColor(Color.WHITE);
        g.drawString("SELECT CHARACTER", 325, 160);
        
        int characters = 4;
        int spacing = 20;
        int placedirX = (1280/2) - (((215 * characters) + (spacing * characters-1))/2 ); //center character cards
        int placeY = 220;
        
        for(int character=1; character <= characters; character++){ 
          
          //====CHECK IF MOUSE IS HOVERING OVER CARD
          if(ClientMain.ScreenMouseX > placedirX && ClientMain.ScreenMouseX < placedirX+215 && ClientMain.ScreenMouseY > placeY && ClientMain.ScreenMouseY < placeY+269){
            if(mouseButton == 1){
              characterSelected = character;
              switch(characterSelected){
                case 1:
                  ClientMain.strType = "mage";
                  break;
                 case 2:
                  ClientMain.strType = "buff";
                  break;
                 case 3:
                  ClientMain.strType = "thief";
                  break;
                 case 4:
                  ClientMain.strType = "doctor";
                  break;
              }
            }
            if(character == characterSelected){
              //show checkmark
              g.drawImage(UI_Home, placedirX, placeY, placedirX+215, placeY+269, 766, 60, 981, 329, null);
            }else{
              g.drawImage(UI_Home, placedirX, placeY, placedirX+215, placeY+269, 551, 60, 766, 329, null);
            }
          }else{
            if(character == characterSelected){
              //if the character is selected, show the draw the selected image overlay
              g.drawImage(UI_Home, placedirX, placeY, placedirX+215, placeY+269, 766, 60, 981, 329, null);
            }else{
              //if character is not HIGHLIGHTED (mouse over) OR SELECTED, default image.
              g.drawImage(UI_Home, placedirX, placeY, placedirX+215, placeY+269, 336, 60, 551, 329, null);
            }
          }
          String strCharacterName = "";
          switch(character){
            case 1: //mage
              strCharacterName = "Mage";
              player.strType = "Mage";
              g.drawImage(ClientAnimation.MageImg, placedirX+62, placeY+40, placedirX+90+62, placeY+90+45, 0, 0, 90, 90, null);
              break;
            case 2:
              strCharacterName = " Buff";
              player.strType = "Buff";
              g.drawImage(ClientAnimation.BuffImg, placedirX+62, placeY+40, placedirX+90+62, placeY+90+45, 0, 0, 90, 90, null);
              break;
            case 3:
              strCharacterName = "Thief";
              player.strType = "Thief";
              g.drawImage(ClientAnimation.ThiefImg, placedirX+62, placeY+40, placedirX+90+62, placeY+90+45, 0, 0, 90, 90, null);
              break;
            case 4:
              strCharacterName = " Doc";
              player.strType = "Doc";
              g.drawImage(ClientAnimation.DoctorImg, placedirX+62, placeY+40, placedirX+90+62, placeY+90+45, 0, 0, 90, 90, null);
              break;
            default:
              break;
          }
          g.setColor(Color.WHITE);
          g.setFont(fontMedium);
          g.drawString(strCharacterName, placedirX + 80, placeY+250);
          placedirX += 215+spacing;
        }
        
        //draw "CONTINUE" button
        int continueBtnX = 556;
        int continueBtnY = 550;
        if(ClientMain.ScreenMouseX > continueBtnX && ClientMain.ScreenMouseX < continueBtnX+168 && ClientMain.ScreenMouseY > continueBtnY && ClientMain.ScreenMouseY < continueBtnY+60){
          //player is hovering over
          g.drawImage(UI_Home, continueBtnX, continueBtnY, continueBtnX + 168, continueBtnY + 60, 168, 120, 336, 180, null);
          if(mouseButton == 1 && characterSelected >0){
            //user has clicked the button
            String playerType = "";
            switch(characterSelected){
              case 1: playerType = "mage"; break;
              case 2: playerType = "buff"; break;
              case 3: playerType = "thief"; break;
              case 4: playerType = "doctor"; break;
              default: playerType = "mage"; break;
            }
            String query = "player," + ClientMain.intPlayerNumber + ",class," + playerType;
            System.out.println(query);
            ClientMain.ssm.sendText (query);
            chooseCharacterMenuVisible = false;
            //change player textures locally
            playerClass[ClientMain.intPlayerNumber] = playerType;
            characterSelected -= 1; //for array (card #1 = array index 0, card #2 = array index 1, etc)
            recievedMessage = true;
            dismissChatTimer.stop();
            dismissChatTimer.start();
          }
        }else{
          g.drawImage(UI_Home, continueBtnX, continueBtnY, continueBtnX + 168, continueBtnY + 60, 0, 120, 168, 180, null);
        }
        g.setFont(fontMenu);
        g.drawString("SELECT", continueBtnX+60, continueBtnY+30);
      }
      //=============================
      //--Draw Help Screen--
      if(helpScreenVisible){
        Color opacity = new Color(255, 255, 255, 255);
        g.setColor(opacity);
        g.fillRect(0, 0, 1300, 800);
        g.drawImage(UI_Help, 0, 0, null);
        g.drawImage(UI_Esc, 0, 0, null);
      }
      
      //===================================
      //--DRAW death screen--
      if(deathScreenVisible){
        ClientMain.thisPlayer.blnUp = false;
        ClientMain.thisPlayer.blnDown = false;
        ClientMain.thisPlayer.blnLeft = false;
        ClientMain.thisPlayer.blnRight = false;
        g.drawImage(UI_Respawn, 0, 0, null);
        int delay = 5000;
        Double countdown = (((player.deathTime+(delay*1e6)) - System.nanoTime())/1e6)/1000;
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.ITALIC, 22));
        g.drawString("Respawn in " + countdown, 25, 680);
        
        if((System.nanoTime()-player.deathTime)/1e6 > delay){
          player.intHealth = player.maxHealth/2;
          player.X = player.SpawnX;
          player.Y = player.SpawnY;
          deathScreenVisible = false;
          ClientMain.ssm.sendText("player," + ClientMain.intPlayerNumber + ",iamaliveayyy");
        }
      }

      //=================================
      //--Draw Pause Menu
      if(pauseScreenVisible){
        Color opacity = new Color(0, 0, 0, 100);
        g.setColor(opacity);
        g.fillRect(0, 0, 1300, 800);
        
        int btnStartX = 490;
        int btnStartY = 300;
        int btnMargin = (30)+15;
        
        int intButtons = 4;
        for(int btnRow=1; btnRow <= intButtons; btnRow++){
          //Check if mouse is hovering over the button
          if(ClientMain.ScreenMouseX > btnStartX && ClientMain.ScreenMouseX < btnStartX+300 && ClientMain.ScreenMouseY > btnStartY && ClientMain.ScreenMouseY < btnStartY+30){
            g.drawImage(UI_PauseButtons, btnStartX, btnStartY, btnStartX+300, btnStartY+30, 0, 30, 300, 60, null);
            if(mouseButton == 1){
              switch(btnRow){
                case 1: pauseScreenVisible = false; break; //=== RESUME
                case 2: pauseScreenVisible = false; bigMapVisible = true; break; //=== MAP
                case 3: pauseScreenVisible = false; helpScreenVisible = true; break;
                case 4: ClientMain.gameHasStarted = false; break; // === QUIT
              }
            }
          }else{
            g.drawImage(UI_PauseButtons, btnStartX, btnStartY, btnStartX+300, btnStartY+30, 0, 0, 300, 30, null);
          }
          //draw the text for the button
          g.setFont(fontMenu);
          g.setColor(Color.WHITE);
          switch(btnRow){
            case 1: g.drawString("RESUME", btnStartX+10, btnStartY+20); break;
            case 2: g.drawString("MAP", btnStartX+10, btnStartY+20); break;
            case 3: g.drawString("HELP", btnStartX+10, btnStartY+20); break;
            case 4: g.drawString("DISCONNECT", btnStartX+10, btnStartY+20); break;
            default: g.drawString("????", btnStartX+10, btnStartY+20); break;
          }
          btnStartY += btnMargin;
        }
        g.drawImage(UI_Esc, 0, 0, null);
      }
      
      //==================================
      //--Draw Crosshair--
      g.drawImage(Crosshair, ClientMain.ScreenMouseX-15, ClientMain.ScreenMouseY-15, null);

    }
  }
  
  
  UserInterface(){
    //THREAD
    //NO CONSTRUCTOR BECAUSE RUN() METHOD ACTS AS THE CONSTRUCTOR
  }
  
  public static void main (String [] args) {
    System.out.println (System.nanoTime()/1e6);
  }
}