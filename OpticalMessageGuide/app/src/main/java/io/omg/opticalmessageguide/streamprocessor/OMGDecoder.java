package io.omg.opticalmessageguide.streamprocessor;


import org.apache.commons.codec.binary.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

import static android.util.Log.d;

public class OMGDecoder extends Observable implements Runnable, Closeable {

    public static final byte DIVIDER        = (byte)0b11111111;
    public static final byte REPEATER_1     = (byte)0b10000000;
    public static final byte REPEATER_2     = (byte)0b11000000;

    private OutputStream outputStream;
    private InputStream inputStream;
    private byte[] payload;


    private MessageDecoderStatus status = MessageDecoderStatus.STOPPED;

    private boolean finish = false;

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public MessageDecoderStatus getStatus() {
        return status;
    }

    public void finish() {
        finish = true;
    }

    public Message getMsg() {
        if (null != payload && payload.length > 0) {
            return MessageParser.parse(payload);
        } else
            return null;
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
                status = MessageDecoderStatus.STARTED;
                decodePayload(inputStream);
            }
        } while(!finish);

    }

    public void decodePayload(InputStream inputStr) throws IOException {
        updateStatus(MessageDecoderStatus.STARTED);
        byte[] nextByte = new byte[1];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte lastByte=DIVIDER;
        byte lastByteAdded=0;
        do {
            if (inputStream.available()>0) {
                inputStream.read(nextByte);
                if (lastByte != nextByte[0]) { //skip double bytes. This will be forbidden by design
                    switch (nextByte[0]) {
                        case DIVIDER:
                            if (verify(baos.toByteArray())) {
                                setPayload(baos.toByteArray());
                            } else {
                                updateStatus(MessageDecoderStatus.ERROR);
                            }

                            return;
                        case REPEATER_1:
                        case REPEATER_2:
                            baos.write(new byte[] {lastByteAdded});
                            break;
                        default:
                            baos.write(nextByte);
                            lastByteAdded = nextByte[0];
                    }

                    lastByte = nextByte[0];
                }
            }

        } while (!finish);

    }

    private boolean verify(byte[] bytes) {
        try {
            Log.i("OMGDecoder",new String(bytes));
            byte[] decodedData = Base64.decodeBase64(bytes);

            byte[] payload = Arrays.copyOf(decodedData, decodedData.length - 4);
            byte[] checksum_data = Arrays.copyOfRange(decodedData, decodedData.length - 4, decodedData.length);

            Checksum checksum = new Adler32();

            // update the current checksum with the specified array of bytes
            checksum.update(payload, 0, payload.length);

            // get the current checksum value
            long checksumValueCalculated = checksum.getValue();

            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            buffer.put(checksum_data);
            buffer.flip();//need flip
            int checksumValueFromData = buffer.getInt();

            Log.i("OMGDecoder", String.format("ChecksumFromData=%d  checksumValueCalculated=%d", checksumValueFromData, checksumValueCalculated));
            return checksumValueFromData == checksumValueCalculated;
        } catch (Exception e) {
            Log.e("OMGDecoder", e.getMessage(), e);
            return false;
        }
    }

    public void setPayload(byte[] bytes) {
        byte[] decodedData = Base64.decodeBase64(bytes);

        this.payload = Arrays.copyOf(decodedData, decodedData.length - 4);
        updateStatus(MessageDecoderStatus.DONE);
    }

    public void encode() throws IOException{
        d("OMGDecoder", "encode: "+inputStream.read()); /// Workaround because first byte is 255 somehow
        decodeContainerMessage();
    }

    @Override
    public void close() throws IOException {
        finish=true;
        outputStream.close();
        inputStream.close();
    }

    private void updateStatus(MessageDecoderStatus newStatus) {
        status = newStatus;
        setChanged();
        notifyObservers(status);
    }

    @Override
    public void run() {
       // android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        try {
            encode();
            updateStatus(MessageDecoderStatus.INITIALIZED);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
