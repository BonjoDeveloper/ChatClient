/**
 * @author Shu
 * @date 17th June 2017
 * @version 1.0
 * File upload function
 * Client can upload the file and send to any other chosen client
 * @parameter
 * @since
 * @return
 */

// * For each upload a client-server pair is established
// * between both parties so that file need not be sent through server.
// * Upload to all is forbidden because it will overload the client
// * and also individual user may or may not want to download your file. 

package com.shu.socket;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.shu.ui.ChatFrame;

public class Upload implements Runnable{

    public String addr;
    public int port;
    public Socket socket;
    public FileInputStream In;
    public OutputStream Out;
    public File file;
    public ChatFrame ui;
    
    public Upload(String addr, int port, File filepath, ChatFrame frame) {
    	
        super();
        try {
            file = filepath; ui = frame;
            socket = new Socket(InetAddress.getByName(addr), port);
            Out = socket.getOutputStream();
            In = new FileInputStream(filepath);
        } 
        catch (Exception ex) {
            System.out.println("Exception [Upload : Upload(...)]");
        }
    }
    
    @Override
    public void run() {
    	
        try {       
            byte[] buffer = new byte[1024];
            int count;
            while((count = In.read(buffer)) >= 0){
                Out.write(buffer, 0, count);
            }
            Out.flush();
            ui.jTextArea1.append("[Applcation > Me] : File upload complete\n");
            ui.jButton5.setEnabled(true); ui.jButton6.setEnabled(true);
            ui.jTextField5.setVisible(true);
            if(In != null){ In.close(); }
            if(Out != null){ Out.close(); }
            if(socket != null){ socket.close(); }
        }
        catch (Exception ex) {
            System.out.println("Exception [Upload : run()]");
            ex.printStackTrace();
        }
    }
}
