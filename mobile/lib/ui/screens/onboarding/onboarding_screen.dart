import 'package:flutter/material.dart';

import '../../../l10n/l10n.dart';
import '../../../session/app_storage.dart';
import '../../components/app_icon.dart';
import '../../components/gradient_button.dart';
import '../../theme/app_colors.dart';
import '../../theme/app_text.dart';
import '../startup/splash_hero.dart';

/// The two questions asked once, before the first feed: age, then gender.
/// Both can be declined. The answers stay on the device.
class OnboardingScreen extends StatefulWidget {
  const OnboardingScreen({super.key, required this.onDone});

  final ValueChanged<Profile> onDone;

  @override
  State<OnboardingScreen> createState() => _OnboardingScreenState();
}

// What the "rather not say" choices are held as until the profile is built.
const _declined = '';

class _OnboardingScreenState extends State<OnboardingScreen> {
  int _step = 1;
  String? _age;
  String? _gender;

  void _finish() {
    widget.onDone(
      Profile(age: _age == _declined ? null : _age, gender: _gender == _declined ? null : _gender),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Stack(
        children: [
          const Align(
            alignment: Alignment.topCenter,
            child: FractionallySizedBox(heightFactor: 0.42, child: SplashHero()),
          ),
          Align(
            alignment: Alignment.bottomCenter,
            child: Container(
              width: double.infinity,
              decoration: const BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topCenter,
                  end: Alignment.bottomCenter,
                  colors: [Color(0x00181430), AppColors.panel, AppColors.panel],
                  stops: [0, 0.08, 1],
                ),
                borderRadius: BorderRadius.vertical(top: Radius.circular(36)),
              ),
              child: SafeArea(
                top: false,
                minimum: const EdgeInsets.only(bottom: 24),
                child: Padding(
                  padding: const EdgeInsets.fromLTRB(24, 28, 24, 12),
                  child: AnimatedSwitcher(
                    duration: const Duration(milliseconds: 350),
                    transitionBuilder: (child, animation) => FadeTransition(
                      opacity: animation,
                      child: SlideTransition(
                        position: Tween(begin: const Offset(0, 0.04), end: Offset.zero).animate(animation),
                        child: child,
                      ),
                    ),
                    layoutBuilder: (current, previous) => current ?? const SizedBox.shrink(),
                    child: _step == 1 ? _ageStep(context) : _genderStep(context),
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _ageStep(BuildContext context) {
    final l10n = context.l10n;
    // The first four are the ages the game is played at most.
    final ages = [
      ('under10', l10n.ageUnder10, true),
      ('10-12', l10n.age10to12, true),
      ('13-15', l10n.age13to15, true),
      ('16-17', l10n.age16to17, true),
      ('18-24', l10n.age18to24, false),
      ('25-34', l10n.age25to34, false),
      ('35plus', l10n.age35plus, false),
      (_declined, l10n.ageSkip, false),
    ];
    return _Step(
      key: const ValueKey(1),
      label: l10n.onboardingStep(1, 2),
      title: l10n.onboardingAgeTitle,
      text: l10n.onboardingAgeText,
      progress: 1,
      button: GradientButton(
        label: '${l10n.onboardingContinue} →',
        onPressed: _age == null ? null : () => setState(() => _step = 2),
      ),
      child: Wrap(
        spacing: 9,
        runSpacing: 9,
        children: [
          for (final (value, label, featured) in ages)
            _Chip(
              label: label,
              featured: featured,
              selected: _age == value,
              onTap: () => setState(() => _age = value),
            ),
        ],
      ),
    );
  }

  Widget _genderStep(BuildContext context) {
    final l10n = context.l10n;
    final genders = <(String, Widget, String)>[
      ('male', AppIcon(AppIcons.male, size: 30, color: AppColors.ink(0.9)), l10n.genderMale),
      ('female', AppIcon(AppIcons.female, size: 30, color: AppColors.ink(0.9)), l10n.genderFemale),
      ('other', const Text('🌈', style: TextStyle(fontSize: 28, height: 1.1)), l10n.genderOther),
    ];
    return _Step(
      key: const ValueKey(2),
      label: l10n.onboardingStep(2, 2),
      title: l10n.onboardingGenderTitle,
      text: l10n.onboardingGenderText,
      progress: 2,
      button: GradientButton(label: l10n.onboardingStart, onPressed: _gender == null ? null : _finish),
      child: Column(
        children: [
          Row(
            children: [
              for (final (index, (value, icon, label)) in genders.indexed) ...[
                if (index > 0) const SizedBox(width: 10),
                Expanded(
                  child: _GenderChip(
                    icon: icon,
                    label: label,
                    selected: _gender == value,
                    onTap: () => setState(() => _gender = value),
                  ),
                ),
              ],
            ],
          ),
          const SizedBox(height: 14),
          GestureDetector(
            behavior: HitTestBehavior.opaque,
            onTap: () => setState(() => _gender = _declined),
            child: Padding(
              padding: const EdgeInsets.all(8),
              child: Text(
                l10n.genderSkip,
                style: AppText.body(
                  size: 12,
                  weight: FontWeight.w800,
                  color: _gender == _declined ? AppColors.first : AppColors.ink(0.3),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _Step extends StatelessWidget {
  const _Step({
    super.key,
    required this.label,
    required this.title,
    required this.text,
    required this.progress,
    required this.button,
    required this.child,
  });

  final String label;
  final String title;
  final String text;

  /// How many of the two bars are filled.
  final int progress;
  final Widget button;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Text(
          label.toUpperCase(),
          style: AppText.body(size: 10, weight: FontWeight.w800, color: AppColors.ink(0.28), letterSpacing: 2),
        ),
        const SizedBox(height: 8),
        Text(title, style: AppText.display(size: 20, height: 1.3)),
        const SizedBox(height: 6),
        Text(text, style: AppText.body(size: 13, color: AppColors.ink(0.4), height: 1.5)),
        const SizedBox(height: 20),
        child,
        const SizedBox(height: 20),
        Row(
          children: [
            for (var bar = 1; bar <= 2; bar++) ...[
              if (bar > 1) const SizedBox(width: 6),
              Expanded(
                child: Container(
                  height: 3,
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(2),
                    gradient: bar <= progress
                        ? const LinearGradient(colors: [Color(0xFFE8503A), Color(0xFFCC66EE)])
                        : null,
                    color: bar <= progress ? null : AppColors.ink(0.1),
                  ),
                ),
              ),
            ],
          ],
        ),
        const SizedBox(height: 18),
        button,
      ],
    );
  }
}

class _Chip extends StatelessWidget {
  const _Chip({required this.label, required this.featured, required this.selected, required this.onTap});

  final String label;
  final bool featured;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final Color border;
    final Color? fill;
    final Color text;
    if (selected) {
      (border, fill, text) = (Colors.transparent, null, AppColors.white);
    } else if (featured) {
      (border, fill, text) = (
        const Color.fromRGBO(204, 102, 238, 0.35),
        const Color.fromRGBO(204, 102, 238, 0.1),
        AppColors.ink(0.85),
      );
    } else {
      (border, fill, text) = (AppColors.ink(0.13), AppColors.ink(0.05), AppColors.ink(0.6));
    }
    return Semantics(
      button: true,
      selected: selected,
      child: GestureDetector(
        onTap: onTap,
        child: AnimatedScale(
          scale: selected ? 1.06 : 1,
          duration: const Duration(milliseconds: 200),
          curve: const Cubic(0.34, 1.56, 0.64, 1),
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 200),
            padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 13),
            decoration: BoxDecoration(
              gradient: selected ? AppColors.accentGradient : null,
              color: fill,
              borderRadius: BorderRadius.circular(50),
              border: Border.all(color: border, width: 1.5),
              boxShadow: [
                if (selected)
                  const BoxShadow(color: Color.fromRGBO(200, 80, 150, 0.35), offset: Offset(0, 4), blurRadius: 20),
              ],
            ),
            child: Text(label, style: AppText.body(size: 14, weight: FontWeight.w800, color: text)),
          ),
        ),
      ),
    );
  }
}

class _GenderChip extends StatelessWidget {
  const _GenderChip({required this.icon, required this.label, required this.selected, required this.onTap});

  final Widget icon;
  final String label;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Semantics(
      button: true,
      selected: selected,
      child: GestureDetector(
        onTap: onTap,
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 220),
          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 16),
          decoration: BoxDecoration(
            gradient: selected
                ? const LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [Color.fromRGBO(232, 80, 58, 0.22), Color.fromRGBO(160, 64, 200, 0.22)],
                  )
                : null,
            color: selected ? null : AppColors.ink(0.04),
            borderRadius: BorderRadius.circular(18),
            border: Border.all(
              color: selected ? const Color.fromRGBO(200, 100, 200, 0.5) : AppColors.ink(0.12),
              width: 1.5,
            ),
          ),
          child: Column(
            children: [
              SizedBox(height: 34, child: Center(child: icon)),
              const SizedBox(height: 8),
              Text(
                label,
                style: AppText.body(
                  size: 12,
                  weight: FontWeight.w800,
                  color: selected ? AppColors.white : AppColors.ink(0.65),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
