package backend;

import java.util.function.Consumer;
import org.json.JSONObject;

public abstract class JSONClient
{
    private Consumer<String> sendCallback;

    protected JSONClient(Consumer<String> sendCallback)
    {
        this.sendCallback = sendCallback;
    }

    protected final void SendMessage(String message)
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
        SendMessage(message.toString());
    }

    public final void ReceiveMessage(String message)
    {
        JSONObject json = new JSONObject(message);
        HandleMessage(json);
    }
    
    protected void HandleMessage(JSONObject message)
    {
        throw new RuntimeException("Unrecognized JSON message:\n" + message);
    }
}
