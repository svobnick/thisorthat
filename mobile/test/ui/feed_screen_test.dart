import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:thisorthat/ui/screens/feed/feed_screen.dart';

import '../support/fake_api.dart';
import '../support/pump_app.dart';

void main() {
  late FakeApi api;

  setUp(() {
    api = FakeApi();
    api.handlers['getItems'] = (_) => FakeApi.ok({
          'items': [FakeApi.item(1, first: 30, last: 9, comments: 248), FakeApi.item(2), FakeApi.item(3)],
        });
  });

  testWidgets('an answer reveals how people voted, and the next tap moves on', (tester) async {
    await pumpApp(tester, Scaffold(body: FeedScreen(api: api.client)));
    expect(find.text('first 1'), findsOneWidget);
    expect(find.textContaining('%', findRichText: true), findsNothing);
    // The comment button counts the comments, or says what it is for.
    expect(find.text('248'), findsOneWidget);
    expect(find.text(l10nOf(tester).actionComments), findsNothing);

    await tester.tap(find.text('first 1'));
    await settle(tester);

    // 31 of 40 with the person's own vote.
    expect(find.textContaining('78%', findRichText: true), findsOneWidget);
    expect(find.textContaining('22%', findRichText: true), findsOneWidget);
    expect(find.text('31 голос'), findsOneWidget);
    expect(find.text('9 голосов'), findsOneWidget);

    await tester.tap(find.text('last 1'));
    await settle(tester);
    expect(find.text('first 2'), findsOneWidget);
    expect(find.textContaining('%', findRichText: true), findsNothing);
    expect(find.text(l10nOf(tester).actionComments), findsOneWidget);

    // Out the last timers: the vote toast.
    await tester.pump(const Duration(seconds: 3));
  });

  testWidgets('a swipe left skips the question and a swipe right brings it back', (tester) async {
    await pumpApp(tester, Scaffold(body: FeedScreen(api: api.client)));

    await tester.drag(find.text('first 1'), const Offset(-200, 0));
    await settle(tester);
    expect(find.text('first 2'), findsOneWidget);

    await tester.drag(find.text('first 2'), const Offset(200, 0));
    await settle(tester);
    expect(find.text('first 1'), findsOneWidget);
  });

  testWidgets('reporting asks why, thanks, and shows the next question', (tester) async {
    await pumpApp(tester, Scaffold(body: FeedScreen(api: api.client)));
    final l10n = l10nOf(tester);

    await tester.tap(find.bySemanticsLabel(l10n.reportAction));
    await settle(tester);
    await tester.tap(find.text(l10n.reportReasonClone));
    await settle(tester);

    expect(find.text(l10n.reportResultText), findsOneWidget);
    expect(api.callsTo('sendReport').single.params, {'item_id': '1', 'reason': 'clone'});
    await tester.tap(find.text(l10n.ok));
    await settle(tester);
    expect(find.text('first 2'), findsOneWidget);
  });

  testWidgets('with no connection the feed says so and retries', (tester) async {
    final working = api.handlers['getItems']!;
    api.handlers['getItems'] = (_) => FakeApi.refuse('down', status: 502);
    await pumpApp(tester, Scaffold(body: FeedScreen(api: api.client)));
    final l10n = l10nOf(tester);
    expect(find.text(l10n.errorServer), findsOneWidget);

    api.handlers['getItems'] = working;
    await tester.tap(find.text(l10n.retry));
    await settle(tester);

    expect(find.text('first 1'), findsOneWidget);
  });
}
