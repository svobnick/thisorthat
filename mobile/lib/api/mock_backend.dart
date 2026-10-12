import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

/// A stand-in for the server that lives inside the app, so it runs with no
/// backend at all. It answers the same requests with the same JSON as the
/// API ([ApiClient] cannot tell the difference) out of a handful of made-up
/// questions, and keeps what the person does in memory until the app closes.
///
/// It follows the rules of the real server where the app depends on them:
/// `/getItems` hands out only questions not answered yet, a vote moves the
/// counter, a new question waits for moderation, a duplicate is refused with
/// the id of the original.
class MockBackend {
  MockBackend({this.latency = const Duration(milliseconds: 300)});

  /// How long every answer takes, so loading states can be seen.
  final Duration latency;

  /// Plug this into [ApiClient].
  late final http.Client client = MockClient(_handle);

  static const _pageOfItems = 30;
  static const _myName = 'Смелый кактус';

  final List<_Item> _items = _seedItems();
  final Map<int, List<Map<String, Object?>>> _comments = _seedComments();
  final Map<int, String> _views = {};
  final List<int> _favorites = [];
  int _nextId = 100000;

  Future<http.Response> _handle(http.Request request) async {
    if (latency > Duration.zero) await Future<void>.delayed(latency);
    final params = request.bodyFields;
    final itemId = int.tryParse(params['item_id'] ?? '');
    switch (request.url.pathSegments.last) {
      case 'register':
        return _ok({'token': '1:${'0' * 32}'});

      case 'getItems':
        final fresh = _items
            .where((item) => item.status == 'approved' && !item.mine && !_views.containsKey(item.id))
            .take(_pageOfItems);
        return _ok({
          'items': [for (final item in fresh) _itemJson(item)],
        });

      case 'setViewed':
        for (final entry in params.entries) {
          final match = RegExp(r'^views\[(\d+)\]$').firstMatch(entry.key);
          if (match == null) continue;
          final id = int.parse(match.group(1)!);
          // An answer is recorded once; sending it again changes nothing.
          if (_views.containsKey(id)) continue;
          _views[id] = entry.value;
          final item = _find(id);
          if (item != null && entry.value == 'first') item.firstVotes++;
          if (item != null && entry.value == 'last') item.lastVotes++;
        }
        return _ok(true);

      case 'sendReport':
        return _ok(true);

      case 'addFavorite':
        if (itemId != null && !_favorites.contains(itemId)) _favorites.add(itemId);
        return _ok(true);

      case 'deleteFavorite':
        _favorites.remove(itemId);
        return _ok(true);

      case 'getFavorite':
        return _page('items', [for (final id in _favorites) if (_find(id) case final item?) _itemJson(item)], params);

      case 'getMyItems':
        return _page('items', [for (final item in _items.reversed) if (item.mine) _itemJson(item)], params);

      case 'addItem':
        final first = (params['first_text'] ?? '').trim();
        final last = (params['last_text'] ?? '').trim();
        if (first.length < 4 || first.length > 150 || last.length < 4 || last.length > 150) {
          return _refuse('Вопрос должен состоять из 4-150 символов');
        }
        if (first == last) return _refuse('Вопросы не могут быть одинаковыми');
        for (final item in _items) {
          if (item.status == 'rejected') continue;
          if ((item.first == first && item.last == last) || (item.first == last && item.last == first)) {
            return _refuse('В нашей базе уже есть такой вопрос', parameters: {'clone_id': '${item.id}'});
          }
        }
        final item = _Item(_nextId++, first, last, 0, 0, status: 'new', mine: true);
        _items.add(item);
        return _ok({'item_id': '${item.id}'});

      case 'getComments':
        return _page('comments', _comments[itemId] ?? const [], params);

      case 'addComment':
        final message = (params['message'] ?? '').trim();
        if (itemId == null || message.isEmpty || message.length > 300) {
          return _refuse('Комментарий должен состоять из 1-300 символов');
        }
        final comment = _comment(_nextId++, 1, _myName, message);
        _comments.putIfAbsent(itemId, () => []).add(comment);
        return _ok(comment);

      default:
        return _refuse('Метод не найден', status: 404);
    }
  }

  Map<String, Object?> _itemJson(_Item item) => item.toJson(comments: _comments[item.id]?.length ?? 0);

  _Item? _find(int id) {
    for (final item in _items) {
      if (item.id == id) return item;
    }
    return null;
  }

  static http.Response _page(String key, List<Map<String, Object?>> all, Map<String, String> params) {
    final offset = int.tryParse(params['offset'] ?? '') ?? 0;
    final limit = int.tryParse(params['limit'] ?? '') ?? 30;
    final page = all.skip(offset).take(limit).toList();
    return _ok({
      key: page,
      'pages': {'count': page.length, 'offset': offset, 'total': all.length},
    });
  }

  static http.Response _ok(Object? result) => _json({'ok': true, 'result': result}, 200);

  static http.Response _refuse(String description, {int status = 400, Map<String, Object?>? parameters}) =>
      _json({'ok': false, 'description': description, 'parameters': ?parameters}, status);

  static http.Response _json(Object body, int status) => http.Response.bytes(
        utf8.encode(jsonEncode(body)),
        status,
        headers: {'content-type': 'application/json; charset=utf-8'},
      );
}

class _Item {
  _Item(this.id, this.first, this.last, this.firstVotes, this.lastVotes, {this.status = 'approved', this.mine = false});

  final int id;
  final String first;
  final String last;
  int firstVotes;
  int lastVotes;
  final String status;

  /// Added by this person; shown in the profile and kept out of their feed.
  final bool mine;

  Map<String, Object?> toJson({required int comments}) => {
        'item_id': '$id',
        'first_text': first,
        'last_text': last,
        'status': status,
        'comments': comments > 0 ? 'available' : null,
        'comments_count': comments,
        'first_vote': firstVotes,
        'last_vote': lastVotes,
      };
}

Map<String, Object?> _comment(int id, int userId, String name, String message) => {
      'comment_id': '$id',
      'user_id': '$userId',
      'parent': '0',
      'message': message,
      'name': name,
      // No picture: the app draws the author's initial instead.
      'avatar': '',
    };

// Made up for the mock: short ones, long ones, a landslide and a question
// hardly anyone has answered, so every shape of the card can be seen.
List<_Item> _seedItems() => [
      _Item(1, 'Уметь летать', 'Уметь становиться невидимым', 6120, 4380),
      _Item(2, 'Чай', 'Кофе', 9034, 4515),
      _Item(3, 'Жить у моря', 'Жить в горах', 8450, 5210),
      _Item(4, 'Вернуться в прошлое', 'Заглянуть в будущее', 3870, 4410),
      _Item(5, 'Всю жизнь без сладкого', 'Всю жизнь без солёного', 2710, 3940),
      _Item(6, 'Кошка', 'Собака', 6120, 7490),
      _Item(7, 'Знать все языки мира', 'Уметь играть на всех музыкальных инструментах', 5230, 2980),
      _Item(8, 'Выходные', 'Понедельник', 9800, 120),
      _Item(
        9,
        'Каждое утро просыпаться в новом незнакомом городе и до вечера не знать, где окажешься завтра и как оттуда добраться домой',
        'Никогда больше не выезжать за пределы своего района, даже в отпуск, на праздники и в гости к самым близким друзьям',
        1840,
        2260,
      ),
      _Item(10, 'Миллион рублей прямо сейчас', 'Десять миллионов через десять лет', 7310, 6890),
      _Item(11, 'Лето круглый год', 'Зима круглый год', 10240, 2130),
      _Item(12, 'Читать мысли людей', 'Понимать язык животных', 4020, 5770),
      _Item(13, 'Пицца', 'Суши', 5640, 3310),
      _Item(14, 'Всегда говорить то, что думаешь', 'Никогда больше не говорить', 6950, 1480),
      _Item(15, 'Суп на завтрак', 'Каша на ужин', 3, 1),
      _Item(16, 'Быть самым умным в комнате', 'Быть самым смешным в комнате', 3350, 4620),
      _Item(17, 'Потерять телефон', 'Потерять кошелёк', 2190, 5830),
      _Item(18, 'Жить без музыки', 'Жить без кино', 1760, 6240),
      _Item(19, 'Отпуск на необитаемом острове', 'Отпуск в огромном мегаполисе', 4480, 4390),
      _Item(20, 'Быть знаменитым, но бедным', 'Быть богатым, но никому не известным', 1520, 8870),
      _Item(21, 'Утро без будильника', 'Вечер без дел', 5110, 3080),
      _Item(90, 'Никогда не мыть посуду', 'Никогда не гладить одежду', 2840, 3160, mine: true),
      _Item(91, 'Арбуз', 'Дыня', 0, 0, status: 'rejected', mine: true),
    ];

Map<int, List<Map<String, Object?>>> _seedComments() => {
      1: [
        _comment(501, 425, 'Волосатый монстр', 'Летать, конечно. Пробки больше не проблема.'),
        _comment(502, 221, 'Умный багажник', 'А я бы выбрал невидимость: столько всего можно узнать!'),
        _comment(503, 317, 'Сонный пингвин', 'Летать страшно, вдруг высоты испугаюсь'),
      ],
      2: [
        _comment(511, 640, 'Тихий ураган', 'Кофе утром, чай вечером. Почему надо выбирать?'),
      ],
      6: [
        _comment(521, 221, 'Умный багажник', 'Собака — друг, кошка — начальник'),
        _comment(522, 808, 'Весёлый кактус', 'У меня и кошка, и собака, они бы обиделись на этот вопрос'),
      ],
      90: [
        _comment(531, 425, 'Волосатый монстр', 'Отличный вопрос!'),
      ],
    };
