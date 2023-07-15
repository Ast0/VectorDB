package backend;

import java.util.function.Consumer;

import org.json.JSONObject;

public class GameClient extends ChatClient
{
    protected GameClient(Consumer<String> sendCallback) {
        super(sendCallback);
    }
    
    @Override
    protected void HandleMessage(JSONObject message)
    {
        switch (message.optString("method"))
        {
            case "GetInventory":
                GetInventory();
                break;
            
            case "GetOrders":
                GetOrders();
                break;
            
            case "CreateOrder":
                CreateOrder(
                    message.getString("type"), 
                    message.getInt("itemID"),  
                    message.getInt("amount"), 
                    message.getInt("price")
                );
                break;
            
            case "FulfillOrder":
                FulfillOrder( 
                    message.getInt("orderID"), 
                    message.getInt("amount")
                );
                break;
            
            default:
                super.HandleMessage(message);
        }
    }

    private void GetInventory()
    {

    }

    private void GetOrders()
    {

    }

    private void CreateOrder(String type, int itemID, int amount, int price)
    {

    }

    private void FulfillOrder(int orderID, int amount)
    {

    }
}
