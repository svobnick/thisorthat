import 'dart:math';

import '../api/api_client.dart';
import 'app_storage.dart';

/// Users are anonymous: the first launch registers the install and the token
/// that comes back is the only link to a person's favorites and questions.
class Session {
  Session({required this.api, required this.storage, required this.clientName});

  final ApiClient api;
  final AppStorage storage;

  /// `android` or `ios`; the server records where a user came from.
  final String clientName;

  /// Makes [ApiClient.token] usable, registering when this install has none.
  Future<void> start() async {
    var token = await storage.readToken();
    if (token == null) {
      token = await api.register(client: clientName, uniqid: _uniqueId());
      await storage.writeToken(token);
    }
    api.token = token;
  }

  static String _uniqueId() {
    final random = Random.secure();
    return [for (var i = 0; i < 16; i++) random.nextInt(256).toRadixString(16).padLeft(2, '0')].join();
  }
}
