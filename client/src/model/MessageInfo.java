package model;

public class MessageInfo {
    private ChatScope scope;
    private String playerName; // if this is an ongoing DM, the name of the receiver. In any other case, the name of the sender.
    // Think of it as : the name we can't guess from the message scope.
    private String messageContent;

    public ChatScope getScope() {
        return scope;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getContent() {
        return messageContent;
    }

    public MessageInfo(String content) {
        this(ChatScope.SERVER_MSG, "[SERVER]", content);
    }

    public MessageInfo(String name, String content) {
        this(ChatScope.GLOBAL_MSG, name, content);
    }

    public MessageInfo(ChatScope scope, String name, String content) {
        this.scope = scope;
        playerName = name;
        messageContent = content;
    }
}
