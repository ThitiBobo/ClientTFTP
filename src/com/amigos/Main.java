package com.amigos;

import com.amigos.graphical.JFormClientTFTP;
import com.amigos.tftp.TFTPPackage;
import com.sun.prism.j2d.J2DPipeline;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class Main
{

    public static void main(String[] args)
    {

        //test TFTPPackage
        TFTPPackage packet = new TFTPPackage(TFTPPackage.OP_CODE_READ,"file Test",TFTPPackage.MODE_OCTET);
        byte[] data = packet.getByteArray();
        System.out.println("packet: " + Arrays.toString(data));
        String str1 = "file Test";
        try {
            System.out.println(Arrays.toString(str1.getBytes("UTF-8")));
            System.out.println(new String(str1.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        TFTPPackage receive = new TFTPPackage(data);
        System.out.println(receive);
//        TFTPPackage packet = new TFTPPackage(TFTPPackage.OP_CODE_READ,"file Test",TFTPPackage.MODE_OCTET);
//        byte[] data = packet.getByteArray();
//        System.out.println(Arrays.toString(data));
//
//
//        TFTPPackage receive = new TFTPPackage(data);
//        System.out.println(receive);
        JFormClientTFTP form = new JFormClientTFTP();
        form.setVisible(true);
    }
}
