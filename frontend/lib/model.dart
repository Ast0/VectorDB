import 'package:json_annotation/json_annotation.dart';

part 'model.g.dart';

typedef JsonMap = Map<String, dynamic>;

DateTime _timeFromJson(int literal) =>
    DateTime.fromMillisecondsSinceEpoch(1000 * literal);

int _timeToJson(DateTime time) => time.millisecondsSinceEpoch ~/ 1000;

@JsonSerializable()
class Message {
  int sender;
  int receiver;
  @JsonKey(fromJson: _timeFromJson, toJson: _timeToJson)
  DateTime time;
  String text;

  Message(this.sender, this.receiver, this.time, this.text);

  factory Message.fromJson(JsonMap json) => _$MessageFromJson(json);
  JsonMap toJson() => _$MessageToJson(this);
}

@JsonSerializable()
class Item {
  int id;
  String name;
  String description;

  Item(this.id, this.name, this.description);

  factory Item.fromJson(JsonMap json) => _$ItemFromJson(json);
  JsonMap toJson() => _$ItemToJson(this);
}

@JsonSerializable()
class Inventory {
  int itemID;
  int userID;
  int amount;

  Inventory(this.itemID, this.userID, this.amount);

  factory Inventory.fromJson(JsonMap json) => _$InventoryFromJson(json);
  JsonMap toJson() => _$InventoryToJson(this);
}

@JsonSerializable()
class Order {
  int id;
  int itemID;
  int userID;
  @JsonKey(fromJson: _timeFromJson, toJson: _timeToJson)
  DateTime time;
  bool isBuy;
  int amount;
  int price;

  Order(this.id, this.itemID, this.userID, this.time, this.isBuy, this.amount,
      this.price);

  factory Order.fromJson(JsonMap json) => _$OrderFromJson(json);
  JsonMap toJson() => _$OrderToJson(this);
}
