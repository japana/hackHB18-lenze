package io.omg.opticalmessageguide.streamprocessor;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public interface MessageDecoder extends Closeable {

    public void encode() throws IOException;

}
