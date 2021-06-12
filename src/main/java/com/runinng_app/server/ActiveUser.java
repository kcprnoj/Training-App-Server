package com.runinng_app.server;

public class ActiveUser {
    public String login;
    public String session;

    ActiveUser (String session, String login){
        this.login = login;
        this.session = session;
    }
}
