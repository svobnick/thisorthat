import 'package:flutter/material.dart';

import '../theme/app_colors.dart';
import 'app_icon.dart';

/// The small outlined circle of the header: report on the feed, back on the
/// screens above it.
class RoundIconButton extends StatelessWidget {
  const RoundIconButton({super.key, required this.icon, required this.label, required this.onPressed});

  final AppIcons icon;

  /// What a screen reader says.
  final String label;
  final VoidCallback? onPressed;

  @override
  Widget build(BuildContext context) {
    return Semantics(
      button: true,
      label: label,
      child: InkResponse(
        onTap: onPressed,
        radius: 24,
        // The circle is 32 across; the touch target around it is 44.
        child: Padding(
          padding: const EdgeInsets.all(6),
          child: Container(
            width: 32,
            height: 32,
            alignment: Alignment.center,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              border: Border.all(color: AppColors.ink(0.18), width: 1.5),
            ),
            child: AppIcon(icon, size: 14, strokeWidth: 2.5, color: AppColors.ink(0.4)),
          ),
        ),
      ),
    );
  }
}
