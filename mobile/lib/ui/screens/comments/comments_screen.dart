import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../../api/api_client.dart';
import '../../../api/models.dart';
import '../../../l10n/l10n.dart';
import '../../components/app_dialog.dart';
import '../../components/app_icon.dart';
import '../../components/gradient_button.dart';
import '../../components/question_summary.dart';
import '../../components/screen_header.dart';
import '../../theme/app_colors.dart';
import '../../theme/app_text.dart';
import 'comments_controller.dart';

/// What people wrote under one question, and a field to add to it.
class CommentsScreen extends StatefulWidget {
  const CommentsScreen({
    super.key,
    required this.api,
    required this.question,
    required this.firstVotes,
    required this.lastVotes,
    required this.showStats,
  });

  final ApiClient api;
  final Question question;
  final int firstVotes;
  final int lastVotes;

  /// False while the person has not answered: the split stays a secret.
  final bool showStats;

  @override
  State<CommentsScreen> createState() => _CommentsScreenState();
}

class _CommentsScreenState extends State<CommentsScreen> {
  late final CommentsController _comments = CommentsController(widget.api, widget.question.id)..loadMore();
  final _input = TextEditingController();
  final _scroll = ScrollController();

  @override
  void initState() {
    super.initState();
    _scroll.addListener(() {
      if (_scroll.position.extentAfter < 300) _comments.loadMore();
    });
  }

  @override
  void dispose() {
    _comments.dispose();
    _input.dispose();
    _scroll.dispose();
    super.dispose();
  }

  Future<void> _send() async {
    try {
      await _comments.add(_input.text);
      _input.clear();
      if (!mounted) return;
      // The new comment is the last one; bring it into view.
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (_scroll.hasClients) {
          _scroll.animateTo(
            _scroll.position.maxScrollExtent,
            duration: const Duration(milliseconds: 300),
            curve: Curves.easeOut,
          );
        }
      });
    } on Object catch (e) {
      if (mounted) await showErrorDialog(context, e);
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;
    return Scaffold(
      body: GestureDetector(
        behavior: HitTestBehavior.translucent,
        onTap: () => FocusScope.of(context).unfocus(),
        child: SafeArea(
          child: Column(
            children: [
              ScreenHeader(title: l10n.commentsTitle),
              Padding(
                padding: const EdgeInsets.fromLTRB(16, 6, 16, 10),
                child: QuestionSummary(
                  firstText: widget.question.firstText,
                  lastText: widget.question.lastText,
                  firstVotes: widget.firstVotes,
                  lastVotes: widget.lastVotes,
                  showStats: widget.showStats,
                ),
              ),
              Expanded(
                child: ListenableBuilder(listenable: _comments, builder: (context, _) => _list(l10n)),
              ),
              _InputBar(controller: _input, comments: _comments, onSend: _send),
            ],
          ),
        ),
      ),
    );
  }

  Widget _list(AppLocalizations l10n) {
    final comments = _comments.comments;
    if (comments.isEmpty) {
      if (_comments.loading) {
        return const Center(child: CircularProgressIndicator(strokeWidth: 3, color: AppColors.first));
      }
      final error = _comments.error;
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                error == null ? l10n.commentsEmpty : failureMessage(l10n, error),
                textAlign: TextAlign.center,
                style: AppText.body(size: 15, weight: FontWeight.w800, color: AppColors.ink(0.45)),
              ),
              if (error != null) ...[
                const SizedBox(height: 20),
                GradientButton(label: l10n.retry, onPressed: _comments.loadMore),
              ],
            ],
          ),
        ),
      );
    }
    return ListView.builder(
      controller: _scroll,
      padding: const EdgeInsets.fromLTRB(16, 6, 16, 12),
      itemCount: comments.length,
      itemBuilder: (context, index) => _CommentTile(
        comment: comments[index],
        // Names take turns in the two colours of the game.
        nameColor: index.isEven ? AppColors.first : AppColors.last,
      ),
    );
  }
}

class _CommentTile extends StatelessWidget {
  const _CommentTile({required this.comment, required this.nameColor});

  final Comment comment;
  final Color nameColor;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 14),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _Avatar(comment: comment, color: nameColor),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(comment.name, style: AppText.body(size: 13, weight: FontWeight.w800, color: nameColor)),
                const SizedBox(height: 2),
                Text(
                  comment.message,
                  style: AppText.body(size: 14, weight: FontWeight.w600, color: AppColors.ink(0.85), height: 1.35),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

/// The author's picture, or their initial on their colour when there is no
/// picture or it does not load.
class _Avatar extends StatelessWidget {
  const _Avatar({required this.comment, required this.color});

  final Comment comment;
  final Color color;

  static const _size = 40.0;

  @override
  Widget build(BuildContext context) {
    final name = comment.name.trim();
    final initial = Container(
      width: _size,
      height: _size,
      alignment: Alignment.center,
      decoration: BoxDecoration(shape: BoxShape.circle, color: color.withValues(alpha: 0.2)),
      child: Text(
        name.isEmpty ? '?' : name.characters.first.toUpperCase(),
        style: AppText.body(size: 16, weight: FontWeight.w900, color: color),
      ),
    );
    if (comment.avatarUrl.isEmpty) return initial;
    return ClipOval(
      child: Image.network(
        comment.avatarUrl,
        width: _size,
        height: _size,
        fit: BoxFit.cover,
        errorBuilder: (_, _, _) => initial,
      ),
    );
  }
}

class _InputBar extends StatelessWidget {
  const _InputBar({required this.controller, required this.comments, required this.onSend});

  final TextEditingController controller;
  final CommentsController comments;
  final VoidCallback onSend;

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;
    return Container(
      padding: const EdgeInsets.fromLTRB(16, 10, 12, 10),
      decoration: BoxDecoration(border: Border(top: BorderSide(color: AppColors.ink(0.06)))),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.end,
        children: [
          Expanded(
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
              decoration: BoxDecoration(
                color: AppColors.panel,
                borderRadius: BorderRadius.circular(22),
                border: Border.all(color: AppColors.ink(0.1), width: 1.5),
              ),
              child: TextField(
                controller: controller,
                minLines: 1,
                maxLines: 4,
                textCapitalization: TextCapitalization.sentences,
                inputFormatters: [LengthLimitingTextInputFormatter(CommentsController.maxLength)],
                style: AppText.body(size: 14, weight: FontWeight.w700, height: 1.3),
                decoration: InputDecoration(
                  isDense: true,
                  border: InputBorder.none,
                  contentPadding: const EdgeInsets.symmetric(vertical: 9),
                  hintText: l10n.commentsHint,
                  hintStyle: AppText.body(size: 14, weight: FontWeight.w700, color: AppColors.ink(0.3), height: 1.3),
                ),
              ),
            ),
          ),
          const SizedBox(width: 8),
          ListenableBuilder(
            listenable: Listenable.merge([controller, comments]),
            builder: (context, _) {
              final enabled = comments.canSend(controller.text);
              return Semantics(
                button: true,
                enabled: enabled,
                label: l10n.commentsSend,
                child: AnimatedOpacity(
                  opacity: enabled ? 1 : 0.35,
                  duration: const Duration(milliseconds: 200),
                  child: Material(
                    type: MaterialType.transparency,
                    child: Ink(
                      width: 44,
                      height: 44,
                      decoration: const BoxDecoration(shape: BoxShape.circle, gradient: AppColors.accentGradient),
                      child: InkWell(
                        key: const ValueKey('comments-send'),
                        customBorder: const CircleBorder(),
                        onTap: enabled ? onSend : null,
                        child: const Center(
                          child: AppIcon(AppIcons.send, size: 18, strokeWidth: 2.2, color: AppColors.white),
                        ),
                      ),
                    ),
                  ),
                ),
              );
            },
          ),
        ],
      ),
    );
  }
}
