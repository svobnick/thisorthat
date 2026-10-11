import 'package:flutter/material.dart';

import '../../../api/api_client.dart';
import '../../../api/models.dart';
import '../../../l10n/l10n.dart';
import '../../components/action_bar.dart';
import '../../components/app_dialog.dart';
import '../../components/app_logo.dart';
import '../../components/question_card.dart';
import '../../components/screen_header.dart';
import '../../components/share_question.dart';
import '../../theme/app_colors.dart';
import '../../theme/app_text.dart';
import '../comments/comments_screen.dart';

/// A question from the profile, shown with its results. There is nothing to
/// answer here.
class QuestionViewScreen extends StatefulWidget {
  const QuestionViewScreen({super.key, required this.api, required this.question, required this.favorite});

  final ApiClient api;
  final Question question;

  /// Whether the question is in favorites. Null for one of the person's own
  /// questions: the API does not say, so the heart is off limits.
  final bool? favorite;

  @override
  State<QuestionViewScreen> createState() => _QuestionViewScreenState();
}

class _QuestionViewScreenState extends State<QuestionViewScreen> {
  final _cardKey = GlobalKey();
  late bool _favorite = widget.favorite ?? false;

  Future<void> _toggleFavorite() async {
    final favorite = !_favorite;
    setState(() => _favorite = favorite);
    try {
      if (favorite) {
        await widget.api.addFavorite(widget.question.id);
      } else {
        await widget.api.deleteFavorite(widget.question.id);
      }
    } on Object catch (e) {
      if (!mounted) return;
      setState(() => _favorite = !favorite);
      await showErrorDialog(context, e);
    }
  }

  @override
  Widget build(BuildContext context) {
    final question = widget.question;
    return Scaffold(
      body: SafeArea(
        child: Column(
          children: [
            const ScreenHeader(center: AppLogo()),
            SizedBox(
              height: 26,
              child: question.status == QuestionStatus.fresh
                  ? Text(
                      context.l10n.moderationStatus,
                      style: AppText.body(size: 12, weight: FontWeight.w800, color: AppColors.ink(0.45)),
                    )
                  : null,
            ),
            Expanded(
              child: Padding(
                padding: const EdgeInsets.fromLTRB(16, 0, 16, 18),
                child: RepaintBoundary(
                  key: _cardKey,
                  child: QuestionCard(
                    firstText: question.firstText,
                    lastText: question.lastText,
                    firstVotes: question.firstVotes,
                    lastVotes: question.lastVotes,
                    revealed: true,
                  ),
                ),
              ),
            ),
            Builder(
              builder: (shareContext) => ActionBar(
                onComments: () => Navigator.of(context).push(
                  MaterialPageRoute<void>(
                    builder: (_) => CommentsScreen(
                      api: widget.api,
                      question: question,
                      firstVotes: question.firstVotes,
                      lastVotes: question.lastVotes,
                      showStats: true,
                    ),
                  ),
                ),
                liked: _favorite,
                onLike: widget.favorite == null ? null : _toggleFavorite,
                onShare: () => shareQuestionCard(shareContext, _cardKey),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
