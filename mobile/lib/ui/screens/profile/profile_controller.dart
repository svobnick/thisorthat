import 'package:flutter/foundation.dart';

import '../../../api/api_client.dart';
import '../../../api/models.dart';

/// One of the two lists of the profile, loaded a page at a time.
class QuestionList extends ChangeNotifier {
  QuestionList(this._fetch, {required this.pageSize});

  final Future<Page<Question>> Function({required int limit, required int offset}) _fetch;
  final int pageSize;

  final List<Question> items = [];
  bool loading = false;
  bool hasMore = true;

  /// Why the list could not be shown.
  Object? error;

  bool _disposed = false;
  int _generation = 0;

  /// Starts over from the first page. What is on screen stays until the new
  /// page arrives.
  Future<void> refresh() => _load(reset: true);

  Future<void> loadMore() async {
    if (loading || !hasMore) return;
    await _load(reset: false);
  }

  Future<void> _load({required bool reset}) async {
    final generation = ++_generation;
    loading = true;
    error = null;
    notifyListeners();
    try {
      final page = await _fetch(limit: pageSize, offset: reset ? 0 : items.length);
      // A refresh started meanwhile owns the list now.
      if (generation != _generation) return;
      if (reset) items.clear();
      items.addAll(page.items);
      hasMore = page.items.length == pageSize && items.length < page.total;
    } on Object catch (e) {
      if (generation != _generation) return;
      if (items.isEmpty) error = e;
      hasMore = false;
    } finally {
      if (generation == _generation) {
        loading = false;
        if (!_disposed) notifyListeners();
      }
    }
  }

  @override
  void dispose() {
    _disposed = true;
    super.dispose();
  }
}
