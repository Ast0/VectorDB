package backend;

import java.util.List;
import java.util.function.Consumer;

import org.json.JSONObject;

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
    protected Chatter getUser(Account account)
    {
        user = new Player(account, this);
        return user;
    }
    
    @Override
    protected void handleMessage(JSONObject message)
    {
        switch (message.optString("method"))
        {
            case "getItems":
                getItems();
                break;
            
            case "getInventory":
                getInventory();
                break;
            
            case "getOrders":
                getOrders();
                break;
            
            case "createOrder":
                createOrder(
                    message.getString("type"), 
                    message.getInt("itemID"),  
                    message.getInt("amount"), 
                    message.getInt("price")
                );
                break;
            
            case "modifyOrder":
                modifyOrder(
                    message.getInt("orderID"),  
                    message.getInt("amount"), 
                    message.getInt("price")
                );
                break;
            
            case "fulfillOrder":
                fulfillOrder( 
                    message.getInt("orderID"), 
                    message.getInt("amount")
                );
                break;
            
            default:
                super.handleMessage(message);
        }
    }

    private void getItems()
    {

    }

    private void getInventory()
    {
        checkLogin();
    }

    private void getOrders()
    {
        checkLogin();
    }

    private void createOrder(String type, int itemID, int amount, int price)
    {
        checkLogin();
    }

    private void modifyOrder(int orderID, int amount, int price)
    {
        checkLogin();
    }

    private void fulfillOrder(int orderID, int amount)
    {
        checkLogin();
    }
    
    @Override
    public void close()
    {
        activePlayers.remove(user);
        super.close();
    }
}
