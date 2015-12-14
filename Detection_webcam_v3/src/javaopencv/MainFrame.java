package javaopencv;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG;

public class MainFrame extends javax.swing.JFrame
{
  static
  {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
  }

  private Boolean begin = false;
  private Boolean firstFrame = true;
  private VideoCapture video = null;
  private CaptureThread thread = null;
  private MatOfByte matOfByte = new MatOfByte();
  private BufferedImage bufImage = null;
  private InputStream in;
  private Mat frameaux = new Mat();
  private Mat frame = new Mat(240, 320, CvType.CV_8UC3);
  private Mat lastFrame = new Mat(240, 320, CvType.CV_8UC3);
  private Mat currentFrame = new Mat(240, 320, CvType.CV_8UC3);
  private Mat processedFrame = new Mat(240, 320, CvType.CV_8UC3);
  private ImagePanel image;
  private BackgroundSubtractorMOG bsMOG = new BackgroundSubtractorMOG();
  private int savedelay = 0;
  String currentDir = "";
  String detectionsDir = "detections";
  private Object jTextField1;
  
  public Rect rectA = new Rect();
  public Rect rectB = new Rect();
  public Rect rectC = new Rect();
  public Rect rectD = new Rect();
  public static int rectWidth = 80;
  public static int rectHeight = 60;
  
  public int cptRect=0;
  public int numeroRect[] = new int[3];
  
  public MainFrame()
  {
    initComponents();
    image = new ImagePanel(new ImageIcon("figs/320x240.gif").getImage());
    jPanelSource1.add(image, BorderLayout.CENTER);
    
  }

  private void start()
  {
    if(!begin)
    {
      int sourcen = Integer.parseInt(jTextFieldSource1.getText());
      System.out.println("Opening source: " + sourcen);

      video = new VideoCapture(sourcen);

      if(video.isOpened())
      {
        thread = new CaptureThread();
        thread.start();
        begin = true;
        firstFrame = true;
      }
    }
  }
  
  public void reinitialise() {
      this.cptRect = 0;
  }

  private void stop()
  {
    if(begin) {
      try{
        Thread.sleep(500);
      } catch(Exception ex){
      }
      video.release();
      begin = false;
    }
  }

  public static String getCurrentTimeStamp()
  {
    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");//dd/MM/yyyy
    Date now = new Date();
    String strDate = sdfDate.format(now);
    return strDate;
  }

  int findBiggestContour(List<MatOfPoint> contours) 
    {
        int idx = -1;
        int cNum = 0;

        for (int i = 0; i < contours.size(); i++)
        {
                int curNum = contours.get(i).toList().size();
                if (curNum > cNum) {
                        idx = i;
                        cNum = curNum;
                }
        }		
        return idx;
    }
  
  
  public boolean isInRectangle(Rect source, Point a) {
      Point debut = new Point (source.x,source.y);
      Point fin = new Point ((source.x + source.width),(source.y + source.height));
      
      if ( (a.x > debut.x) && (a.x < fin.x) && (a.y > debut.y) && (a.y < fin.y) ) {
          return true;
      } else return false;
  }
  
  public void  detection_contours(Mat inmat, Mat outmat)
  {
    Mat v = new Mat();
    Mat vv = outmat.clone();
    List<MatOfPoint> contours = new ArrayList();
    MatOfInt hullI = new MatOfInt();
    List<MatOfPoint> hullP = new ArrayList<MatOfPoint>();
    MatOfInt4 defects = new MatOfInt4();
    Rect boundingRect = null;
    double maxArea = 100;
    int maxAreaIdx;
    Rect r;
    ArrayList<Rect> rect_array = new ArrayList();
    
    // Trouve tous les contours
    Imgproc.findContours(vv, contours, v, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
    int key = findBiggestContour(contours);
    if (key!= 0){
        Imgproc.drawContours(inmat, contours, key, new Scalar(0, 0, 255)); 
    }
        
    for(int idx = 0; idx < contours.size(); idx++)
    {
      Mat contour = contours.get(idx);
      double contourarea = Imgproc.contourArea(contour);
      if(contourarea > maxArea)
      {
        // maxArea = contourarea;
        maxAreaIdx = idx;
        r = Imgproc.boundingRect(contours.get(maxAreaIdx));
        rect_array.add(r);
        Imgproc.drawContours(inmat, contours, maxAreaIdx, new Scalar(0, 0, 255));
      }
    }
    
    // Dessine les rectangle autour des contours
    if(rect_array.size() > 0) {
      Iterator<Rect> it2 = rect_array.iterator();
      while(it2.hasNext()){
        Rect obj = it2.next();
        Core.rectangle(currentFrame, obj.br(), obj.tl(),
          new Scalar(0, 255, 0), 1);
      }
    }

   
    Imgproc.convexHull(contours.get(key), hullI, false);

    if (hullI != null) {
        hullP.clear();
        for (int i = 0; contours.size() >= i; i++)
        hullP.add(new MatOfPoint());

    int[] cId = hullI.toArray();
    List<Point> lp = new ArrayList<Point>();
    Point[] contourPts = contours.get(key).toArray();
    
    
    int findRectA = 0;
    int findRectB = 0;
    int findRectC = 0;
    int findRectD = 0;
    
    
    
    for (int i = 0; i < cId.length; i++)
        {
            lp.add(contourPts[cId[i]]);
            Core.circle(inmat, contourPts[cId[i]], 2, new Scalar(241, 247, 45), -3);

            if (isInRectangle(rectA,contourPts[cId[i]])) 
                findRectA++;
            
            if (isInRectangle(rectB,contourPts[cId[i]])) 
                findRectB++;
            
            if (isInRectangle(rectC,contourPts[cId[i]])) 
                findRectC++;
            
            if (isInRectangle(rectD,contourPts[cId[i]])) 
                findRectD++;
    }

    // TODO : Ajout
    
    if (findRectA>=5) {
        if (cptRect==0) {
            numeroRect[cptRect] = 1;
            cptRect++;
            System.out.println("Haut gauche"); 
        } else {
            if (numeroRect[cptRect-1] != 1) {
                numeroRect[cptRect] = 1;
                cptRect++;
                System.out.println("Haut gauche"); 
           }    
        }
    }
    
    if (findRectB>=5) {
        if (cptRect==0) {
            numeroRect[cptRect] = 2;
            cptRect++;
            System.out.println("Bas gauche"); 
        } else {
            if (numeroRect[cptRect-1] != 2) {
                numeroRect[cptRect] = 2;
                cptRect++;
                System.out.println("Bas gauche"); 
           }    
        }
    }
    
    if (findRectC>=5) {
        if (cptRect==0) {
            numeroRect[cptRect] = 3;
            cptRect++;
            System.out.println("Haut droite"); 
        } else {
            if (numeroRect[cptRect-1] != 3) {
                numeroRect[cptRect] = 3;
                cptRect++;
                System.out.println("Haut droite"); 
           }    
        }
    }
    
    if (findRectD>=5) {
        if (cptRect==0) {
            numeroRect[cptRect] = 4;
            cptRect++;
            System.out.println("Bas droite"); 
        } else {
            if (numeroRect[cptRect-1] != 4) {
                numeroRect[cptRect] = 4;
                cptRect++;
                 System.out.println("Bas droite"); 
           }    
        }
    }
    
    
    
    // Si on a sélectionné 3 fenètres
    if (cptRect == 3) {
       if ((numeroRect[0] == 1) && (numeroRect[1] == 4) && (numeroRect[2] == 2))
          this.jTextField2.setText("Authenticated");
       
       cptRect=0;
    }


    //hg.hullP.get(hg.cMaxId) returns the locations of the points in the convex hull of the hand
    hullP.get(key).fromList(lp);
    lp.clear();

    }
         
       
    
  }
  
  public void initialiseRectangle() {
    rectA.x = 10;     rectA.y = 10;
    rectB.x = 10;     rectB.y = 170;
    rectC.x = 230;     rectC.y = 10;
    rectD.x = 230;     rectD.y = 170;

    rectA.height = MainFrame.rectHeight;
    rectB.height = MainFrame.rectHeight;
    rectC.height = MainFrame.rectHeight;
    rectD.height = MainFrame.rectHeight;

    rectA.width = MainFrame.rectWidth;
    rectB.width = MainFrame.rectWidth;
    rectC.width = MainFrame.rectWidth;
    rectD.width = MainFrame.rectWidth;
  }
    
  class CaptureThread extends Thread
  {
    @Override
    public void run()
    {
      cptRect = 0;
      if(video.isOpened())
      {
        while(begin == true)
        {
            //video.read(frameaux);
            video.retrieve(frameaux);
            Imgproc.resize(frameaux, frame, frame.size());
            frame.copyTo(currentFrame);
            
            initialiseRectangle();
          
          
          /* a.x = 10;
           a.y = 10;
           a.height = 80;
           a.width = 60; */
            
        
          if(jCheckBoxMotionDetection.isSelected())
          {
            if(firstFrame)
          {
            frame.copyTo(lastFrame);
            firstFrame = false;
            continue;
          }
            
            Imgproc.GaussianBlur(currentFrame, currentFrame, new Size(3, 3), 0);
            Imgproc.GaussianBlur(lastFrame, lastFrame, new Size(3, 3), 0);
            
            //bsMOG.apply(frame, processedFrame, 0.005);
            Core.subtract(currentFrame, lastFrame, processedFrame);
            //Core.absdiff(frame,lastFrame,processedFrame);
            
            Imgproc.cvtColor(processedFrame, processedFrame, Imgproc.COLOR_RGB2GRAY);
            //
            
            int threshold = jSliderThreshold.getValue();
            //Imgproc.adaptiveThreshold(processedFrame, processedFrame, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 5, 2);
            Imgproc.threshold(processedFrame, processedFrame, threshold, 255, Imgproc.THRESH_BINARY);

            
           
             detection_contours(currentFrame, processedFrame);

            } 
           
            //currentFrame.copyTo(processedFrame);
           
          
          
        // Dessine les rectangles d'authentifications       
        Core.rectangle(currentFrame, rectA.br(), rectA.tl(), new Scalar(0, 255, 255), 2); 
        Core.rectangle(currentFrame, rectB.br(), rectB.tl(), new Scalar(0, 255, 0), 2); 
        Core.rectangle(currentFrame, rectC.br(), rectC.tl(), new Scalar(255, 255, 0), 2); 
        Core.rectangle(currentFrame, rectD.br(), rectD.tl(), new Scalar(255, 0, 0), 2); 
        
        currentFrame.copyTo(processedFrame);
    
          Highgui.imencode(".jpg", processedFrame, matOfByte);
          byte[] byteArray = matOfByte.toArray();

          try
          {
            in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
          }
          catch(Exception ex)
          {
            ex.printStackTrace();
          }

          //image.updateImage(new ImageIcon("figs/lena.png").getImage());
          image.updateImage(bufImage);

          

          try
          {
            Thread.sleep(1);
          }
          catch(Exception ex)
          {
          }
        }
      }
    }
  }


  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelSource1 = new javax.swing.JPanel();
        jLabelSource1 = new javax.swing.JLabel();
        jTextFieldSource1 = new javax.swing.JTextField();
        jButtonStart = new javax.swing.JButton();
        jButtonStop = new javax.swing.JButton();
        jCheckBoxMotionDetection = new javax.swing.JCheckBox();
        jSliderThreshold = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Java OpenCV Webcam");

        jPanelSource1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout jPanelSource1Layout = new javax.swing.GroupLayout(jPanelSource1);
        jPanelSource1.setLayout(jPanelSource1Layout);
        jPanelSource1Layout.setHorizontalGroup(
            jPanelSource1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 318, Short.MAX_VALUE)
        );
        jPanelSource1Layout.setVerticalGroup(
            jPanelSource1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 238, Short.MAX_VALUE)
        );

        jLabelSource1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelSource1.setText("Source 1:");

        jTextFieldSource1.setText("0");

        jButtonStart.setText("Start");
        jButtonStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStartActionPerformed(evt);
            }
        });

        jButtonStop.setText("Stop");
        jButtonStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStopActionPerformed(evt);
            }
        });

        jCheckBoxMotionDetection.setText("Motion Detection");

        jSliderThreshold.setMaximum(255);
        jSliderThreshold.setPaintLabels(true);
        jSliderThreshold.setPaintTicks(true);
        jSliderThreshold.setValue(15);

        jLabel1.setText("Threshold:");

        jLabel2.setText("(zero for local webcamera)");

        jTextField2.setEditable(false);
        jTextField2.setText("not authenticated");
        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        jButton1.setText("Reinitialize");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jCheckBoxMotionDetection)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSliderThreshold, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jPanelSource1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabelSource1)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jTextFieldSource1, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel2)))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(63, 63, 63)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButtonStart, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonStop, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(27, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelSource1)
                    .addComponent(jTextFieldSource1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelSource1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSliderThreshold, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jCheckBoxMotionDetection)
                        .addComponent(jLabel1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonStart)
                    .addComponent(jButtonStop))
                .addGap(36, 36, 36)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonStartActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonStartActionPerformed
    {//GEN-HEADEREND:event_jButtonStartActionPerformed
      start();
    }//GEN-LAST:event_jButtonStartActionPerformed

    private void jButtonStopActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonStopActionPerformed
    {//GEN-HEADEREND:event_jButtonStopActionPerformed
      stop();
    }//GEN-LAST:event_jButtonStopActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
       reinitialise();
    }//GEN-LAST:event_jButton1ActionPerformed

  public static void main(String args[])
  {
    /* Set the Nimbus look and feel */
    //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
     * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
     */
    try
    {
      for(javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
      {
        if("Windows".equals(info.getName()))
        {
          javax.swing.UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    }
    catch(ClassNotFoundException ex)
    {
      java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    catch(InstantiationException ex)
    {
      java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    catch(IllegalAccessException ex)
    {
      java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    catch(javax.swing.UnsupportedLookAndFeelException ex)
    {
      java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    //</editor-fold>

    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable()
    {
      public void run()
      {
        MainFrame mainFrame = new MainFrame();
        mainFrame.setVisible(true);
        mainFrame.setLocationRelativeTo(null);
      }
    });
  }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonStart;
    private javax.swing.JButton jButtonStop;
    private javax.swing.JCheckBox jCheckBoxMotionDetection;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelSource1;
    private javax.swing.JPanel jPanelSource1;
    private javax.swing.JSlider jSliderThreshold;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextFieldSource1;
    // End of variables declaration//GEN-END:variables
}
