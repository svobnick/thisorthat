import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:thisorthat/ui/components/gradient_button.dart';
import 'package:thisorthat/ui/screens/create/create_screen.dart';

import '../support/fake_api.dart';
import '../support/pump_app.dart';

void main() {
  late FakeApi api;
  var added = 0;

  Future<void> fill(WidgetTester tester, String first, String last) async {
    await tester.enterText(find.descendant(of: find.byKey(const ValueKey('create-first')), matching: find.byType(TextField)), first);
    await tester.enterText(find.descendant(of: find.byKey(const ValueKey('create-last')), matching: find.byType(TextField)), last);
    await tester.tap(find.byType(GradientButton));
    await settle(tester);
  }

  setUp(() {
    api = FakeApi();
    added = 0;
  });

  Future<void> pump(WidgetTester tester) =>
      pumpApp(tester, Scaffold(body: CreateScreen(api: api.client, onAdded: () => added++)));

  testWidgets('an answer shorter than four characters is not sent', (tester) async {
    await pump(tester);

    await fill(tester, 'Чай', 'Кофе с молоком');

    expect(find.text(l10nOf(tester).createValidation), findsOneWidget);
    expect(api.calls, isEmpty);
  });

  testWidgets('a question is sent trimmed, the form empties and the profile opens', (tester) async {
    api.handlers['addItem'] = (_) => FakeApi.ok({'item_id': '10000'});
    await pump(tester);

    await fill(tester, ' Visa ', 'Mastercard');
    expect(api.calls.single.params, {'first_text': 'Visa', 'last_text': 'Mastercard'});
    await tester.tap(find.text(l10nOf(tester).createdOk));
    await settle(tester);

    expect(added, 1);
    expect(find.text('Mastercard'), findsNothing);
  });

  testWidgets('a question that exists already can go to favorites instead', (tester) async {
    api.handlers['addItem'] = (_) => FakeApi.refuse('В нашей базе уже есть такой вопрос', parameters: {'clone_id': '18007'});
    await pump(tester);
    final l10n = l10nOf(tester);

    await fill(tester, 'Visa', 'Mastercard');
    expect(find.text(l10n.cloneTitle), findsOneWidget);
    await tester.tap(find.text(l10n.yes));
    await settle(tester);

    expect(api.callsTo('addFavorite').single.params, {'item_id': '18007'});
  });

  testWidgets('any other refusal is shown in the words of the server', (tester) async {
    api.handlers['addItem'] = (_) => FakeApi.refuse('Нельзя отправлять вопросы чаще, чем раз в день');
    await pump(tester);

    await fill(tester, 'Visa', 'Mastercard');

    expect(find.text('Нельзя отправлять вопросы чаще, чем раз в день'), findsOneWidget);
  });
}
