package com.amigos.tftp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class ClientTFTP
{

    public static int receiveFile(String fileName, int port, InetAddress ia)
    {
        while (true)
        {
            try
            {

                FileOutputStream fea = new FileOutputStream("cheminrelatif" + fileName);
                DatagramSocket ds = new DatagramSocket();
                TFTPPackage rrq = new TFTPPackage(TFTPPackage.OP_CODE_READ, fileName, "ecriture"); //mode � changer
                byte[] rrqByte = rrq.getByteArray();
                DatagramPacket RRQ = new DatagramPacket(rrqByte, rrqByte.length, ia, port);
                ds.send(RRQ);

                short numPaquet = 1;
                byte[] buffer = new byte[516];
                DatagramPacket dr = new DatagramPacket(buffer, 516);
                do
                {
                    ds.receive(dr);
                    TFTPPackage data = new TFTPPackage(dr.getData());
                    data.getIdBlock();
                    //ecriture dans le fichier
                    if (data.getIdBlock() == numPaquet)
                    {
                        fea.write(data.getByteArray(), 2, 516);
                        sendAcknowledgment(numPaquet, ds, ia, port);
                        numPaquet++;
                    }
                    else
                    {
                        sendAcknowledgment((short) (numPaquet - 1), ds, ia, port);
                    }
                }

                while (!isLastPacket(dr));

                ds.setSoTimeout(1000);   // set the timeout in millisecounds.

                while (true)
                {        // receive data until timeout
                    try
                    {
                        System.out.println("Receiving message...");
                        ds.receive(dr); // receive the packet
                        System.out.println("Message received");
                        sendAcknowledgment((short) (numPaquet), ds, ia, port);
                    }
                    catch (SocketTimeoutException e)
                    {
                        // timeout exception.
                        System.out.println("Timeout reached!!! " + e);
                        fea.close();
                        ds.close();
                    }
                }

            }
            catch (SocketException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (FileNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return 0;

        }
    }

    public static int sendFile(InetAddress IPserv, short portServ, String pathFichierLocal) throws FileNotFoundException
    {
        // Ouvrir nomFichierLocal
        FileInputStream fileStream = new FileInputStream(pathFichierLocal);
        // Création d'un paquet WRQ
        TFTPPackage wrq = new TFTPPackage(TFTPPackage.OP_CODE_WRITE, pathFichierLocal, ""); //TO DO modeeee!!!!!!!!!
        byte[] wrqByte = wrq.getByteArray();

        try
        {
            // Affectation port anonyme
            DatagramSocket ds = new DatagramSocket();
            // Création d'un paquet WRQ
            DatagramPacket dp = new DatagramPacket(wrqByte, wrqByte.length, IPserv, portServ);
            ds.send(dp); //La machine A �met un "WRQ" vers adr_ip_serv, port_serv (Machine B)

            // on reçoit le paquet du serveur après émission du WRQ
            byte[] ackByte = new byte[4];
            DatagramPacket rep = new DatagramPacket(ackByte, ackByte.length);
            ds.receive(rep);
            // Vérifier que la paquet est bien un ACK 0
            TFTPPackage ack0 = new TFTPPackage((short) 0);
            // Si oui, on commence l'envoi du fichier
            if (ack0.getByteArray() == ackByte)
            {
                int readResult = 0;
                int offset = 0;
                int idBlock = 1;
                do
                {
                    // Bloc de data de 512 octets
                    byte[] blocData = new byte[512];
                    // On lit 512 octets dans blocData
                    readResult = fileStream.read(blocData, offset, blocData.length);
                    // On crée un nouveau paquet data

//                    data = new TFTPPackage((short) 1, ackByte);
//                    byte[] dataByte = data.getByteArray();
//                    DatagramPacket dpp = new DatagramPacket(dataByte, dataByte.length, IPserv, portServ);
//                    ds.send(dpp);
                    offset += 512;
                    idBlock++;
                }
                // tant que EOF n'a pas été rencontré
                while (readResult != -1);
            }

        }
        catch (SocketException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return 0;

    }

    private static boolean isLastPacket(DatagramPacket dp)
    {
        return dp.getLength() < 516 ? true : false;
        /*if (dp.getLength() < 516) //v�rifier aussi si le paquet n'est pas un code erreur
        {
            return true;
        }
        else
        {
            return false;
        }*/
    }

    private static void sendAcknowledgment(short idBlock, DatagramSocket ds, InetAddress ia, int port)
    {
        try
        {
            TFTPPackage ack = new TFTPPackage(idBlock);
            byte[] ackByte = ack.getByteArray();
            DatagramPacket dp = new DatagramPacket(ackByte, ackByte.length, ia, port);
            ds.send(dp);
        }
        catch (UnknownHostException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
