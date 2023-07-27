package model;

import com.scalar.db.api.PutBuilder.Buildable;
import com.scalar.db.api.Result;
import com.scalar.db.io.Key;

import backend.Database;

public class Inventory extends DBTuple
{
    public int userID;
    public int itemID;
    public int amount;

    @Override
    public String getNamespace() { return Database.GAME; }
    @Override
    public String getTable() { return Database.INVENTORY; }
    @Override
    protected Key getPartitionKey()
    {
        return Key.ofInt("userID", userID);
    }
    @Override
    protected Key getClusteringKey()
    {
        return Key.ofInt("itemID", itemID);
    }

    public Inventory(Result tuple) { update(tuple); }
    public Inventory(int userID, int itemID)
    {
        this.userID = userID;
        this.itemID = itemID;
    }

    @Override
    public void update(Result tuple)
    {
        userID = tuple.getInt("userID");
        itemID = tuple.getInt("itemID");
        amount = tuple.getInt("amount");
    }

    @Override
    protected Buildable completePut(Buildable put)
    {
        return put.intValue("itemID", itemID)
                .intValue("amount", amount);
    }
}
