import 'package:flutter/widgets.dart';

import '../../l10n/l10n.dart';
import '../theme/app_colors.dart';
import '../theme/app_text.dart';

/// The wordmark: a red "this", a quiet "or", a purple "that", each coloured
/// word standing on a hard shadow of its own darker shade.
class AppLogo extends StatelessWidget {
  const AppLogo({super.key, this.size = 24});

  /// Font size of the two coloured words: 24 in the header, 38 on the splash.
  final double size;

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;
    final large = size > 30;
    TextStyle word(Color color, Color deep, Color glow) => AppText.display(
          size: size,
          color: color,
          letterSpacing: large ? -1 : -0.5,
          shadows: [
            Shadow(color: deep, offset: const Offset(0, 3)),
            Shadow(color: glow, offset: const Offset(0, 6), blurRadius: 24),
          ],
        );
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Text(
          l10n.logoFirst,
          style: word(AppColors.first, AppColors.firstDeep, const Color.fromRGBO(232, 80, 58, 0.6)),
        ),
        SizedBox(width: large ? 10 : 7),
        Text(
          l10n.logoOr,
          style: AppText.body(size: 16, color: AppColors.ink(0.35), letterSpacing: 3),
        ),
        SizedBox(width: large ? 10 : 7),
        Text(
          l10n.logoLast,
          style: word(AppColors.last, AppColors.lastDeep, const Color.fromRGBO(160, 64, 200, 0.6)),
        ),
      ],
    );
  }
}
