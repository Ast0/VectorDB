import 'dart:async';
import 'dart:convert';

import 'package:web_socket_channel/web_socket_channel.dart';

import 'model.dart';

class ServerResponse {
  final bool _error;
  final String? _message;

  bool get isError => _error;
  String get message => _message!;

  ServerResponse(this._error, this._message);
  ServerResponse.fromJson(JsonMap message)
      : _error = message["type"] == "error",
        _message = message["message"];
}

class ServerValue<T> extends ServerResponse {
  final T? _value;

  bool get hasValue => !isError && _value != null;
  T get value => _value!;

  ServerValue.fromJson(JsonMap message, {T Function(dynamic)? converter})
      : _value = converter != null && message["value"] != null
            ? converter(message["value"])
            : message["value"],
        super.fromJson(message);
}

class ServerList<T> extends ServerValue<List<T>> {
  ServerList.fromJson(message, T Function(JsonMap) converter)
      : super.fromJson(message, converter: (list) {
          return (list as List<dynamic>)
              .map(
                (e) => converter(e as JsonMap),
              )
              .toList();
        });
}

class WebSocketServer {
  final WebSocketChannel _channel;
  // ignore: unused_field
  late final StreamSubscription _subscription;
  final Map<String, Completer<JsonMap>> _pending;

  WebSocketServer(Uri uri)
      : _channel = WebSocketChannel.connect(uri),
        _pending = {} {
    _subscription = _channel.stream.listen(_receiveMessage);
  }

  void sendJson(JsonMap message) async {
    _channel.sink.add(jsonEncode(message));
  }

  Future<JsonMap> callMethod(String name, JsonMap arguments) async {
    if (_pending.containsKey(name)) {
      throw Exception("Parallel calls to the same method are not supported.");
    }
    arguments["method"] = name;
    sendJson(arguments);

    var completer = Completer<JsonMap>();
    _pending[name] = completer;
    return completer.future;
  }

  void _receiveMessage(dynamic message) {
    try {
      JsonMap json = jsonDecode(message);
      String? method = json["method"];

      if (method != null) {
        if (_pending.containsKey(method)) {
          _pending[method]!.complete(json);
          _pending.remove(method);
        }
      } else if (json["type"] == "event") {
        _handleEvent(json);
      }
    } catch (e) {
      rethrow;
    }
  }

  void _handleEvent(JsonMap event) {}

  void close() {
    _channel.sink.close(1000);
  }
}
