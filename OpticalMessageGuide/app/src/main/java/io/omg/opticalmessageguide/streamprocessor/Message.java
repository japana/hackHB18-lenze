package io.omg.opticalmessageguide.streamprocessor;

public class Message {

    private long programId;
    private int errorId;

    private String message;

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

}
