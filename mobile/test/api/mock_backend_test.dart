import 'package:flutter_test/flutter_test.dart';
import 'package:thisorthat/api/api_client.dart';
import 'package:thisorthat/api/mock_backend.dart';
import 'package:thisorthat/api/models.dart';

void main() {
  late ApiClient api;

  setUp(() {
    api = ApiClient(baseUrl: Uri.parse('https://mock.invalid/'), client: MockBackend(latency: Duration.zero).client);
  });

  test('the feed hands out only questions not answered yet, until none is left', () async {
    final first = await api.getItems();
    expect(first, isNotEmpty);

    await api.setViewed({first[0].id: Choice.first, first[1].id: Choice.skip});
    final second = await api.getItems();

    expect(second.map((q) => q.id), isNot(contains(first[0].id)));
    expect(second.map((q) => q.id), isNot(contains(first[1].id)));
    expect(second, hasLength(first.length - 2));

    await api.setViewed({for (final question in second) question.id: Choice.last});
    expect(await api.getItems(), isEmpty);
  });

  test('a vote moves the counter once, however often it is sent', () async {
    final question = (await api.getItems()).first;
    await api.addFavorite(question.id);

    await api.setViewed({question.id: Choice.first});
    await api.setViewed({question.id: Choice.first});

    final stored = (await api.getFavorite(limit: 100, offset: 0)).items.single;
    expect(stored.firstVotes, question.firstVotes + 1);
    expect(stored.lastVotes, question.lastVotes);
  });

  test('a new question waits for moderation in the profile and stays out of the feed', () async {
    final id = await api.addItem(firstText: 'Ехать на поезде', lastText: 'Лететь на самолёте');

    final mine = await api.getMyItems(limit: 30, offset: 0);
    expect(mine.items.first.id, id);
    expect(mine.items.first.status, QuestionStatus.fresh);
    expect((await api.getItems()).map((q) => q.id), isNot(contains(id)));
  });

  test('a question that exists already is refused with the id of the original', () async {
    final existing = (await api.getItems()).first;

    await expectLater(
      api.addItem(firstText: existing.lastText, lastText: existing.firstText),
      throwsA(isA<ApiException>().having((e) => e.cloneId, 'cloneId', existing.id)),
    );
  });

  test('favorites and comments keep what was added', () async {
    final question = (await api.getItems()).firstWhere((q) => !q.hasComments);

    await api.addFavorite(question.id);
    final comment = await api.addComment(question.id, 'Хороший вопрос');

    expect((await api.getFavorite(limit: 100, offset: 0)).items.map((q) => q.id), [question.id]);
    final comments = await api.getComments(question.id, limit: 30, offset: 0);
    expect(comments.items.single.message, 'Хороший вопрос');
    expect(comments.items.single.id, comment.id);
    expect((await api.getItems()).firstWhere((q) => q.id == question.id).hasComments, isTrue);

    await api.deleteFavorite(question.id);
    expect((await api.getFavorite(limit: 100, offset: 0)).items, isEmpty);
  });
}
