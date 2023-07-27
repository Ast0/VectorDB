package backend;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;

import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.Result;
import com.scalar.db.exception.transaction.AbortException;

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
            
            account = Account.getByName(transaction, username);
            if (account != null)
            {
                transaction.commit();
                return error("An account with that username already exists.");
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

            if (account != null && account.password == password)
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
            Stream<Message> result = transaction.scan(new Message().getScanAll()).stream().map(Message::new);
            transaction.commit();

            JSONArray array = new JSONArray(result.filter(t -> t.sender == contact && t.receiver == user.id || t.receiver == contact && t.sender == user.id).map(t -> t.toJson()).toList());

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
    
    protected JSONObject handleTransactionException(DistributedTransaction transaction, Exception e)
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
