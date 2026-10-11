import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:thisorthat/l10n/l10n.dart';

/// Puts [home] on a phone-sized screen with the app's strings.
Future<void> pumpApp(WidgetTester tester, Widget home) async {
  tester.view.physicalSize = const Size(393, 852) * 3;
  tester.view.devicePixelRatio = 3;
  addTearDown(tester.view.reset);
  await tester.pumpWidget(
    MaterialApp(
      localizationsDelegates: AppLocalizations.localizationsDelegates,
      supportedLocales: AppLocalizations.supportedLocales,
      home: home,
    ),
  );
  await settle(tester);
}

/// Lets requests answer and animations finish. The feed never comes to rest
/// (its swipe hint keeps nudging), so `pumpAndSettle` is not an option.
Future<void> settle(WidgetTester tester) async {
  for (var i = 0; i < 12; i++) {
    await tester.pump(const Duration(milliseconds: 100));
  }
}

AppLocalizations l10nOf(WidgetTester tester) => AppLocalizations.of(tester.element(find.byType(Scaffold).first));
