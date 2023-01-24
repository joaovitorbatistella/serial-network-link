/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.main;

import com.fazecast.jSerialComm.SerialPort;
import com.mycompany.main.TargetThread;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javax.swing.JOptionPane.showMessageDialog;
import javax.swing.JTextArea;

/**
 *
 * @author joaovb
 */
public class HandleReceive {
    private final String commTarget;
    private SerialPort comm;
    private String message = "";
    public byte origin = 00000001;
    public byte target = 00000010;
    private final JTextArea textAreaTarget;
    private static final Framming framming = new Framming();
    private String fileName = "";
    
    public HandleReceive(String c1, JTextArea textArea)
    {
        this.commTarget = c1;
        this.textAreaTarget = textArea;
    }
    
    public void startReader() throws Exception
    {
        //TargetThread tTarget = new TargetThread("Target", this.commTarget);
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
                
                byte[] readBuffer = new byte[this.comm.bytesAvailable()];
                int numRead = this.comm.readBytes(readBuffer, readBuffer.length);
                
                if(numRead > 0) {
                    if(this.isSpecial(readBuffer) == 1) {
                        this.message += this.getText(readBuffer);
                        this.comm.closePort();
                        
                        this.finish();
                        this.end();
                        return;
                    }
                    
                    if (this.isSpecial(readBuffer) == 2){
                        this.fileName = this.getText(readBuffer);
                    } else {
                        this.message += this.getText(readBuffer);
                    }
                    
                    this.finish();

                    start = System.nanoTime(); 
                }
                
            }
        } catch (InterruptedException e) {
            return;
        } catch (Exception e) {
            return;
        }
        
        this.comm.closePort();
    }
    
    public void end() throws Exception {
        try {
            if(!this.fileName.isEmpty()) {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");  
                LocalDateTime now = LocalDateTime.now();  
                String filePath = "/home/" + System.getProperty("user.name") + "/Downloads/" + now.toString() + "-" + this.fileName;
                File newFile = new File(filePath);
                if (newFile.createNewFile()) {
                    System.out.println("File created: " + newFile.getName());
                    FileWriter fileWriter = new FileWriter(newFile, StandardCharsets.UTF_8);
                    
                    fileWriter.write(this.message);
                    fileWriter.close();
                    showMessageDialog(null, "File created at: " + filePath);
                } else {
                    showMessageDialog(null, "File \"" + newFile.getName() + "\" already exists!");
                    System.out.println("File already exists.");
                }
            } else {
                this.textAreaTarget.setText(this.message);
                this.fileName = "";
        }
        } catch (IOException e) {
            System.out.println("e: " + e.getMessage());
            this.comm.closePort();
            this.fileName = "";
        }
        this.fileName = "";
    }
    
    public void finish () {
        byte[] frame = HandleReceive.framming.make("", this.target, true, false);                    
        this.send(frame);
    }
    
    public void send(byte[] frame) {
        this.comm.writeBytes(frame, frame.length);
    }
    
    private String getText(byte[] frame)
    {
        String message = "";
        if(frame.length>=6) {
            byte[] filteredByteArray = Arrays.copyOfRange(frame, 3, frame.length-3);
            message = new String(filteredByteArray, StandardCharsets.UTF_8);
        }
        
        return message;
    }
    
    private int isSpecial(byte[] frame)
    {
        if(frame.length>=6) {
            byte lastByte = frame[frame.length-1];
            byte firstByte = frame[0];         
            if((char) lastByte == 127) {
                return 1;
            }
            
            if((char) firstByte == 70) {
                return 2;
            }
            
            return 0;
        }
        
        return 0;
    }
        
    private static SerialPort openSerialConnection(String commPort)
    {
        System.out.println("commPort: " + commPort);
        SerialPort port = SerialPort.getCommPort(commPort);
        port.openPort();
        return port;
    }
}   
