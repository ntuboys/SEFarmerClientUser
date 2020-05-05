/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author arek
 */
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.json.simple.*;

interface Callback {

  void getData(JSONObject obj);
}

class ServerConnection {

  Socket socket;
  String host;
  int port;
  PrintWriter socketWriter;
  BufferedReader socketReader;
  boolean init = false;
  SwingWorker<Void, Void> loop;
  public boolean authed = false;

  ServerConnection(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public List<Callback> listeners = new ArrayList<Callback>();

  public void gotData(JSONObject obj) {
    for (Callback listener : listeners) {
      listener.getData(obj);
    }
  }

  boolean init() {
    if (init) {
      return false;
    }
    try {
      socket = new Socket(host, port);
      socketWriter = new PrintWriter(socket.getOutputStream(), true);
      socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    } catch (UnknownHostException ex) {
      System.out.println("Server not found: " + ex.getMessage());
    } catch (IOException ex) {
      System.out.println("I/O error: " + ex.getMessage());
    }
    init = true;
    return true;
  }

  boolean auth(String user, String pass) {
    JSONObject toSend = new JSONObject();
    toSend.put("clientType", "user");
    toSend.put("purpose", "auth");
    JSONObject dataToSend = new JSONObject();

    dataToSend.put("username", user);
    dataToSend.put("password", pass);
    toSend.put("data", dataToSend);
    socketWriter.println(toSend);
    try {
      String line = socketReader.readLine();
      JSONObject obj = (JSONObject) JSONValue.parse(line);
      System.out.println(line);
      if (obj.get("purpose").equals("auth")) {
        if (obj.get("result").equals("pass")) {
          startLoop();
          authed = true;
          return true;
        } else {
          return false;
        }
      }
    } catch (IOException ex) {

    }
    return false;
  }

  void startLoop() {
    loop = new SwingWorker<Void, Void>() {
      @Override
      protected Void doInBackground() throws Exception {
        while (init) {
          try {
            String line = socketReader.readLine();
            JSONObject obj = (JSONObject) JSONValue.parse(line);
            gotData(obj);
            System.out.println(line);
          } catch (IOException ex) {
            System.out.println("I/O error occured");
          }
        }
        return null;
      }
    };
    loop.execute();
  }

  void sendData(String data) {
    JSONObject toSend = new JSONObject();
    toSend.put("message", data);
    socketWriter.println(toSend);
  }
}

public class UserGUI extends javax.swing.JFrame implements Callback {

  ServerConnection connection;

  public void getData(JSONObject obj) {
    // order: id, temp, wind, humid, light
    JSONArray arr = (JSONArray) obj.get("data");

    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
    int rowCount = model.getRowCount();
    for (int i = rowCount - 1; i >= 0; i--) {
      model.removeRow(i);
    }
    for (int i = 0; i < arr.size(); i++) {
      String id, temp, wind, humid, light;
      JSONObject ele = (JSONObject) arr.get(i);
      id = ele.get("id").toString();
      temp = ele.get("temp").toString();
      wind = ele.get("wind").toString();
      humid = ele.get("humidity").toString();
      light = ele.get("humidity").toString();
      model.addRow(new Object[]{id, temp, wind, humid, light});
    }

  }

  /**
   * Creates new form UserGUI
   */
  public UserGUI() {
    initComponents();
    JTextField usernameField = new JTextField(5);
    JTextField passwordField = new JTextField(5);

    JPanel myPanel = new JPanel();
    myPanel.add(new JLabel("Username: "));
    myPanel.add(usernameField);// a spacer
    myPanel.add(new JLabel("Password: "));
    myPanel.add(passwordField);

    int result = JOptionPane.showConfirmDialog(null, myPanel,
            "Login", JOptionPane.OK_CANCEL_OPTION);
    if (!(result == JOptionPane.OK_OPTION)) {
      System.exit(1);
    } else {
      connection = new ServerConnection("localhost", 1111);
      if (connection.init()) {
        System.out.println("init passed");
      }
      if (!connection.auth(usernameField.getText(), passwordField.getText())) {
        JPanel authFailPanel = new JPanel();
        authFailPanel.add(new JLabel("Auth Failed"));
        JOptionPane.showConfirmDialog(null, authFailPanel, "Error", JOptionPane.DEFAULT_OPTION);
        System.exit(1);
      } else {
        System.out.println("auth successful");
        connection.listeners.add(this);
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

    jScrollPane1 = new javax.swing.JScrollPane();
    jTable1 = new javax.swing.JTable();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

    jTable1.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {
        "ID", "Temp", "Wind", "Humidity", "Light"
      }
    ));
    jScrollPane1.setViewportView(jTable1);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 741, Short.MAX_VALUE)
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE)
        .addContainerGap())
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    /* Set the Nimbus look and feel */
    //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
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
      java.util.logging.Logger.getLogger(UserGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (InstantiationException ex) {
      java.util.logging.Logger.getLogger(UserGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      java.util.logging.Logger.getLogger(UserGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (javax.swing.UnsupportedLookAndFeelException ex) {
      java.util.logging.Logger.getLogger(UserGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    //</editor-fold>

    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        new UserGUI().setVisible(true);
      }
    });
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JTable jTable1;
  // End of variables declaration//GEN-END:variables
}
