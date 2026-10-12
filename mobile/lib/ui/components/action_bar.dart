import 'package:flutter/material.dart';

import '../../l10n/l10n.dart';
import '../theme/app_colors.dart';
import '../theme/app_text.dart';
import 'app_icon.dart';

/// The row under a question: discuss, like, share. A null callback greys its
/// button out.
class ActionBar extends StatelessWidget {
  const ActionBar({
    super.key,
    required this.onComments,
    this.commentsCount = 0,
    required this.liked,
    required this.onLike,
    required this.onShare,
  });

  final VoidCallback? onComments;

  /// Shown next to the comment icon when there are any.
  final int commentsCount;
  final bool liked;
  final VoidCallback? onLike;
  final VoidCallback? onShare;

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;
    final divider = Container(width: 1, height: 24, color: AppColors.ink(0.08));
    return Container(
      padding: const EdgeInsets.fromLTRB(20, 10, 20, 8),
      decoration: BoxDecoration(border: Border(top: BorderSide(color: AppColors.ink(0.06)))),
      child: Row(
        children: [
          Expanded(
            child: _ActionButton(
              icon: AppIcons.comment,
              label: commentsCount > 0 ? '$commentsCount' : l10n.actionComments,
              onTap: onComments,
            ),
          ),
          divider,
          Expanded(
            child: _ActionButton(icon: AppIcons.heart, label: l10n.actionLike, active: liked, onTap: onLike),
          ),
          divider,
          Expanded(child: _ActionButton(icon: AppIcons.share, label: l10n.actionShare, onTap: onShare)),
        ],
      ),
    );
  }
}

class _ActionButton extends StatefulWidget {
  const _ActionButton({required this.icon, required this.label, required this.onTap, this.active = false});

  final AppIcons icon;
  final String label;
  final VoidCallback? onTap;
  final bool active;

  @override
  State<_ActionButton> createState() => _ActionButtonState();
}

class _ActionButtonState extends State<_ActionButton> {
  bool _bumped = false;

  Future<void> _tap() async {
    widget.onTap!();
    setState(() => _bumped = true);
    await Future<void>.delayed(const Duration(milliseconds: 220));
    if (mounted) setState(() => _bumped = false);
  }

  @override
  Widget build(BuildContext context) {
    final enabled = widget.onTap != null;
    final active = widget.active;
    return Opacity(
      opacity: enabled ? 1 : 0.35,
      child: AnimatedScale(
        scale: _bumped ? 1.15 : 1,
        duration: const Duration(milliseconds: 180),
        curve: const Cubic(0.34, 1.56, 0.64, 1),
        child: Material(
          type: MaterialType.transparency,
          child: InkWell(
            onTap: enabled ? _tap : null,
            borderRadius: BorderRadius.circular(12),
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 8),
              // A label that does not fit its third shrinks instead of wrapping.
              child: FittedBox(
                fit: BoxFit.scaleDown,
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    AppIcon(
                      widget.icon,
                      color: active ? AppColors.first : AppColors.ink(0.55),
                      fillOpacity: active ? 0.25 : 0,
                      glow: active ? const Color.fromRGBO(240, 112, 96, 0.6) : null,
                    ),
                    const SizedBox(width: 7),
                    Text(
                      widget.label,
                      style: AppText.body(
                        size: 13,
                        weight: FontWeight.w800,
                        color: active ? AppColors.first : AppColors.ink(0.45),
                        letterSpacing: 0.2,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
