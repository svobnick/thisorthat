import 'package:flutter/foundation.dart';

import '../../../api/api_client.dart';
import '../../../api/models.dart';

/// One question in the feed, with what this person did to it.
class FeedEntry {
  FeedEntry(this.question);

  final Question question;

  /// Null until the question was answered or left behind.
  Choice? choice;

  /// The server has [choice]; it cannot change any more.
  bool sent = false;

  bool reported = false;
  bool favorite = false;

  bool get answered => choice == Choice.first || choice == Choice.last;

  /// A question swiped past without an answer can still be answered when the
  /// person comes back to it, until the skip has gone to the server.
  bool get canVote => !answered && !sent && !reported;

  /// The numbers shown once a question is settled, the person's own vote
  /// included.
  int get firstVotes => question.firstVotes + (choice == Choice.first ? 1 : 0);
  int get lastVotes => question.lastVotes + (choice == Choice.last ? 1 : 0);
}

/// The endless feed: the questions seen so far, a pointer into them, and the
/// answers waiting to be sent.
///
/// Answers go to the server in batches. The server counts a vote every time
/// it receives one, so an entry is sent once and then frozen; and it hands
/// out "questions not answered yet", so answers are always sent before more
/// questions are asked for.
class FeedController extends ChangeNotifier {
  FeedController(this._api, {this.batchSize = 10, this.lowWater = 5});

  final ApiClient _api;

  /// How many unsent answers trigger a send.
  final int batchSize;

  /// Fetch more when fewer questions than this are left ahead.
  final int lowWater;

  // A short batch only means the server moved on to its next section.
  static const _maxFetchAttempts = 8;

  final List<FeedEntry> _entries = [];
  final Set<int> _seen = {};
  final List<FeedEntry> _unsent = [];
  int _index = 0;
  bool _fetching = false;
  bool _flushing = false;
  bool _disposed = false;

  /// Why there is no question to show, when there is none.
  Object? error;

  /// The server had nothing new on the last fetch.
  bool exhausted = false;

  FeedEntry? get current => _index < _entries.length ? _entries[_index] : null;
  int get position => _index;
  bool get canGoBack => _index > 0;
  bool get loading => _fetching && current == null;

  Future<void> start() => _fetchMore();

  Future<void> retry() => _fetchMore();

  void vote(Choice choice) {
    final entry = current;
    if (entry == null || !entry.canVote || choice == Choice.skip) return;
    _settle(entry, choice);
    notifyListeners();
    _flushIfDue();
  }

  /// Moves on. A question left without an answer counts as skipped.
  void next() {
    final entry = current;
    if (entry == null) return;
    if (entry.choice == null) _settle(entry, Choice.skip);
    _index++;
    notifyListeners();
    _flushIfDue();
    if (_entries.length - _index < lowWater) _fetchMore();
  }

  void previous() {
    if (!canGoBack) return;
    _index--;
    notifyListeners();
  }

  /// Reports the current question. It counts as skipped unless it was
  /// answered first; the caller moves on with [next].
  Future<void> report(ReportReason reason) async {
    final entry = current;
    if (entry == null) return;
    entry.reported = true;
    if (!entry.answered) _settle(entry, Choice.skip);
    notifyListeners();
    await _api.sendReport(entry.question.id, reason);
  }

  /// Flips the current question in and out of favorites. The heart moves at
  /// once and moves back if the server refuses.
  Future<void> toggleFavorite() async {
    final entry = current;
    if (entry == null) return;
    final favorite = !entry.favorite;
    entry.favorite = favorite;
    notifyListeners();
    try {
      if (favorite) {
        await _api.addFavorite(entry.question.id);
      } else {
        await _api.deleteFavorite(entry.question.id);
      }
    } on Object {
      entry.favorite = !favorite;
      if (!_disposed) notifyListeners();
      rethrow;
    }
  }

  /// Sends every settled answer. Called when a batch fills up, before more
  /// questions are fetched, and when the app goes to the background. A failed
  /// send keeps the answers for the next try.
  Future<void> flush() async {
    if (_flushing) return;
    // A skip is held back while the person is looking at the question again.
    final batch = [
      for (final entry in _unsent)
        if (entry.answered || entry.reported || !identical(entry, current)) entry,
    ];
    if (batch.isEmpty) return;
    _flushing = true;
    try {
      await _api.setViewed({for (final entry in batch) entry.question.id: entry.choice!});
      for (final entry in batch) {
        entry.sent = true;
        _unsent.remove(entry);
      }
      if (!_disposed) notifyListeners();
    } on Object {
      // Kept in [_unsent].
    } finally {
      _flushing = false;
    }
  }

  void _settle(FeedEntry entry, Choice choice) {
    if (entry.choice == null) _unsent.add(entry);
    entry.choice = choice;
  }

  void _flushIfDue() {
    if (_unsent.length >= batchSize) flush();
  }

  Future<void> _fetchMore() async {
    if (_fetching) return;
    _fetching = true;
    error = null;
    exhausted = false;
    notifyListeners();
    try {
      await flush();
      var added = 0;
      for (var attempt = 0; attempt < _maxFetchAttempts && added == 0; attempt++) {
        for (final question in await _api.getItems()) {
          if (_seen.add(question.id)) {
            _entries.add(FeedEntry(question));
            added++;
          }
        }
      }
      exhausted = added == 0;
    } on Object catch (e) {
      error = e;
    } finally {
      _fetching = false;
      if (!_disposed) notifyListeners();
    }
  }

  @override
  void dispose() {
    _disposed = true;
    super.dispose();
  }
}
