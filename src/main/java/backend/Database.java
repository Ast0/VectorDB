package backend;

import java.io.IOException;
import java.util.List;

import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.DistributedTransactionManager;
import com.scalar.db.api.Result;
import com.scalar.db.api.Scan;
import com.scalar.db.exception.storage.ExecutionException;
import com.scalar.db.exception.transaction.TransactionException;
import com.scalar.db.service.StorageFactory;
import com.scalar.db.service.TransactionFactory;

import model.Account;
import model.Item;
import model.Order;

public class Database
{
    public static final String CHAT = "chat";
    public static final String GAME = "game";

    public static final String ACCOUNT = "account";
    public static final String MESSAGE = "message";
    public static final String ITEM = "item";
    public static final String INVENTORY = "inventory";
    public static final String ORDER = "order";

    private String propertiesPath;
    private DistributedTransactionManager manager;

    private Item[] items = null;

    private int nextAccountID;
    private int nextOrderID;

    public Database(String propertiesPath) throws IOException
    {
        this.propertiesPath = propertiesPath;

        TransactionFactory factory = TransactionFactory.create(propertiesPath);
        manager = factory.getTransactionManager();
    }

    private static Database singleton;

    public static Database createSingleton(String propertiesPath) throws IOException
    {
        return singleton = new Database(propertiesPath);
    }

    public static Database getSingleton()
    {
        return singleton;
    }

    public DistributedTransaction startTransaction() throws TransactionException
    {
        return manager.start();
    }

    public Item[] getItems() throws IOException, ExecutionException
    {
        if (items == null)
        {
            StorageFactory factory = StorageFactory.create(propertiesPath);
            Scan scan = new Item(0).getScanAll();
            
            List<Result> result = factory.getStorage().scan(scan).all();
            items = result.stream().map(t -> new Item(t)).toArray(Item[]::new);
        }

        return items;
    }

    public Account createAccount()
    {
        int id = nextAccountID++;
        if (nextAccountID == 0)
        {
            throw new RuntimeException("Ran out of account ids!");
        }
        return new Account(id);
    }
    
    public Order createOrder()
    {
        int id = nextOrderID++;
        if (nextOrderID == 0)
        {
            throw new RuntimeException("Ran out of order ids!");
        }
        return new Order(id);
    }
}
