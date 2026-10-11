import 'package:flutter/material.dart';

import '../../../api/models.dart';
import '../../../l10n/l10n.dart';
import '../../theme/app_colors.dart';
import '../../theme/app_text.dart';

/// Asks why a question is being reported. Resolves to null when the sheet is
/// dismissed.
Future<ReportReason?> showReportSheet(BuildContext context) {
  return showModalBottomSheet<ReportReason>(
    context: context,
    backgroundColor: AppColors.panel,
    barrierColor: AppColors.shade(0.6),
    shape: const RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(36))),
    builder: (context) {
      final l10n = context.l10n;
      return SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(24, 28, 24, 20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text(
                l10n.reportAction.toUpperCase(),
                style: AppText.body(size: 10, weight: FontWeight.w800, color: AppColors.ink(0.28), letterSpacing: 2),
              ),
              const SizedBox(height: 8),
              Text(l10n.reportQuestion, style: AppText.display(size: 17, height: 1.3)),
              const SizedBox(height: 20),
              for (final (reason, label) in [
                (ReportReason.typo, l10n.reportReasonTypo),
                (ReportReason.abuse, l10n.reportReasonAbuse),
                (ReportReason.clone, l10n.reportReasonClone),
              ])
                Padding(
                  padding: const EdgeInsets.only(bottom: 9),
                  child: Material(
                    color: AppColors.ink(0.05),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(18),
                      side: BorderSide(color: AppColors.ink(0.13), width: 1.5),
                    ),
                    child: InkWell(
                      onTap: () => Navigator.of(context).pop(reason),
                      borderRadius: BorderRadius.circular(18),
                      child: Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 16),
                        child: Text(
                          label,
                          style: AppText.body(size: 14, weight: FontWeight.w800, color: AppColors.ink(0.85)),
                        ),
                      ),
                    ),
                  ),
                ),
            ],
          ),
        ),
      );
    },
  );
}
