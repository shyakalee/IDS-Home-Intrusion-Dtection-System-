/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ids_project;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
/**
 *
 * @author SHYAKA
 */
public class FrameCaptureMaster extends JFrame{
    

    private class SkipCapture extends AbstractAction {

        public SkipCapture() {
            super("Skip");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }

    private class SnapMeAction extends AbstractAction {

        public SnapMeAction() {
            super("Snap");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }

    private class captureCompleted extends AbstractAction {

        public captureCompleted() {
            super("Completed");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }

    private class saveImage extends AbstractAction {

        public saveImage() {
            super("Save");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }

    private class deleteImage extends AbstractAction {

        public deleteImage() {
            super("Delete");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }

    private class StartAction extends AbstractAction implements Runnable {

        public StartAction() {
            super("Start");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            btStart.setEnabled(false);
            btSnapMe.setEnabled(true);
            executor.execute(this);
        }

        @Override
        public void run() {
            panel.start();
        }
    }

    private Executor executor = Executors.newSingleThreadExecutor();
    private Dimension captureSize = new Dimension(640, 480);
    private Dimension displaySize = new Dimension(640, 480);
    private Webcam webcam = Webcam.getDefault();
    private WebcamPanel panel;

    private JButton btSnapMe = new JButton(new SnapMeAction());
    private JButton btStart = new JButton(new StartAction());
    private JButton btComplete = new JButton(new captureCompleted());
    private JButton btSave = new JButton(new saveImage());
    private JButton btDelete = new JButton(new deleteImage());
    private JButton btSkip = new JButton(new SkipCapture());
    private JComboBox comboBox = new JComboBox();
    
    
    public FrameCaptureMaster() {
        super("Frame");

        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowDeiconified(WindowEvent arg0) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
            }
        });

        List<Webcam> webcams = Webcam.getWebcams();
        for (Webcam webcam : webcams) {
            System.out.println(webcam.getName());
            if (webcam.getName().startsWith("Integrated Webcam 0")) { //or if (webcam.getName().startsWith("USB 2.0 CAMERA 1")) {
                this.webcam = webcam;
                break;
            }
            
        }

        panel = new WebcamPanel(webcam, displaySize, false);
        webcam.setViewSize(captureSize);

        panel.setFPSDisplayed(true);
        panel.setFillArea(true);

        btSnapMe.setEnabled(true);
        btSave.setEnabled(true);
        btDelete.setEnabled(true);

        setLayout(new FlowLayout());

        Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new GridLayout(10, 1));
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(btSnapMe);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(btSave);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(btDelete);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(btComplete);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(btSkip);

        JLabel label1 = new JLabel("Test");
        label1.setText("Bla bla bla");
        JLabel label2 = new JLabel("Test");
        label2.setText(" ");

        Panel captionAndWebcamPanel = new Panel();
        captionAndWebcamPanel.add(label1);
        captionAndWebcamPanel.add(label2);
        captionAndWebcamPanel.add(panel);
        captionAndWebcamPanel.add(label2);
        captionAndWebcamPanel.add(comboBox);
        captionAndWebcamPanel.setLayout(new BoxLayout(captionAndWebcamPanel, BoxLayout.Y_AXIS));

        add(captionAndWebcamPanel);

        add(buttonPanel);
        pack();
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        btStart.doClick();

        setSize(900, 600);
        
        
        
        btSnapMe.addActionListener(new ActionListener() {
                    int count=0;
                        @Override
                    
                    public void actionPerformed(ActionEvent e) {
                        count++;
                        Webcam w=Webcam.getDefault();
                        w.open();
                        try {
                            ImageIO.write(webcam.getImage(),"JPG", new File("C:\\Users\\SHYAKA\\IDS Uploads\\" + "motionIDS_00" + count + ".jpg"));
                        } catch (IOException ex) {
                            Logger.getLogger(WebcamViewer.class.getName()).log(Level.SEVERE, null, ex);
                        }                       
                    }
                });
    }
}
   
