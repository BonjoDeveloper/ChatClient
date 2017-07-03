/**
 * @author Shu
 * @date 17th June 2017
 * @version 1.0
 * @parameter
 * @since
 * @return
 */

// * Messenger first connects to the jServer
// * specified by its IP-address and port number.
// * Arriving messages are then displayed on message board along with their senders.


package com.shu.socket;

import java.io.Serializable;

public class Message implements Serializable {
    public String type, sender, content, recipient;    
    public Message(String type, String sender, String content, String recipient) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.recipient = recipient;
    }
    
    @Override
    public String toString() {    	
        return "{type='"+type+"', sender='"+sender+"', content='"+content+"', recipient='"+recipient+"'}";
    }
}
