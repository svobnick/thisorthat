import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:thisorthat/api/api_client.dart';
import 'package:thisorthat/api/mock_backend.dart';
import 'package:thisorthat/app.dart';
import 'package:thisorthat/session/app_storage.dart';
import 'package:thisorthat/ui/components/gradient_button.dart';

import '../support/pump_app.dart';

void main() {
  late Directory directory;

  setUp(() => directory = Directory.systemTemp.createTempSync('thisorthat-app'));
  tearDown(() => directory.deleteSync(recursive: true));

  // The storage is real files, and real I/O only moves outside the test's
  // fake clock: one step of it per turn, so take turns until it is through.
  Future<void> settleIo(WidgetTester tester) async {
    for (var i = 0; i < 30; i++) {
      await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 5)));
      await tester.pump(const Duration(milliseconds: 100));
    }
  }

  ThisOrThatApp app() => ThisOrThatApp(
        api: ApiClient(baseUrl: Uri.parse('https://mock.invalid/'), client: MockBackend().client),
        storage: AppStorage(directory),
        clientName: 'ios',
      );

  testWidgets('on the mock backend a first launch goes from onboarding to a question that can be answered',
      (tester) async {
    tester.view.physicalSize = const Size(393, 852) * 3;
    tester.view.devicePixelRatio = 3;
    addTearDown(tester.view.reset);

    await tester.pumpWidget(app());
    await settleIo(tester);
    final l10n = l10nOf(tester);

    await tester.tap(find.text(l10n.ageSkip));
    await settle(tester);
    await tester.tap(find.byType(GradientButton));
    await settle(tester);
    await tester.tap(find.text(l10n.genderSkip));
    await settle(tester);
    await tester.tap(find.byType(GradientButton));
    await settleIo(tester);

    expect(find.text('Уметь летать'), findsOneWidget);
    await tester.tap(find.text('Уметь летать'));
    await settle(tester);
    expect(find.textContaining('58%', findRichText: true), findsOneWidget);

    // The onboarding is not asked twice.
    await tester.pump(const Duration(seconds: 3));
    await tester.pumpWidget(const SizedBox());
    await tester.pumpWidget(app());
    await settleIo(tester);
    expect(find.text(l10n.onboardingAgeTitle), findsNothing);
    expect(find.text('Уметь летать'), findsOneWidget);
  });
}
