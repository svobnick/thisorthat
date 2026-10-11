import 'package:flutter/foundation.dart';

import '../../../api/api_client.dart';
import '../../../api/models.dart';

/// The comments of one question, loaded a page at a time.
class CommentsController extends ChangeNotifier {
  CommentsController(this._api, this.questionId);

  static const pageSize = 30;

  /// The shortest comment the send button accepts; the longest is the API's.
  static const minLength = 4;
  static const maxLength = 300;

  final ApiClient _api;
  final int questionId;

  final List<Comment> comments = [];
  bool loading = false;
  bool sending = false;
  bool hasMore = true;

  /// Why the first page could not be shown.
  Object? error;

  bool _disposed = false;

  bool canSend(String text) => !sending && text.trim().length >= minLength;

  Future<void> loadMore() async {
    if (loading || !hasMore) return;
    loading = true;
    error = null;
    notifyListeners();
    try {
      final page = await _api.getComments(questionId, limit: pageSize, offset: comments.length);
      comments.addAll(page.items);
      hasMore = page.items.length == pageSize && comments.length < page.total;
    } on Object catch (e) {
      if (comments.isEmpty) error = e;
      hasMore = comments.isEmpty;
    } finally {
      loading = false;
      if (!_disposed) notifyListeners();
    }
  }

  /// Posts a comment and puts it at the end of the list. Throws what the
  /// server answered when it refuses.
  Future<void> add(String text) async {
    sending = true;
    notifyListeners();
    try {
      comments.add(await _api.addComment(questionId, text.trim()));
    } finally {
      sending = false;
      if (!_disposed) notifyListeners();
    }
  }

  @override
  void dispose() {
    _disposed = true;
    super.dispose();
  }
}
