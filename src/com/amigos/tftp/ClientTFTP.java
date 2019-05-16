package com.amigos.tftp;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class ClientTFTP {

    public static int receiveFile(String fileName, int port,InetAddress ia ){
    	while(true)
    	{
	        try {
	
	        	FileOutputStream fea= new FileOutputStream ("cheminrelatif"+ fileName);
	        	DatagramSocket ds= new DatagramSocket();
	        	TFTPPackage rrq= new TFTPPackage(TFTPPackage.OP_CODE_READ,fileName,"ecriture"); //mode � changer
		       	byte[] rrqByte = rrq.getByteArray();
	        	DatagramPacket RRQ= new DatagramPacket(rrqByte,rrqByte.length,ia, port);
	        	ds.send(RRQ);
	        	
	        	short numPaquet=1;
	        	byte[] buffer = new byte[516];
	    		DatagramPacket dr = new DatagramPacket(buffer, 516);
	        	do
	        	{
	                ds.receive(dr);
	                TFTPPackage data= new TFTPPackage(dr.getData());
	                data.getIdBlock();
	                //ecriture dans le fichier
	                if(data.getIdBlock()==numPaquet)
	                {
		                fea.write(data.getByteArray(),2,516);
		                sendAcknowledgment(numPaquet,ds,ia,port);
		                numPaquet++;
	                }
	                else
	                {
	                	sendAcknowledgment((short) (numPaquet-1),ds,ia,port);
	                }
	        	}
	        
	        	while(!isLastPacket(dr));
	        	
	        	
	        	ds.setSoTimeout(1000);   // set the timeout in millisecounds.
	
	        	while(true) {        // receive data until timeout
	        		try {
	        			System.out.println("Receiving message...");
	        			ds.receive(dr); // receive the packet
	        			System.out.println("Message received");
	        			sendAcknowledgment((short) (numPaquet),ds,ia,port);
	        		}
	        		catch (SocketTimeoutException e) {
	        			// timeout exception.
	        			System.out.println("Timeout reached!!! " + e);
	        			fea.close();
	        			ds.close();
	        		}
	        	}
	        
	     
	       
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }




        return 0;
    }


    public static int sendFile(InetAddress IPserv,  int portServ, String nomFichierLocal){

    	TFTPPackage wrq = new TFTPPackage((short) 2, nomFichierLocal, ""); //TO DO modeeee!!!!!!!!!
     	byte[] wrqByte = wrq.getByteArray();

     	TFTPPackage ack = new TFTPPackage((short) 0);

     	 try {
 			DatagramSocket ds= new DatagramSocket();
 			DatagramPacket dp = new DatagramPacket(wrqByte, wrqByte.length, IPserv, portServ);
 			ds.send(dp);

 			//byte[] buffer = new byte[128];
    		//DatagramPacket rep = new DatagramPacket(buffer, 128);
    		//ds.receive(rep);


<<<<<<< HEAD
 			byte[] ackByte = new byte[4];
    		DatagramPacket rep = new DatagramPacket(ackByte, ackByte.length);
    		ds.receive(rep);
    		
    		TFTPPackage tp = new TFTPPackage();
    		
    		TFTPPackage data = new TFTPPackage((short) 1, ackByte);
    		byte[] dataByte = data.getByteArray();
    		DatagramPacket dpp = new DatagramPacket(dataByte, dataByte.length, IPserv, portServ);    		
    		ds.send(dpp);
=======
>>>>>>> b5f4aacc6e927119f61d2887658f19071bd89e88







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
       if(dp.getLength()<516) //v�rifier aussi si le paquet n'est pas un code erreur
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

