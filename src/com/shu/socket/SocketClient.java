/**
 * @author Shu
 * @date 17th June 2017
 * @version 1.0
 * This is a client socket class
 * @parameter
 * @since
 * @return
 */

package com.shu.socket;

import com.shu.ui.ChatFrame;
import java.io.*;
import java.net.*;
import java.util.Date;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class SocketClient implements Runnable {
	
	//TODO the code below has a GUI framework
    public int port;
    public String serverAddr;
    public Socket socket;
    public ChatFrame ui;
    public ObjectInputStream In;
    public ObjectOutputStream Out;

    public SocketClient(ChatFrame frame) throws IOException {
        ui = frame; this.serverAddr = ui.serverAddr; this.port = ui.port;
        socket = new Socket(InetAddress.getByName(serverAddr), port);
        Out = new ObjectOutputStream(socket.getOutputStream());
        Out.flush();
        In = new ObjectInputStream(socket.getInputStream());        
    }

    @Override
	// * Messenger first connects to the Server, specified by its IP-address and port number. 
	// * Arriving messages are then displayed on message board along with their senders.
	// * When a user wants to send a file, first his request is sent via a message of type upload_req.
	// * The recipient then does the following:
	// * 	1. The recipient side sends its reply in a message of type upload_res
	// * 	2. If request is accepted then the recipient opens a new port
	// * 	3. For positive reply, recipient's IP address and port number is sent back
	// * 	4. The sender, on receiving positive reply connects to this socket and starts file upload
	// * An advantage of this approach is that the clients can chat and transfer files at the same time. Unlike messages, files do not go through jServer.
    public void run() {
        boolean keepRunning = true;
        while(keepRunning) {
            try {
                Message msg = (Message) In.readObject();
                System.out.println("Incoming : "+msg.toString());
                
                if(msg.type.equals("message")) {
                    if(msg.recipient.equals(ui.username)) {
                        ui.jTextArea1.append("["+msg.sender +" > Me] : " + msg.content + "\n");
                    }
                    else {
                        ui.jTextArea1.append("["+ msg.sender +" > "+ msg.recipient +"] : " + msg.content + "\n");
                    }                                            
                    if(!msg.content.equals(".bye") && !msg.sender.equals(ui.username)) {
                        String msgTime = (new Date()).toString();
                    }
                }
                
                // login with username and password
                else if(msg.type.equals("login")) {
                    if(msg.content.equals("TRUE")) {
                        ui.jButton2.setEnabled(false); ui.jButton3.setEnabled(false);
                        ui.jButton4.setEnabled(true); ui.jButton5.setEnabled(true);
                        ui.jTextArea1.append("[SERVER > Me] : Login Successful\n");
                        ui.jTextField3.setEnabled(false); ui.jPasswordField1.setEnabled(false);
                    }
                    else {
                        ui.jTextArea1.append("[SERVER > Me] : Login Failed\n");
                    }
                }
                
                else if(msg.type.equals("test")) {
                    ui.jButton1.setEnabled(false);
                    ui.jButton2.setEnabled(true); ui.jButton3.setEnabled(true);
                    ui.jTextField3.setEnabled(true); ui.jPasswordField1.setEnabled(true);
                    ui.jTextField1.setEditable(false); ui.jTextField2.setEditable(false);
                    ui.jButton7.setEnabled(true);
                }
                
            	// * The jList1 loaded with active users
            	// * When a new user signs-in,
                // * his information is broadcast to all other user via a message.
                // * This message has type newuser and its content is the username of signed-in user.                
                else if(msg.type.equals("newuser")) {
                    if(!msg.content.equals(ui.username)) { // Checking whether the incoming username not list in Database.xml
                        boolean exists = false;
                        for(int i = 0; i < ui.model.getSize(); i++) {
                            if(ui.model.getElementAt(i).equals(msg.content)) {
                                exists = true; break;// checking whether incoming username is already in jList1
                            }
                        }
                        if(!exists) {ui.model.addElement(msg.content);}// If everything alright then add the jList1
                    }
                }
                
                // sign up with new username and password
                // the data will store into Data.xml
                else if(msg.type.equals("signup")) {
                    if(msg.content.equals("TRUE")) {
                        ui.jButton2.setEnabled(false); ui.jButton3.setEnabled(false);
                        ui.jButton4.setEnabled(true); ui.jButton5.setEnabled(true);
                        ui.jTextArea1.append("[SERVER > Me] : Singup Successful\n");
                    }
                    else {
                        ui.jTextArea1.append("[SERVER > Me] : Signup Failed\n");
                    }
                }
                
                else if(msg.type.equals("signout")) {
                    if(msg.content.equals(ui.username)) {
                        ui.jTextArea1.append("["+ msg.sender +" > Me] : Bye\n");
                        ui.jButton1.setEnabled(true); ui.jButton4.setEnabled(false); 
                        ui.jTextField1.setEditable(true); ui.jTextField2.setEditable(true);
                        for(int i = 1; i < ui.model.size(); i++) {
                            ui.model.removeElementAt(i);
                        }
                        // handle a thread from a client
                        ui.clientThread.stop();
                    }
                    else {
                        ui.model.removeElement(msg.content);
                        ui.jTextArea1.append("["+ msg.sender +" > All] : "+ msg.content +" has signed out\n");
                    }
                }
                
                else if(msg.type.equals("upload_req")) {
                    
                    if(JOptionPane.showConfirmDialog(ui, ("Accept '"+msg.content+"' from "+msg.sender+" ?")) == 0){
                        
                        JFileChooser jf = new JFileChooser();
                        jf.setSelectedFile(new File(msg.content));
                        int returnVal = jf.showSaveDialog(ui);
                       
                        String saveTo = jf.getSelectedFile().getPath();
                        if(saveTo != null && returnVal == JFileChooser.APPROVE_OPTION){
                        	// On recipient side, start a new thread for download
                            Download dwn = new Download(saveTo, ui);
                            Thread t = new Thread(dwn);
                            t.start();
                            send(new Message("upload_res", ui.username, (""+dwn.port), msg.sender));
                        }
                        else{
                        	// Reply to sender with IP address and port number
                            send(new Message("upload_res", ui.username, "NO", msg.sender));
                        }
                    }
                    else{
                    	// On sender side, start a new thread for file upload
                        send(new Message("upload_res", ui.username, "NO", msg.sender));
                    }
                }
                else if(msg.type.equals("upload_res")){
                    if(!msg.content.equals("NO")){
                        int port  = Integer.parseInt(msg.content);
                        String addr = msg.sender;
                        
                        ui.jButton5.setEnabled(false); ui.jButton6.setEnabled(false);
                        Upload upl = new Upload(addr, port, ui.file, ui);
                        Thread t = new Thread(upl);
                        t.start();
                    }
                    else{
                        ui.jTextArea1.append("[SERVER > Me] : "+msg.sender+" rejected file request\n");
                    }
                }
                else{
                    ui.jTextArea1.append("[SERVER > Me] : Unknown message type\n");
                }
            }
            catch(Exception ex) {
                keepRunning = false;
                ui.jTextArea1.append("[Application > Me] : Connection Failure\n");
                ui.jButton1.setEnabled(true); ui.jTextField1.setEditable(true); ui.jTextField2.setEditable(true);
                ui.jButton4.setEnabled(false); ui.jButton5.setEnabled(false); ui.jButton5.setEnabled(false);
                
                for(int i = 1; i < ui.model.size(); i++) {
                    ui.model.removeElementAt(i);
                }
                ui.clientThread.stop();
                System.out.println("Exception SocketClient run()");
                ex.printStackTrace();
            }
        }
    }
    
    // send message
    public void send(Message msg) {
        try {
            Out.writeObject(msg);
            Out.flush();
            System.out.println("Outgoing : "+msg.toString());
            
            if(msg.type.equals("message") && !msg.content.equals(".bye")) {
                String msgTime = (new Date()).toString();
            }
        }
        catch (IOException ex) {
            System.out.println("Exception SocketClient send()");
        }
    }

    // close thread
    public void closeThread(Thread t) {
        t = null;
    }
}
