import 'package:flutter/material.dart';

import '../../../l10n/l10n.dart';
import '../../components/gradient_button.dart';
import '../../theme/app_colors.dart';
import '../../theme/app_text.dart';
import 'splash_hero.dart';

/// Shown while the session starts. When that fails it says why and offers to
/// try again.
class StartupScreen extends StatelessWidget {
  const StartupScreen({super.key, this.error, required this.onRetry});

  final Object? error;
  final VoidCallback onRetry;

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;
    final error = this.error;
    return Scaffold(
      body: SafeArea(
        child: Stack(
          children: [
            const Center(child: SplashHero()),
            Align(
              alignment: Alignment.bottomCenter,
              child: Padding(
                padding: const EdgeInsets.fromLTRB(24, 0, 24, 36),
                child: error == null
                    ? const CircularProgressIndicator(strokeWidth: 3, color: AppColors.first)
                    : Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(
                            failureMessage(l10n, error),
                            textAlign: TextAlign.center,
                            style: AppText.body(size: 14, weight: FontWeight.w800, color: AppColors.ink(0.6)),
                          ),
                          const SizedBox(height: 20),
                          GradientButton(label: l10n.retry, onPressed: onRetry),
                        ],
                      ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
