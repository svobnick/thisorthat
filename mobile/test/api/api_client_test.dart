import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:thisorthat/api/api_client.dart';
import 'package:thisorthat/api/models.dart';

import '../support/fake_api.dart';

void main() {
  late FakeApi api;

  setUp(() => api = FakeApi());

  test('register posts the client and the id without a token and returns the new one', () async {
    api.handlers['register'] = (_) => FakeApi.ok({'token': '7:abc'});

    final token = await api.client.register(client: 'ios', uniqid: 'u1');

    expect(token, '7:abc');
    expect(api.calls.single.method, 'register');
    expect(api.calls.single.params, {'client': 'ios', 'uniqid': 'u1'});
  });

  test('getItems asks for approved questions and reads ids, votes and the comments flag', () async {
    api.handlers['getItems'] = (_) => FakeApi.ok({
          'items': [
            {...FakeApi.item(65373, first: 860, last: 1165, comments: 248), 'last_text': 'padded '},
            // Counters have been seen as strings too.
            {...FakeApi.item(2), 'first_vote': '5', 'last_vote': '0'},
            // The API the app started on says only whether there are comments.
            {...FakeApi.item(3), 'comments': 'available', 'comments_count': null},
          ],
        });

    final items = await api.client.getItems();

    expect(api.calls.single.params, {'status': 'approved'});
    expect(items.first.id, 65373);
    expect(items.first.lastText, 'padded');
    expect((items.first.firstVotes, items.first.lastVotes), (860, 1165));
    expect((items[0].hasComments, items[0].commentsCount), (true, 248));
    expect((items[1].firstVotes, items[1].hasComments), (5, false));
    expect((items[2].hasComments, items[2].commentsCount), (true, 0));
  });

  test('setProfile sends the answers given and leaves out the declined ones', () async {
    await api.client.setProfile(age: '13-15');

    expect(api.calls.single.method, 'setProfile');
    expect(api.calls.single.params, {'age': '13-15'});
  });

  test('setViewed sends each answer as views[id]', () async {
    await api.client.setViewed({100: Choice.first, 245: Choice.last, 376: Choice.skip});

    expect(api.calls.single.params, {'views[100]': 'first', 'views[245]': 'last', 'views[376]': 'skip'});
  });

  test('a refusal carries the server description and the clone id', () async {
    api.handlers['addItem'] =
        (_) => FakeApi.refuse('В нашей базе уже есть такой вопрос', parameters: {'clone_id': '18007'});

    await expectLater(
      api.client.addItem(firstText: 'Visa', lastText: 'Mastercard'),
      throwsA(
        isA<ApiException>()
            .having((e) => e.kind, 'kind', ApiFailure.rejected)
            .having((e) => e.description, 'description', 'В нашей базе уже есть такой вопрос')
            .having((e) => e.cloneId, 'cloneId', 18007),
      ),
    );
  });

  test('a 5xx and a dead connection are told apart from a refusal', () async {
    api.handlers['getItems'] = (_) => http.Response('<html>Bad Gateway</html>', 502);
    await expectLater(
      api.client.getItems(),
      throwsA(isA<ApiException>().having((e) => e.kind, 'kind', ApiFailure.server)),
    );

    final offline = ApiClient(
      baseUrl: Uri.parse('https://api.test/'),
      client: MockClient((_) => throw http.ClientException('no route to host')),
    );
    await expectLater(
      offline.getItems(),
      throwsA(isA<ApiException>().having((e) => e.kind, 'kind', ApiFailure.network)),
    );
  });

  test('voteShare rounds to whole percent that add up to 100', () {
    expect(voteShare(3443, 11679), (first: 23, last: 77));
    expect(voteShare(1, 2), (first: 33, last: 67));
    expect(voteShare(0, 0), (first: 50, last: 50));
  });
}
