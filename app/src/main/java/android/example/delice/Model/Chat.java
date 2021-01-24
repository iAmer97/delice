package android.example.delice.Model;

public class Chat {

    private String id, lastMessage,lastMessageFrom;
    private String lastMessageIsSeen;
    //public long time;

    public Chat() {
    }

    public Chat(String id, String lastMessage, String lastMessageFrom, String lastMessageIsSeen) {
        this.id = id;
        this.lastMessage = lastMessage;
        this.lastMessageFrom = lastMessageFrom;
        this.lastMessageIsSeen = lastMessageIsSeen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String isLastMessageIsSeen() {
        return lastMessageIsSeen;
    }

    public void setLastMessageIsSeen(String lastMessageIsSeen) {
        this.lastMessageIsSeen = lastMessageIsSeen;
    }

    public String getLastMessageFrom() {
        return lastMessageFrom;
    }

    public void setLastMessageFrom(String lastMessaageFrom) {
        this.lastMessageFrom = lastMessaageFrom;
    }
}
