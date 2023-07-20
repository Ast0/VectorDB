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
            handleError("Received invalid JSON message.", e);
        }
        catch (Exception e)
        {
            handleError("Internal Server Error", e);
        }
    }
    
    protected void handleMessage(JSONObject message)
    {
        handleError("Unrecognized JSON message:\n" + message, null);
    }

    protected void handleError(String message, Exception cause)
    {
        throw new RuntimeException(message, cause);
    }

    public void close() {}
}
