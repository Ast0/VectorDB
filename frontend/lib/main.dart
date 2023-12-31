import 'package:flutter/material.dart';
import 'package:vectordb/server.dart';
import 'chat_widgets.dart';
import 'game_widgets.dart';

void main() {
  GameServer server = GameServer(Uri.base.resolve("ws://localhost:8080/game"));
  runApp(MainApp(server: server));
}

class MainApp extends StatelessWidget {
  final GameServer server;

  const MainApp({required this.server, super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Theme(
        data: ThemeData.from(
            colorScheme: ColorScheme.fromSeed(seedColor: Colors.cyan)),
        child: Scaffold(
          body: server.userID == null
              ? Container(
                  alignment: Alignment.center, child: LoginForm(server: server))
              : Row(
                  children: [
                    Flexible(child: MarketView(server: server)),
                    Flexible(child: ChatView(server: server))
                  ],
                ),
        ),
      ),
    );
  }
}

class LoginForm extends StatefulWidget {
  final GameServer server;

  const LoginForm({required this.server, super.key});

  @override
  State<StatefulWidget> createState() => LoginState();
}

class LoginState extends State<LoginForm> {
  bool login = false;
  String? error;

  final GlobalKey<FormState> _formKey = GlobalKey<FormState>();
  String? username;
  String? password;

  @override
  Widget build(BuildContext context) {
    return Container(
      color: Theme.of(context).dialogBackgroundColor,
      child: Form(
        key: _formKey,
        child: Column(
          children: <Widget>[
            TextFormField(
              onSaved: (newValue) => username = newValue,
              decoration: const InputDecoration(
                hintText: 'Username',
              ),
              validator: (String? value) {
                if (value == null || value.isEmpty) {
                  return 'Please enter a username';
                }
                return null;
              },
            ),
            TextFormField(
              onSaved: (newValue) => password = newValue,
              decoration: const InputDecoration(
                hintText: 'Password',
              ),
              obscureText: true,
              validator: (String? value) {
                if (value == null || value.isEmpty) {
                  return 'Please enter a password';
                }
                return null;
              },
            ),
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 16.0),
              child: Row(
                children: [
                  ElevatedButton(
                    onPressed: () async {
                      // Validate will return true if the form is valid, or false if
                      // the form is invalid.
                      if (_formKey.currentState!.validate()) {
                        _formKey.currentState!.save();

                        var result =
                            await widget.server.login(username!, password!);
                        if (result.isError) {
                          setState(() {
                            error = result.message;
                          });
                        } else {
                          setState(() {
                            login = true;
                            error = null;
                          });
                        }
                      }
                    },
                    child: const Text('Login'),
                  ),
                  ElevatedButton(
                    onPressed: () async {
                      // Validate will return true if the form is valid, or false if
                      // the form is invalid.
                      if (_formKey.currentState!.validate()) {
                        _formKey.currentState!.save();

                        var result = await widget.server
                            .createAccount(username!, "null", password!);
                        if (result.isError) {
                          setState(() {
                            error = result.message;
                          });
                        } else {
                          setState(() {
                            error = null;
                          });
                        }
                      }
                    },
                    child: const Text('Create Account'),
                  ),
                ],
              ),
            ),
            if (error != null) Text(error!)
          ],
        ),
      ),
    );
  }
}
