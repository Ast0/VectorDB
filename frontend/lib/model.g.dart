// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Message _$MessageFromJson(Map<String, dynamic> json) => Message(
      json['sender'] as int,
      json['receiver'] as int,
      _timeFromJson(json['time'] as int),
      json['text'] as String,
    );

Map<String, dynamic> _$MessageToJson(Message instance) => <String, dynamic>{
      'sender': instance.sender,
      'receiver': instance.receiver,
      'time': _timeToJson(instance.time),
      'text': instance.text,
    };

Item _$ItemFromJson(Map<String, dynamic> json) => Item(
      json['id'] as int,
      json['name'] as String,
      json['description'] as String,
    );

Map<String, dynamic> _$ItemToJson(Item instance) => <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'description': instance.description,
    };

Inventory _$InventoryFromJson(Map<String, dynamic> json) => Inventory(
      json['itemID'] as int,
      json['userID'] as int,
      json['amount'] as int,
    );

Map<String, dynamic> _$InventoryToJson(Inventory instance) => <String, dynamic>{
      'itemID': instance.itemID,
      'userID': instance.userID,
      'amount': instance.amount,
    };

Order _$OrderFromJson(Map<String, dynamic> json) => Order(
      json['id'] as int,
      json['itemID'] as int,
      json['userID'] as int,
      _timeFromJson(json['time'] as int),
      json['isBuy'] as bool,
      json['amount'] as int,
      json['price'] as int,
    );

Map<String, dynamic> _$OrderToJson(Order instance) => <String, dynamic>{
      'id': instance.id,
      'itemID': instance.itemID,
      'userID': instance.userID,
      'time': _timeToJson(instance.time),
      'isBuy': instance.isBuy,
      'amount': instance.amount,
      'price': instance.price,
    };
