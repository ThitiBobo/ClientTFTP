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
import java.util.ArrayList;
import java.util.Arrays;

public class ClientTFTP
{

    public static int receiveFile(String fileName, int port, InetAddress ia)
    {
        while (true)
        {
            try
            {

                System.out.println("Creation du fichier");
                FileOutputStream fea = new FileOutputStream("cheminrelatif" + fileName);
                System.out.println("Ouverture du socket");
                DatagramSocket ds = new DatagramSocket();
                TFTPPackage rrq = new TFTPPackage(TFTPPackage.OP_CODE_READ, fileName, TFTPPackage.MODE_OCTET); //mode � changer
                byte[] rrqByte = rrq.getByteArray();
                DatagramPacket RRQ = new DatagramPacket(rrqByte, rrqByte.length, ia, port);
                System.out.println("Envoie du RRQ");
                ds.send(RRQ);
                System.out.println("Envoie du RRQ");
                byte numPaquet = 1;
                byte[] buffer = new byte[516];
                DatagramPacket dr = new DatagramPacket(buffer, 516);
                do
                {
                    System.out.println("Reception de paquet");
                    ds.receive(dr);
                    System.out.println(Arrays.toString(dr.getData()));

                    TFTPPackage data = new TFTPPackage(dr.getData());
                    if (data.getOpCode() == TFTPPackage.OP_CODE_ERROR)
                    {
                        System.out.println("Error");
                    }
                    // verifier si la paquet recue est pas une erreur
                    data.getIdBlock();
                    //ecriture dans le fichier
                    if (data.getIdBlock() == numPaquet)
                    {
                        fea.write(data.getData(), 0, 512);
                        sendAcknowledgment(numPaquet, ds, ia, dr.getPort());
                        numPaquet++;
                    }
                    else
                    {
                        sendAcknowledgment((byte) (numPaquet - 1), ds, ia, port);
                    }
                    System.out.println("lst packet" + isLastPacket(dr));
                }
                while (!isLastPacket(dr));

                ds.setSoTimeout(1000);   // set the timeout in millisecounds.

                while (ds.isConnected())
                {        // receive data until timeout
                    try
                    {
                        System.out.println("Receiving message...");
                        ds.receive(dr); // receive the packet
                        System.out.println("Message received");
                        sendAcknowledgment((byte) (numPaquet - 1), ds, ia, dr.getPort());
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
            System.out.println("Fin");
            return 0;

        }
    }

    public static int sendFile(InetAddress IPserv, short portServ, String nomFichierLocal, String pathFichierLocal) throws FileNotFoundException
    {
        // Ouvrir nomFichierLocal
        FileInputStream fileStream = new FileInputStream(pathFichierLocal);
        // Création d'un paquet WRQ
        TFTPPackage wrq = new TFTPPackage(TFTPPackage.OP_CODE_WRITE, nomFichierLocal, TFTPPackage.MODE_OCTET);
        byte[] wrqByte = wrq.getByteArray();
        System.out.println(Arrays.toString(wrqByte));

        try
        {
            // Affectation port anonyme
            DatagramSocket ds = new DatagramSocket();
            // Création d'un paquet WRQ
            DatagramPacket dp = new DatagramPacket(wrqByte, wrqByte.length, IPserv, portServ);
            ds.send(dp); //La machine A emet un "WRQ" vers adr_ip_serv, port_serv (Machine B)

            // on reçoit le paquet du serveur après émission du WRQ
            byte[] ackByte0 = new byte[4];
            DatagramPacket rep = new DatagramPacket(ackByte0, ackByte0.length);
            ds.receive(rep);
            // Si on reçoit un ACK0, on commence l'envoi du fichier
            if (getPacketOPcode(ackByte0) == TFTPPackage.OP_CODE_ACK)
            {
                int eof = 0;
                byte idBlock = 1;
                TFTPPackage ackN = null;
                byte[] ackServResponse;
                do
                {
                    // on présume que le serveur va envoyer un ACK alors on crée un array de taille 4 mais si le serveur répond
                    // autre chose qu'un ack N, peut poser PB -> à changer, pourrait être plus rigoureux
                    ackServResponse = new byte[4];
                    // Liste de bytes de data
                    ArrayList<Byte> dataList = new ArrayList<>();
                    int byteCourant;
                    for (int i = 0; i < 512; i++)
                    {
                        byteCourant = fileStream.read();
                        if (byteCourant == -1)
                        {
                            eof = -1;
                            break;
                        }
                        else if (byteCourant > -128 && byteCourant < 128)
                        {
                            System.out.print((char) byteCourant);
                            dataList.add((byte) byteCourant);
                            //System.out.println((byte) byteCourant);
                        }
                    }

                    System.out.println();
                    // on crée un array de bytes à partir de la liste de bytes
                    byte[] dataArray = new byte[dataList.size()];
                    for (int i = 0; i < dataList.size(); i++)
                    {
                        dataArray[i] = dataList.get(i);
                    }

                    //////////////// MAJ 17/05 -- PROBLEME IL SEMBLE QUE PUMPKIN NE RECOIT JAMAIS LE DATA(1)
                    // OU ALORS LE DATA(1) N'EST PAS BIEN FORMÉ
                    // "UDP PACKET RECEIVE FAILED"
                    // On crée un nouveau paquet DATA(idBlock)
                    TFTPPackage packetObject = new TFTPPackage(idBlock, dataArray);
                    byte[] packet = packetObject.getByteArray();

                    System.out.println(Arrays.toString(packet));
                    DatagramPacket dppData = new DatagramPacket(packet, packet.length, IPserv, portServ);
                    // On l'envoie
                    ///////////////// PROB

                    // On réceptionne le ACK du serveur (norme du protocole) // PROB : RECOIT UN ACK0
                    DatagramPacket serverResponse = new DatagramPacket(ackServResponse, ackServResponse.length);
                    ds.receive(serverResponse);

                    ackServResponse = serverResponse.getData();
                    System.out.println(Arrays.toString(ackServResponse));
                    idBlock++;

                }
                // tant que EOF n'a pas été rencontré et que le ACK est bon
                while (eof != -1 && getPacketOPcode(ackServResponse) == TFTPPackage.OP_CODE_ACK /**
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

    private static void sendAcknowledgment(byte idBlock, DatagramSocket ds, InetAddress ia, int port)
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
