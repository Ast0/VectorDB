package model;

import com.scalar.db.api.PutBuilder.Buildable;
import com.scalar.db.api.Result;
import com.scalar.db.io.Key;

import backend.Database;

public class Account extends DBTuple
{
    public int id;
    public String username;
    public String email;
    public String password;

    @Override
    public String getNamespace() { return Database.CHAT; }
    @Override
    public String getTable() { return Database.ACCOUNT; }
    @Override
    protected Key getPartitionKey() { return Key.ofInt("id", id); }

    public Account(Result tuple) { update(tuple); }
    public Account(int id) { this.id = id; }

    @Override
    public void update(Result tuple)
    {
        id = tuple.getInt("id");
        username = tuple.getText("username");
        email = tuple.getText("email");
        password = tuple.getText("password");
    }

    @Override
    protected Buildable completePut(Buildable put)
    {
        return put.intValue("id", id)
                .textValue("username", username)
                .textValue("email", email)
                .textValue("password", password);
    }
}
