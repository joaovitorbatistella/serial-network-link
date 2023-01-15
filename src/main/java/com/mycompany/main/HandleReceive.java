/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.main;

import com.fazecast.jSerialComm.SerialPort;
import com.mycompany.main.TargetThread;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

/**
 *
 * @author joaovb
 */
public class HandleReceive {
    private final String commTarget;
    private SerialPort comm;
    private String message = "";
    private final JTextArea textAreaTarget;
    private static final Framming framming = new Framming();
    
    public HandleReceive(String c1, JTextArea textArea)
    {
        this.commTarget = c1;
        this.textAreaTarget = textArea;
    }
    
    public void startReader()
    {
        //TargetThread tTarget = new TargetThread("Target", this.commTarget);
        byte target = 00000010;
        this.comm = HandleReceive.openSerialConnection(this.commTarget);
//        commPortTarget.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
        this.comm.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 15, 0);
        try {
            long start = System.nanoTime();
            while (true) {
                if((System.nanoTime() - start)/1000000000 >= 15) { 
                    System.out.println("Timeout 02!");
                    break;
                }
                
                if(this.comm.bytesAvailable() == 0){
                    Thread.sleep(20);
                }
                
                byte[] readBuffer = new byte[this.comm.bytesAvailable()];
                int numRead = this.comm.readBytes(readBuffer, readBuffer.length);
                
                if(numRead > 0) {
                    this.message += this.getText(readBuffer);

                    byte[] frame = HandleReceive.framming.make("", target, true);                    
                    this.send(frame);

                    start = System.nanoTime(); 
                }
                
            }
        } catch (InterruptedException e) {
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.textAreaTarget.setText(this.message);
        this.comm.closePort();
    }
    
    public void send(byte[] frame) {
        this.comm.writeBytes(frame, frame.length);
    }
    
    private String getText(byte[] frame)
    {
        byte[] filteredByteArray = Arrays.copyOfRange(frame, 3, frame.length-3);
                       
        String message = new String(filteredByteArray, StandardCharsets.UTF_8);
        return message;
    }
        
    private static SerialPort openSerialConnection(String commPort)
    {
        SerialPort port = SerialPort.getCommPort(commPort);
        port.openPort();
        return port;
    }
}   
