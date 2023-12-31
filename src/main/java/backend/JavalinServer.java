package backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.javalin.Javalin;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;

import model.Chatter;
import model.Player;

public class JavalinServer
{
    private Map<WsContext, JSONClient> clientMap;

    private List<Chatter> activeChatters;
    private List<Player> activePlayers;

    public JavalinServer()
    {
        clientMap = new HashMap<WsContext, JSONClient>();
        activeChatters = new ArrayList<Chatter>();
        activePlayers = new ArrayList<Player>();
    }

    public void start()
    {
        try
        {
            Database.createSingleton(".\\scalardb.properties");
            
            Javalin app = Javalin.create(config -> {
            }).start();
            
            app.ws("/chat", ws -> configureWs(ws, false));
            app.ws("/game", ws -> configureWs(ws, true));
            
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not start server.", e);
        }
    }

    public static void main(String[] args)
    {
        new JavalinServer().start();
    }

    private void configureWs(WsConfig ws, boolean useGameClient)
    {
        if (useGameClient)
        {
            ws.onConnect(ctx -> {
                System.out.println("Connected.");
                clientMap.put(ctx, new GameClient(message -> ctx.send(message), activeChatters, activePlayers));
            });
        }
        else
        {
            ws.onConnect(ctx -> {
                System.out.println("Connected.");
                ctx.enableAutomaticPings(10, TimeUnit.SECONDS);
                clientMap.put(ctx, new ChatClient(message -> ctx.send(message), activeChatters));
            });
        }

        ws.onClose(ctx -> {
                System.out.println("Disconnected.");
            clientMap.get(ctx).close();
            clientMap.remove(ctx);
        });
        ws.onMessage(ctx -> {
            System.out.println("Received message.");
            clientMap.get(ctx).receiveMessage(ctx.message());
        });
    }
}
