import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:thisorthat/api/models.dart';
import 'package:thisorthat/ui/screens/comments/comments_screen.dart';

import '../support/fake_api.dart';
import '../support/pump_app.dart';

void main() {
  late FakeApi api;

  Map<String, Object?> comment(int id, String message) => {
        'comment_id': '$id',
        'user_id': '425',
        'parent': '0',
        'message': message,
        'name': 'Волосатый монстр',
        'avatar': '',
      };

  Future<void> pump(WidgetTester tester) => pumpApp(
        tester,
        CommentsScreen(
          api: api.client,
          question: const Question(id: 4, firstText: 'first', lastText: 'last', firstVotes: 3, lastVotes: 1),
          firstVotes: 3,
          lastVotes: 1,
          showStats: false,
        ),
      );

  setUp(() => api = FakeApi());

  testWidgets('comments are listed, and the split stays hidden before an answer', (tester) async {
    api.handlers['getComments'] = (_) => FakeApi.ok(FakeApi.page('comments', [comment(17, 'Отличный вопрос!')]));

    await pump(tester);

    expect(api.calls.single.params, {'item_id': '4', 'limit': '30', 'offset': '0'});
    expect(find.text('Отличный вопрос!'), findsOneWidget);
    expect(find.textContaining('75', findRichText: true), findsNothing);
  });

  testWidgets('a comment of four characters can be sent and lands in the list', (tester) async {
    api.handlers['getComments'] = (_) => FakeApi.ok(FakeApi.page('comments', []));
    api.handlers['addComment'] = (params) => FakeApi.ok(comment(300, params['message']!));
    await pump(tester);
    expect(find.text(l10nOf(tester).commentsEmpty), findsOneWidget);
    final send = find.byKey(const ValueKey('comments-send'));

    await tester.enterText(find.byType(TextField), 'Нет');
    await tester.pump();
    expect(tester.widget<InkWell>(send).onTap, isNull);

    await tester.enterText(find.byType(TextField), 'Да нет');
    await tester.pump();
    await tester.tap(send);
    await settle(tester);

    expect(api.callsTo('addComment').single.params, {'item_id': '4', 'message': 'Да нет', 'parent': '0'});
    expect(find.text('Да нет'), findsOneWidget);
    expect(tester.widget<TextField>(find.byType(TextField)).controller!.text, isEmpty);
  });

  testWidgets('a refused comment stays in the field and the reason is shown', (tester) async {
    api.handlers['getComments'] = (_) => FakeApi.ok(FakeApi.page('comments', []));
    api.handlers['addComment'] = (_) => FakeApi.refuse('Слишком часто');
    await pump(tester);

    await tester.enterText(find.byType(TextField), 'Да нет');
    await tester.pump();
    await tester.tap(find.byKey(const ValueKey('comments-send')));
    await settle(tester);

    expect(find.text('Слишком часто'), findsOneWidget);
    expect(tester.widget<TextField>(find.byType(TextField)).controller!.text, 'Да нет');
  });
}
