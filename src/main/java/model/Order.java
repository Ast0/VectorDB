package model;

import java.time.Instant;

import com.scalar.db.api.PutBuilder.Buildable;
import com.scalar.db.api.Result;
import com.scalar.db.io.Key;

import backend.Database;

public class Order extends DBTuple
{
    public int id;
    public int itemID;
    public int userID;
    public Instant time;
    public boolean isBuy;
    public int amount;
    public int price;

    @Override
    public String getNamespace() { return Database.GAME; }
    @Override
    public String getTable() { return Database.ORDER; }
    @Override
    protected Key getPartitionKey() { return Key.ofInt("id", id); }

    public Order(Result tuple) { update(tuple); }
    public Order(int id) { this.id = id; }

    @Override
    public void update(Result tuple)
    {
        id = tuple.getInt("id");
        itemID = tuple.getInt("itemID");
        userID = tuple.getInt("userID");
        time = Instant.ofEpochSecond(tuple.getBigInt("time"));
        isBuy = tuple.getBoolean("isBuy");
        amount = tuple.getInt("amount");
        price = tuple.getInt("price");
    }

    @Override
    protected Buildable completePut(Buildable put)
    {
        return put//.intValue("id", id)
                .intValue("itemID", itemID)
                .intValue("userID", userID)
                .bigIntValue("time", time.getEpochSecond())
                .booleanValue("isBuy", isBuy)
                .intValue("amount", amount)
                .intValue("price", price);
    }
}
