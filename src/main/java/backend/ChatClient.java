package backend;

import java.util.List;
import java.util.function.Consumer;

import org.json.JSONObject;

import model.Account;
import model.Chatter;

public class ChatClient extends JSONClient
{
    private Chatter user;
    private List<Chatter> activeChatters;

    protected ChatClient(Consumer<String> sendCallback, List<Chatter> activeChatters) {
        super(sendCallback);

        this.activeChatters = activeChatters;
    }

    @Override
    protected void handleMessage(JSONObject message)
    {
        switch (message.optString("method"))
        {
            case "createAccount":
                createAccount(
                    message.getString("username"), 
                    message.getString("email"), 
                    message.getString("password")
                );
                break;

            case "login":
                login(
                    message.getString("username"), 
                    message.getString("password")
                );
                break;
            
            case "logout":
                logout();
                break;

            case "changePassword":
                changePassword(
                    message.getString("oldPassword"), 
                    message.getString("newPassword")
                );
                break;
            
            case "findUser":
                findUser(
                    message.getString("username")
                );
                break;
            
            case "getMessages":
                getMessages(
                    message.optInt("contact"), 
                    message.optNumber("limit")
                );
                break;
            
            case "sendMessage":
                sendMessage(
                    message.getInt("contact"), 
                    message.getString("text")
                );
                break;
            
            default:
                super.handleMessage(message);
        }
    }

    protected Chatter getUser(Account account)
    {
        return new Chatter(account, this);
    }

    private void createAccount(String username, String email, String password)
    {

    }

    private void login(String username, String password)
    {

    }

    private void logout()
    {

    }

    private void changePassword(String oldPassword, String newPassword)
    {
        checkLogin();
    }

    private void findUser(String username)
    {

    }

    private void getMessages(int contact, Number limit)
    {
        checkLogin();
    }

    private void sendMessage(int contact, String text)
    {
        checkLogin();
    }

    protected void checkLogin()
    {
        if (user == null)
        {
            throw new RuntimeException("Client must be logged in to perform this action.");
        }
    }
    
    @Override
    public void close()
    {
        activeChatters.remove(user);
        super.close();
    }
}
