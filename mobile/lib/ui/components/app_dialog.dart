import 'package:flutter/material.dart';

import '../../l10n/l10n.dart';
import '../theme/app_colors.dart';
import '../theme/app_text.dart';
import 'gradient_button.dart';

/// A message with one or two answers, in the panel style of the mock-up's
/// onboarding. Resolves to true for [confirmLabel], false for [cancelLabel],
/// null when dismissed.
Future<bool?> showAppDialog(
  BuildContext context, {
  String? emblem,
  required String title,
  required String text,
  required String confirmLabel,
  String? cancelLabel,
}) {
  return showDialog<bool>(
    context: context,
    barrierColor: AppColors.shade(0.6),
    builder: (context) => Dialog(
      backgroundColor: AppColors.panel,
      insetPadding: const EdgeInsets.symmetric(horizontal: 28),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(28),
        side: BorderSide(color: AppColors.ink(0.08)),
      ),
      child: Padding(
        padding: const EdgeInsets.fromLTRB(24, 28, 24, 24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            if (emblem != null) ...[
              Text(emblem, style: AppText.body(size: 22, color: AppColors.ink(0.6))),
              const SizedBox(height: 14),
            ],
            Text(title, textAlign: TextAlign.center, style: AppText.display(size: 17, height: 1.3)),
            const SizedBox(height: 10),
            Text(
              text,
              textAlign: TextAlign.center,
              style: AppText.body(size: 13, color: AppColors.ink(0.5), height: 1.5),
            ),
            const SizedBox(height: 22),
            GradientButton(label: confirmLabel, onPressed: () => Navigator.of(context).pop(true)),
            if (cancelLabel != null) ...[
              const SizedBox(height: 6),
              TextButton(
                onPressed: () => Navigator.of(context).pop(false),
                child: Text(cancelLabel, style: AppText.body(size: 13, weight: FontWeight.w800, color: AppColors.ink(0.45))),
              ),
            ],
          ],
        ),
      ),
    ),
  );
}

Future<void> showErrorDialog(BuildContext context, Object error) {
  final l10n = context.l10n;
  return showAppDialog(
    context,
    title: l10n.errorTitle,
    text: failureMessage(l10n, error),
    confirmLabel: l10n.ok,
  );
}
