/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.main;

import com.mycompany.main.OriginThread;
import com.fazecast.jSerialComm.*;
import java.util.logging.Level;
import com.mycompany.main.Framming;
import java.awt.List;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.logging.Logger;
import javax.swing.JTextPane;

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
    public String fileName = "";
    public LinkedHashMap<String,javax.swing.JTextPane> stats;
    private static final Framming framming = new Framming();
    public long start;
    
    public HandleSend(String txt, String c1, String fileName, LinkedHashMap<String,javax.swing.JTextPane> stats)
    {
        this.message = txt;
        this.commOrigin = c1;
        this.fileName = fileName;
        this.stats = stats;
    }
    
    public synchronized void startTransaction() throws InterruptedException 
    {       
        // OriginThread tOrigin = new OriginThread("Origin", this.commOrigin);
        
        this.comm = HandleSend.openSerialConnection(this.commOrigin);
        
        int idx = 0;
        int offset = 0;
        int exit = 0;
        int frameLength = 10;
        this.start = System.nanoTime();
        if(!this.fileName.isEmpty()) {
            byte[] frame = HandleSend.framming.make(this.fileName, this.origin, false, true);
            this.send(frame);
            if (!this.continueOrNot(frame)) {
                return;
            }
            this.start = System.nanoTime();
        }
        
        try {
            do { 
                String toFraming;
                if (exit == 1) {
                    System.out.println("exit");
                    toFraming = this.message.substring(offset, this.message.length());
                    byte[] frame = HandleSend.framming.make(toFraming, this.origin, false, false);
                    this.send(frame);
                    if (!this.continueOrNot(frame)) {
                        break;
                    }
                    this.start = System.nanoTime();
                    this.finishTransaction();
                    break;
                } else {
                    if (this.message.length() - 1 < frameLength) {
                        toFraming = this.message.substring(offset, this.message.length());
                        byte[] frame = HandleSend.framming.make(toFraming, this.origin, false, false);
                        this.send(frame);
                        if (!this.continueOrNot(frame)) {
                            break;
                        }
                        this.start = System.nanoTime();
                        this.finishTransaction();
                        if (!this.continueOrNot(HandleSend.framming.make("", this.origin, false, false))) {
                            break;
                        }
                        break;
                    }

                    toFraming = this.message.substring(offset, offset + frameLength);
                    System.out.println("\n " + offset + " - " + frameLength);
                    offset += frameLength;
                    byte[] frame = HandleSend.framming.make(toFraming, this.origin, false, false);
                    this.send(frame);
                    if (!this.continueOrNot(frame)) {
                        break;
                    }
                    this.start = System.nanoTime();
                    if ((offset + frameLength) >= this.message.length()) {
                        exit = 1;
                    }

                }
                idx++;
            } while (true);
            this.comm.closePort();

        } catch (Exception e) {
            this.comm.closePort();
        }
        JTextPane txtFrames = this.stats.get("frames");
        if(txtFrames == null) {
            return;
        }
        
        txtFrames.setText(String.valueOf(idx+1));
    }
    
    private void finishTransaction() throws InterruptedException {
        System.out.println("finish");
        byte[] frame = HandleSend.framming.make("", this.origin, true, false);
        this.send(frame);
    }

    private boolean continueOrNot(byte[] toRebroadcasting) throws InterruptedException {
        long timer = 100;
        while (true) {
            timer*=1.05;
            if(timer > 100) {
                Thread.sleep(timer);
            }
            if ((System.nanoTime() - this.start) / 1000000000 >= 15) {
                System.out.println("Timeout 01!");
                return false;
            }

            byte[] readBuffer = new byte[this.comm.bytesAvailable()];
            int numRead = this.comm.readBytes(readBuffer, readBuffer.length);

            if (numRead > 0 && readBuffer.length < 15) {
                return true;
            }  else {
                this.send(toRebroadcasting);
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
