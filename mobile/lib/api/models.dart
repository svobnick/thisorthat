/// What a person did with a question. The names are the API's own values for
/// `/setViewed`.
enum Choice { first, last, skip }

/// Why a question is reported. The names are the API's own values for
/// `/sendReport`.
enum ReportReason { typo, abuse, clone }

enum QuestionStatus {
  /// Waiting for moderation; the API calls it `new`.
  fresh,
  approved,
  rejected;

  static QuestionStatus parse(Object? value) => switch (value) {
        'new' => fresh,
        'rejected' => rejected,
        _ => approved,
      };
}

class Question {
  const Question({
    required this.id,
    required this.firstText,
    required this.lastText,
    required this.firstVotes,
    required this.lastVotes,
    this.status = QuestionStatus.approved,
    this.hasComments = false,
  });

  final int id;
  final String firstText;
  final String lastText;
  final int firstVotes;
  final int lastVotes;
  final QuestionStatus status;
  final bool hasComments;

  factory Question.fromJson(Map<String, dynamic> json) => Question(
        id: _int(json['item_id']),
        firstText: (json['first_text'] as String).trim(),
        lastText: (json['last_text'] as String).trim(),
        firstVotes: _int(json['first_vote']),
        lastVotes: _int(json['last_vote']),
        status: QuestionStatus.parse(json['status']),
        hasComments: json['comments'] != null,
      );
}

class Comment {
  const Comment({
    required this.id,
    required this.userId,
    required this.name,
    required this.message,
    required this.avatarUrl,
  });

  final int id;
  final int userId;
  final String name;
  final String message;
  final String avatarUrl;

  factory Comment.fromJson(Map<String, dynamic> json) => Comment(
        id: _int(json['comment_id']),
        userId: _int(json['user_id']),
        name: json['name'] as String? ?? '',
        message: json['message'] as String? ?? '',
        avatarUrl: json['avatar'] as String? ?? '',
      );
}

/// How the two answers split, in whole percent that always add up to 100.
/// Nobody answered yet reads as an even split.
({int first, int last}) voteShare(int firstVotes, int lastVotes) {
  final total = firstVotes + lastVotes;
  if (total == 0) return (first: 50, last: 50);
  final first = (firstVotes / total * 100).round();
  return (first: first, last: 100 - first);
}

// Ids come as strings and counters as numbers, but the API is PHP over MySQL
// and either may arrive as the other.
int _int(Object? value) => switch (value) {
      int v => v,
      num v => v.toInt(),
      String v => int.tryParse(v) ?? 0,
      _ => 0,
    };
