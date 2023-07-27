package backend;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.Result;
import com.scalar.db.api.Scan;
import com.scalar.db.api.Scan.Ordering;
import com.scalar.db.exception.storage.ExecutionException;
import com.scalar.db.exception.transaction.CrudConflictException;
import com.scalar.db.exception.transaction.CrudException;
import com.scalar.db.io.Key;

import model.Account;
import model.Chatter;
import model.DBTuple;
import model.Inventory;
import model.Order;
import model.Player;

public class GameClient extends ChatClient
{
    private static final int MONEY = 1;

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
                return getOrders(
                    message.getInt("itemID")
                );
            
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

    private JSONArray resultToJsonArray(List<Result> results, DBTuple model)
    {
        JSONArray array = new JSONArray(results.size());

        for (Result tuple : results)
        {
            model.update(tuple);
            array.put(model.toJson());
        }

        return array;
    }

    private JSONObject getItems()
    {
        try
        {
            return value(new JSONArray(database.getItems()));
        }
        catch (JSONException | IOException | ExecutionException e)
        {
            return error(e.getMessage());
        }
    }

    private JSONObject getInventory()
    {
        checkLogin();

        DistributedTransaction transaction = null;
        try
        {
            transaction = database.startTransaction();
            
            Inventory temp = new Inventory(0, 0);
            Key key = Key.ofInt("userID", user.id);
            Scan scan = temp.getScanBuilder().partitionKey(key).build();

            List<Result> result = transaction.scan(scan);
            transaction.commit();
            return value(resultToJsonArray(result, temp));
        }
        catch (Exception e)
        {
            return handleTransactionException(transaction, e);
        }    
    }

    private JSONObject getOrders(int itemID)
    {
        checkLogin();

        DistributedTransaction transaction = null;
        try
        {
            transaction = database.startTransaction();
            
            Order temp = new Order(0);
            Key key = Key.ofInt("itemID", itemID);
            Scan scan = temp.getScanBuilder().partitionKey(key).build();

            List<Result> result = transaction.scan(scan);
            transaction.commit();
            return value(resultToJsonArray(result, temp));
        }
        catch (Exception e)
        {
            return handleTransactionException(transaction, e);
        } 
    }

    private JSONObject createOrder(String type, int itemID, int amount, int price)
    {
        checkLogin();
        
        boolean buy = false;
        if (type.equalsIgnoreCase("buy"))
            buy = true;
        else if (!type.equalsIgnoreCase("sell"))
            return error("unknown order type");
        
        Order order = database.createOrder();
        order.isBuy = buy;
        order.itemID = itemID;
        order.userID = user.id;
        order.time = Instant.now();
        order.amount = amount;
        order.price = price;
        
        DistributedTransaction transaction = null;
        try
        {
            transaction = database.startTransaction();

            if (!subtractInventory(transaction, buy ? MONEY : itemID, buy ? amount * price : amount))
            {
                transaction.abort();
                return error(buy ? "not enough currency" : "not enough stock in inventory");
            }

            fulfillMatchingOrders(transaction, order);
            if (order.amount > 0)
                transaction.put(order.getPut());
            
            transaction.commit();
            return success();
        }
        catch (Exception e)
        {
            return handleTransactionException(transaction, e);
        } 
    }

    private JSONObject modifyOrder(int orderID, int amount, int price)
    {
        checkLogin();

        DistributedTransaction transaction = null;
        try
        {
            transaction = database.startTransaction();
            Order order = new Order(orderID);

            Optional<Result> result = transaction.get(order.getGet());
            if (result.isEmpty())
            {
                transaction.commit();
                return error("order not found");
            }

            order.update(result.get());

            if (order.isBuy)
            {
                int difference = amount * price - order.amount * order.price;

                if (difference > 0)
                    if (!subtractInventory(transaction, MONEY, difference))
                    {
                        transaction.abort();
                        return error("not enough stock in inventory");
                    }
                else if (difference < 0)
                    addInventory(transaction, user.id, MONEY, -difference);
            }
            else
            {
                int difference = amount - order.amount;
                
                if (difference > 0)
                {
                    if (!subtractInventory(transaction, order.itemID, difference))
                    {
                        transaction.abort();
                        return error("not enough currency");
                    }
                }
                else if (difference < 0)
                    addInventory(transaction, user.id, order.itemID, -difference);
            }

            fulfillMatchingOrders(transaction, order);

            if (order.amount > 0)
                transaction.put(order.getPut());
            else
                transaction.delete(order.getDelete());

            transaction.commit();
            return success();
        }
        catch (Exception e)
        {
            return handleTransactionException(transaction, e);
        } 
    }

    private JSONObject fulfillOrder(int orderID, int amount)
    {
        checkLogin();

        DistributedTransaction transaction = null;
        try
        {
            transaction = database.startTransaction();
            Order order = new Order(orderID);

            Optional<Result> result = transaction.get(order.getGet());
            if (result.isEmpty())
                return error("order not found");
            
            if (!subtractInventory(transaction, order.isBuy ? order.itemID : MONEY, order.isBuy ? amount : amount * order.price))
            {
                return error(!order.isBuy ? "not enough currency" : "not enough stock in inventory");
            }

            fulfillSingleOrder(transaction, order, amount);
            transaction.commit();
            return success();
        }
        catch (Exception e)
        {
            return handleTransactionException(transaction, e);
        } 
    }

    private void addInventory(DistributedTransaction transaction, int userID, int itemID, int amount) throws CrudConflictException, CrudException
    {
        Inventory inventory = new Inventory(userID, itemID);
        Optional<Result> result = transaction.get(inventory.getGet());

        if (result.isPresent())
        {
            inventory.update(result.get());
            inventory.amount += amount;
        }
        else
        {
            inventory.amount = amount;
        }
        transaction.put(inventory.getPut());
    }

    private boolean subtractInventory(DistributedTransaction transaction, int itemID, int amount) throws CrudConflictException, CrudException
    {
        Inventory inventory = new Inventory(user.id, itemID);
        Optional<Result> result = transaction.get(inventory.getGet());

        if (result.isPresent())
            inventory.update(result.get());
        
        if (result.isEmpty() || inventory.amount < amount)
            return false;
        
        inventory.amount -= amount;
        transaction.put(inventory.getPut());
        return true;
    }

    private void fulfillSingleOrder(DistributedTransaction transaction, Order order, int amount) throws IllegalArgumentException, CrudConflictException, CrudException
    {
        if (order.amount < amount)
            throw new IllegalArgumentException("invalid amount fo order");
        
        order.amount -= amount;
        addInventory(transaction, order.userID, order.isBuy ? order.itemID : MONEY, order.isBuy ? amount : amount * order.price);

        if (order.amount > 0)
            transaction.put(order.getPut());
        else
            transaction.delete(order.getDelete());
    }

    private void fulfillMatchingOrders(DistributedTransaction transaction, Order order) throws CrudConflictException, CrudException
    {
        Key key = Key.ofInt("itemID", order.itemID);
        Ordering ordering = order.isBuy ? Ordering.asc("price") : Ordering.desc("price");

        Scan scan = order.getScanBuilder().partitionKey(key).ordering(ordering).build();
        List<Result> result = transaction.scan(scan);
        Order other = new Order(0);

        for (Result tuple : result)
        {
            if (order.amount <= 0)
                break;

            other.update(tuple);
            if (other.isBuy != order.isBuy && order.isBuy ? other.price <= order.price : other.price >= order.price)
            {
                fulfillSingleOrder(transaction, order, other.amount < order.amount ? other.amount : order.amount);
                order.amount -= other.amount;
            }
        }
    }
    
    @Override
    public void close()
    {
        activePlayers.remove(user);
        super.close();
    }
}
