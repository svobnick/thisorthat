import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:path_provider/path_provider.dart';

import 'api/api_client.dart';
import 'api/mock_backend.dart';
import 'app.dart';
import 'session/app_storage.dart';

/// The server to talk to: `--dart-define=API_BASE_URL=https://api.thisorthat.ru/`.
/// Left out, the app runs on the mock backend built into it.
const _apiBaseUrl = String.fromEnvironment('API_BASE_URL');

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await SystemChrome.setPreferredOrientations([DeviceOrientation.portraitUp]);
  // On Android this is the native app's `filesDir`, where its token lives.
  final directory = await getApplicationSupportDirectory();
  final mock = _apiBaseUrl.isEmpty;
  if (mock) debugPrint('No API_BASE_URL: running on the built-in mock backend.');
  runApp(
    ThisOrThatApp(
      api: mock
          ? ApiClient(baseUrl: Uri.parse('https://mock.invalid/'), client: MockBackend().client)
          : ApiClient(baseUrl: Uri.parse(_apiBaseUrl)),
      // The mock keeps its files apart, so its token never replaces the one
      // that ties this install to a real user.
      storage: AppStorage(mock ? Directory('${directory.path}/mock') : directory),
      clientName: Platform.isIOS ? 'ios' : 'android',
    ),
  );
}
