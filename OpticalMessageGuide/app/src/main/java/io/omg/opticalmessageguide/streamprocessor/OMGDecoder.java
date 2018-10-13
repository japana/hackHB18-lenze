package io.omg.opticalmessageguide.streamprocessor;

import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Observable;
import java.util.Observer;

public class OMGDecoder extends Observable implements MessageDecoder, Runnable {

    public static final byte DIVIDER = (byte)0b11111111;

    private OutputStream outputStream;
    private InputStream inputStream;
    private byte[] payload;



    private boolean finish = false;

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void finish() {
        finish = true;
    }

    public String getMsg() {

        //byte[] b64 = Base64.decode(payload, Base64.DEFAULT);

        return new String(payload);
    }

    public OMGDecoder(Observer... observers) throws IOException {
        outputStream = new PipedOutputStream();
        inputStream = new PipedInputStream();
        ((PipedInputStream)inputStream).connect((PipedOutputStream)outputStream);
        new Thread(this).start();
        for (Observer obs : observers) {
            this.addObserver(obs);
        }

    }

    public void decodeContainerMessage() throws IOException{
        byte[] nextByte = new byte[1];
        do {
            inputStream.read(nextByte);
            if (DIVIDER == nextByte[0]) {
                decodePayload(inputStream);
            }
        } while(!finish);

    }

    public void decodePayload(InputStream inputStr) throws IOException {
        byte[] nextByte = new byte[1];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte lastByte=DIVIDER;
        do {
            inputStream.read(nextByte);
            if (lastByte != nextByte[0]) { //skip double bytes. This will be forbidden by design
                if (DIVIDER != nextByte[0]) {
                    baos.write(nextByte);
                } else {
                    setPayload(baos.toByteArray());
                    return;
                }
                lastByte = nextByte[0];
            }

        } while (!finish);

    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
        this.setChanged();
        notifyObservers(payload);
    }



    public void encode() throws IOException{
        Log.d("OMGDecoder", "encode: "+inputStream.read()); /// Workaround because first byte is 255 somehow
        decodeContainerMessage();
    }

    @Override
    public void close() throws IOException {
        finish=true;
        outputStream.close();
        inputStream.close();
    }

    @Override
    public void run() {
       // android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        try {
            encode();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
