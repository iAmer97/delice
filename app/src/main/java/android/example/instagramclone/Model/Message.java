package android.example.instagramclone.Model;

public class Message {

    private String type, message,from;
    private long time;
    private boolean isSeen;

    public Message(){}

    public Message(String type, String message, String from, long time, boolean isSeen) {
        this.type = type;
        this.message = message;
        this.from = from;
        this.time = time;
        this.isSeen = isSeen;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}
