/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.main;

import com.fazecast.jSerialComm.*;

/**
 *
 * @author joaovb
 */
public class OriginThread  implements Runnable{
    
    private String name;
    private String commName;
    private SerialPort comm;
    
    public OriginThread(String name, String comm)
    {
        this.name = name;
        this.commName = comm;
        
        new Thread(this, this.name).start();
    }

    @Override
    public synchronized void run() {
        System.out.println("Run Thread: " + this.name);
        this.comm = OriginThread.openSerialConnection(this.commName);

        
    }
    
    public void send(byte[] frame) {
//        byte origin = 00000001;
//        byte target = 00000010;
        
        this.comm.writeBytes(frame, frame.length);
    }
    
    private static SerialPort openSerialConnection(String commPort)
    {
        SerialPort port = SerialPort.getCommPort(commPort);
        port.openPort();
        return port;
    }

}
