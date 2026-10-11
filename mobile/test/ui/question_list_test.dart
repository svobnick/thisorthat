import 'package:flutter_test/flutter_test.dart';
import 'package:thisorthat/ui/screens/profile/profile_controller.dart';

import '../support/fake_api.dart';

void main() {
  test('a list loads page by page until it has everything, and a refresh starts over', () async {
    final api = FakeApi();
    api.handlers['getMyItems'] = (params) {
      final offset = int.parse(params['offset']!);
      final ids = [for (var id = offset + 1; id <= 5 && id <= offset + 2; id++) id];
      return FakeApi.ok(FakeApi.page('items', [for (final id in ids) FakeApi.item(id, status: 'new')], total: 5));
    };
    final list = QuestionList(api.client.getMyItems, pageSize: 2);

    await list.refresh();
    expect(list.items.map((q) => q.id), [1, 2]);
    expect(list.hasMore, isTrue);

    await list.loadMore();
    await list.loadMore();
    expect(list.items.map((q) => q.id), [1, 2, 3, 4, 5]);
    expect(list.hasMore, isFalse);

    await list.loadMore();
    expect(api.calls, hasLength(3));

    await list.refresh();
    expect(list.items.map((q) => q.id), [1, 2]);
  });

  test('a list that cannot load says why', () async {
    final api = FakeApi();
    api.handlers['getFavorite'] = (_) => FakeApi.refuse('down', status: 500);
    final list = QuestionList(api.client.getFavorite, pageSize: 100);

    await list.refresh();

    expect(list.items, isEmpty);
    expect(list.error, isNotNull);
    expect(list.loading, isFalse);
  });
}
