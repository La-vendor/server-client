package com.lavendor;

public class User {
    private long id;
    private String nick;
    private String login;

    public User() {
    }

    public String getNick() {
        return nick;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
