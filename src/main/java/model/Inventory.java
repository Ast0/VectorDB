package model;

import com.scalar.db.api.PutBuilder.Buildable;
import com.scalar.db.api.Result;
import com.scalar.db.io.Key;

import backend.Database;

public class Inventory extends DBTuple
{
    public int itemID;
    public int userID;
    public int amount;

    @Override
    public String getNamespace() { return Database.GAME; }
    @Override
    public String getTable() { return Database.INVENTORY; }
    @Override
    protected Key getPartitionKey()
    {
        return Key.of("itemID", itemID, "userID", userID);
    }

    public Inventory(Result tuple) { update(tuple); }
    public Inventory(int itemID, int userID)
    {
        this.itemID = itemID;
        this.userID = userID;
    }

    @Override
    public void update(Result tuple)
    {
        itemID = tuple.getInt("itemID");
        userID = tuple.getInt("userID");
        amount = tuple.getInt("amount");
    }

    @Override
    protected Buildable completePut(Buildable put)
    {
        return put//.intValue("itemID", itemID)
                //.intValue("userID", userID)
                .intValue("amount", amount);
    }
}
