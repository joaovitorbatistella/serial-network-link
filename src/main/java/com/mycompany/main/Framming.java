/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.main;

import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 *
 * @author joaovb
 */
public class Framming {
    
    public byte[] make(String content, byte address, boolean finish)
    {
        //char scapeCharacter = '*';
        //char startTextCharacter = '~';
        //char endTextCharacter = '|';joao vitor batistella
        byte startEndFrame = 126;
        byte ack = 1;
        byte endFrameWhenFinish = 127;

        
        String data;
        //data = scapeCharacter + startTextCharacter + content + scapeCharacter + endTextCharacter;
        data = content;
        
        String hexCRC = CRC16CCITT.main(data);
        byte[] crc = Framming.hexStringToByte(hexCRC);
        
//        System.out.println("\nstartEndFrame: " + startEndFrame);
//        System.out.println("\naddress: " + address);
//        System.out.println("\nack: " + ack);
//        System.out.println("\ndata: " + data);
//        System.out.println("\ncrc[0]: " + crc[0]);
//        System.out.println("\ncrc[1]: " + crc[1]);
//        System.out.println("\nstartEndFrame: " + startEndFrame +"\n\n");
        
        byte[] preFrame = {startEndFrame, address, ack};
        byte[] dataFrame = data.getBytes();
        byte[] postFrame = {crc[0], crc[1], ((finish == true) ? endFrameWhenFinish : startEndFrame)};
        
        return ByteBuffer.allocate(preFrame.length + dataFrame.length + postFrame.length)
                .put(preFrame)
                .put(dataFrame)
                .put(postFrame)
                .array();
    }
        
    private static byte[] hexStringToByte (String hex)
    {
        int val = Integer.parseInt(hex, 16);

        BigInteger big = BigInteger.valueOf(val);
        byte[] ans = (big.toByteArray());
        return ans;
    }
}
