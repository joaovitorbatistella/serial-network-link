/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.main;

import com.mycompany.main.OriginThread;
import com.fazecast.jSerialComm.*;
import java.util.logging.Level;
import com.mycompany.main.Framming;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 *
 * @author joaovb
 */
public class HandleSend {
    
    private final String message;
    private final String commOrigin;
    public SerialPort comm;
    public byte origin = 00000001;
    public byte target = 00000010;
    private static final Framming framming = new Framming();
    
    public HandleSend(String txt, String c1)
    {
        this.message = txt;
        this.commOrigin = c1;
    }
    
    public synchronized void startTransaction() 
    {       
        // OriginThread tOrigin = new OriginThread("Origin", this.commOrigin);
        
        this.comm = HandleSend.openSerialConnection(this.commOrigin);
        
        int idx = 0;
        int offset = 0;
        int exit = 0;
        int frameLength = 5;
        try {
            do {
                String toFraming;
                if (exit == 1) {
                    System.out.println("exit");
                    toFraming = this.message.substring(offset, this.message.length());
                    byte[] frame = HandleSend.framming.make(toFraming, this.origin, false);
                    this.send(frame);
                    if (!this.continueOrNot()) {
                        break;
                    }
                    this.finishTransaction();
                    break;
                } else {
                    if (this.message.length() - 1 < frameLength) {
                        toFraming = this.message.substring(offset, this.message.length());
                        byte[] frame = HandleSend.framming.make(toFraming, this.origin, false);
                        this.send(frame);
                        if (!this.continueOrNot()) {
                            break;
                        }
                        this.finishTransaction();
                        break;
                    }

                    toFraming = this.message.substring(offset, offset + frameLength);
                    offset += frameLength;
                    byte[] frame = HandleSend.framming.make(toFraming, this.origin, false);
                    this.send(frame);

                    if (!this.continueOrNot()) {
                        break;
                    }
                    
                    if ((this.message.length() - offset) < this.message.length()) {
                        exit = 1;
                    }

                }

                Thread.sleep(20);
 
                idx++;
            } while (true);
            this.comm.closePort();

        } catch (InterruptedException ex) {
            this.comm.closePort();
            Logger.getLogger(OriginThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private void finishTransaction() {
        System.out.println("finish");
        byte[] frame = HandleSend.framming.make("", this.origin, true);
        this.send(frame);
    }
    
    private boolean continueOrNot() {
        long start = System.nanoTime();
        while (true) {
            if ((System.nanoTime() - start) / 1000000000 >= 15) {
                System.out.println("Timeout 01!");
                return false;
            }

            byte[] readBuffer = new byte[this.comm.bytesAvailable()];
            int numRead = this.comm.readBytes(readBuffer, readBuffer.length);
            if (numRead > 0) {
                return true;
            }
        }
    }
    
    private static SerialPort openSerialConnection(String commPort)
    {
        SerialPort port = SerialPort.getCommPort(commPort);
        port.openPort();
        return port;
    }
    
    public void send(byte[] frame) {
//        byte origin = 00000001;
//        byte target = 00000010;
          System.out.println("frame: " + Arrays.toString(frame));
        this.comm.writeBytes(frame, frame.length);
    }
    
}
