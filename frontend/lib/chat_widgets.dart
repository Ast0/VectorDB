import 'package:flutter/material.dart';

import 'common_widgets.dart';
import 'model.dart';
import 'server.dart';
import 'websocket.dart';

class ChatView extends StatefulWidget {
  final ChatServer server;

  const ChatView({required this.server, super.key});

  @override
  State<StatefulWidget> createState() => ChatState(server);
}

class ChatState extends State<ChatView> {
  Map<int, String>? contacts;
  int? selected;
  String? error;

  ChatState(ChatServer server) {
    reloadContacts(server);
  }

  Future reloadContacts(ChatServer server) async {
    var result = await server.getMessages(null, null);
    _setError(result);

    if (result.hasValue) {
      var userID = server.userID;
      var newContacts = <int, String>{};

      for (var message in result.value) {
        int contact =
            message.sender == userID ? message.receiver : message.sender;
        if (!newContacts.containsKey(contact)) {
          var findName = await server.getUsername(contact);

          if (findName.hasValue) {
            newContacts[contact] = findName.value;
          }
        }
      }
      setState(() {
        contacts = newContacts;
      });
    }
  }

  Future<bool> addContactByName(String name) async {
    var findID = await widget.server.findUserID(name);

    if (findID.hasValue) {
      setState(() {
        contacts![findID.value] = name;
      });
    }
    return findID.hasValue;
  }

  Future<bool> addContactByID(int id) async {
    var findName = await widget.server.getUsername(id);

    if (findName.hasValue) {
      setState(() {
        contacts![id] = findName.value;
      });
    }
    return findName.hasValue;
  }

  void _setError(ServerResponse response) {
    if (response.isError || error != null) {
      setState(() {
        error = response.isError ? response.message : null;
      });
    }
  }

  void selectChat(int index) {
    setState(() {
      selected = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Row(
          children: [
            contacts == null
                ? LoadingCircle()
                : Column(
                    children: [
                      NavBar(
                        onSelect: selectChat,
                        entries: contacts!.values.toList(),
                      ),
                      Container()
                    ],
                  ),
            error == null
                ? IndexedStack(
                    index: selected,
                    children: [
                      if (contacts != null)
                        for (var contact in contacts!.keys)
                          Conversation(
                              key: ValueKey(contact),
                              server: widget.server,
                              contactID: contact)
                    ],
                  )
                : Container(
                    alignment: Alignment.center,
                    child: Text(error!),
                  )
          ],
        )
      ],
    );
  }
}

class Conversation extends StatefulWidget {
  final ChatServer server;
  final int contactID;

  const Conversation(
      {super.key, required this.server, required this.contactID});

  @override
  State<StatefulWidget> createState() => ConversationState();
}

class ConversationState extends State<Conversation> {
  List<Message>? messages;
  int limit = 256;
  String? receiveError;

  TextEditingController textField = TextEditingController();
  String? sendError;

  ConversationState() {
    reloadMessages();
  }

  Future reloadMessages() async {
    var result = await widget.server.getMessages(widget.contactID, limit);

    setState(() {
      if (result.isError) {
        receiveError = result.message;
      } else {
        messages = result.hasValue ? result.value : const [];
        messages!.sort((a, b) => Comparable.compare(a.time, b.time));
        receiveError = null;
      }
    });
  }

  Future sendMessage(String text) async {
    if (messages == null) {
      return;
    }
    var result = await widget.server.sendMessage(widget.contactID, text);

    setState(() {
      if (result.isError) {
        sendError = result.message;
      } else {
        textField.clear();
        messages!.add(Message(
            widget.server.userID!, widget.contactID, DateTime.now(), text));
        sendError = null;
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        SingleChildScrollView(
          child: messages == null
              ? LoadingCircle()
              : Column(children: [
                  for (var message in messages!)
                    Container(
                      alignment: message.sender == widget.contactID
                          ? Alignment.centerLeft
                          : Alignment.centerRight,
                      child: Text(message.text),
                    )
                ]),
        ),
        Container(
            decoration: const BoxDecoration(),
            child: Row(
              children: [
                TextField(
                  controller: textField,
                  onSubmitted: sendMessage,
                ),
                ElevatedButton(
                    onPressed: () => sendMessage(textField.text),
                    child: const Text("Send"))
              ],
            ))
      ],
    );
  }
}
