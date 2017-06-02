import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.*;
import javax.imageio.*;
import java.awt.image.*;

public class ClientMain implements ActionListener, KeyListener, MouseListener, MouseMotionListener {
  
  //****************************************
  //Mr. Cadawas || ICS4U1 CPT
  //Mogen Cheng, Mark Abadir, Ross Baumgartner
  //2017-01-22
  //****************************************
  //
  //FILE: ClientMain
  //DESCRIPTION: The main class run by the player.
  //
  //             This class seamlessly switches between multiple JPanels, handles
  //             all Client-side keyboard and mouse inputs, and connects/disconnects,
  //             sends and recieves data from SuperSocketMaster based on user interaction.
  //
  //REASON:      Essential class for recieving, rendering, displaying, sending and manipulating data
  //             for the Client.
  //
  //****************************************
  
  static String initialMapName = "map1";
  static String currentMap = initialMapName; //used for host
  static String ip = "localhost";
  static int port = 0;
  static boolean gameHasStarted = false;
  
  //properties
  static SuperSocketMaster ssm;
  static boolean isHost = false;
  static player thisPlayer;
  
  JFrame frame;
  ClientAnimation gamePanel;
  ClientHome homePanel;
  static ServerMain sm = new ServerMain(initialMapName);
  static Thread sm_thread = null;
  
  Timer timer;
  Timer mainMenuTimer;
  static int intPlayerNumber = -1;
  static int [][] intPlayerData = new int [4][4];
  static long [][] longPlayerCooldown = new long[4][2];
  static long [][] longEnemyData;
  static int ScreenMouseX;
  static int ScreenMouseY;
  static String strType = ""; //player type
  
  //messaging
  
  //blank cursor variables
  BufferedImage cursorImg;
  Cursor crosshair;
  Cursor default_crosshair;
  boolean blnActivate = false;
  String strInteract = "NA";
  
  //methods
  public void actionPerformed (ActionEvent evt) throws NullPointerException {
    if (evt.getSource() == ssm) {
      //----------------------------------------------------------------------------------------------------------------
      String [] strData = ssm.readText().split (",");
      int intPlayer;
      
      if (strData [0].equals("server")) {
        if (strData [1].equals("assign")) {
          if (intPlayerNumber == -1) {
            intPlayerNumber = Integer.parseInt(strData [2]);
            gamePanel.intPlayerNumber = intPlayerNumber;
            timer.start();
            System.out.println("Assigned as player: " + intPlayerNumber);
            ssm.sendText("player," + intPlayerNumber +",iamaliveayyy");
            String strType = thisPlayer.strType;
            gamePanel.playerClass [intPlayerNumber] = strType;
          }else {
            ssm.sendText ("player," + intPlayerNumber + ",class," + thisPlayer.strType);
          }
        }else if (strData [1].equals("enemy") && intPlayerNumber != -1) { //get enemy data
          longEnemyData [Integer.parseInt(strData[2])][0] = Long.parseLong(strData [3]);
          longEnemyData [Integer.parseInt(strData[2])][1] = Long.parseLong(strData [4]);
          longEnemyData [Integer.parseInt(strData[2])][2] = Long.parseLong(strData [5]);
          
          switch(strData[6]){
            case "shark":
              longEnemyData[Integer.parseInt(strData[2])][4] = 0;
              break;
            case "ghost":
              longEnemyData[Integer.parseInt(strData[2])][4] = 1;
              break;
            default:
              longEnemyData[Integer.parseInt(strData[2])][4] = 0;
              break;
          }
          
        }else if (strData [1].equals("enemyCount")) { //create a new array to start recieving enemy data
          try {
            longEnemyData = new long [Integer.parseInt(strData[2])][5]; //x, y, degrees, time since last hit, enemyType
          }catch (Exception e) {
          }
        }else if (strData [1].equals("updateTiles")) { //update a tile in-game (when enemies are dead, doors will open/close)
          thisPlayer.currentMap.updateInteractions(Integer.parseInt(strData [2]), Integer.parseInt(strData [3]), Integer.parseInt(strData [4]));
          gamePanel.currentMap.updateInteractions (Integer.parseInt(strData [2]), Integer.parseInt(strData [3]), Integer.parseInt(strData [4]));
        }else if (strData [1].equals("acknowledgeactivate") && Integer.parseInt(strData [2]) == intPlayerNumber) {
          blnActivate = true;
        }else if (strData [1].equals("acknowledgedeactivate") && Integer.parseInt(strData [2]) == intPlayerNumber) {
          blnActivate = false;
          strInteract = "NA";
        }else if(strData[1].equals("enemyDamaged")){ //tells Client to do the flash animation
          longEnemyData[Integer.parseInt(strData[2])][3] = System.nanoTime();
          
        }else if(strData[1].equals("changeMap")){ //format: server,changeMap,[newMapName],[intSpawnPointX],[intSpawnPointY]
          //MAP CHANGE
          System.out.println("changing map server--");
          String newMapName = strData[2];
          int intSpawnPointX = Integer.parseInt(strData[3]);
          int intSpawnPointY = Integer.parseInt(strData[4]);
          System.out.println("Changing map to: " + newMapName);
          gamePanel.timer.stop();
          timer.stop();
          try{
              Thread.sleep(100);
            }catch(Exception e){}
          this.ssm.disconnect();
          if(isHost){
            //reinitialize server IF hosting
            sm.timer.stop();
            try{
              Thread.sleep(100);
            }catch(Exception e){}
            sm.ssm.disconnect();
            sm = new ServerMain(newMapName);
            sm_thread = new Thread(sm);
            sm_thread.start();
          }
          thisPlayer = new player (strType, intSpawnPointX, intSpawnPointY, newMapName);
          intPlayerNumber = -1;
          try{
            gamePanel = new ClientAnimation(newMapName);
            gamePanel.UI_Thread.start();
            gamePanel.UI.timerRefreshUI.start();
          }catch(Exception e){
            System.out.println("COULD NOT CREATE NEW GAMEPANEL "+e);
          }
          gamePanel.setPreferredSize(new Dimension(1280, 720));
          frame.setContentPane(gamePanel);
          frame.pack();
          //connect to the new server with the new map
          ssm = new SuperSocketMaster(ip, port, this);
          ssm.connect();
          ssm.sendText("joined");
          System.out.println("Connecting to: " + ip);
          //set focus to the game
          frame.requestFocus();
          //hide cursor
          frame.getContentPane().setCursor(crosshair);
          timer.start();
        }else if (strData [1].equals("enemyAttack")) {
          if (Integer.parseInt(strData [3]) == intPlayerNumber) {
            thisPlayer.intHealth -= Integer.parseInt(strData [4]);
          }
        }
        //ATTACH OTHER TYPES OF SERVER MESSAGES HERE
        
      }else if (strData [0].equals("player")) {
        intPlayer = Integer.parseInt (strData [1]);
        if (strData [2].equals("stats")) {
          intPlayerData [intPlayer][0] = Integer.parseInt (strData [3]);  //0 = X
          intPlayerData [intPlayer][1] = Integer.parseInt (strData [4]);  //1 = Y
          intPlayerData [intPlayer][2] = Integer.parseInt (strData [5]);  //2 = Angle
          intPlayerData [intPlayer][3] = Integer.parseInt (strData [6]);  //3 = Move state
          longPlayerCooldown[intPlayer][0] = Long.parseLong (strData[7]); // cooldown1
          longPlayerCooldown[intPlayer][1] = Long.parseLong (strData[8]); // cooldown 2
        }else if (strData [2].equals ("class") && gamePanel.playerClass[Integer.parseInt(strData[1])] != strData[3]) {
          gamePanel.playerClass [intPlayer] = strData [3];
          ssm.sendText ("player," + intPlayerNumber + ",class," + gamePanel.playerClass[intPlayerNumber]);
        }else if (strData [2].equals ("message")) {
          //CREATE AN EMPTY SPACE FOR MESSAGE
          //shift all messages up 1
          for(int i=0; i<UserInterface.chatMessages.length-1;i++){
            UserInterface.chatMessages[i] = UserInterface.chatMessages[i+1];
          }
          //recieve the chat message
          UserInterface.chatMessages[UserInterface.chatMessages.length-1] = "Player " + (Integer.parseInt(strData[1])+1) + ": " + strData[3];
          //notify ANIMATIon that a message has been recieved
          UserInterface.recievedMessage = true;
          UserInterface.dismissChatTimer.stop();
          UserInterface.dismissChatTimer.start();
        }else if( strData[2].equals("sendatk")){
          if(strData[4].equals("atk1")){
            longPlayerCooldown[Integer.parseInt(strData[1])][0] = System.nanoTime(); //attack 1
          }else{
            longPlayerCooldown[Integer.parseInt(strData[1])][1] = System.nanoTime(); //attack 2
          }
        }
      }
      
    }else if (evt.getSource() == timer) {
      //-----------------------------------------------------------------------------------------------------------------
      thisPlayer.MouseX = ScreenMouseX - gamePanel.imgX;
      thisPlayer.MouseY = ScreenMouseY - gamePanel.imgY;
      if(!UserInterface.chatVisible){ //allow the player to move if the chat is not visible
        int InteractionValue = thisPlayer.move();
        //System.out.println(InteractionValue);
        if (InteractionValue != 0 && blnActivate == false) {
          if (InteractionValue == 255) {
            strInteract = "enemies";
          }else if (InteractionValue == 100) {
            strInteract = "nextmap";
          }
          ssm.sendText("player," + intPlayerNumber + ",activate," + strInteract);
        }else if (InteractionValue == 0 && blnActivate == true) {
          ssm.sendText("player," + intPlayerNumber + ",deactivate," + strInteract);
        }
      }
      if(!gamePanel.UI.deathScreenVisible){
      ssm.sendText ("player," + intPlayerNumber + ",stats," + thisPlayer.X + "," + thisPlayer.Y + "," + thisPlayer.degreesAngle + "," + thisPlayer.intMoveState + "," + longPlayerCooldown[intPlayerNumber][0] + "," + longPlayerCooldown[intPlayerNumber][1]);
      }
      intPlayerData [intPlayerNumber][0] = thisPlayer.X;
      intPlayerData [intPlayerNumber][1] = thisPlayer.Y;
      intPlayerData [intPlayerNumber][2] = thisPlayer.degreesAngle;
      intPlayerData [intPlayerNumber][3] = thisPlayer.intMoveState;
      gamePanel.repaint();
      
      if(!gameHasStarted){
        //player has pressed !QUIT!
        //reset ALL player interactions and panels.
        ssm.disconnect();
        System.exit(0);
        
        //shutdown server if exists
        if(sm_thread != null){
          sm.ssm.disconnect();
          sm_thread.stop();
        }
        System.exit(0);
        
        //OLD CODE
        /*
         gamePanel.UI_Thread.stop(); //stop UI thread
         gamePanel.UI = null;
         gamePanel.UI_Thread = null;
         ssm.disconnect();
         intPlayerNumber = -1;
         homePanel = null; //ClientHome
         gamePanel = null; //ClientAnimtion
         thisPlayer = null;
         ssm = null;
         server = null;
         longEnemyData = null;
         intPlayerData = null;
         intPlayerData = new int [4][4];
         
         homePanel = new ClientHome();
         homePanel.setPreferredSize(new Dimension(1280, 720));
         homePanel.setLayout(null);
         
         frame.setContentPane(homePanel);
         frame.pack();
         timer.stop();
         mainMenuTimer.start();
         
         //set cursor to default
         frame.getContentPane().setCursor(default_crosshair);
         */
      }
    }else if(evt.getSource() == mainMenuTimer){
      //System.out.print("tick");
      if(gameHasStarted){
        //change content panel to game panel.
        //PLAYER HAS PRESSED !CONNECT!
        
        homePanel.timer.stop();
        homePanel = null; 
        
        try {
          gamePanel = new ClientAnimation(initialMapName);
        }catch (IOException e) {}
        gamePanel.setPreferredSize(new Dimension(1280, 720));
        gamePanel.setLayout(null);
        
        frame.setContentPane(gamePanel);
        frame.pack();
        frame.setVisible(true);
        timer = new Timer(1000/60, this);
        thisPlayer = new player ("mage", 500, 500, initialMapName);
        ssm = new SuperSocketMaster(ip, port, this);
        ssm.connect();
        ssm.sendText ("joined");
        System.out.println("Connecting to: " + ip);
        mainMenuTimer.stop();
        
        //set focus to the game
        frame.requestFocus();
        
        //hide cursor
        frame.getContentPane().setCursor(crosshair);
      }
    }
  }
  public void keyPressed (KeyEvent k){
    //if the player is still in the main menu, these commands will not be sent.
    if(gameHasStarted && thisPlayer != null){
      if (k.getKeyCode() == 38 || k.getKeyChar() == 'w') thisPlayer.blnUp = true;
      if (k.getKeyCode() == 40 || k.getKeyChar() == 's') thisPlayer.blnDown = true;
      if (k.getKeyCode() == 37 || k.getKeyChar() == 'a') thisPlayer.blnLeft = true;
      if (k.getKeyCode() == 39 || k.getKeyChar() == 'd') thisPlayer.blnRight = true;
      
      //Minimap hotkey, only works if they are not using the chat.
      if(k.getKeyChar() == 'm' && !UserInterface.chatVisible){
        if(UserInterface.bigMapVisible){
          UserInterface.bigMapVisible = false;
        }else{
          UserInterface.bigMapVisible = true;
        }
      }
      
      if(k.getKeyChar() == 'h' && !UserInterface.chatVisible){
        if(UserInterface.chooseCharacterMenuVisible){
          UserInterface.chooseCharacterMenuVisible = false;
        }else{
          UserInterface.chooseCharacterMenuVisible = true;
        }
      }
      
      //=================================================================================CHAT
      if(k.getKeyCode() == 10){ //pressed [ENTER]
        if(UserInterface.chatVisible){
          //send message to server here
          if(UserInterface.chatMessage.length() >0){
            //System.out.println("[" + intPlayerNumber + "]" + " Chat Message: " + UserInterface.chatMessage);
            //System.out.println("player," + intPlayerNumber + ",message," + UserInterface.chatMessage);
            
            ssm.sendText("player," + intPlayerNumber + ",message," + UserInterface.chatMessage);
            //create space for the chat array
            for(int i=0; i<UserInterface.chatMessages.length-1;i++){
              UserInterface.chatMessages[i] = UserInterface.chatMessages[i+1];
            }
            //input this new message.
            UserInterface.chatMessages[UserInterface.chatMessages.length-1] = "Player " + (intPlayerNumber+1) + ": " + UserInterface.chatMessage;
            UserInterface.chatMessage = ""; //reset chat message.
            UserInterface.recievedMessage = true;
            UserInterface.dismissChatTimer.stop();
            UserInterface.dismissChatTimer.start();
            
          }
          UserInterface.chatVisible = false;
        }else{
          UserInterface.chatVisible = true;
        }
      }
      
      //==================SEND CHARACTERS TO CHAT IF CHAT IS OPEN
      if(UserInterface.chatVisible){
        //IF PLAYER PRESSES [BACKSPACE]
        if(k.getKeyCode() == 8){
          if(UserInterface.chatMessage.length() >= 1){ //cannot substring under 1
            UserInterface.chatMessage = UserInterface.chatMessage.substring(0, UserInterface.chatMessage.length()-1);
          }
        }else{
          //add letter to strMessage 
          //ignore commas and new lines in chat
          if(k.getKeyChar() != ',' && k.getKeyCode() != 16 && k.getKeyCode() != 10){ 
            UserInterface.chatMessage += k.getKeyChar();
          }
        }
      }
      
      //==============================================================Show pause screen [ESC]
      if(k.getKeyCode() == 27){
        if(UserInterface.pauseScreenVisible){
          UserInterface.pauseScreenVisible = false;
        }else{
          UserInterface.pauseScreenVisible = true;
          UserInterface.bigMapVisible = false;
          UserInterface.helpScreenVisible = false;
        }
      }
    }else{
      homePanel.keyCode = k.getKeyCode();
      homePanel.keyChar = k.getKeyChar() + "";
    }
  }
  public void keyReleased (KeyEvent k) {
    if(gameHasStarted && thisPlayer != null){
      if (k.getKeyCode() == 38 || k.getKeyChar() == 'w') thisPlayer.blnUp = false;
      if (k.getKeyCode() == 40 || k.getKeyChar() == 's') thisPlayer.blnDown = false;
      if (k.getKeyCode() == 37 || k.getKeyChar() == 'a') thisPlayer.blnLeft = false;
      if (k.getKeyCode() == 39 || k.getKeyChar() == 'd') thisPlayer.blnRight = false;
    }
  }
  public void keyTyped (KeyEvent k) {
    if (k.getKeyChar() == 'p') System.out.println(thisPlayer.X + ", " + thisPlayer.Y + ", " + (thisPlayer.X - thisPlayer.X%60)/60 + ", " + (thisPlayer.Y - thisPlayer.Y%60)/60);
    if (k.getKeyChar() >= '0' && k.getKeyChar() <= '9') {
      ssm.sendText("player," + intPlayerNumber + ",kill," + k.getKeyChar());
    }
  }
  
  public void mouseDragged (MouseEvent m) {
    ScreenMouseX = m.getX();
    ScreenMouseY = m.getY();
    if(gameHasStarted && gamePanel != null){
      //send mouse click data to the game panel
      gamePanel.mouseButton = m.getButton();
      gamePanel.shortTimer.start();
    }else{
      //otherwise send it to the home screen
      homePanel.mouseButton = m.getButton();
    }
    
  }
  public void mouseExited(MouseEvent m){
  }
  public void mouseEntered(MouseEvent m){}
  public void mouseReleased(MouseEvent m){
  }
  public void mousePressed(MouseEvent m) throws NullPointerException{
    if(gameHasStarted && gamePanel != null){
      //send mouse click data to the game panel
      gamePanel.mouseButton = m.getButton();
      gamePanel.shortTimer.start();
      
      if(gameHasStarted && gamePanel != null){
        //send mouse click data to the game panel
        gamePanel.mouseButton = m.getButton();
        gamePanel.shortTimer.start();
        
        if(m.getButton() == 1 && !UserInterface.chooseCharacterMenuVisible && gamePanel.UI != null){
          //compare cooldown time to when the attack was last sent.
          double timeSinceCooldown = (System.nanoTime() - gamePanel.UI.longcooldownatk1)/1e6;
          if(timeSinceCooldown > Double.parseDouble(gamePanel.UI.strDataStats[gamePanel.UI.characterSelected][2])){
            //attack only if cooldown is finished
            ssm.sendText("player," + ClientMain.intPlayerNumber + ",sendatk," + gamePanel.UI.atk1 + ",atk1");
            //reset cooldown
            //show animations for attack
            gamePanel.timerAtk.stop();
            gamePanel.timerAtk.start();
            gamePanel.intAtkAnim = 0;
            gamePanel.UI.longcooldownatk1 = System.nanoTime();
            longPlayerCooldown[intPlayerNumber][0] = System.nanoTime();
          }
          
        }else if(m.getButton() == 3 && !UserInterface.chooseCharacterMenuVisible && gamePanel.UI != null){
          
          //compare current time to when the attack was last sent.
          double timeSinceCooldown = (System.nanoTime() - gamePanel.UI.longcooldownatk2)/1e6;
          if(timeSinceCooldown > Double.parseDouble(gamePanel.UI.strDataStats[gamePanel.UI.characterSelected][4])){
            //attack only if cooldown is finished
            ssm.sendText("player," + ClientMain.intPlayerNumber + ",sendatk," + gamePanel.UI.atk2 + ",atk2");
            //reset cooldown
            gamePanel.timerAtk.stop();
            gamePanel.timerAtk.start();
            gamePanel.intAtkAnim = 0;
            gamePanel.UI.longcooldownatk2 = System.nanoTime();
            longPlayerCooldown[intPlayerNumber][1] = System.nanoTime();
          }
        }
      }else{
        //otherwise send it to the home screen
        homePanel.mouseButton = m.getButton();
      }
    }else{
      //otherwise send it to the home screen
      homePanel.mouseButton = m.getButton();
    }
  }
  public void mouseClicked(MouseEvent m) throws NullPointerException{
    if(gameHasStarted && gamePanel != null){
      //send mouse click data to the game panel
      gamePanel.mouseButton = m.getButton();
      gamePanel.shortTimer.start();
      
      if(m.getButton() == 1 && !UserInterface.chooseCharacterMenuVisible){
        //compare current time to when the attack was last sent.
        double timeSinceCooldown = (System.nanoTime() - gamePanel.UI.longcooldownatk1)/1e6;
        if(timeSinceCooldown > Double.parseDouble(gamePanel.UI.strDataStats[gamePanel.UI.characterSelected][2])){
          //attack only if cooldown is finished
          ssm.sendText("player," + ClientMain.intPlayerNumber + ",sendatk," + gamePanel.UI.atk1 + ",atk1");
          //reset cooldown
          gamePanel.UI.longcooldownatk1 = System.nanoTime();
          longPlayerCooldown[intPlayerNumber][0] = System.nanoTime();
        }
        
      }else if(m.getButton() == 3 && !UserInterface.chooseCharacterMenuVisible){
        
        //compare current time to when the attack was last sent.
        double timeSinceCooldown = (System.nanoTime() - gamePanel.UI.longcooldownatk2)/1e6;
        try{
          
          if(timeSinceCooldown > Double.parseDouble(gamePanel.UI.strDataStats[gamePanel.UI.characterSelected][4])){
            //attack only if cooldown is finished
            ssm.sendText("player," + ClientMain.intPlayerNumber + ",sendatk," + gamePanel.UI.atk2 + ",atk2");
            //reset cooldown
            gamePanel.UI.longcooldownatk2 = System.nanoTime();
            longPlayerCooldown[intPlayerNumber][1] = System.nanoTime();
          }
        }catch(NullPointerException e){}
      }
    }else{
      //otherwise send it to the home screen
      homePanel.mouseButton = m.getButton();
    }
  }
  public void mouseMoved (MouseEvent m) {
    ScreenMouseX = m.getX();
    ScreenMouseY = m.getY();
  }
  
  //constructor
  public ClientMain(String currentMap){
    this.currentMap = currentMap;
    
    mainMenuTimer = new Timer(2000, this);
    mainMenuTimer.start();
    
    frame = new JFrame("Client");
    frame.setDefaultCloseOperation(3);
    
    homePanel = new ClientHome();
    homePanel.setPreferredSize(new Dimension(1280, 720));
    homePanel.setLayout(null);
    
    frame.setContentPane(homePanel);
    frame.pack();
    frame.setVisible(true);
    frame.setResizable (false);
    frame.addKeyListener(this);
    frame.addMouseListener(this);
    frame.addMouseMotionListener(this);
    
    
    //load/set cursor
    try{
      //cursorImg = ImageIO.read(new File("UI_crosshair.png"));
      cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
      crosshair = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "crosshair");
      default_crosshair = Toolkit.getDefaultToolkit().createCustomCursor(ImageIO.read(new File("empty")), new Point(0,0), "crosshair");
    }catch(Exception e){}
    
    frame.addWindowListener(new java.awt.event.WindowAdapter() {
      @Override
      public void windowClosing(java.awt.event.WindowEvent windowEvent) {
        if (ssm != null) {
          ssm.disconnect();
        }
      }
    });
  }
  
  //main
  public static void main (String [] args) {
    try {
      ClientMain cm = new ClientMain (args [0]);
    }catch (ArrayIndexOutOfBoundsException e) {
      ClientMain cm = new ClientMain ("map1");
    }
  }
}