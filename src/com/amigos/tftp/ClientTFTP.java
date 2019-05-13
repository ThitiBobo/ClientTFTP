package com.amigos.tftp;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ClientTFTP {

    public static int receiveFile(String fileName, int port,InetAddress ia ){
        try {
        	FileOutputStream fea= new FileOutputStream ("cheminrelatif"+ fileName); 
        	DatagramSocket ds= new DatagramSocket();
        	TFTPPackage rrq=new TFTPPackage(fileName,""); //mode à changer
	       	byte[] rrqByte = rrq.getByteArray();
        	DatagramPacket RRQ= new DatagramPacket(rrqByte,rrqByte.length,ia, port);
        	ds.send(RRQ);
        	
        	//receive data 
        	//fea.write(data1,2,10); 
        	//fea.close();  
			
			
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public static int sendFile(){
       return 0;
    }

    private static boolean isLastPacket(DatagramPacket dp){
       if(dp.getLength()<516) //vérifier aussi si le paquet n'est pas un code erreur
       {
    	   return true;
       }
       else
       {
    	   return false;
       }
    }

    private static void sendAcknowledgment(short idBlock, DatagramSocket ds,InetAddress ia, int port){
       
		try {
			 	TFTPPackage ack=new TFTPPackage(idBlock);
		       	byte[] ackByte = ack.getByteArray();		
				DatagramPacket dp = new DatagramPacket(ackByte, ackByte.length, ia, port);
				ds.send(dp);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
		}
		
        
       
    }




}
