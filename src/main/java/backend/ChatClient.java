package backend;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.json.JSONArray;
import org.json.JSONObject;

import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.Result;
import com.scalar.db.api.Scan;
import com.scalar.db.api.Scan.Ordering;
import com.scalar.db.api.ScanBuilder.BuildableScan;
import com.scalar.db.exception.transaction.AbortException;
import com.scalar.db.io.Key;

import model.Account;
import model.Chatter;
import model.Message;

public class ChatClient extends JSONClient
{
    private Chatter user;
    private List<Chatter> activeChatters;
    protected Database database;

    protected ChatClient(Consumer<String> sendCallback, List<Chatter> activeChatters) {
        super(sendCallback);

        this.activeChatters = activeChatters;
        database = Database.getSingleton();
    }

    protected JSONObject success()
    {
        return new JSONObject().put("type", "success");
    }

    protected JSONObject value(Object value)
    {
        return new JSONObject().put("type", "value").put("value", value);
    }

    protected JSONObject error(String message)
    {
        return new JSONObject().put("type", "error").put("message", message);
    }

    @Override
    protected void handleMessage(JSONObject message)
    {
        JSONObject response;

        try
        {
            System.out.printf("Received method call to %s.\n", message.optString("method"));
            response = handleMethod(message);
        }
        catch (Exception e)
        {
            System.out.println(e);
            response = error(e.getMessage());
        }

        response.put("method", message.optString("method"));
        SendMessage(response);
    }
    
    protected JSONObject handleMethod(JSONObject message)
    {
        switch (message.optString("method"))
        {
            case "createAccount":
                return createAccount(
                    message.getString("username"), 
                    message.getString("email"), 
                    message.getString("password")
                );

            case "login":
                return login(
                    message.getString("username"), 
                    message.getString("password")
                );
            
            case "logout":
                return logout();

            case "changePassword":
                changePassword(
                    message.getString("oldPassword"), 
                    message.getString("newPassword")
                );
            
            case "findUserID":
                return findUserID(
                    message.getString("username")
                );
            
            case "getUsername":
                return getUsername(
                    message.getInt("id")
                );

            case "getMessages":
                return getMessages(
                    message.optInt("contact"), 
                    message.optNumber("limit")
                );
            
            case "sendMessage":
                return sendMessage(
                    message.getInt("contact"), 
                    message.getString("text")
                );
            
            default:
                return error("Invalid JSON message");
        }
    }

    protected Chatter loginUser(Account account)
    {
        return new Chatter(account, this);
    }

    protected void logoutUser()
    {
        activeChatters.remove(user);
        user = null;
    }

    private JSONObject createAccount(String username, String email, String password)
    {
        DistributedTransaction transaction = null;
        try
        {
            transaction = database.startTransaction();
            Account account;
            
            try {
                account = Account.getByName(transaction, username);
                if (account != null)
                    return error("An account with that username already exists.");
            } catch (Exception e) {
                return error(e.getMessage());
            }

            account = database.createAccount();

            account.username = username;
            account.email = email;
            account.password = password;

            transaction.put(account.getPut());
            transaction.commit();

            return success();
        }
        catch (Exception e)
        {
            return handleTransactionException(transaction, e);
        }
    }

    private JSONObject login(String username, String password)
    {
        DistributedTransaction transaction = null;
        try
        {
            transaction = database.startTransaction();

            Account account = Account.getByName(transaction, username);
            transaction.commit();

            if (account != null) // TODO passwords
            {
                user = loginUser(account);
                return value(user.id);
            }
            
            return error("Login failed.");
        }
        catch (Exception e)
        {
            return handleTransactionException(transaction, e);
        }
    }

    private JSONObject logout()
    {
        if (user != null)
        {
            logoutUser();
        }
        return success();
    }

    private JSONObject changePassword(String oldPassword, String newPassword)
    {
        checkLogin();
        return error("Not implemented...");
    }

    private JSONObject findUserID(String username)
    {
        DistributedTransaction transaction = null;
        try
        {
            transaction = database.startTransaction();

            Account account = Account.getByName(transaction, username);
            transaction.commit();

            if (account != null)
            {
                return value(account.id);
            }
            
            return error("An account with that username does not exist.");
        }
        catch (Exception e)
        {
            return handleTransactionException(transaction, e);
        }
    }

    private JSONObject getUsername(int id)
    {
        checkLogin();
        
        DistributedTransaction transaction = null;
        try
        {
            transaction = database.startTransaction();

            Account account = new Account(id);
            Optional<Result> result = transaction.get(account.getGet());
            transaction.commit();

            if (result.isPresent())
            {
                return value(user.name);
            }
            
            return error("User not found.");
        }
        catch (Exception e)
        {
            return handleTransactionException(transaction, e);
        }
    }

    private JSONObject getMessages(int contact, Number limit)
    {
        checkLogin();

        DistributedTransaction transaction = null;
        try
        {
            transaction = database.startTransaction();

            Ordering ordering = Ordering.desc("time");

            Key key;
            if (contact == 0)
                key = Key.ofInt("sender", user.id);
            else
                key = Key.of("sender", user.id, "receiver", contact);


            BuildableScan scan = new Message().getScanBuilder()
                        .partitionKey(key)
                        .ordering(ordering);
            if (limit != null)
            {
                scan = scan.limit(limit.intValue());
            }
            
            List<Result> result = transaction.scan(scan.build());
            
            if (contact == 0)
                key = Key.ofInt("receiver", user.id);
            else
                key = Key.of("receiver", user.id, "sender", contact);


            scan = new Message().getScanBuilder()
                        .partitionKey(key)
                        .ordering(ordering);
            if (limit != null)
            {
                scan = scan.limit(limit.intValue());
            }
            

            result.addAll(transaction.scan(scan.build()));
            transaction.commit();

            JSONArray array = new JSONArray(result.size());
            array.putAll(result.stream().map(Message::new));

            return value(array);
        }
        catch (Exception e)
        {
            return handleTransactionException(transaction, e);
        }
    }

    private JSONObject sendMessage(int contact, String text)
    {
        checkLogin();

        DistributedTransaction transaction = null;
        try
        {
            transaction = database.startTransaction();

            Message message = new Message();
            message.sender = user.id;
            message.receiver = contact;
            message.time = Instant.now();
            message.text = text;

            transaction.put(message.getPut());
            transaction.commit();
            return success();
        }
        catch (Exception e)
        {
            return handleTransactionException(transaction, e);
        }
    }

    protected void checkLogin()
    {
        if (user == null)
        {
            throw new RuntimeException("Client must be logged in to perform this action.");
        }
    }

    @Override
    protected void handleError(String message, Exception cause)
    {
        try
        {
            SendMessage(error(message));
        }
        catch (Exception e)
        {
            super.handleError("Error handling error: " + message, e);
        }
    }
    
    private JSONObject handleTransactionException(DistributedTransaction transaction, Exception e)
    {
        try
        {
            if (transaction != null)
                transaction.abort();
        }
        catch (AbortException f)
        {
            return error(f.getMessage());
        }
        return error(e.getMessage());
    }

    @Override
    public void close()
    {
        activeChatters.remove(user);
        super.close();
    }
}
