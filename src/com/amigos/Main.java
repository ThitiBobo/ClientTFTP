package com.amigos;

import com.amigos.tftp.TFTPPackage;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

        //test TFTPPackage
        TFTPPackage packet = new TFTPPackage(TFTPPackage.OP_CODE_READ,"file Test",TFTPPackage.MODE_OCTET);
        byte[] data = packet.getByteArray();
        System.out.println(Arrays.toString(data));
        

        TFTPPackage receive = new TFTPPackage(data);
        System.out.println(receive);
    }
}
