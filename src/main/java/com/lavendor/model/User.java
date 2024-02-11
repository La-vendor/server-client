package com.lavendor.model;

import java.util.Objects;

public class User {
    private long id;
    private String nick;
    private String login;

    public User() {
    }

    public User(long id, String nick, String login) {
        this.id = id;
        this.nick = nick;
        this.login = login;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && Objects.equals(nick, user.nick) && Objects.equals(login, user.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nick, login);
    }
}
