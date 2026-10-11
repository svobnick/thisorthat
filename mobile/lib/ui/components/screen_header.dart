import 'package:flutter/material.dart';

import '../../l10n/l10n.dart';
import '../theme/app_text.dart';
import 'app_icon.dart';
import 'round_icon_button.dart';

/// The top row of a screen opened over the tabs: back on the left, a title or
/// the logo in the middle.
class ScreenHeader extends StatelessWidget {
  const ScreenHeader({super.key, this.title, this.center});

  final String? title;

  /// Shown in the middle instead of [title].
  final Widget? center;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(18, 4, 18, 4),
      child: Stack(
        alignment: Alignment.center,
        children: [
          center ?? Text(title ?? '', style: AppText.display(size: 16)),
          Align(
            alignment: Alignment.centerLeft,
            child: RoundIconButton(
              icon: AppIcons.back,
              label: context.l10n.back,
              onPressed: () => Navigator.of(context).maybePop(),
            ),
          ),
        ],
      ),
    );
  }
}
