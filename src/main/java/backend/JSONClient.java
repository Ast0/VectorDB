package backend;

import java.util.function.Consumer;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class JSONClient
{
    private Consumer<String> sendCallback;

    protected JSONClient(Consumer<String> sendCallback)
    {
        this.sendCallback = sendCallback;
    }

    protected final void sendMessage(String message)
    {
        try
        {
            sendCallback.accept(message);
        }
        catch (Exception e)
        {
            throw e;
        }
    }
    
    protected final void SendMessage(JSONObject message)
    {
        sendMessage(message.toString());
    }

    public final void receiveMessage(String message)
    {
        try
        {
            JSONObject json = new JSONObject(message);
            handleMessage(json);
        }
        catch (JSONException e)
        {
            throw new RuntimeException("Received invalid JSON message.", e);
        }
    }
    
    protected void handleMessage(JSONObject message)
    {
        throw new RuntimeException("Unrecognized JSON message:\n" + message);
    }

    public void close() {}
}
