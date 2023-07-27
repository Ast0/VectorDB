package model;

import java.time.Instant;

import org.json.JSONObject;

import com.scalar.db.api.PutBuilder.Buildable;
import com.scalar.db.api.Result;
import com.scalar.db.io.Key;

import backend.Database;

public class Message extends DBTuple
{
    public int sender;
    public int receiver;
    public Instant time;
    public String text;

    @Override
    public String getNamespace() { return Database.CHAT; }
    @Override
    public String getTable() { return Database.MESSAGE; }
    @Override
    protected Key getPartitionKey()
    {
        return Key.ofInt("sender", sender);
    }
    @Override
    protected Key getClusteringKey()
    {
        return Key.of("receiver", receiver, "time", time.getEpochSecond());
    }

    public Message(Result tuple) { update(tuple); }
    public Message() {}

    @Override
    public void update(Result tuple)
    {
        sender = tuple.getInt("sender");
        receiver = tuple.getInt("receiver");
        time = Instant.ofEpochSecond(tuple.getBigInt("time"));
        text = tuple.getText("text");
    }

    @Override
    protected Buildable completePut(Buildable put)
    {
        return put//.intValue("sender", sender)
                .intValue("receiver", receiver)
                .bigIntValue("time", time.getEpochSecond())
                .textValue("text", text);
    }

    @Override
    public JSONObject toJson() 
    {
        JSONObject json = new JSONObject();

        json.put("sender", sender);
        json.put("receiver", receiver);
        json.put("time", time.getEpochSecond());
        json.put("text", text);

        return json;
    }
}
