import 'package:flutter/material.dart';

import '../../api/models.dart';
import '../theme/app_colors.dart';
import '../theme/app_text.dart';

/// A question folded into two lines, each on its own colour: the row of a
/// list, and the reminder above the comments. With [showStats] every answer
/// carries its share of the votes.
class QuestionSummary extends StatelessWidget {
  const QuestionSummary({
    super.key,
    required this.firstText,
    required this.lastText,
    required this.firstVotes,
    required this.lastVotes,
    this.showStats = true,
    this.tag,
    this.onTap,
  });

  final String firstText;
  final String lastText;
  final int firstVotes;
  final int lastVotes;
  final bool showStats;

  /// A short note along the top, such as a moderation status.
  final String? tag;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final share = voteShare(firstVotes, lastVotes);
    final tag = this.tag;
    return ClipRRect(
      borderRadius: BorderRadius.circular(20),
      child: Stack(
        children: [
          Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              if (tag != null)
                Container(
                  color: AppColors.ink(0.1),
                  padding: const EdgeInsets.fromLTRB(16, 5, 16, 5),
                  child: Text(
                    tag.toUpperCase(),
                    style: AppText.body(size: 9, weight: FontWeight.w800, color: AppColors.ink(0.6), letterSpacing: 1),
                  ),
                ),
              _line(firstText, share.first, AppColors.firstGradient),
              _line(lastText, share.last, AppColors.lastGradient),
            ],
          ),
          if (onTap != null)
            Positioned.fill(
              child: Material(
                type: MaterialType.transparency,
                child: InkWell(onTap: onTap, splashColor: AppColors.ink(0.18)),
              ),
            ),
        ],
      ),
    );
  }

  Widget _line(String text, int percent, Gradient gradient) {
    return Container(
      constraints: const BoxConstraints(minHeight: 46),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
      decoration: BoxDecoration(gradient: gradient),
      child: Row(
        children: [
          Expanded(
            child: Text(
              text,
              maxLines: 3,
              overflow: TextOverflow.ellipsis,
              style: AppText.body(size: 14, weight: FontWeight.w800, color: AppColors.ink(0.95), height: 1.25),
            ),
          ),
          if (showStats) ...[
            const SizedBox(width: 12),
            Text.rich(
              TextSpan(
                text: '$percent',
                children: [TextSpan(text: '%', style: TextStyle(fontSize: 11, color: AppColors.ink(0.7)))],
              ),
              style: AppText.display(size: 17, color: AppColors.ink(0.95), height: 1),
            ),
          ],
        ],
      ),
    );
  }
}
