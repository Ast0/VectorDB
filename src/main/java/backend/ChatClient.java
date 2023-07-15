package backend;

import java.util.function.Consumer;

import org.json.JSONObject;

public class ChatClient extends JSONClient
{
    protected ChatClient(Consumer<String> sendCallback) {
        super(sendCallback);
    }

    @Override
    protected void HandleMessage(JSONObject message)
    {
        switch (message.optString("method"))
        {
            case "CreateAccount":
                CreateAccount(
                    message.getString("username"), 
                    message.getString("email"), 
                    message.getString("password")
                );
                break;

            case "Login":
                Login(
                    message.getString("username"), 
                    message.getString("password")
                );
                break;
            
            case "Logout":
                Logout();
                break;

            case "ChangePassword":
                ChangePassword(
                    message.getString("oldPassword"), 
                    message.getString("newPassword")
                );
                break;
            
            case "GetMessages":
                GetMessages(
                    message.optString("contact"), 
                    message.optNumber("amount")
                );
                break;
            
            case "SendMessage":
                SendMessage(
                    message.getString("contact"), 
                    message.getString("text")
                );
                break;
            
            default:
                super.HandleMessage(message);
        }
    }

    private void CreateAccount(String username, String email, String password)
    {

    }

    private void Login(String username, String password)
    {

    }

    private void Logout()
    {

    }

    private void ChangePassword(String oldPassword, String newPassword)
    {

    }

    private void GetMessages(String contact, Number amount)
    {

    }

    private void SendMessage(String contact, String text)
    {

    }
}
