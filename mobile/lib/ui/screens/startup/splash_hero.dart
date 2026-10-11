import 'package:flutter/widgets.dart';

import '../../../l10n/l10n.dart';
import '../../components/app_logo.dart';
import '../../theme/app_colors.dart';
import '../../theme/app_text.dart';

/// The big logo over its tagline: the face of the app while it starts and
/// above the onboarding questions.
class SplashHero extends StatelessWidget {
  const SplashHero({super.key});

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        const FittedBox(fit: BoxFit.scaleDown, child: AppLogo(size: 38)),
        const SizedBox(height: 10),
        Text(
          context.l10n.tagline.toUpperCase(),
          textAlign: TextAlign.center,
          style: AppText.body(size: 13, color: AppColors.ink(0.3), letterSpacing: 2),
        ),
      ],
    );
  }
}
