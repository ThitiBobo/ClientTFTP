package com.amigos.tftp;

public class TFTPPackage {

    public static short OP_CODE_READ = 1;
    public static short OP_CODE_WRITE = 2;
    public static short OP_CODE_DATA = 3;
    public static short OP_CODE_ACK = 4;
    public static short OP_CODE_ERROR = 5;

    private short _idBlock;
    private short _opCode;
    private short _errorCode;
    private String _mode;
    private String _errorMessage;
    private String _filename;
    private byte[] _data;

    public TFTPPackage(String filename, String mode){

    }

    public TFTPPackage(short idBlock, byte[] data){

    }

    public TFTPPackage(short idBlock){

    }

    public TFTPPackage(short errorCode, String message){

    }

    public byte[] getByteArray(){
        return null;
    }



}
