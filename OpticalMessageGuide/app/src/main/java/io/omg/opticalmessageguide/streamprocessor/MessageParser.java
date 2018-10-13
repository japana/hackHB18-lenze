package io.omg.opticalmessageguide.streamprocessor;

public class MessageParser {

    public static Message parse(byte[] payload) {
        Message msg = new Message();
        msg.setMessage(new String(payload));
        return msg;
    }
}
