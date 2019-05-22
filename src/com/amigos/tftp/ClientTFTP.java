package com.amigos.tftp;

import CustomedExceptions.ServerSideException;
import CustomedExceptions.UnknownFileFormatException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import javax.imageio.ImageIO;

public class ClientTFTP
{

    public static int receiveFile(String fileName, int port, InetAddress ia) throws FileNotFoundException
    {
        try
        {

            System.out.println("Creation du fichier");
            FileOutputStream fea = new FileOutputStream("C:\\Users\\Dorian\\Desktop\\PUMPKIN\\" + fileName);
            System.out.println("Ouverture du socket");
            DatagramSocket ds = new DatagramSocket();
            TFTPPackage rrq = new TFTPPackage(TFTPPackage.OP_CODE_READ, fileName, TFTPPackage.MODE_OCTET); //mode � changer
            byte[] rrqByte = rrq.getByteArray();
            DatagramPacket RRQ = new DatagramPacket(rrqByte, rrqByte.length, ia, port);
            System.out.println("Envoie du RRQ");
            ds.send(RRQ);
            System.out.println("Envoie du RRQ");
            int numPaquet = 1;
            byte[] buffer;
            DatagramPacket dr;
            do
            {
                buffer = new byte[516];
                dr = new DatagramPacket(buffer, 516);
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
                    fea.write(data.getData(), 0, getSizeDataBlock(data.getData()));
                    sendAcknowledgment(numPaquet, ds, ia, dr.getPort());
                    numPaquet++;
                }
                else
                {
                    sendAcknowledgment((byte) (numPaquet - 1), ds, ia, port);
                }

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
                }
                finally
                {
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
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Fin");
        return 0;
    }

    /**
     * Checks if a file is an image or note
     *
     * @param nomFichier the filename
     * @return true if the file is .png or .jpg. False if it is .txt. Exception
     * otherwise
     * @throws UnknownFileFormatException if the format is not .txt, .png or
     * .jpg
     */
    private static boolean isImg(String nomFichier) throws UnknownFileFormatException
    {
        String extension = nomFichier.substring(nomFichier.lastIndexOf(".") + 1);
        if ("txt".equals(extension))
        {
            return false;
        }
        else if ("png".equals(extension) || "jpg".equals(extension))
        {
            return true;
        }
        throw new UnknownFileFormatException("The file format \" " + extension + "\" is not managed by the program.");
    }

    /**
     * Envoie un fichier en respectant la norme RFC 1350 TFTP
     *
     * @param IPserv L'ip du serveur
     * @param portServ Le port du serveur
     * @param nomFichierLocal Le nom du fichier local à envoyer
     * @param pathFichierLocal Le chemin vers le fichier local
     * @return
     * @throws FileNotFoundException : Si le fichier n'est pas trouvé
     * @throws ServerSideException : Si une exception est générée côté serveur
     */
    public static int sendFile(InetAddress IPserv, short portServ, String nomFichierLocal, String pathFichierLocal) throws FileNotFoundException, ServerSideException, IOException
    {
        FileInputStream fileStream = new FileInputStream(pathFichierLocal);
        ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
        BufferedImage image = null;
        byte[] imageBytes;
        try
        {
            //Check du format du fichier
            boolean isFileAnImg = isImg(nomFichierLocal);
            if (isFileAnImg)
            {
                File f = new File(pathFichierLocal);
                try
                {
                    image = ImageIO.read(f);
                    ImageIO.write(image, nomFichierLocal.substring(nomFichierLocal.lastIndexOf(".") + 1), imageStream);

                }
                catch (IOException e)
                {
                    System.out.println("EXCEPTION : " + e);
                    return -1;
                }
            }

            // Création d'un paquet WRQ (objet)
            TFTPPackage wrq = new TFTPPackage(TFTPPackage.OP_CODE_WRITE, nomFichierLocal, TFTPPackage.MODE_OCTET);
            // Conversion du paquet WRQ (objet) en paquet WRQ brut (tableau de bytes)
            byte[] wrqByte = wrq.getByteArray();
            // affichage (debug)
            System.out.println("WRQ : " + Arrays.toString(wrqByte));
            try
            {
                /**
                 * EMISSION WRQ
                 */
                // Création d'un datagramSocket (Affectation port anonyme)
                DatagramSocket ds = new DatagramSocket();
                // timeout de 30 s
                ds.setSoTimeout(30000);
                // Création d'un DatagramPacket WRQ
                DatagramPacket dpWrq = new DatagramPacket(wrqByte, wrqByte.length, IPserv, portServ);
                //La machine A emet un "WRQ" vers adr_ip_serv, port_serv (Machine B)
                ds.send(dpWrq);

                /**
                 * RECEPTION ACK(0)
                 */
                // Création d'un tableau de 100 bytes qui contiendra la réponse du serveur
                byte[] firstServerResponse = new byte[100];
                // Création d'un DatagramPacket contenant le réponse du serveur
                DatagramPacket rep = new DatagramPacket(firstServerResponse, firstServerResponse.length);
                // Réception du DatagramPacket
                ds.receive(rep);
                // affichage (DEBUG)
                System.out.println("Reponse serveur : " + Arrays.toString(firstServerResponse));

                // Si on reçoit un ACK0, on commence l'envoi du fichier
                if (getPacketOPcode(firstServerResponse) == TFTPPackage.OP_CODE_ACK && getPacketNo(firstServerResponse) == 0)
                {
                    int eof = 0;
                    int idBlock = 1;
                    TFTPPackage ackN = null;
                    byte[] serverResponse;
                    byte[] packet;
                    imageBytes = imageStream.toByteArray();
                    long nbBlocsTailleMax = imageBytes.length / getMaxDataBlockSize();
                    int tailleDernierBloc = imageBytes.length % getMaxDataBlockSize();
                    int nbBlocsTailleMaxFaits = 0;
                    // émission des données
                    do
                    {
                        // réallocation d'un tableau de taille 100 pour la réponse du serveur.
                        serverResponse = new byte[100];

                        /**
                         * si le fichier est une image -> lecture = traitement
                         * différent qu'avec le texte
                         */
                        if (isFileAnImg)
                        {
                            byte[] rawData;
                            if (nbBlocsTailleMaxFaits < nbBlocsTailleMax)
                            {
                                rawData = new byte[getMaxDataBlockSize()];
                                for (int i = nbBlocsTailleMaxFaits * getMaxDataBlockSize(), j = 0; i < (nbBlocsTailleMaxFaits + 1) * getMaxDataBlockSize(); i++, j++)
                                {
                                    rawData[j] = imageBytes[i];
                                }
                                nbBlocsTailleMaxFaits++;
                            }
                            else
                            {
                                rawData = new byte[tailleDernierBloc];
                                for (int i = nbBlocsTailleMaxFaits * getMaxDataBlockSize(), j = 0; i < imageBytes.length; i++, j++)
                                {
                                    rawData[j] = imageBytes[i];
                                }
                                eof = -1;
                                //System.out.println("L'EOF est -1");
                            }
                            packet = (new TFTPPackage(idBlock, rawData)).getByteArray();
                        }
                        // si le fichier n'est pas une image, c'est du texte
                        /**
                         * Lecture du fichier texte par blocs de 512 bytes. On
                         * utilise un type dynamique (la liste) car on ne sait
                         * pas à l'avance le nombre de bytes que l'on va lire
                         * dans le fichier. A partir de cette liste, on créera
                         * ensuite un tableau de bytes pour lequel, cette
                         * fois-ci, l'on connaitra la taille.
                         */
                        else
                        {
                            ArrayList<Byte> dataList = new ArrayList<>();
                            int byteCourant;
                            for (int i = 0; i < 512; i++)
                            {
                                byteCourant = fileStream.read();
                                if (byteCourant == -1)
                                {
                                    // on sort de la boucle si on atteint l'EOF
                                    eof = -1;
                                    break;
                                }
                                else if (byteCourant > -128 && byteCourant < 128)
                                {
                                    dataList.add((byte) byteCourant);
                                    //System.out.println((byte) byteCourant);
                                }
                            }
                            packet = (new TFTPPackage(idBlock, ByteArrayList_To_ByteArray(dataList))).getByteArray();
                            System.out.println(idBlock + " : " + Arrays.toString(packet));
                        }
                        //    System.out.println("DATA(" + value + ") : " + Arrays.toString(packet));

                        /**
                         * Envoi du paquet
                         */
                        // Création du datagramPacket correspondant.
                        DatagramPacket dpData = new DatagramPacket(packet, packet.length, IPserv, rep.getPort());
                        // envoi
                        ds.send(dpData);

                        /**
                         * Réception de la réponse du serveur
                         */
                        DatagramPacket serverResponseDp = new DatagramPacket(serverResponse, serverResponse.length);
                        ds.receive(serverResponseDp);
                        //debug
                        //      System.out.println("Réponse du serveur : " + Arrays.toString(serverResponse));
                        // si le paquet est un ERROR
                        if (getPacketOPcode(serverResponse) == TFTPPackage.OP_CODE_ERROR)
                        {
                            String error = "Operations resulted in a server-side error \n";
                            error += ErrorPacketToString(serverResponse);
                            fileStream.close();
                            imageStream.close();
                            ds.close();
                            // return 1
                            throw new ServerSideException(error);
                        }
                        idBlock++;
                    }
                    // tant que l'eof n'est pas rencontré et que le ACK est correct (opcode = ack et numéro de paquet OK)
                    while ((eof != -1) && (getPacketOPcode(serverResponse) == TFTPPackage.OP_CODE_ACK) /*&& (getPacketNo(serverResponse) == idBlock)*/);
                    ds.close();
                }

                // Sinon, si le serveur a répondu autre chose qu'un ACK0, il a répondu une erreur.
                else
                {
                    String error = "Operations resulted in a server-side error \n";
                    String er2 = ErrorPacketToString(firstServerResponse);
                    error += er2;
                    System.out.println(error);
                    fileStream.close();
                    imageStream.close();
                    ds.close();
                    // return 1
                    throw new ServerSideException(error);
                }
            }
            catch (SocketException ex)
            {
                System.out.println("EXCEPTION (SOCKET) : " + ex);
                fileStream.close();
                imageStream.close();
                return -1;
            }
        }
        catch (UnknownFileFormatException ex)
        {
            System.out.println("EXCEPTION : " + ex);
            fileStream.close();
            imageStream.close();
            return -3;
        }
        fileStream.close();
        imageStream.close();
        return 0;
    }

    /**
     * Convertit un Tableau de Bytes en Array de bytes
     *
     * @param bytesAsList Liste de Bytes
     * @return Array de bytes
     */
    private static byte[] ByteArrayList_To_ByteArray(ArrayList<Byte> bytesAsList)
    {
        byte[] byteArray = new byte[bytesAsList.size()];
        for (int i = 0; i < bytesAsList.size(); i++)
        {
            byteArray[i] = bytesAsList.get(i);
        }
        return byteArray;
    }

    /**
     * Récupère les deux premiers bytes d'un paquet TFTP : L'opcode.
     *
     * @param buff le paquet TFTP
     * @return un Opcode (short)
     */
    private static short getPacketOPcode(byte[] buff)
    {
        byte[] twoFirstBytes = new byte[2];
        twoFirstBytes[0] = buff[0];
        twoFirstBytes[1] = buff[1];
        return ByteBuffer.wrap(twoFirstBytes).getShort();
    }

    /**
     * Récupère les deux seconds bytes d'un paquet TFTP : Le num de bloc (DATA
     * ou ACK) ou l'errorCode (ERROR)
     *
     * @param buff un paquet TFTP ACK, DATA ou ERROR
     * @return un short étant le numéro de paquet DATA ou ACK ou l'errorCode
     * d'un paquet ERROR
     */
    private static short getPacketNo(byte[] buff)
    {
        byte[] SecondAndThirdBytes = new byte[2];
        SecondAndThirdBytes[0] = buff[2];
        SecondAndThirdBytes[1] = buff[3];
        return ByteBuffer.wrap(SecondAndThirdBytes).getShort();
    }

    /**
     * Permet de convertir un paquet TFTP Opcode = 5 (ERORR) en chaîne de
     * caractères.
     *
     * @param buff le paquet
     * @return String correspondant au paquet
     */
    private static String ErrorPacketToString(byte[] buff)
    {
        short opcode = getPacketOPcode(buff);
        short errorCode = getPacketNo(buff);
        String result = "Opcode : " + opcode + "\n";
        result += "ErrorCode : " + errorCode + "\n";
        result += "ErrorMessage : ";
        int i = 4;
        while (buff[i] != 0)
        {
            result += (char) buff[i];
        }
        result += "\n";
        return result;
    }

    private static boolean isLastPacket(DatagramPacket dp)
    {
        return dp.getLength() < 516 ? true : false;
    }

    private static void sendAcknowledgment(int idBlock, DatagramSocket ds, InetAddress ia, int port)
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
            System.out.println("EXCEPTION : " + e);
        }
        catch (IOException e)
        {
            System.out.println("EXCEPTION : " + e);
        }

    }

    private static int getMaxDataBlockSize()
    {
        return 512;
    }

    private static int getSizeDataBlock(byte[] data)
    {
        int i = 0;
        while (i < data.length && data[i] != (byte) 0)
        {
            i++;
        }
        return i;
    }
}
