package model;

import backend.ChatClient;

public class Chatter
{
    public final int id;
    public final String name;
    public final String email;

    private final ChatClient client;

    public Chatter(Account account, ChatClient client)
    {
        id = account.id;
        name = account.username;
        email = account.email;

        this.client = client;
    }

    
}
