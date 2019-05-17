package com.amigos;

import com.amigos.tftp.ClientTFTP;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main
{

    public static void main(String[] args)
    {

        //test sendFile
        try
        {
            ClientTFTP.sendFile(InetAddress.getByName("localhost"), (short) 69, "test.txt", "C:\\Users\\Dorian\\Desktop\\test.txt");

            //ClientTFTP.sendFile(InetAddress.getByName("169.254.169.34"), (short) 69, "test.txt", "/home/thiti/IdeaProjects/ClientTFTP/test.txt");
            //ClientTFTP.receiveFile("DSC_0220.JPG",69,InetAddress.getByName("10.42.145.176"));
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        //test TFTPPackage
//        byte[] b = {1,2,3,4,5,6};
//        TFTPPackage packet = new TFTPPackage(TFTPPackage.OP_CODE_DATA,b);
//        byte[] data = packet.getByteArray();
//        System.out.println("packet: " + Arrays.toString(data));
//        String str1 = "file Test";
//        try {
//            System.out.println(Arrays.toString(str1.getBytes("UTF-8")));
//            System.out.println(new String(str1.getBytes("UTF-8")));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//
//        TFTPPackage receive = new TFTPPackage(data);
//        System.out.println(receive);
////        TFTPPackage packet = new TFTPPackage(TFTPPackage.OP_CODE_READ,"file Test",TFTPPackage.MODE_OCTET);
////        byte[] data = packet.getByteArray();
////        System.out.println(Arrays.toString(data));
////
////
////        TFTPPackage receive = new TFTPPackage(data);
////        System.out.println(receive);
//       JFormClientTFTP form = new JFormClientTFTP();
//        form.setVisible(true);
//    }
//    catch (UnknownHostException ex
//
//
//
//        )
//        {
//            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//    }
    }
}
