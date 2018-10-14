package io.omg.opticalmessageguide;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import io.omg.opticalmessageguide.streamprocessor.Message;
import io.omg.opticalmessageguide.streamprocessor.MessageDecoderStatus;
import io.omg.opticalmessageguide.streamprocessor.OMGDecoder;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void testEncoder() throws Exception {

        String b64Original = "SGVsbG8gV29ybGQgaW4gQmFzZTY0";


        String expected = "Hello World in Base64";
        byte[] b64Exp = Base64.encodeBase64(expected.getBytes());

        Observer observer = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                OMGDecoder dec = (OMGDecoder) o;
                System.out.println(dec.getMsg());
            }
        };

        byte[] trash = "bbbbbbb".getBytes();
        byte[] header_footer = {OMGDecoder.DIVIDER};
        byte[] helloWorld = b64Original.getBytes();

        for (byte b:helloWorld) {
            System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }


        System.out.println();
        for (byte b:b64Exp) {
            System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }


        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(trash);
        os.write(header_footer);
        os.write(helloWorld);
        os.write(header_footer);
        os.write(trash);

        try (OMGDecoder decoder = new OMGDecoder(observer)) {
            decoder.getOutputStream().write(os.toByteArray());

            Thread.sleep(1000);
            String value = decoder.getMsg().getMessage();
            Assert.assertEquals("whatever", "Hello World in Base64", value);
        }




    }


    @Test
    public void testCreationString() {

        String test = "Hello World";
        byte[] base64 = Base64.encodeBase64(test.getBytes());

        System.out.println(new String(base64));

        byte b = (byte)0b10000000;
        System.out.println(""+ b + " = "+String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));

        Checksum checksum = new Adler32();


        // update the current checksum with the specified array of bytes
        checksum.update(base64, 0, base64.length);

        // get the current checksum value
        long checksumValue = checksum.getValue();

        //11000110 11010000 00101010 0010

        System.out.println("CRC32 checksum for input string is: " + String.format("%32s", Long.toBinaryString(checksumValue)).replace(' ', '0'));

    }


    @Test
    public void testDataArray() throws Exception {

        byte[] progId = {
                // progId
                0b01101100, 0b01111100, 0b01001100, 0b01100000
        };

        byte[] errId = {
                // errId
                0b00000011, 0b00001010
        };

        byte[] payload = ArrayUtils.addAll(progId, errId);

 /*       byte[] checksum_data = {
                //Adler32
                0b00000111, 0b01011010, 0b00000001 ,(byte)0b10100010
        };

        byte[] data = ArrayUtils.addAll(payload, checksum_data);
*/
        Checksum checksum = new Adler32();

        // update the current checksum with the specified array of bytes
        checksum.update(payload, 0, payload.length);

        // get the current checksum value
        long checksumValue = checksum.getValue();


        System.out.println("CRC32 checksum for input string is: " + String.format("%32s", Long.toBinaryString(checksumValue)).replace(' ', '0'));

        // Adler Checksum
//        0b00001100, 0b01101101, 0b00000010, (byte)0b10100010

/*
        byte[] base64 = Base64.encodeBase64(data);
        byte[] delimiter = {OMGDecoder.DIVIDER};

        byte[] parsebledata = ArrayUtils.addAll(delimiter, base64 );


        for (int i:parsebledata) {
            System.out.println(i);
        }

        System.out.println();

        System.out.println(new String(base64));

        System.out.println(0b10000000);


        long prgId_expected = 0b01101100011111000100110001100000;
        int errId_expected = 0b0000001100001010;



        try (OMGDecoder decoder = new OMGDecoder()) {
            decoder.getOutputStream().write(parsebledata);

            Thread.sleep(3000);
            Assert.assertEquals("", MessageDecoderStatus.DONE, decoder.getStatus());

            Message msg = decoder.getMsg();
            Assert.assertEquals("ProgramId", prgId_expected, msg.getProgramId());
            Assert.assertEquals("ErrorId", errId_expected, msg.getErrorId());
        }

*/



        byte b = (byte)0b10000000;
        System.out.println(""+ b + " = "+String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));






    }

    byte[] calcChecksum(byte[] data) {

        Checksum checksum = new Adler32();

        // update the current checksum with the specified array of bytes
        checksum.update(data, 0, data.length);

        // get the current checksum value
        int checksumValue = (int)checksum.getValue();

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(checksumValue);
        return buffer.array();

    }

    private byte[] replaceRepeatingBytes(byte[] input) {
        byte[] retArr = Arrays.copyOf(input, input.length);
        for (int i = 1; i<retArr.length; i++) {
            if (retArr[i] == retArr[i-1]) {
                retArr[i] = (byte)128;
            }
        }
        return retArr;
    }

    public byte[] createTestData(int programId, char errId, float param1) {

        ByteBuffer bb_payload = ByteBuffer.allocate(Integer.BYTES + Character.BYTES+Float.BYTES );

        bb_payload.putInt(programId);
        bb_payload.putChar(errId);
        bb_payload.putFloat(param1);

        byte[] payload = bb_payload.array();

        ByteBuffer bb_verified = ByteBuffer.allocate(payload.length + Integer.BYTES);
        bb_verified.put(payload).put(calcChecksum(payload));

        byte[] base64 = Base64.encodeBase64(bb_verified.array());

        System.out.println(new String(base64));

        //byte[] encodedStream = ByteBuffer.allocate(base64.length+1).put((byte)255).put(replaceRepeatingBytes(base64)).array();

        return replaceRepeatingBytes(base64);

    }


    @Test
    public void validateTestData() throws Exception{
        byte[] testData = createTestData(1234,(char)5, 6.4f);

        for (byte b:testData) {
            System.out.println((int)b);
        }

        try (OMGDecoder decoder = new OMGDecoder()) {
            decoder.getOutputStream().write(testData);

            Thread.sleep(3000);
            //Assert.assertEquals("", MessageDecoderStatus.DONE, decoder.getStatus());

            Message msg = decoder.getMsg();
            Assert.assertEquals("ProgramId", 1234, msg.getProgramId());
            Assert.assertEquals("ErrorId", 5, msg.getErrorId());
        }


    }

}