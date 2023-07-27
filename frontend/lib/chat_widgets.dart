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
  String? addError;

  final GlobalKey<FormState> _formKey = GlobalKey<FormState>();

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

  Future addContact(String username) async {
    if (await addContactByName(username)) {
      setState(() {
        addError = null;
      });
    } else {
      setState(() {
        addError = "Contact not found.";
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Row(
          children: [
            SizedBox(
              width: 200,
              child: Container(
                color: Theme.of(context).dividerColor,
                child: contacts == null
                    ? LoadingCircle()
                    : Column(
                        children: [
                          NavBar(
                            onSelect: selectChat,
                            entries: contacts!.values.toList(),
                          ),
                          Container(
                              child: Form(
                            key: _formKey,
                            child: Column(children: [
                              Row(
                                children: [
                                  Expanded(
                                    child: TextFormField(
                                      onSaved: (newValue) =>
                                          addContact(newValue!),
                                      decoration: const InputDecoration(
                                        hintText: 'Add Contact',
                                      ),
                                      validator: (String? value) {
                                        if (value == null || value.isEmpty) {
                                          return 'Please enter a username';
                                        }
                                        return null;
                                      },
                                    ),
                                  ),
                                  ElevatedButton(
                                    onPressed: () async {
                                      // Validate will return true if the form is valid, or false if
                                      // the form is invalid.
                                      if (_formKey.currentState!.validate()) {
                                        _formKey.currentState!.save();
                                      }
                                    },
                                    child: const Text('Add'),
                                  ),
                                ],
                              ),
                              if (addError != null) Text(addError!)
                            ]),
                          ))
                        ],
                      ),
              ),
            ),
            SizedBox(
                height: 640,
                child: error == null
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
                      ))
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
  State<StatefulWidget> createState() => ConversationState(server, contactID);
}

class ConversationState extends State<Conversation> {
  List<Message>? messages;
  int limit = 256;
  String? receiveError;

  TextEditingController textField = TextEditingController();
  String? sendError;

  ConversationState(ChatServer server, int contactID) {
    reloadMessages(server, contactID);
  }

  Future reloadMessages(ChatServer server, int contactID) async {
    var result = await server.getMessages(contactID, limit);

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
      mainAxisAlignment: MainAxisAlignment.end,
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
                      child: Text(
                          textAlign: message.sender == widget.contactID
                              ? TextAlign.left
                              : TextAlign.right,
                          message.text),
                    )
                ]),
        ),
        Container(
            decoration: const BoxDecoration(),
            child: Row(
              children: [
                SizedBox(
                  width: 300,
                  child: TextField(
                    controller: textField,
                    onSubmitted: sendMessage,
                  ),
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
