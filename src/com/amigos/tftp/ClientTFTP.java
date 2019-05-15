package com.amigos.tftp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ClientTFTP {

    public static int receiveFile(){
        try {
			DatagramSocket ds= new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        
        
        return 0;
    }

    public static int sendFile(InetAddress IPserv,  int portServ, String nomFichierLocal){
    	
    	TFTPPackage wrq = new TFTPPackage((short) 2, nomFichierLocal, ""); //TO DO modeeee!!!!!!!!!
     	byte[] wrqByte = wrq.getByteArray();
     	
     	TFTPPackage ack = new TFTPPackage((short) portServ);
     	
     	 try {
 			DatagramSocket ds= new DatagramSocket();
 			DatagramPacket dp = new DatagramPacket(wrqByte, wrqByte.length, IPserv, portServ);
 			ds.send(dp);
 			
 			
 			
 			
 			
 			
 			
 			
 		} catch (SocketException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	
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
