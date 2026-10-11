import 'dart:async';
import 'dart:convert';

import 'package:http/http.dart' as http;

import 'models.dart';

enum ApiFailure {
  /// The server understood the request and said no; [ApiException.description]
  /// is its explanation, in Russian.
  rejected,

  /// The server broke, or answered with something that is not its JSON.
  server,

  /// The request never got an answer.
  network,
}

class ApiException implements Exception {
  const ApiException(this.kind, {this.description, this.cloneId});

  final ApiFailure kind;
  final String? description;

  /// Set when `/addItem` refused a question because this one already exists.
  final int? cloneId;

  @override
  String toString() => 'ApiException(${kind.name}, $description)';
}

/// One page of a paged list.
class Page<T> {
  const Page(this.items, this.total);

  final List<T> items;

  /// How many there are on the server in all.
  final int total;
}

/// Client of the thisorthat.ru API, https://docs.thisorthat.ru. Every call
/// the app makes is a method here.
class ApiClient {
  ApiClient({
    required this.baseUrl,
    http.Client? client,
    this.timeout = const Duration(seconds: 20),
  }) : _http = client ?? http.Client();

  final Uri baseUrl;
  final Duration timeout;
  final http.Client _http;

  /// The key every method but [register] needs. Set once at startup.
  String token = '';

  Future<String> register({required String client, required String uniqid}) async {
    final result = await _call('register', {'client': client, 'uniqid': uniqid}, auth: false);
    return _map(result)['token'] as String;
  }

  /// Up to 30 approved questions this person has not answered yet. A short or
  /// empty list does not mean the end: the server walks its sections one call
  /// at a time, so asking again moves on to the next one.
  Future<List<Question>> getItems() async {
    final result = await _call('getItems', {'status': 'approved'});
    return _questions(result);
  }

  /// Records answers. The server counts a vote every time it is sent, so each
  /// question must go out exactly once.
  Future<void> setViewed(Map<int, Choice> answers) async {
    await _call('setViewed', {
      for (final entry in answers.entries) 'views[${entry.key}]': entry.value.name,
    });
  }

  Future<void> sendReport(int questionId, ReportReason reason) async {
    await _call('sendReport', {'item_id': '$questionId', 'reason': reason.name});
  }

  Future<void> addFavorite(int questionId) async {
    await _call('addFavorite', {'item_id': '$questionId'});
  }

  Future<void> deleteFavorite(int questionId) async {
    await _call('deleteFavorite', {'item_id': '$questionId'});
  }

  Future<Page<Question>> getMyItems({required int limit, required int offset}) async {
    final result = await _call('getMyItems', {'limit': '$limit', 'offset': '$offset'});
    return Page(_questions(result), _total(result));
  }

  Future<Page<Question>> getFavorite({required int limit, required int offset}) async {
    final result = await _call('getFavorite', {'limit': '$limit', 'offset': '$offset'});
    return Page(_questions(result), _total(result));
  }

  Future<Page<Comment>> getComments(int questionId, {required int limit, required int offset}) async {
    final result = await _call('getComments', {
      'item_id': '$questionId',
      'limit': '$limit',
      'offset': '$offset',
    });
    final comments = (_map(result)['comments'] as List? ?? const [])
        .map((json) => Comment.fromJson(json as Map<String, dynamic>))
        .toList();
    return Page(comments, _total(result));
  }

  Future<Comment> addComment(int questionId, String message) async {
    final result = await _call('addComment', {
      'item_id': '$questionId',
      'message': message,
      'parent': '0',
    });
    return Comment.fromJson(_map(result));
  }

  /// Sends a new question to moderation and returns its id. Throws an
  /// [ApiException] with [ApiException.cloneId] when it already exists.
  Future<int> addItem({required String firstText, required String lastText}) async {
    final result = await _call('addItem', {'first_text': firstText, 'last_text': lastText});
    return int.parse('${_map(result)['item_id']}');
  }

  void close() => _http.close();

  Future<Object?> _call(String method, Map<String, String> params, {bool auth = true}) async {
    final http.Response response;
    try {
      response = await _http
          .post(baseUrl.resolve(method), body: {if (auth) 'token': token, ...params})
          .timeout(timeout);
    } on Exception {
      throw const ApiException(ApiFailure.network);
    }

    if (response.statusCode >= 500) throw const ApiException(ApiFailure.server);

    final Object? body;
    try {
      body = jsonDecode(utf8.decode(response.bodyBytes));
    } on FormatException {
      throw const ApiException(ApiFailure.server);
    }
    if (body is! Map<String, dynamic>) throw const ApiException(ApiFailure.server);

    if (body['ok'] == true) return body['result'];

    final parameters = body['parameters'];
    throw ApiException(
      ApiFailure.rejected,
      description: body['description'] as String?,
      cloneId: parameters is Map ? int.tryParse('${parameters['clone_id']}') : null,
    );
  }
}

Map<String, dynamic> _map(Object? result) {
  if (result is Map<String, dynamic>) return result;
  throw const ApiException(ApiFailure.server);
}

List<Question> _questions(Object? result) => (_map(result)['items'] as List? ?? const [])
    .map((json) => Question.fromJson(json as Map<String, dynamic>))
    .toList();

int _total(Object? result) {
  final pages = _map(result)['pages'];
  return pages is Map ? int.tryParse('${pages['total']}') ?? 0 : 0;
}
