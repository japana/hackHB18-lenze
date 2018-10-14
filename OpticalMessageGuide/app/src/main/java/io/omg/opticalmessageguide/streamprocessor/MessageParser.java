package io.omg.opticalmessageguide.streamprocessor;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MessageParser {

    private static Map<Integer, String> errorMap = new HashMap<>();
    private static Map<Integer, String> hintMap = new HashMap<>();

    static {
        errorMap.put(779, "Die Temperatur ist zu hoch.");
        errorMap.put(778, "Die Temperatur ist zu niedrig.");
    }

    static {
        hintMap.put(779, "Bitte prüfen Sie ob die Thermometer zu nah an den Heizöfen stehen");
        hintMap.put(778, "Bitte stellen Sie sicher, das alle Türen zum Außenbereich geschlossen sind.");
    }



    public static Message parse(byte[] payload) {
        Message msg = new Message();

        byte[] progId_data = Arrays.copyOf(payload,4);
        byte[] errorId_data = Arrays.copyOfRange(payload,4,6);

        ByteBuffer buffer_prg = ByteBuffer.allocate(Integer.BYTES);
        buffer_prg.put(progId_data);
        buffer_prg.flip();//need flip
        int progId = buffer_prg.getInt();

        ByteBuffer buffer_err = ByteBuffer.allocate(Character.BYTES);
        buffer_err.put(errorId_data);
        buffer_err.flip();//need flip
        int errId = buffer_err.getChar();

        msg.setProgramId(progId);
        msg.setErrorId(errId);

        msg.setMessage(errorMap.get(errId));
        msg.setHint((hintMap.get(errId)));
        return msg;
    }
}
