import 'package:vectordb/model.dart';

import 'websocket.dart';

class ChatServer extends WebSocketServer {
  ChatServer(super.uri);

  Future<ServerResponse> createAccount(
          String username, String email, String password) async =>
      ServerResponse.fromJson(await callMethod("createAccount",
          {"username": username, "email": email, "password": password}));

  Future<ServerValue<int>> login(String username, String password) async =>
      ServerValue.fromJson(await callMethod(
          "login", {"username": username, "password": password}));

  Future<ServerResponse> logout() async =>
      ServerResponse.fromJson(await callMethod("logout", {}));

  Future<ServerResponse> changePassword(
          String oldPassword, String newPassword) async =>
      ServerResponse.fromJson(await callMethod("changePassword",
          {"oldPassword": oldPassword, "newPassword": newPassword}));

  Future<ServerValue<int>> findUserID(String username) async =>
      ServerValue.fromJson(
          await callMethod("findUserID", {"username": username}));

  Future<ServerValue<String>> getUsername(int id) async =>
      ServerValue.fromJson(await callMethod("getUsername", {"id": id}));

  Future<ServerValue<List<Message>>> getMessages(
      int? contact, int? limit) async {
    JsonMap args = {};

    if (contact != null) args["contact"] = contact;
    if (limit != null) args["limit"] = limit;

    return ServerList.fromJson(
        await callMethod("getMessages", args), Message.fromJson);
  }

  Future<ServerResponse> sendMessage(int contact, String text) async =>
      ServerResponse.fromJson(
          await callMethod("sendMessage", {"contact": contact, "text": text}));
}

class GameServer extends ChatServer {
  GameServer(super.uri);
}
