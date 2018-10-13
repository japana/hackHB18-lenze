package io.omg.opticalmessageguide.services;

import java.io.IOException;
import java.io.InputStream;

public class Encoder {


    public String encode(InputStream input) throws IOException{
        byte[] bytes = new byte[10];
        input.read(bytes);
        return new String(bytes).trim();
    }
}
