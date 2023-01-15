/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.main;

import com.fazecast.jSerialComm.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joaovb
 */
public class TargetThread  implements Runnable{
    
    private String name;
    private String comm;
    public boolean received = false;
    
    public TargetThread(String name, String comm)
    {
        this.name = name;
        this.comm = comm;
        new Thread(this, this.name).start();
    }

    @Override
    public synchronized void run() {
        System.out.println("Run Thread: " + this.name);
        
        SerialPort commPortTarget = TargetThread.openSerialConnection(this.comm);
        
        
        try {
            this.wait();
                       
            
        } catch (InterruptedException ex) {
            Logger.getLogger(TargetThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static SerialPort openSerialConnection(String commPort)
    {
        SerialPort port = SerialPort.getCommPort(commPort);
        port.openPort();
        return port;
    }
    
}
