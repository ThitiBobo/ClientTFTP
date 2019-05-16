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
import java.nio.ByteBuffer;

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

    public static int sendFile(InetAddress IPserv, short portServ, String nomFichierLocal, String pathFichierLocal) throws FileNotFoundException
    {
        // Ouvrir nomFichierLocal
        FileInputStream fileStream = new FileInputStream(pathFichierLocal);
        // Création d'un paquet WRQ
        TFTPPackage wrq = new TFTPPackage(TFTPPackage.OP_CODE_WRITE, nomFichierLocal, ""); //TO DO modeeee!!!!!!!!!
        byte[] wrqByte = wrq.getByteArray();

        try
        {
            // Affectation port anonyme
            DatagramSocket ds = new DatagramSocket();
            // Création d'un paquet WRQ
            DatagramPacket dp = new DatagramPacket(wrqByte, wrqByte.length, IPserv, portServ);
            ds.send(dp); //La machine A �met un "WRQ" vers adr_ip_serv, port_serv (Machine B)

            // on reçoit le paquet du serveur après émission du WRQ
            byte[] ackByte0 = new byte[4];
            DatagramPacket rep = new DatagramPacket(ackByte0, ackByte0.length);
            ds.receive(rep);
            TFTPPackage ack0 = new TFTPPackage((short) 0);
            // Si on reçoit un ACK0, on commence l'envoi du fichier
            if (getPacketOPcode(ackByte0) == TFTPPackage.OP_CODE_ACK)
            {
                int readResult = 0;
                int offset = 0;
                short idBlock = 1;
                TFTPPackage ackN = null;
                byte[] ackServResponse;
                do
                {
                    // on présume que le serveur va envoyer un ACK alors on crée un array de taille 4 mais si le serveur répond
                    // autre chose qu'un ack N, peut poser PB -> à changer, pourrait être plus rigoureux
                    ackServResponse = new byte[4];
                    // Bloc de data de 512 octets
                    byte[] dataBlock = new byte[512];
                    // On lit 512 octets dans blocData

                    /**
                     * GENERE UNE EXCEPTION -> A CHANGER (16/05/2019) 22h40*
                     * java.lang.IndexOutOfBoundsException
                     */
                    readResult = fileStream.read(dataBlock, offset, dataBlock.length);
                    // On crée un nouveau paquet DATA(idBlock)
                    byte[] packet = (new TFTPPackage(idBlock, dataBlock)).getByteArray();
                    DatagramPacket dpp = new DatagramPacket(packet, packet.length, IPserv, portServ);
                    // On l'envoie
                    ds.send(dpp);

                    // On réceptionne le ACK du serveur (norme du protocole)
                    DatagramPacket serverResponse = new DatagramPacket(ackServResponse, ackServResponse.length);
                    ds.receive(serverResponse);
                    offset += 512;
                    idBlock++;

                }
                // tant que EOF n'a pas été rencontré et que le ACK est bon
                while (readResult != -1 && getPacketOPcode(ackServResponse) == TFTPPackage.OP_CODE_ACK /**
                         * && getPacketNo(ackServResponse) == idBlock*
                         */
                        );
            }

        }
        catch (SocketException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(e);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(e);
        }
        return 0;
    }

    private static short getPacketOPcode(byte[] buff)
    {
        byte[] twoFirstBytes = new byte[2];
        twoFirstBytes[0] = buff[0];
        twoFirstBytes[1] = buff[1];
        return ByteBuffer.wrap(twoFirstBytes).getShort();
    }

    private static short getPacketNo(byte[] buff)
    {
        byte[] SecondAndThirdBytes = new byte[2];
        SecondAndThirdBytes[0] = buff[2];
        SecondAndThirdBytes[1] = buff[3];
        return ByteBuffer.wrap(SecondAndThirdBytes).getShort();
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
