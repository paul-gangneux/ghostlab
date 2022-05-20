package model;

public enum ChatScope {
    INCOMING_PRIVATE_MSG,
    OUTGOING_PRIVATE_MSG,
    OUTGOING_FAILED_PRIVATE_MSG,
    TEAM_MSG,
    GLOBAL_MSG,
    SERVER_MSG
    // should we add sending / receiving scopes ? nah
    // How does the chat store with the private message you sent, since the server doesn't remind you ?
    // -> View has to store this (done)
}
