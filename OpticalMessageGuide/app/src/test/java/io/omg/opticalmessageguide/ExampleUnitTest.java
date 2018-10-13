package io.omg.opticalmessageguide;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import io.omg.opticalmessageguide.streamprocessor.OMGDecoder;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void testEncoder() throws Exception {

        String expected = "Hello world";

        byte[] trash = "bbbbbbb".getBytes();
        byte[] header_footer = {OMGDecoder.DIVIDER};
        byte[] helloWorld = expected.getBytes();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(trash);
        os.write(header_footer);
        os.write(helloWorld);
        os.write(header_footer);
        os.write(trash);

        OMGDecoder encoder = new OMGDecoder();
        encoder.getOutputStream().write(os.toByteArray());
        Thread.sleep(1000);
        String value = encoder.getMsg();
        encoder.close();



        Assert.assertEquals("whatever", expected, value);

    }

}