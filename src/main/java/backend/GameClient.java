package backend;

import java.util.List;
import java.util.function.Consumer;

import org.json.JSONObject;

import kotlin.NotImplementedError;
import model.Account;
import model.Chatter;
import model.Player;

public class GameClient extends ChatClient
{
    private Player user;
    private List<Player> activePlayers;

    protected GameClient(Consumer<String> sendCallback, List<Chatter> activeChatters, List<Player> activePlayers) {
        super(sendCallback, activeChatters);

        this.activePlayers = activePlayers;
    }

    @Override
    protected Chatter loginUser(Account account)
    {
        user = new Player(account, this);
        return user;
    }

    @Override
    protected void logoutUser()
    {
        activePlayers.remove(user);
        user = null;
        super.logoutUser();
    }
    
    @Override
    protected JSONObject handleMethod(JSONObject message)
    {
        switch (message.optString("method"))
        {
            case "getItems":
                return getItems();
            
            case "getInventory":
                return getInventory();
            
            case "getOrders":
                return getOrders();
            
            case "createOrder":
                return createOrder(
                    message.getString("type"), 
                    message.getInt("itemID"),  
                    message.getInt("amount"), 
                    message.getInt("price")
                );
            
            case "modifyOrder":
                return modifyOrder(
                    message.getInt("orderID"),  
                    message.getInt("amount"), 
                    message.getInt("price")
                );
            
            case "fulfillOrder":
                return fulfillOrder( 
                    message.getInt("orderID"), 
                    message.getInt("amount")
                );
            
            default:
                return super.handleMethod(message);
        }
    }

    private JSONObject getItems()
    {
        throw new NotImplementedError();
    }

    private JSONObject getInventory()
    {
        checkLogin();
        throw new NotImplementedError();
    }

    private JSONObject getOrders()
    {
        checkLogin();
        throw new NotImplementedError();
    }

    private JSONObject createOrder(String type, int itemID, int amount, int price)
    {
        checkLogin();
        throw new NotImplementedError();
    }

    private JSONObject modifyOrder(int orderID, int amount, int price)
    {
        checkLogin();
        throw new NotImplementedError();
    }

    private JSONObject fulfillOrder(int orderID, int amount)
    {
        checkLogin();
        throw new NotImplementedError();
    }
    
    @Override
    public void close()
    {
        activePlayers.remove(user);
        super.close();
    }
}
