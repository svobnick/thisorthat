import 'package:flutter/material.dart';

import '../theme/app_colors.dart';
import '../theme/app_text.dart';

/// The primary button: full width, the red-to-purple gradient, an Unbounded
/// label. Fades out when it cannot be pressed.
class GradientButton extends StatelessWidget {
  const GradientButton({super.key, required this.label, required this.onPressed, this.busy = false});

  final String label;
  final VoidCallback? onPressed;

  /// A request is on its way: the label gives place to a spinner and presses
  /// are ignored.
  final bool busy;

  @override
  Widget build(BuildContext context) {
    final enabled = onPressed != null;
    return AnimatedOpacity(
      opacity: enabled ? 1 : 0.35,
      duration: const Duration(milliseconds: 200),
      child: DecoratedBox(
        decoration: BoxDecoration(
          gradient: AppColors.accentGradient,
          borderRadius: BorderRadius.circular(16),
          boxShadow: const [
            BoxShadow(color: Color.fromRGBO(180, 60, 160, 0.4), offset: Offset(0, 6), blurRadius: 24),
          ],
        ),
        child: Material(
          type: MaterialType.transparency,
          child: InkWell(
            onTap: enabled && !busy ? onPressed : null,
            borderRadius: BorderRadius.circular(16),
            child: SizedBox(
              height: 54,
              width: double.infinity,
              child: Center(
                child: busy
                    ? const SizedBox.square(
                        dimension: 20,
                        child: CircularProgressIndicator(strokeWidth: 2.5, color: AppColors.white),
                      )
                    : Text(label, style: AppText.display(size: 14, letterSpacing: 0.3)),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
