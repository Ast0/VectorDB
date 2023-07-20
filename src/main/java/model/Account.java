package model;

import java.util.List;

import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.PutBuilder.Buildable;
import com.scalar.db.exception.transaction.CrudConflictException;
import com.scalar.db.exception.transaction.CrudException;
import com.scalar.db.api.Result;
import com.scalar.db.api.Scan;
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
        return put//.intValue("id", id)
                .textValue("username", username)
                .textValue("email", email)
                .textValue("password", password);
    }

    public static Account getByName(DistributedTransaction transaction, String username) throws CrudConflictException, CrudException
    {
        Account account = new Account(0);

        List<Result> result = transaction.scan(account.getScanAll());

        Account temp = new Account(0);
        boolean found = false;
        
        for (Result tuple : result)
        {
            temp.update(tuple);
            
            if (username.equals(temp.username))
            {
                if (found)
                {
                    throw new RuntimeException(String.format("Found %d accounts with name \"%s\"", result.size(), username));
                }
                found = true;
                account.update(tuple);
            }
        }

        return found ? account : null;
    }
    // {
    //     Account account = new Account(0);
    //     Key key = Key.ofText("username", username);

    //     Scan scan = account.getScanBuilder()
    //                     .partitionKey(key)
    //                     .build();
        
    //     List<Result> result = transaction.scan(scan);

    //     if (result.size() > 1)
    //     {
    //         throw new RuntimeException(String.format("Found %d accounts with name \"%s\"", result.size(), username));
    //     }
    //     else if (result.size() == 0)
    //     {
    //         return null;
    //     }
        
    //     account.update(result.get(0));
    //     return account;
    // }
}
