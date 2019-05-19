package com.amigos.tftp;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class TFTPPackage {

    public static byte OP_CODE_READ = 1;
    public static byte OP_CODE_WRITE = 2;
    public static byte OP_CODE_DATA = 3;
    public static byte OP_CODE_ACK = 4;
    public static byte OP_CODE_ERROR = 5;

    public static String MODE_NETASCII = "netascii";
    public static String MODE_OCTET = "octet";
    public static String MODE_MAIL = "mail";

    private final int maxSizeBlock = 512;

    private int _idBlock;
    private byte _opCode;
    private byte _errorCode;
    private String _mode;
    private String _errorMessage;
    private String _filename;
    private byte[] _data;
    private int _length;

    public int getMaxSizeBlock() {
        return maxSizeBlock;
    }

    public int getIdBlock() {
        return _idBlock;
    }

    public short getOpCode() {
        return _opCode;
    }

    public short getErrorCode() {
        return _errorCode;
    }

    public String getMode() {
        return _mode;
    }

    public String getErrorMessage() {
        return _errorMessage;
    }

    public String getFilename() {
        return _filename;
    }

    public byte[] getData() {
        return _data;
    }

    public int getLength() {
        return _length;
    }

    public TFTPPackage(byte opCode, String filename, String mode){ // lecture (RRQ) ou �criture (WRQ)
        _opCode = opCode;
        _filename = filename;
        _mode = mode;
        _length = 2 + filename.getBytes().length + mode.getBytes().length + 2;
    }

    public TFTPPackage(int idBlock, byte[] data){ //DATA
        _opCode = OP_CODE_DATA;
        _idBlock = idBlock;
        _data = data;
        _length = 4 + data.length;

    }
    

    public TFTPPackage(int idBlock){ //ACK
        _opCode = OP_CODE_ACK;
        _idBlock = idBlock;
        _length = 4;
    }

    
    public TFTPPackage(byte errorCode, String message){ //ERROR
        _opCode = OP_CODE_ERROR;
        _errorCode = errorCode;
        _errorMessage = message;
        _length = 4 + message.getBytes().length + 1;
    }

    
    public TFTPPackage(byte[] packet){
        ByteBuffer buffer = ByteBuffer.wrap(packet);
        buffer.position(1);
        _opCode = buffer.get();
        if((_opCode == OP_CODE_READ) || (_opCode == OP_CODE_WRITE)){

            int firstZeroByte = 0;
            int secondZeroByte = 0;
            int findFlag = 0;

            // find both bytes at zero
            while(findFlag < 2){
                if (buffer.get() == (byte)0){
                    if (findFlag == 0)
                        firstZeroByte = buffer.position() - 1;
                    else
                        secondZeroByte = buffer.position() - 1;
                    findFlag++;
                }
            }
            byte[] filename = new byte[firstZeroByte - 2];
            buffer.position(2);
            buffer.get(filename,0,firstZeroByte - 2);
            _filename = new String(filename);

            buffer.position(firstZeroByte + 1);
            byte[] mode = new byte[secondZeroByte - firstZeroByte - 1];
            buffer.get(mode,0,secondZeroByte - firstZeroByte - 1);
            _mode = new String(mode);

        }else if(_opCode == OP_CODE_DATA){
            ByteBuffer databuffer = ByteBuffer.allocate(4);
            databuffer.put((byte)0);
            databuffer.put((byte)0);
            databuffer.put(buffer.get());
            databuffer.put(buffer.get());
            _idBlock = databuffer.getInt(0);
            int pos;
            _data = new byte[buffer.limit() - buffer.position()];
            buffer.get(_data,0,buffer.limit() - buffer.position());

        }else if(_opCode == OP_CODE_ACK){
            ByteBuffer databuffer = ByteBuffer.allocate(4);
            databuffer.put((byte)0);
            databuffer.put((byte)0);
            databuffer.put(buffer.get());
            databuffer.put(buffer.get());
            _idBlock = databuffer.getInt(0);
        }else if(_opCode == OP_CODE_ERROR){
            buffer.get();
            _errorCode = buffer.get();
            throw new NotImplementedException();
        }


    }

    
    public byte[] getByteArray(){
        ByteBuffer buffer = ByteBuffer.allocate(_length);
        buffer.put((byte)0);
        buffer.put(_opCode);
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
            ByteBuffer datebuffer = ByteBuffer.allocate(4);
            datebuffer.putInt(_idBlock);
            buffer.put(datebuffer.get(2));
            buffer.put(datebuffer.get(3));
            buffer.put(_data);
        }else if(_opCode == OP_CODE_ACK){
            ByteBuffer datebuffer = ByteBuffer.allocate(4);
            datebuffer.putInt(_idBlock);
            buffer.put(datebuffer.get(2));
            buffer.put(datebuffer.get(3));
        }else if(_opCode == OP_CODE_ERROR){
            buffer.put((byte)0);
            buffer.put(_errorCode);
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

