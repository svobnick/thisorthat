import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:thisorthat/api/api_client.dart';

/// One call the app made: the API method and its form fields, the token left
/// out.
typedef ApiCall = ({String method, Map<String, String> params});

/// An [ApiClient] over canned answers. A test sets [handlers] per API method
/// and reads back [calls].
class FakeApi {
  FakeApi() {
    client = ApiClient(
      baseUrl: Uri.parse('https://api.test/'),
      client: MockClient((request) async {
        final method = request.url.pathSegments.last;
        final params = Map.of(request.bodyFields)..remove('token');
        calls.add((method: method, params: params));
        final handler = handlers[method];
        if (handler == null) return ok(true);
        return handler(params);
      }),
    )..token = '1:${'0' * 32}';
  }

  late final ApiClient client;
  final List<ApiCall> calls = [];
  final Map<String, http.Response Function(Map<String, String> params)> handlers = {};

  List<ApiCall> callsTo(String method) => [for (final call in calls) if (call.method == method) call];

  static http.Response ok(Object? result) => _json({'ok': true, 'result': result}, 200);

  static http.Response refuse(String description, {int status = 400, Map<String, Object?>? parameters}) =>
      _json({'ok': false, 'description': description, 'parameters': ?parameters}, status);

  static http.Response _json(Object body, int status) => http.Response.bytes(
        utf8.encode(jsonEncode(body)),
        status,
        headers: {'content-type': 'application/json; charset=utf-8'},
      );

  /// A question as `/getItems` sends it.
  static Map<String, Object?> item(int id, {int first = 30, int last = 10, String status = 'approved'}) => {
        'item_id': '$id',
        'first_text': 'first $id',
        'last_text': 'last $id',
        'status': status,
        'comments': null,
        'first_vote': first,
        'last_vote': last,
      };

  static Map<String, Object?> page(String key, List<Object?> items, {int? total}) => {
        key: items,
        'pages': {'count': items.length, 'offset': 0, 'total': total ?? items.length},
      };
}
