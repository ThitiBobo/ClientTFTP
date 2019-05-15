package com.amigos.tftp;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class TFTPPackage {

    public static short OP_CODE_READ = 1;
    public static short OP_CODE_WRITE = 2;
    public static short OP_CODE_DATA = 3;
    public static short OP_CODE_ACK = 4;
    public static short OP_CODE_ERROR = 5;

    public static String MODE_NETASCII = "netascii";
    public static String MODE_OCTET = "octet";
    public static String MODE_MAIL = "mail";

    private final int maxSizeBlock = 512;

    private short _idBlock;
    private short _opCode;
    private short _errorCode;
    private String _mode;
    private String _errorMessage;
    private String _filename;
    private byte[] _data;


    private int _length;

    public TFTPPackage(short opCode, String filename, String mode){ // lecture (RRQ) ou écriture (WRQ)
        _opCode = opCode;
        _filename = filename;
        _mode = mode;
        _length = 2 + filename.getBytes().length + mode.getBytes().length + 2;
    }

    public TFTPPackage(short idBlock, byte[] data){ //DATA
        _opCode = OP_CODE_DATA;
        _idBlock = idBlock;
        _data = data;
        _length = 4 + data.length;

    }

    public TFTPPackage(short idBlock){ //ACK
        _opCode = OP_CODE_ACK;
        _idBlock = idBlock;
        _length = 4;
    }

    public TFTPPackage(short errorCode, String message){ //ERROR
        _opCode = OP_CODE_ERROR;
        _errorCode = errorCode;
        _errorMessage = message;
        _length = 4 + message.getBytes().length + 1;
    }

    public TFTPPackage(byte[] packet){
        ByteBuffer buffer = ByteBuffer.wrap(packet);
        _opCode = buffer.getShort();
        if((_opCode == OP_CODE_READ) || (_opCode == OP_CODE_WRITE)){

            int fistZeroByte = 0;
            int secondZeroByte = 0;
            int findFlag = 0;

            // find both bytes at zero
            while(findFlag < 2){
                if (buffer.get() == (byte)0){
                    if (findFlag == 0)
                        fistZeroByte = buffer.position() - 1;
                    else
                        secondZeroByte = buffer.position() - 1;
                    findFlag++;
                }
            }
            buffer.position(2);
            StringBuilder builder = new StringBuilder();
            while (buffer.position() < fistZeroByte){
                builder.append(buffer.getChar());
            }
            _filename = builder.toString();
            builder = new StringBuilder();
            while (buffer.position() < secondZeroByte){
                builder.append(buffer.getChar());
            }
            _mode = builder.toString();
        }else if(_opCode == OP_CODE_DATA){
            _idBlock = buffer.getShort();

        }else if(_opCode == OP_CODE_ACK){
            _idBlock = buffer.getShort();
        }else if(_opCode == OP_CODE_ERROR){
            _errorCode = buffer.getShort();
        }


    }

    public byte[] getByteArray(){
        ByteBuffer buffer = ByteBuffer.allocate(_length);
        buffer.putShort(_opCode);
        if((_opCode == OP_CODE_READ) || (_opCode == OP_CODE_WRITE)){
            try {
                buffer.put(_filename.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            buffer.put((byte)0);
            try {
                buffer.put(_mode.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            buffer.put((byte)0);
        }else if(_opCode == OP_CODE_DATA){
            buffer.putShort(_idBlock);
            buffer.put(_data);
        }else if(_opCode == OP_CODE_ACK){
            buffer.putShort(_idBlock);
        }else if(_opCode == OP_CODE_ERROR){
            buffer.putShort(_errorCode);
            try {
                buffer.put(_errorMessage.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            buffer.put((byte)0);
        }
        return buffer.array();
    }


    @Override
    public String toString() {
        return "TFTPPackage{" +
                "maxSizeBlock=" + maxSizeBlock +
                ", _idBlock=" + _idBlock +
                ", _opCode=" + _opCode +
                ", _errorCode=" + _errorCode +
                ", _mode='" + _mode + '\'' +
                ", _errorMessage='" + _errorMessage + '\'' +
                ", _filename='" + _filename + '\'' +
                ", _data=" + Arrays.toString(_data) +
                ", _length=" + _length +
                '}';
    }
}
