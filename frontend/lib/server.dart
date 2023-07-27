import 'package:vectordb/model.dart';

import 'websocket.dart';

class ChatServer extends WebSocketServer {
  int? _userID;

  ChatServer(super.uri);

  int? get userID => _userID;

  Future<ServerResponse> createAccount(
          String username, String email, String password) async =>
      ServerResponse.fromJson(await callMethod("createAccount",
          {"username": username, "email": email, "password": password}));

  Future<ServerValue<int>> login(String username, String password) async {
    var response = ServerValue<int>.fromJson(await callMethod(
        "login", {"username": username, "password": password}));

    if (response.hasValue) {
      _userID = response.value;
    }
    return response;
  }

  Future<ServerResponse> logout() async {
    _userID = null;
    return ServerResponse.fromJson(await callMethod("logout", {}));
  }

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

  Future<ServerList<Item>> getItems() async => ServerList.fromJson(
      await callMethod("getMessages", const {}), Item.fromJson);

  Future<ServerList<Inventory>> getInventory() async => ServerList.fromJson(
      await callMethod("getMessages", const {}), Inventory.fromJson);

  Future<ServerList<Order>> getOrders(int itemID) async => ServerList.fromJson(
      await callMethod("getMessages", {"itemID": itemID}), Order.fromJson);

  Future<ServerResponse> createOrder(
          bool isBuy, int itemID, int amount, int price) async =>
      ServerResponse.fromJson(await callMethod("sendMessage", {
        "type": isBuy ? "buy" : "sell",
        "itemID": itemID,
        "amount": amount,
        "price": price
      }));

  Future<ServerResponse> modifyOrder(
          int orderID, int amount, int price) async =>
      ServerResponse.fromJson(await callMethod("sendMessage",
          {"orderID": orderID, "amount": amount, "price": price}));

  Future<ServerResponse> fulfillOrder(int orderID, int amount) async =>
      ServerResponse.fromJson(await callMethod(
          "sendMessage", {"orderID": orderID, "amount": amount}));
}
