package model;

import backend.GameClient;

public class Player extends Chatter
{
    private GameClient client;

    public Player(Account account, GameClient client)
    {
        super(account, client);
        this.client = client;
    }

    
}
