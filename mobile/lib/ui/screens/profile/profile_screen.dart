import 'package:flutter/material.dart';

import '../../../api/api_client.dart';
import '../../../api/models.dart';
import '../../../l10n/l10n.dart';
import '../../components/gradient_button.dart';
import '../../components/question_summary.dart';
import '../../theme/app_colors.dart';
import '../../theme/app_text.dart';
import 'profile_controller.dart';
import 'question_view_screen.dart';

/// The person's own questions and the ones they liked.
class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key, required this.api});

  final ApiClient api;

  @override
  State<ProfileScreen> createState() => ProfileScreenState();
}

class ProfileScreenState extends State<ProfileScreen> {
  late final QuestionList _mine = QuestionList(widget.api.getMyItems, pageSize: 30);
  late final QuestionList _favorites = QuestionList(widget.api.getFavorite, pageSize: 100);
  bool _showFavorites = false;

  @override
  void dispose() {
    _mine.dispose();
    _favorites.dispose();
    super.dispose();
  }

  /// Reloads both lists; the tabs call it every time the profile is opened.
  void refresh() {
    _mine.refresh();
    _favorites.refresh();
  }

  Future<void> _open(Question question) async {
    final favorites = _showFavorites;
    await Navigator.of(context).push(
      MaterialPageRoute<void>(
        builder: (_) => QuestionViewScreen(api: widget.api, question: question, favorite: favorites ? true : null),
      ),
    );
    // The heart may have been taken off there.
    if (favorites) _favorites.refresh();
  }

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;
    return SafeArea(
      bottom: false,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(24, 16, 24, 14),
            child: Text(l10n.profileTitle, style: AppText.display(size: 20, height: 1.3)),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Row(
              children: [
                Expanded(
                  child: _Tab(
                    label: l10n.profileMyQuestions,
                    selected: !_showFavorites,
                    onTap: () => setState(() => _showFavorites = false),
                  ),
                ),
                const SizedBox(width: 9),
                Expanded(
                  child: _Tab(
                    label: l10n.profileFavorites,
                    selected: _showFavorites,
                    onTap: () => setState(() => _showFavorites = true),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 14),
          Expanded(
            child: IndexedStack(
              index: _showFavorites ? 1 : 0,
              children: [
                _QuestionListView(list: _mine, showStatus: true, onOpen: _open),
                _QuestionListView(list: _favorites, showStatus: false, onOpen: _open),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _Tab extends StatelessWidget {
  const _Tab({required this.label, required this.selected, required this.onTap});

  final String label;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Semantics(
      button: true,
      selected: selected,
      child: GestureDetector(
        behavior: HitTestBehavior.opaque,
        onTap: onTap,
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          height: 46,
          alignment: Alignment.center,
          decoration: BoxDecoration(
            gradient: selected ? AppColors.accentGradient : null,
            color: selected ? null : AppColors.ink(0.05),
            borderRadius: BorderRadius.circular(50),
            border: Border.all(color: selected ? Colors.transparent : AppColors.ink(0.13), width: 1.5),
          ),
          child: Text(
            label,
            style: AppText.body(
              size: 14,
              weight: FontWeight.w800,
              color: selected ? AppColors.white : AppColors.ink(0.6),
            ),
          ),
        ),
      ),
    );
  }
}

class _QuestionListView extends StatelessWidget {
  const _QuestionListView({required this.list, required this.showStatus, required this.onOpen});

  final QuestionList list;

  /// Whether a row says where its question is in moderation.
  final bool showStatus;
  final ValueChanged<Question> onOpen;

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;
    return ListenableBuilder(
      listenable: list,
      builder: (context, _) {
        final items = list.items;
        if (items.isEmpty) {
          if (list.loading) {
            return const Center(child: CircularProgressIndicator(strokeWidth: 3, color: AppColors.first));
          }
          final error = list.error;
          return Center(
            child: Padding(
              padding: const EdgeInsets.all(32),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    error == null ? l10n.profileEmpty : failureMessage(l10n, error),
                    textAlign: TextAlign.center,
                    style: AppText.body(size: 15, weight: FontWeight.w800, color: AppColors.ink(0.45)),
                  ),
                  if (error != null) ...[
                    const SizedBox(height: 20),
                    GradientButton(label: l10n.retry, onPressed: list.refresh),
                  ],
                ],
              ),
            ),
          );
        }
        return NotificationListener<ScrollNotification>(
          onNotification: (notification) {
            if (notification.metrics.extentAfter < 300) list.loadMore();
            return false;
          },
          child: ListView.separated(
            padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
            itemCount: items.length,
            separatorBuilder: (_, _) => const SizedBox(height: 10),
            itemBuilder: (context, index) {
              final question = items[index];
              return QuestionSummary(
                firstText: question.firstText,
                lastText: question.lastText,
                firstVotes: question.firstVotes,
                lastVotes: question.lastVotes,
                tag: !showStatus
                    ? null
                    : switch (question.status) {
                        QuestionStatus.fresh => l10n.statusNew,
                        QuestionStatus.rejected => l10n.statusRejected,
                        QuestionStatus.approved => null,
                      },
                onTap: () => onOpen(question),
              );
            },
          ),
        );
      },
    );
  }
}
