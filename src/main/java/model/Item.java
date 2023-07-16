package model;

import com.scalar.db.api.PutBuilder.Buildable;
import com.scalar.db.api.Result;
import com.scalar.db.io.Key;

import backend.Database;

public class Item extends DBTuple
{
    public int id;
    public String name;
    public String description;

    @Override
    public String getNamespace() { return Database.GAME; }
    @Override
    public String getTable() { return Database.ITEM; }
    @Override
    protected Key getPartitionKey() { return Key.ofInt("id", id); }

    public Item(Result tuple) { update(tuple); }
    public Item(int id) { this.id = id; }

    @Override
    public void update(Result tuple)
    {
        id = tuple.getInt("id");
        name = tuple.getText("name");
        description = tuple.getText("description");
    }

    @Override
    protected Buildable completePut(Buildable put)
    {
        return put.intValue("id", id)
                .textValue("name", name)
                .textValue("description", description);
    }
}
