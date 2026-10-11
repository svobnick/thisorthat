import 'dart:io';

import 'package:flutter_test/flutter_test.dart';
import 'package:thisorthat/session/app_storage.dart';
import 'package:thisorthat/session/session.dart';

import '../support/fake_api.dart';

void main() {
  late Directory directory;
  late FakeApi api;
  late Session session;

  setUp(() {
    directory = Directory.systemTemp.createTempSync('thisorthat-test');
    api = FakeApi()..client.token = '';
    api.handlers['register'] = (_) => FakeApi.ok({'token': '42:fresh'});
    session = Session(api: api.client, storage: AppStorage(directory), clientName: 'android');
  });

  tearDown(() => directory.deleteSync(recursive: true));

  test('the first launch registers once and keeps the token', () async {
    await session.start();

    expect(api.client.token, '42:fresh');
    final call = api.calls.single;
    expect(call.method, 'register');
    expect(call.params['client'], 'android');
    // The API accepts only this alphabet for the id.
    expect(call.params['uniqid'], matches(RegExp(r'^[a-z0-9]{32}$')));

    await session.start();
    expect(api.calls, hasLength(1));
  });

  test('an install upgraded from the native app keeps its token', () async {
    // The native app wrote `filesDir/authToken`; the storage directory is that `filesDir`.
    File('${directory.path}/authToken').writeAsStringSync('7:native');

    await session.start();

    expect(api.client.token, '7:native');
    expect(api.calls, isEmpty);
  });

  test('onboarding answers survive a restart, declined ones as null', () async {
    final storage = AppStorage(directory);
    expect(await storage.readProfile(), isNull);

    await storage.writeProfile(const Profile(age: '13-15'));

    final profile = await AppStorage(directory).readProfile();
    expect((profile?.age, profile?.gender), ('13-15', null));
  });
}
