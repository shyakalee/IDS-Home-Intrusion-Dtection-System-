
/**
 * @Author INGABIRE domitille  | Mugenzi J Claude // HOME INTRUSION DETECTION SYSTEN PROJECT
 */
package ids_project;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.WebcamDiscoveryEvent;
import com.github.sarxos.webcam.WebcamDiscoveryListener;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamPicker;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import gnu.io.*;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.concurrent.TimeoutException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import javax.swing.JOptionPane;
import java.awt.FlowLayout;

/**
 *
 * @author SHYAKA
 */
public class pictureGrab extends javax.swing.JFrame implements SerialPortEventListener,Runnable, WebcamListener, WindowListener, Thread.UncaughtExceptionHandler, ItemListener, WebcamDiscoveryListener{
    
    //*************************** @ HOME INTRUSION DETECTION SYSTEM **********************************    
    
    
    int count=0;
    Integer motionCounts=0;
    String picName="motionIDS_00";
    
    
    //********* swing webcam interface getting
        private final Webcam webcam = null;
	private final WebcamPanel panel = null;
	private final WebcamPicker picker = null;
        private JComboBox allwebcam;
                
        // ************* Live feed *************************************
        Webcam webcamX=Webcam.getDefault();
        private final Dimension vid=WebcamResolution.VGA.getSize();
        private final Dimension ds=new Dimension(378,251);
        private final WebcamPanel wCamPanel = new WebcamPanel(webcamX,ds,false);  
                
        // *************************************************************
           
    private JButton connect;
    
    SerialPort serialPort;
    
    private static final String PORT_NAMES[]= {"COM12","COM7","COM3","COM14"}; //Commonly COM13 Arduino Uno    
    
    static SerialPort chosenPort;    
    
    static CommPortIdentifier portId;

    InputStream inputStream;
   
    Thread readThread;    
      
    private BufferedReader input;
    private OutputStream output;
    private static final int TIME_OUT=2000;
    private static final int DATA_RATE=9600;    //serial commmnication default speed
    
    public void initialize() throws IOException{ 
        disconnect.setEnabled(false);
        detectedMessage.setVisible(false);
        motionsCount.setVisible(false);
        
        allWebcamsAvailable();  //list all webcams available in a comb box control                
    }
    
    public synchronized void close(){
        if(serialPort!=null){
            serialPort.removeEventListener();
            serialPort.close();        
        }    
    }
      //****************************** @HOME INTRUSION DETECTION SYSTEM ****************************
    
    //check if statically COM Port is available
    public void SerialPortChecker(){        
        
        System.setProperty("gnu.io.rxtx.SerialPorts", "COM3");
        CommPortIdentifier portId=null;
        Enumeration portEnum=CommPortIdentifier.getPortIdentifiers();
        
        while(portEnum.hasMoreElements()){
            CommPortIdentifier currPortId=(CommPortIdentifier)portEnum.nextElement();
            for(String portName:PORT_NAMES){
                if(currPortId.getName().equals(portName)){  
                    JOptionPane.showMessageDialog(null,"COM Ports are Found,And Picture Is Taken","Ports",JOptionPane.INFORMATION_MESSAGE);
                       try{
                           capture();                       
                       }catch(Exception e){}
                    portId=currPortId;
                    break;              
                }          
            }     
        }
        if(portId==null){
            JOptionPane.showMessageDialog(connect, "No Arduino COM Port Detected", "Error", JOptionPane.WARNING_MESSAGE);            
            //JOptionPane.showMessageDialog(null, " No COM Port Found");
            return;        
        }
        try{
        serialPort=(SerialPort)portId.open(this.getName(), TIME_OUT);
        serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8,SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        
        input=new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
        output=serialPort.getOutputStream();
        
        serialPort.addEventListener(this);
        serialPort.notifyOnDataAvailable(true);        
      }        
       catch(Exception exComFind){
           System.err.println(exComFind.toString());      
       }       
    }
    
     //******************** @HOME INTRUSION DETECTION SYSTEM *********************
    
    @Override
    
    public synchronized void serialEvent(SerialPortEvent oEvent){
        if(oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE){
         try{
          switch(oEvent.getEventType()){
          case SerialPortEvent.BI:
          case SerialPortEvent.OE:
          case SerialPortEvent.FE:
          case SerialPortEvent.PE:
          case SerialPortEvent.CD:
          case SerialPortEvent.CTS:
          case SerialPortEvent.DSR:
          case SerialPortEvent.RI:
            break;
          case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
            break;
          case SerialPortEvent.DATA_AVAILABLE:
              byte[] readBuffer=new byte[12];                                            
               try{
               while(inputStream.available() > 0){
               inputStream.read(readBuffer);                                                
              }           
               
               //*********** picture taken according to serial data availability  *** and save them to IDS Folder **************
                   capture();
               //***************************************************************************************************************    
                   
              String x=new String(readBuffer);
              //getting serial data communication from arduino
              //System.out.println(new String(readBuffer));
              //jTextArea1.setText(x); 
               waiting(1);                                            
              } catch (IOException ex) {
              Logger.getLogger(PortShower.class.getName()).log(Level.SEVERE, null, ex);
            }
           }         
         }                
            catch(Exception serEv){
            System.out.println(serEv.toString());
            }   
        }   
    }    
    
     private void waiting(int n) {
        long t0,t1;
        t0=System.currentTimeMillis();
        do{
            t1=System.currentTimeMillis();        
        }while((t1-t0)<(n*1000));        
    }
     
    /**
     * @Author MUGENZI // @HOME INTRUSION DETECTION SYSTEN PROJECT
     */
    public pictureGrab() throws IOException{
        initComponents();
        initialize();               
    }
    
    private void liveFeed(){
        detectedMessage.setVisible(false);
        motionsCount.setVisible(false);
        webcamX.setViewSize(vid);
        wCamPanel.setFillArea(true);
        feedPanel.setLayout(new FlowLayout());
        feedPanel.add(wCamPanel);        
    }
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        shadowPanel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        MainPanelLeft = new javax.swing.JPanel();
        panelOne = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        panelTwo = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        StatusPanel = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        StatusPanel1 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        close = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        takenSnap = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        detectedMessage = new javax.swing.JLabel();
        motionsCount = new javax.swing.JLabel();
        allWebcams = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        wbc = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        feedPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        selectPort = new javax.swing.JButton();
        availablePorts = new javax.swing.JComboBox();
        disconnect = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        snapIt = new javax.swing.JButton();
        checkPort = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("IDS Project");
        setAutoRequestFocus(false);
        setBackground(new java.awt.Color(255, 0, 0));
        setLocationByPlatform(true);
        setUndecorated(true);
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                formMouseDragged(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        shadowPanel.setBackground(new java.awt.Color(153, 153, 255));

        jPanel4.setBackground(new java.awt.Color(220, 245, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel4.setAutoscrolls(true);

        MainPanelLeft.setBackground(new java.awt.Color(54, 33, 89));

        panelOne.setBackground(new java.awt.Color(30, 55, 130));
        panelOne.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                panelOneMousePressed(evt);
            }
        });
        panelOne.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/db.png"))); // NOI18N
        panelOne.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jLabel3.setFont(new java.awt.Font("Architects Daughter", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("DATABASE");
        panelOne.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 0, 100, 40));

        panelTwo.setBackground(new java.awt.Color(10, 15, 45));
        panelTwo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                panelTwoMousePressed(evt);
            }
        });
        panelTwo.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/serialAndCOM_New.png"))); // NOI18N
        panelTwo.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 48, -1));

        jLabel6.setFont(new java.awt.Font("Architects Daughter", 1, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("PORTS");
        panelTwo.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 0, -1, 40));

        jLabel15.setFont(new java.awt.Font("BIRTH OF A HERO", 1, 18)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(255, 255, 255));
        jLabel15.setText("SECURITY SYSTEM");

        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Camera Double.png"))); // NOI18N

        javax.swing.GroupLayout MainPanelLeftLayout = new javax.swing.GroupLayout(MainPanelLeft);
        MainPanelLeft.setLayout(MainPanelLeftLayout);
        MainPanelLeftLayout.setHorizontalGroup(
            MainPanelLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelTwo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelOne, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, MainPanelLeftLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22))
            .addGroup(MainPanelLeftLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator2)
                .addContainerGap())
        );
        MainPanelLeftLayout.setVerticalGroup(
            MainPanelLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, MainPanelLeftLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(panelOne, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelTwo, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(185, 185, 185))
        );

        StatusPanel.setBackground(new java.awt.Color(110, 89, 222));
        StatusPanel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        StatusPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel11.setFont(new java.awt.Font("BIRTH OF A HERO", 0, 48)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("HOME SECURITY SYSTEM");
        StatusPanel.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 0, -1, 60));

        StatusPanel1.setBackground(new java.awt.Color(0, 51, 51));
        StatusPanel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(204, 204, 204));
        jLabel10.setText("INTEGRATED POLYTECHNIC REGIONAL CENTER(IPRC) - HUYE");

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(204, 204, 204));
        jLabel12.setText("Developpers:");

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(204, 204, 255));
        jLabel5.setText("Ingabire Domittille  &  MUGENZI J Claude");

        javax.swing.GroupLayout StatusPanel1Layout = new javax.swing.GroupLayout(StatusPanel1);
        StatusPanel1.setLayout(StatusPanel1Layout);
        StatusPanel1Layout.setHorizontalGroup(
            StatusPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(StatusPanel1Layout.createSequentialGroup()
                .addGap(108, 108, 108)
                .addComponent(jLabel10)
                .addGap(23, 23, 23)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(35, Short.MAX_VALUE))
        );
        StatusPanel1Layout.setVerticalGroup(
            StatusPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(StatusPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(StatusPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, StatusPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel5))
                    .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, StatusPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        close.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        close.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/closebox.png"))); // NOI18N
        close.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeActionPerformed(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(220, 245, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        takenSnap.setBackground(new java.awt.Color(204, 255, 204));
        takenSnap.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        jLabel1.setFont(new java.awt.Font("Sitka Text", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(10, 100, 255));
        jLabel1.setText("RECENTLY TAKEN MOTION");

        detectedMessage.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        detectedMessage.setText("Motions Detected!!!!  BeWare Please");

        motionsCount.setBackground(new java.awt.Color(240, 0, 0));
        motionsCount.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        motionsCount.setForeground(new java.awt.Color(255, 0, 0));

        allWebcams.setToolTipText("");
        allWebcams.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                allWebcamsMouseClicked(evt);
            }
        });
        allWebcams.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allWebcamsActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(10, 100, 255));
        jLabel7.setText("Available System Webcam");

        wbc.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        wbc.setText("OPEN");
        wbc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wbcActionPerformed(evt);
            }
        });

        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/videoStream.png"))); // NOI18N
        jLabel14.setToolTipText("LIVE FEED FROM WEBCAM");
        jLabel14.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel14MouseClicked(evt);
            }
        });

        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/closeFeed.png"))); // NOI18N
        jLabel16.setToolTipText("CLOSE LIVE FEED FROM WEBCAM");
        jLabel16.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel16MouseClicked(evt);
            }
        });

        feedPanel.setBackground(new java.awt.Color(78, 74, 58));
        feedPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        feedPanel.setPreferredSize(new java.awt.Dimension(370, 251));
        feedPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setBackground(new java.awt.Color(204, 204, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        selectPort.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        selectPort.setText("FIND PORTS");
        selectPort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectPortActionPerformed(evt);
            }
        });

        availablePorts.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        availablePorts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                availablePortsActionPerformed(evt);
            }
        });

        disconnect.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        disconnect.setText("DISCONNECT");
        disconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disconnectActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(availablePorts, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(selectPort)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(disconnect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(availablePorts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectPort)
                    .addComponent(disconnect))
                .addContainerGap())
        );

        jPanel2.setBackground(new java.awt.Color(204, 204, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setLayout(new java.awt.GridBagLayout());

        snapIt.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        snapIt.setText("TEST");
        snapIt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                snapItActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 27;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(13, 12, 13, 0);
        jPanel2.add(snapIt, gridBagConstraints);

        checkPort.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        checkPort.setText("CHECK PORT");
        checkPort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkPortActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(13, 6, 13, 12);
        jPanel2.add(checkPort, gridBagConstraints);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/refresh.png"))); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                            .addComponent(allWebcams, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(wbc)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(feedPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 344, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(71, 71, 71)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel16))))
                .addGap(14, 14, 14)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(motionsCount, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(115, 115, 115)
                        .addComponent(detectedMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(53, 53, 53)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(takenSnap, javax.swing.GroupLayout.PREFERRED_SIZE, 348, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(31, 31, 31)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(27, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(57, 57, 57)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(takenSnap, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(allWebcams, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(wbc))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel14)
                                .addGap(184, 184, 184)
                                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(feedPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(detectedMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(motionsCount, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(MainPanelLeft, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(StatusPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                .addComponent(StatusPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                .addComponent(close, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18))))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(close, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(StatusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(StatusPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(MainPanelLeft, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout shadowPanelLayout = new javax.swing.GroupLayout(shadowPanel);
        shadowPanel.setLayout(shadowPanelLayout);
        shadowPanelLayout.setHorizontalGroup(
            shadowPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowPanelLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 1231, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(23, Short.MAX_VALUE))
        );
        shadowPanelLayout.setVerticalGroup(
            shadowPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowPanelLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(26, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(shadowPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(shadowPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        getAccessibleContext().setAccessibleDescription("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    //***************** @HOME INTRUSION DETECTION SYSTEM *****************
    
    private void readPir(String inpuLine){ 
        
        String[] values=inpuLine.split(":");        
        int pir=Integer.parseInt(values[1],16);          
    }  
    
    //function to capture stills and save with #nt names on pc thru webcam
    
    public void capture() throws TimeoutException{
        count++;
        motionCounts++;        
        try {
       //Webcam webcam=Webcam.getWebcams().get(0); //webcam.getDefault() 0 for def webccam  1 for external
       //Webcam webcam=Webcam.getWebcams(20000).get(1);            
            Webcam webcam=Webcam.getWebcams().get(1);            
       //String webcamName="USB 2.0 CAMERA";
      //Webcam webcam= Webcam.getDefault();            
      //Webcam usbWebcam=Webcam.getWebcamByName(webcamName); 
       
      //*************resizing saved image****************       
       Dimension d=new Dimension(2592,1944);
       webcam.setCustomViewSizes(new Dimension[]{d});
       webcam.setViewSize(d);
       //*************************************************
        
       webcam.open();           
            
        // saving the first image to IDS Uploads with name motionIDS_00 add counter value
        ImageIO.write(webcam.getImage(),"JPG", new File("C:\\Users\\student\\Google Drive\\IDS_Uploads\\" + "motionIDS_00" + count + ".jpg"));
        
              
            pictureGrab pic=new pictureGrab();
            //takenSnap.setIcon();
            String path="C:\\Users\\student\\Google Drive\\IDS_Uploads\\" + "motionIDS_00" + count + ".jpg";
            BufferedImage img = ImageIO.read(new File(path));
                      
            Image dimg=img.getScaledInstance(246,148,0);
            ImageIcon imageIcon=new ImageIcon(dimg); 
            
            // save the second image to be setted in the label as image icon
            File img1=new File("C:\\Users\\student\\Google Drive\\IDS_Uploads\\" + "motionIDS_00" + count + ".jpg");
                        
            img=ImageIO.read(img1);            
            Image imgx=img.getScaledInstance(348, 251, img.SCALE_DEFAULT);  //width    //height
            
            //************** SAVE CAPTURED STILLS IN DATABASE ***************
            //int countCap=0;
            try{
            //countCap ++;
            Connection conn;
            java.util.Date date=new java.util.Date();
            java.sql.Date sqlDate=new java.sql.Date(date.getTime());
            java.sql.Timestamp sqlTime=new java.sql.Timestamp(date.getTime());
            
            String emputi=null;
            String name="motionIDS_00" + count;
            Class.forName("com.mysql.jdbc.Driver");
            conn=DriverManager.getConnection("jdbc:mysql://localhost/ids_log","root","");
            String sql="INSERT INTO logs VALUES(?,?,?,?)";
            
            PreparedStatement pst=conn.prepareStatement(sql);
            pst.setString(1, emputi);
            pst.setString(2, name);
            pst.setDate(3, sqlDate);
            pst.setTimestamp(4, sqlTime);
            pst.executeUpdate();    //RECORDS TO DATABASE
                     
            pst.close();
            conn.close();           
            
            } catch (Exception e) {
               JOptionPane.showMessageDialog(null, e.getMessage());
               // System.out.println(e.getMessage());
            }           
            
            //***************************************************************
           takenSnap.setIcon(new ImageIcon(img));
            //set number of motions in a lbael
            motionsCount.setText(motionCounts.toString());
            motionsCount.repaint();
            
            //JFrame frame= new JFrame();
            JLabel label=new JLabel(new ImageIcon(imgx));
            detectedMessage.setVisible(false);
            motionsCount.setVisible(false);
            //frame.getContentPane().add(label,BorderLayout.CENTER);
            //frame.pack();
            //frame.setSize(400, 400);
            //frame.setVisible(true); 
                      
            takenSnap.revalidate();
            takenSnap.repaint();
            takenSnap.update(takenSnap.getGraphics());            
            
            webcam.close(); 
            //JOptionPane.showMessageDialog(null, "saved Success");
            
        } catch (IOException ex) {
            //ex.printStackTrace();
            JOptionPane.showMessageDialog(null,ex.getMessage());
            //Logger.getLogger(pictureGrab.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void formMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
            //notthing to do here
    }//GEN-LAST:event_formMouseDragged

    private void closeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeActionPerformed
        //closing the interface of the popgram
        System.exit(0);
    }//GEN-LAST:event_closeActionPerformed
            
    private void panelTwoMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panelTwoMousePressed
        int timeDelay=2000;
        setColors(panelTwo);
        resetColors(panelOne);
//        resetColors(panelThree);
//        resetColors(panelFour);
        try {
            Thread.sleep(timeDelay);            
        } catch (InterruptedException ex) {
            Logger.getLogger(pictureGrab.class.getName()).log(Level.SEVERE, null, ex);
        }
        PortShower ps=new PortShower();
        ps.setVisible(true);                       
    }//GEN-LAST:event_panelTwoMousePressed

    private void panelOneMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panelOneMousePressed
        try{
        setColors(panelOne);
        resetColors(panelTwo);
//        resetColors(panelThree);
//        resetColors(panelFour);
        Thread.sleep(1000);
        AllCaptured s=new AllCaptured();
        s.setVisible(true);      
        }catch(Exception e){System.out.println(e.getMessage());}
    }//GEN-LAST:event_panelOneMousePressed

    
    private void checkPortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkPortActionPerformed
        try{
            SerialPortChecker();
        }catch(Exception e){
        JOptionPane.showMessageDialog(null, e.getMessage());}
    }//GEN-LAST:event_checkPortActionPerformed
    
    private void snapItActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_snapItActionPerformed
        try{
            capture();
        }catch(Exception e){
            
        }
    }//GEN-LAST:event_snapItActionPerformed
        //********************** @HOME INTRUSION DETECTION SYSTEM **********************
    
    private void disconnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disconnectActionPerformed
        try{
            Thread.sleep(1000);
            serialPort.close();
            webcam.close();
            JOptionPane.showMessageDialog(null, "Disconnecting..","Error",ERROR);
            System.out.println("Port Disconneccted..");
            connect.setEnabled(true);
            selectPort.enable(false);
        }catch(Exception e){
            System.out.println(" "+e.getMessage());
        }
    }//GEN-LAST:event_disconnectActionPerformed

    public void allWebcamsAvailable(){
         int i=0;
        List<Webcam> web= Webcam.getWebcams();
        for(i=0;i<web.size();i++){
            System.out.println(web.get(i));
            allWebcams.addItem(web.get(i));     //get webcams in a console
        }   
    }
    
    private void availablePortsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_availablePortsActionPerformed
        Object selectedItem=availablePorts.getSelectedItem();
        String com=selectedItem.toString();
        try {
            SimpleRead(com);
            disconnect.setEnabled(true);
            //selectPort.setEnabled(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }//GEN-LAST:event_availablePortsActionPerformed

    private void selectPortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectPortActionPerformed
        java.util.Enumeration<CommPortIdentifier> portEnum=CommPortIdentifier.getPortIdentifiers();
        int i=0;
        String[] r=new String[5];

        while(portEnum.hasMoreElements() && i < 5){
            CommPortIdentifier portIdentifier=portEnum.nextElement();
            r[i]=portIdentifier.getName();
            i++;
        }
        availablePorts.setModel(new javax.swing.DefaultComboBoxModel<>(r));
        //JOptionPane.showMessageDialog(null,"COM Ports Found");
        //System.out.println("Found "+r[i].toString());
    }//GEN-LAST:event_selectPortActionPerformed

    private void wbcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wbcActionPerformed
        try{
        liveFeed();
        Thread t=new Thread(){
            @Override
            public void run(){
              wCamPanel.start();                        
            }                        
        };
        t.setDaemon(true);
        t.start();
        }catch(Exception e){e.getMessage();}
    }//GEN-LAST:event_wbcActionPerformed
         //********************** @HOME INTRUSION DETECTION SYSTEM **********************
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try{
          this.dispose();
          Thread.sleep(1000);
          this.setVisible(true);          
        }catch(Exception e){JOptionPane.showMessageDialog(null, "Error In Refreshing..");}        
    }//GEN-LAST:event_jButton1ActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened

    }//GEN-LAST:event_formWindowOpened

    private void jLabel14MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel14MouseClicked
        try{        
        Thread t=new Thread(){
            @Override
            public void run(){
                liveFeed();
              wCamPanel.start();                        
            }                        
        };        
        t.setDaemon(true);
        t.start();
        }catch(Exception e){e.getMessage();
        }        
    }//GEN-LAST:event_jLabel14MouseClicked

    private void jLabel16MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel16MouseClicked
        try{
           wCamPanel.stop();
        }catch(Exception e){e.getMessage();}
        
    }//GEN-LAST:event_jLabel16MouseClicked

    private void allWebcamsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allWebcamsActionPerformed
        try {
            Object selectedItem=webcam.getWebcams();
            String web=selectedItem.toString();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }//GEN-LAST:event_allWebcamsActionPerformed

    private void allWebcamsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_allWebcamsMouseClicked

    }//GEN-LAST:event_allWebcamsMouseClicked


    //********* setting colors for the panes **********
    void setColors(JPanel panel){
        panel.setBackground(new Color(30,55,110)); 
        //panel.setBackground(new Color(224, 29, 36));
    }    
    void resetColors(JPanel panel){
        panel.setBackground(new Color(10,15,45));        
    }
    //************************************************
     
    
    public static void main(String args[]) throws Exception{
        /* Set the Nimbus look and feel *///<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(pictureGrab.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(pictureGrab.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(pictureGrab.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(pictureGrab.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            
            pictureGrab mainy=new pictureGrab();                       
            @Override
            public void run() {                
                try {                    
                    Thread t=new Thread();
                    //Thread.sleep(1000);
                    mainy.initialize();                 
                    new pictureGrab().setVisible(true);
                    t.start(); 
                    
                    //JOptionPane.showMessageDialog(null, "Started");                    
                } catch (IOException ex) {
                    Logger.getLogger(pictureGrab.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel MainPanelLeft;
    private javax.swing.JPanel StatusPanel;
    private javax.swing.JPanel StatusPanel1;
    private javax.swing.JComboBox allWebcams;
    private javax.swing.JComboBox availablePorts;
    private javax.swing.JButton checkPort;
    private javax.swing.JButton close;
    private javax.swing.JLabel detectedMessage;
    private javax.swing.JButton disconnect;
    private javax.swing.JPanel feedPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel motionsCount;
    private javax.swing.JPanel panelOne;
    private javax.swing.JPanel panelTwo;
    private javax.swing.JButton selectPort;
    private javax.swing.JPanel shadowPanel;
    private javax.swing.JButton snapIt;
    private javax.swing.JLabel takenSnap;
    private javax.swing.JButton wbc;
    // End of variables declaration//GEN-END:variables

    
    //******************  read serial datas when seleccted combo box port is active ******************
    
  public void SimpleRead(String Com){
        
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        while(portList.hasMoreElements()){
            portId=(CommPortIdentifier)portList.nextElement();
            
            if(portId.getPortType()==CommPortIdentifier.PORT_SERIAL){
            
            if(portId.getName().equals(Com)){
                try{
                    serialPort=(SerialPort) portId.open("SimpleReadApp",2000);
                    System.out.println("\n");
                    System.out.println("Connected Success To " + Com);
                    JOptionPane.showMessageDialog(null, "Successfully Connected to " +Com+"\n"+"Waiting for Incoming Serial Datas..");
                    System.out.println("Getting Datas...");
                    //JOptionPane.showMessageDialog(null, "Connected to Port via 2000");                                    
                }catch(PortInUseException pin){
                    //JOptionPane.showMessageDialog(null, "Port In use_SHYAKA","Error",ERROR);
                    System.out.println(pin);
                }
                try{
                     inputStream=serialPort.getInputStream();
                     
                     //JOptionPane.showMessageDialog(null, "Getting Data Now!!!");
                }catch(IOException e){
                System.out.println(e);
                }
                
                try{
                     serialPort.addEventListener(this);
                }catch(TooManyListenersException e){
                System.out.println(e);
                }
                serialPort.notifyOnDataAvailable(true);
                try{
                    serialPort.setSerialPortParams(9600, serialPort.DATABITS_8,serialPort.STOPBITS_1,serialPort.PARITY_NONE);                                   
                }catch(UnsupportedCommOperationException e){
                    System.out.println(e);               
                }           
            }        
        }
    } 
 }   
      //********************** @HOME INTRUSION DETECTION SYSTEM **********************
  
    @Override
    public void run() {
       
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void webcamOpen(WebcamEvent we) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void webcamClosed(WebcamEvent we) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void webcamDisposed(WebcamEvent we) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void webcamImageObtained(WebcamEvent we) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowOpened(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowClosing(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowClosed(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowIconified(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowActivated(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void webcamFound(WebcamDiscoveryEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void webcamGone(WebcamDiscoveryEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

//********************** @HOME INTRUSION DETECTION SYSTEM **********************
//********************** @HOME INTRUSION DETECTION SYSTEM **********************
//********************** @HOME INTRUSION DETECTION SYSTEM **********************
//********************** @HOME INTRUSION DETECTION SYSTEM **********************