import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.*;
import javax.imageio.*;
import java.awt.image.*;

public class ClientHome extends JPanel implements ActionListener{
  Timer timer;
  Timer timerResetClick;
  static int mouseButton = 0;
  static String keyChar = "";
  static int keyCode;
  int tab = 1;
  int characterSelection = 0;
  
  //Images
  BufferedImage homeImg;
  BufferedImage Crosshair;
  BufferedImage helpImg;
  BufferedImage wallImg1;
  BufferedImage wallImg2;
  BufferedImage tabImg1;
  BufferedImage playButtonsImg;
  BufferedImage btnBack_Img;
  BufferedImage btnBack_Img_rollover;
  BufferedImage connecting_Img;
  
  JTextField ipTextField;
  JButton btnBack;
  JButton btnConnect;
  JButton btnNavigation[] = new JButton[4];
  
  JButton btnQuitYes;
  JButton btnQuitNo;
  
  //Fonts
  Font menuFont = new Font("Arial", Font.PLAIN, 24);
  Font textFont = new Font("Arial", Font.PLAIN, 22);
  
  public void actionPerformed(ActionEvent e){
    if(e.getSource() == timer){
      this.repaint();
    }
    for(int button=0; button<4; button++){
      if(e.getSource() == btnNavigation[button]){
        switch(button){
          case 0: tab = 1; break; //home button
          case 1: tab = 2; break; //play button
          case 2: tab = 3; break; //help button
          case 3: tab = 4; break; //exit button
          default: break;
        }
        mouseButton = 0;
      }
    }
    
    if(e.getSource() == btnConnect){
      //connect button
      tab = 22;//change tab to "connecting to server" screen
      ClientMain.ip = ipTextField.getText();
      ClientMain.port = 6112; //wow
      ClientMain.gameHasStarted = true;
    }
    
    if(e.getSource() == btnQuitNo){
      tab = 1; //return to page 1
    }
    
    if(e.getSource() == btnQuitYes){
      System.exit(0); //quit game
    }
    
    if(e.getSource() == btnBack){
      tab = 2; //return to "PLAY" tab
    }
  }
  
  public void paintComponent (Graphics g) {
    //anti alias!
    super.paintComponent(g);
    Graphics2D graphics2D = (Graphics2D) g;
    graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
    graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); 
    
    //Make the buttons have a toggle effect
    for(int button=0; button<4; button++){
      if(tab == button+1){
        btnNavigation[button].setIcon(new ImageIcon(btnBack_Img_rollover));
        //btnNavigation[button].setRolloverIcon(new ImageIcon(btnBack_Img));
      }else{
        btnNavigation[button].setIcon(new ImageIcon(btnBack_Img));
        btnNavigation[button].setRolloverIcon(new ImageIcon(btnBack_Img_rollover));
      }
    }
    
    g.drawImage(wallImg1, 0, 0, null);
    //======TABBED USER INTERFACE=======
    g.setColor(Color.WHITE);
    g.setFont(menuFont);
    
    //======================HIDE/SHOW JCOMPONENTS based on what tab the user is in
    
    if(tab == 21) {
      ipTextField.setVisible(true);
      btnBack.setVisible(true);
      btnConnect.setVisible(true);
      btnQuitYes.setVisible(false);
      btnQuitNo.setVisible(false);
    }else if(tab == 22){
      btnBack.setVisible(true);
      ipTextField.setVisible(false);
      btnConnect.setVisible(false);
      btnQuitYes.setVisible(false);
      btnQuitNo.setVisible(false);
    }else if(tab == 4){
      btnQuitYes.setVisible(true);
      btnQuitNo.setVisible(true);
      btnBack.setVisible(false);
      ipTextField.setVisible(false);
      btnConnect.setVisible(false);
    }else{
      ipTextField.setVisible(false);
      btnBack.setVisible(false);
      btnConnect.setVisible(false);
      btnQuitYes.setVisible(false);
      btnQuitNo.setVisible(false);
    }
    
    //player has pressed CONNECT
    if(tab == 22){
      ipTextField.setVisible(false);
      btnBack.setVisible(false);
      btnConnect.setVisible(false);
      btnQuitYes.setVisible(false);
      btnQuitNo.setVisible(false);
      for(int menuItem=0; menuItem<4; menuItem++){
        btnNavigation[menuItem].setVisible(false);
      }
    }
    
    //================================================================TABS CONTORL
    switch(tab){
      
      case 1: //==============HOME TAB============
        g.setColor(Color.WHITE);
        g.drawImage(tabImg1, 0, 0, null);
        //g.drawString("Welcome to [strName]", 304, 300);
        break;
      case 2: //=============PLAY TAB==================
        
        //draw buttons to show in the tab
        
        int moveY = 0;
        g.drawImage(playButtonsImg, 0, 72+moveY, 1300, 142+72+moveY, 0, (210*3), 1280, (210*3)+142, null);
        
        //draw JOIN GAME IMAGE
        int btnY1 = 72+142; //80
        g.drawImage(playButtonsImg, 0, btnY1, 1300, btnY1+210, 0, 0, 1280, 210, null);
        if( (ClientMain.ScreenMouseY-23) > btnY1 && (ClientMain.ScreenMouseY-23) < btnY1+210){
          g.drawImage(playButtonsImg, 0, btnY1, 1300, btnY1+210, 0, 420, 1280, 630, null);
          if(mouseButton == 1){
            mouseButton = 0;
            tab = 21;
          }
        }
        
        //draw START GAME image
        
         int btnY2 = 72+142+(210); //80
         g.drawImage(playButtonsImg, 0, btnY2, 1300, btnY2+210, 0, 210, 1280, 420, null);
         if((ClientMain.ScreenMouseY-23) > btnY2 && (ClientMain.ScreenMouseY-23) < btnY2+210){
         g.drawImage(playButtonsImg, 0, btnY2, 1300, btnY2+210, 0, 420, 1280, 630, null);
         if(mouseButton == 1){
         mouseButton = 0;
         tab = 23;
         }
         }
         break;
         
        
      case 3: //===================HELP SCREEN===============
        g.drawImage(helpImg, 0, 0, null);
        break;
        
      case 4: //===================QUIT SCREEN================
        g.setColor(new Color(0, 0, 0, 170));
        g.drawImage(homeImg, 712, 75, 712+427, 75+190, 0, 330, 427, 520, null);
        break;
        
      case 21: //===============================TAB FOR CONNECT TO GAME
        //sub tab in PLAY
        //This is where players can enter an IP and connect to a server.
        moveY = 0;
        g.drawImage(playButtonsImg, 0, 72+moveY, 1300, 142+72+moveY, 0, (210*3)+142, 1280, (210*3)+(142*2), null);
        g.setColor(new Color(255, 255, 255));
        g.drawString("Enter IP Address", 304, 300);
        break;
      
      case 22: //=================PLAYER ATTMPTS TO CONNECT TO SERVER
        g.drawImage(connecting_Img, 0, 0, null);
        break;

      case 23: //==================PLAYER IS HOST

        //Start Server

        //Connect with Client
        ClientMain.isHost = true;
        ClientMain.ip = "localhost";
        ClientMain.port = 6112; //wow
        ClientMain.gameHasStarted = true;
        ClientMain.ssm = new SuperSocketMaster(ClientMain.ip, ClientMain.port, this);
        ClientMain.sm_thread = new Thread(ClientMain.sm);
        ClientMain.sm_thread.start();
        tab = 22; //show connecting screen
        break;

      default:
        break;
    }
    
  }
  
  ClientHome(){
    super(true);
    timer = new Timer(1000/30, this);
    timer.start();
    
    try{
      homeImg = ImageIO.read(new File("UI_home.png"));
      Crosshair = ImageIO.read(new File("UI_crosshair.png"));
      helpImg = ImageIO.read(new File("UI_help.png"));
      wallImg1 = ImageIO.read(new File("UI_wall2.png"));
      wallImg2 = ImageIO.read(new File("UI_wall1.png"));
      tabImg1 = ImageIO.read(new File("UI_tab1.png"));
      playButtonsImg = ImageIO.read(new File("UI_playbtn.png"));
      connecting_Img = ImageIO.read(new File("UI_connecting.png"));
    }catch(Exception e){
    }
    
    //J COMPONENTS
    //IP TEXT FIELD
    ipTextField = new JTextField(ClientMain.ip);
    ipTextField.setSize(672, 40);
    ipTextField.setLocation(304, 320);
    ipTextField.setVisible(false);
    ipTextField.setBorder(BorderFactory.createEmptyBorder());
    ipTextField.setFont(textFont);
    
    //================================================BACK BTN
    btnBack = new JButton("BACK");
    btnBack.setLocation(472, 380);
    btnBack.setSize(168, 60);
    btnBack.setVisible(false);
    //get the button image from [BufferedReader] homeImg
    btnBack_Img = new BufferedImage(168, 60, BufferedImage.TYPE_INT_ARGB);
    btnBack_Img_rollover = new BufferedImage(168, 60, BufferedImage.TYPE_INT_ARGB);
    Graphics btnGfx1 = btnBack_Img.createGraphics();
    Graphics btnGfx2 = btnBack_Img_rollover.createGraphics();
    btnGfx1.drawImage(homeImg, 0, 0, 168, 60, 0, 60, 168, 120, null);
    btnGfx2.drawImage(homeImg, 0, 0, 168, 60, 168, 60, 336, 120, null);
    btnBack.setIcon(new ImageIcon(btnBack_Img));
    //styling for back button
    btnBack.setRolloverIcon(new ImageIcon(btnBack_Img_rollover));
    btnBack.setHorizontalTextPosition(JButton.CENTER);
    btnBack.setVerticalTextPosition(JButton.CENTER);
    btnBack.setForeground(Color.WHITE);
    btnBack.setBorderPainted(false);
    btnBack.setFocusPainted(false);
    btnBack.setContentAreaFilled(false);
    btnBack.addActionListener(this);
    
    //=================================================create the navigation bar buttons
    int navButtonPosX = 304;
    int navButtonSpacing = 10;
    for(int button=0; button<4; button++){
      String labelName = "";
      switch(button){
        case 0: labelName = "HOME"; break;
        case 1: labelName = "PLAY"; break;
        case 2: labelName = "HELP"; break;
        case 3: labelName = "EXIT"; break;
      }
      //create buton with appropriate label.
      //also add styling to the button including: setIcon, setRolloverIcon
      btnNavigation[button] = new JButton(labelName);
      btnNavigation[button].setHorizontalTextPosition(JButton.CENTER);
      btnNavigation[button].setVerticalTextPosition(JButton.CENTER);
      btnNavigation[button].setForeground(Color.WHITE);
      btnNavigation[button].setSize(168, 60);
      btnNavigation[button].setLocation(navButtonPosX, 5);
      btnNavigation[button].setIcon(new ImageIcon(btnBack_Img));
      btnNavigation[button].setRolloverIcon(new ImageIcon(btnBack_Img_rollover));
      btnNavigation[button].setBorderPainted(false);
      btnNavigation[button].setFocusPainted(false);
      btnNavigation[button].setContentAreaFilled(false);
      btnNavigation[button].setVisible(true);
      this.add(btnNavigation[button]); //add this button into JPanel.
      btnNavigation[button].addActionListener(this);
      //once done creating this icon, move on to the next.
      navButtonPosX += 168 + navButtonSpacing;
    }
    
    //===================================================connect button
    btnConnect = new JButton("CONNECT");
    btnConnect.setIcon(new ImageIcon(btnBack_Img));
    //styling for back button
    btnConnect.setLocation(304, 380);
    btnConnect.setSize(168, 60);
    btnConnect.setRolloverIcon(new ImageIcon(btnBack_Img_rollover));
    btnConnect.setHorizontalTextPosition(JButton.CENTER);
    btnConnect.setVerticalTextPosition(JButton.CENTER);
    btnConnect.setForeground(Color.WHITE);
    btnConnect.setBorderPainted(false);
    btnConnect.setFocusPainted(false);
    btnConnect.setContentAreaFilled(false);
    btnConnect.addActionListener(this);
    
    //====================================================QUIT buttons (yes/no)
    btnQuitYes = new JButton("YES");
    btnQuitYes.setIcon(new ImageIcon(btnBack_Img));
    //styling for quit button #1
    btnQuitYes.setLocation(745, 192);
    btnQuitYes.setSize(168, 60);
    btnQuitYes.setRolloverIcon(new ImageIcon(btnBack_Img_rollover));
    btnQuitYes.setHorizontalTextPosition(JButton.CENTER);
    btnQuitYes.setVerticalTextPosition(JButton.CENTER);
    btnQuitYes.setForeground(Color.WHITE);
    btnQuitYes.setBorderPainted(false);
    btnQuitYes.setFocusPainted(false);
    btnQuitYes.setContentAreaFilled(false);
    btnQuitYes.addActionListener(this);
    
    btnQuitNo = new JButton("NO");
    btnQuitNo.setIcon(new ImageIcon(btnBack_Img));
    //styling for quit button #2
    btnQuitNo.setLocation(930, 192);
    btnQuitNo.setSize(168, 60);
    btnQuitNo.setRolloverIcon(new ImageIcon(btnBack_Img_rollover));
    btnQuitNo.setHorizontalTextPosition(JButton.CENTER);
    btnQuitNo.setVerticalTextPosition(JButton.CENTER);
    btnQuitNo.setForeground(Color.WHITE);
    btnQuitNo.setBorderPainted(false);
    btnQuitNo.setFocusPainted(false);
    btnQuitNo.setContentAreaFilled(false);
    btnQuitNo.addActionListener(this);
    
    
    //ADD ALL JCOMPONENTS
    this.add(ipTextField);
    this.add(btnBack);
    this.add(btnConnect);
    this.add(btnQuitYes);
    this.add(btnQuitNo);
  }
}