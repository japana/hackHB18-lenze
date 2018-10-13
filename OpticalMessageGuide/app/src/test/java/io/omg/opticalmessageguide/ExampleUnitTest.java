package io.omg.opticalmessageguide;

import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Observable;
import java.util.Observer;

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
            String value = decoder.getMsg();
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

    }

}