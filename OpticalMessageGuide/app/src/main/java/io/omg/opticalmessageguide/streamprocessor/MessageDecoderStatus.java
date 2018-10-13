package io.omg.opticalmessageguide.streamprocessor;

public enum MessageDecoderStatus {
    STOPPED, // Decoder thread is stopped
    INITIALIZED, // Decoder has been initialized
    WAITING, // Decoder is waiting for inial delimiter to be parsed
    STARTED, // Delimiter found, parsing the payload
    ERROR, // Error while parsing the payload
    DONE;  // payload succesfully parsed
}
