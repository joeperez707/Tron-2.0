package Server;

import Jesty.Settings;
import Jesty.TCPBridge.ClientWorker;
import org.json.JSONWriter;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by S199753733 on 12/21/2016.
 */
public class ChatContexts {

    private ArrayList<ChatContext> chatContexts;

    public ChatContexts() {
        chatContexts = new ArrayList<>();
    }

    public void addNewContext(ChatContext chatContext) {
        chatContexts.add(chatContext);
    }

    public void removeContext(ChatContext chatContext) {
        chatContexts.remove(chatContext);
    }

    public ChatContext getContext (String name) {
        for (ChatContext chatContext: chatContexts) {
//            System.out.println("The name: " + name);
//            System.out.println("Is the chatContext null?" + (chatContext == null));
//            System.out.println("The chatContext name: " + chatContext.name);
            if (chatContext.name.equals(name)) {
                return chatContext;
            }
        }
    return null;
    }

    public void doChatMessage(GameServer gameServer, User user, String name, String message) {
        //Inner command messages (stuff that does not have a gui)
        if (message.startsWith("/")) {
            String toSend = "";
            if (message.startsWith("/help")) {
                //Todo: do help
                toSend = "List of all inner chat commands:\n" +
                        "/help (Command) --> Gets help for that command\n" +
                        "/list [all, online, RankName, lobby, clients, propertys] --> get a list of users that meet the criteria\n" +
                        "/ping --> Pong!";
            }
            if (message.startsWith("/list all")) {
                toSend = Authenticate.getUserList();
            }
            else if (message.startsWith("/ping")) {
                toSend = "Pong";
            }
            else if (message.startsWith("/list lobby")) {
                Lobby lobby = user.getCurrentLobby();
                if (lobby != null) {
                    List<User> users = lobby.getUsers();
                    for (User u: users) {
                        toSend += u.chatFormatDisplay() + "\n";
                    }
                }
            }
            else if (message.startsWith("/list clients")) {
                if (Authenticate.checkRank(user.getRank(), Rank.Op)) {
                    for (ClientWorker w: gameServer.getClients()) {
                        toSend += w.toString() + "\n";
                    }
                }
            }
            else if (message.startsWith("/list propertys")) {
                if (Authenticate.checkRank(user.getRank(), Rank.Op)) {
                    toSend = Settings.listPropertys();
                }
            }
            else if (message.startsWith("/changeproperty")) {
                if (Authenticate.checkRank(user.getRank(), Rank.Op)) {
                    String[] split = message.split(" ");
                    Settings.setProperty(split[1], split[2]);
                    toSend = "Changed property: " + split[1] + " to " + split[2] + ".\n" + Settings.listPropertys();
                }
            }
            else if (message.startsWith("/loadpropertys")) {
                if (Authenticate.checkRank(user.getRank(), Rank.Op)) {
                    Settings.load();
                    toSend = "Loaded propertys.\n" + Settings.listPropertys();
                }
            }

            if (!toSend.isEmpty()) {
                StringWriter stringWriter = new StringWriter();
                new JSONWriter(stringWriter).object()
                        .key("argument").value("chatmessage")
                        .key("name").value("Server-->" + user.getName())
                        .key("displayname").value("Server")
                        .key("message").value(toSend)
                        .endObject();
                user.clientWorker.sendMessage(stringWriter.toString());
            }

        }
        else {
            ChatContext chatContext = getContext(name);
            if (chatContext == null) return;
            chatContext.sendMessage(gameServer, user.chatFormatDisplay() + " " + message);
        }
    }

    public void removeUser(User user) {
        for (ChatContext chatContext: chatContexts) {
            if (chatContext.removeUser(user)) {
                //Dissolve the chat
                removeContext(chatContext);
                break;
            }
        }
    }

    public void removeUser (ChatContext chatContext, User user) {
        if (chatContext.removeUser(user)) {
            removeContext(chatContext);
        }
    }
}
