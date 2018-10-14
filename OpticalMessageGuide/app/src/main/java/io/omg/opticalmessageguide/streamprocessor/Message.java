package io.omg.opticalmessageguide.streamprocessor;

import java.io.Serializable;

public class Message implements Serializable {

    private long programId;
    private int errorId;

    private String message;


    private String hint;

    public Message(){}

    public Message(long programId, int errorId, String message) {
        this.programId = programId;
        this.errorId = errorId;
        this.message = message;
    }


    public long getProgramId() {
        return programId;
    }

    public void setProgramId(long programId) {
        this.programId = programId;
    }

    public int getErrorId() {
        return errorId;
    }

    public void setErrorId(int errorId) {
        this.errorId = errorId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }


}
