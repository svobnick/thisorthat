import 'package:flutter_test/flutter_test.dart';
import 'package:thisorthat/api/models.dart';
import 'package:thisorthat/ui/screens/feed/feed_controller.dart';

import '../support/fake_api.dart';

void main() {
  late FakeApi api;
  // What the server would hand out: everything not answered yet, 30 at most.
  late List<int> unanswered;

  setUp(() {
    api = FakeApi();
    unanswered = [for (var id = 1; id <= 100; id++) id];
    api.handlers['getItems'] =
        (_) => FakeApi.ok({'items': [for (final id in unanswered.take(30)) FakeApi.item(id)]});
    api.handlers['setViewed'] = (params) {
      for (final key in params.keys) {
        unanswered.remove(int.parse(key.substring('views['.length, key.length - 1)));
      }
      return FakeApi.ok(true);
    };
  });

  Map<String, String> sentAnswers() =>
      {for (final call in api.callsTo('setViewed')) ...call.params};

  test('a vote shows at once and goes to the server when a batch fills up', () async {
    final feed = FeedController(api.client, batchSize: 3);
    await feed.start();

    feed.vote(Choice.first);
    expect(feed.current!.canVote, isFalse);
    expect(feed.current!.firstVotes, 31, reason: "the person's own vote is counted in");
    feed.next();
    feed.vote(Choice.last);
    feed.next();
    expect(api.callsTo('setViewed'), isEmpty);

    feed.vote(Choice.last);
    await pumpEventQueue();

    expect(api.callsTo('setViewed').single.params, {'views[1]': 'first', 'views[2]': 'last', 'views[3]': 'last'});
  });

  test('a question swiped past is skipped, but can still be answered until the skip is sent', () async {
    final feed = FeedController(api.client);
    await feed.start();

    feed.next();
    feed.previous();
    expect(feed.current!.question.id, 1);
    expect(feed.current!.canVote, isTrue);

    feed.vote(Choice.last);
    feed.next();
    feed.next();
    await feed.flush();

    expect(sentAnswers(), {'views[1]': 'last', 'views[2]': 'skip'});
  });

  test('nothing is sent twice, and a sent skip can no longer be answered', () async {
    final feed = FeedController(api.client);
    await feed.start();

    feed.next();
    feed.next();
    await feed.flush();
    await feed.flush();
    expect(api.callsTo('setViewed'), hasLength(1));

    feed.previous();
    expect(feed.current!.canVote, isFalse);
    feed.vote(Choice.first);
    await feed.flush();
    expect(sentAnswers(), {'views[1]': 'skip', 'views[2]': 'skip'});
  });

  test('a skip is held back while the person is looking at that question again', () async {
    final feed = FeedController(api.client);
    await feed.start();

    feed.next();
    feed.previous();
    await feed.flush();

    expect(api.callsTo('setViewed'), isEmpty);
  });

  test('answers are sent before more questions are fetched, and none comes twice', () async {
    final feed = FeedController(api.client, lowWater: 5);
    await feed.start();

    final shown = <int>[];
    for (var i = 0; i < 40; i++) {
      shown.add(feed.current!.question.id);
      feed.vote(Choice.first);
      feed.next();
      await pumpEventQueue();
    }

    expect(shown, [for (var id = 1; id <= 40; id++) id]);
    final order = [for (final call in api.calls) call.method];
    final secondFetch = order.lastIndexOf('getItems');
    expect(order.sublist(0, secondFetch), contains('setViewed'));
  });

  test('a failed send keeps the answers for the next try', () async {
    final feed = FeedController(api.client);
    await feed.start();
    final accept = api.handlers['setViewed']!;
    api.handlers['setViewed'] = (_) => FakeApi.refuse('nope', status: 500);

    feed.vote(Choice.first);
    feed.next();
    await feed.flush();
    api.handlers['setViewed'] = accept;
    await feed.flush();

    expect(sentAnswers(), {'views[1]': 'first'});
    expect(unanswered, isNot(contains(1)));
  });

  test('an empty batch only means the next section: the feed asks again', () async {
    var asked = 0;
    api.handlers['getItems'] = (_) => FakeApi.ok({'items': [if (++asked == 3) FakeApi.item(7)]});
    final feed = FeedController(api.client);

    await feed.start();

    expect(feed.current!.question.id, 7);
    expect(feed.exhausted, isFalse);
  });

  test('a reported question counts as skipped and cannot be answered', () async {
    final feed = FeedController(api.client);
    await feed.start();

    await feed.report(ReportReason.abuse);
    final reported = feed.current!;
    feed.next();
    await feed.flush();

    expect(api.callsTo('sendReport').single.params, {'item_id': '1', 'reason': 'abuse'});
    expect(reported.canVote, isFalse);
    expect(sentAnswers(), {'views[1]': 'skip'});
  });

  test('a like that the server refuses is taken back', () async {
    api.handlers['addFavorite'] = (_) => FakeApi.refuse('nope');
    final feed = FeedController(api.client);
    await feed.start();

    await expectLater(feed.toggleFavorite(), throwsA(anything));

    expect(feed.current!.favorite, isFalse);
  });

  test('when the first fetch fails the feed says why and can retry', () async {
    final working = api.handlers['getItems']!;
    api.handlers['getItems'] = (_) => FakeApi.refuse('down', status: 503);
    final feed = FeedController(api.client);

    await feed.start();
    expect(feed.current, isNull);
    expect(feed.error, isNotNull);

    api.handlers['getItems'] = working;
    await feed.retry();
    expect(feed.current!.question.id, 1);
    expect(feed.error, isNull);
  });
}
