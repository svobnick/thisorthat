import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:path_provider/path_provider.dart';

import 'api/api_client.dart';
import 'app.dart';
import 'session/app_storage.dart';

/// Where the API lives. Overridable for a local stand-in:
/// `--dart-define=API_BASE_URL=http://localhost:8787/`.
const _apiBaseUrl = String.fromEnvironment('API_BASE_URL', defaultValue: 'https://api.thisorthat.ru/');

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await SystemChrome.setPreferredOrientations([DeviceOrientation.portraitUp]);
  // On Android this is the native app's `filesDir`, where its token lives.
  final directory = await getApplicationSupportDirectory();
  runApp(
    ThisOrThatApp(
      api: ApiClient(baseUrl: Uri.parse(_apiBaseUrl)),
      storage: AppStorage(directory),
      clientName: Platform.isIOS ? 'ios' : 'android',
    ),
  );
}
