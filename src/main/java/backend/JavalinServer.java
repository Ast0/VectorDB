package backend;

import java.util.Map;
import java.util.function.Consumer;

import io.javalin.Javalin;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;

public class JavalinServer
{
    private Map<WsContext, JSONClient> clientMap;

    public void start()
    {
        Javalin app = Javalin.create(config -> {
        }).start();

        app.ws("/chat", ws -> ConfigureWs(ws, false));
        app.ws("/game", ws -> ConfigureWs(ws, true));

    }

    public static void main(String[] args)
    {
        new JavalinServer().start();
    }

    private void ConfigureWs(WsConfig ws, boolean useGameClient)
    {
        if (useGameClient)
        {
            ws.onConnect(ctx -> {
                clientMap.put(ctx, new GameClient(message -> ctx.send(message)));
            });
        }
        else
        {
            ws.onConnect(ctx -> {
                clientMap.put(ctx, new ChatClient(message -> ctx.send(message)));
            });
        }

        ws.onClose(ctx -> clientMap.remove(ctx));
        ws.onMessage(ctx -> clientMap.get(ctx).ReceiveMessage(ctx.message()));
    }
}
