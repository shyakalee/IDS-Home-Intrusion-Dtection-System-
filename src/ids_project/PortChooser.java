/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ids_project;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import javax.swing.JComboBox;
////////////////////////////////

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author SHYAKA
 */

public class PortChooser{
    
    static int x=0;
    static SerialPort sr;
    
    static SerialPort chosenPort;  
    
    static CommPortIdentifier portId;
    //static enumeration portlist;
    InputStream inputStream;
    SerialPort serialPort;
    Thread readThread;
    
    
    public static void main(String[] args){
        
       // SerialPort[] s=SerialPort.getCommPorts();
        
        
        JFrame window=new JFrame();
        window.setTitle("Serial Port Chooser");
        window.setSize(600, 400);        
        window.setLayout(new BorderLayout());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        window.setVisible(true);
        
        JComboBox<String> portList=new JComboBox<String>();
        //portList.addItem("shyaka");
        JButton connectButton= new JButton("Connect");
        
        JPanel topPanel=new JPanel();
        topPanel.add(portList);        
        
        topPanel.add(connectButton);
        window.add(topPanel, BorderLayout.NORTH);
        
        connectButton.addActionListener(new ActionListener() {
            private JComboBox JComboBox1;

            @Override
            public void actionPerformed(ActionEvent e) {
                java.util.Enumeration<CommPortIdentifier> portEnum=CommPortIdentifier.getPortIdentifiers();
                int i=0;
                String[] r=new String[5];
                while(portEnum.hasMoreElements() && i < 5){
                    CommPortIdentifier portIdentifier=portEnum.nextElement();
                    r[i]=portIdentifier.getName();
                    i++;              
                }
                JComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(r));
//                JComboBox1.addItem();
            
            }
        });
        
        
          
        
    }
    
}
