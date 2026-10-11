import 'package:flutter/material.dart';

import '../../../api/api_client.dart';
import '../../../l10n/l10n.dart';
import '../../components/app_icon.dart';
import '../../theme/app_colors.dart';
import '../../theme/app_text.dart';
import '../create/create_screen.dart';
import '../feed/feed_screen.dart';
import '../profile/profile_screen.dart';

/// The three tabs of the app over the pill bar that switches them.
class HomeShell extends StatefulWidget {
  const HomeShell({super.key, required this.api});

  final ApiClient api;

  @override
  State<HomeShell> createState() => _HomeShellState();
}

class _HomeShellState extends State<HomeShell> {
  static const _profileTab = 2;

  final _profile = GlobalKey<ProfileScreenState>();
  int _tab = 0;

  void _select(int tab) {
    FocusScope.of(context).unfocus();
    setState(() => _tab = tab);
    // A question added or liked a moment ago should be there already.
    if (tab == _profileTab) _profile.currentState?.refresh();
  }

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;
    return Scaffold(
      body: IndexedStack(
        index: _tab,
        children: [
          FeedScreen(api: widget.api),
          CreateScreen(api: widget.api, onAdded: () => _select(_profileTab)),
          ProfileScreen(key: _profile, api: widget.api),
        ],
      ),
      bottomNavigationBar: SafeArea(
        top: false,
        minimum: const EdgeInsets.only(bottom: 12),
        child: Padding(
          padding: const EdgeInsets.fromLTRB(16, 6, 16, 0),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              for (final (index, (icon, label)) in [
                (AppIcons.feed, l10n.navFeed),
                (AppIcons.plus, l10n.navCreate),
                (AppIcons.user, l10n.navProfile),
              ].indexed) ...[
                if (index > 0) const SizedBox(width: 4),
                _NavPill(icon: icon, label: label, active: _tab == index, onTap: () => _select(index)),
              ],
            ],
          ),
        ),
      ),
    );
  }
}

/// A tab of the bar. Only the active one spells its name out.
class _NavPill extends StatelessWidget {
  const _NavPill({required this.icon, required this.label, required this.active, required this.onTap});

  final AppIcons icon;
  final String label;
  final bool active;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Semantics(
      button: true,
      selected: active,
      label: label,
      excludeSemantics: true,
      child: GestureDetector(
        behavior: HitTestBehavior.opaque,
        onTap: onTap,
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 220),
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
          decoration: BoxDecoration(
            color: active ? AppColors.ink(0.1) : AppColors.ink(0),
            borderRadius: BorderRadius.circular(50),
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              AppIcon(
                icon,
                size: 18,
                strokeWidth: 2.5,
                color: active ? AppColors.first : AppColors.ink(0.4),
                glow: active ? const Color.fromRGBO(240, 112, 96, 0.7) : null,
              ),
              ClipRect(
                child: AnimatedAlign(
                  alignment: Alignment.centerLeft,
                  widthFactor: active ? 1 : 0,
                  heightFactor: 1,
                  duration: const Duration(milliseconds: 300),
                  curve: Curves.ease,
                  child: Padding(
                    padding: const EdgeInsets.only(left: 6),
                    child: Text(
                      label.toUpperCase(),
                      maxLines: 1,
                      softWrap: false,
                      style: AppText.body(size: 12, weight: FontWeight.w900, letterSpacing: 0.3),
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
