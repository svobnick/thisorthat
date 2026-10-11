import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'api/api_client.dart';
import 'l10n/l10n.dart';
import 'session/app_storage.dart';
import 'session/session.dart';
import 'ui/screens/home/home_shell.dart';
import 'ui/screens/onboarding/onboarding_screen.dart';
import 'ui/screens/startup/startup_screen.dart';
import 'ui/theme/app_colors.dart';
import 'ui/theme/app_theme.dart';

class ThisOrThatApp extends StatelessWidget {
  const ThisOrThatApp({super.key, required this.api, required this.storage, required this.clientName});

  final ApiClient api;
  final AppStorage storage;

  /// `android` or `ios`, as the server is told at registration.
  final String clientName;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      onGenerateTitle: (context) => context.l10n.appName,
      debugShowCheckedModeBanner: false,
      theme: buildAppTheme(),
      localizationsDelegates: AppLocalizations.localizationsDelegates,
      supportedLocales: AppLocalizations.supportedLocales,
      builder: (context, child) => AnnotatedRegion<SystemUiOverlayStyle>(
        value: SystemUiOverlayStyle.light.copyWith(
          statusBarColor: Colors.transparent,
          systemNavigationBarColor: AppColors.bg,
          systemNavigationBarIconBrightness: Brightness.light,
        ),
        // The layout is drawn to the pixel; huge system fonts would break it.
        child: MediaQuery.withClampedTextScaling(maxScaleFactor: 1.15, child: child!),
      ),
      home: _Root(session: Session(api: api, storage: storage, clientName: clientName)),
    );
  }
}

enum _Phase { starting, onboarding, home }

/// Walks a launch through its steps: get a token, ask the onboarding
/// questions once, show the tabs.
class _Root extends StatefulWidget {
  const _Root({required this.session});

  final Session session;

  @override
  State<_Root> createState() => _RootState();
}

class _RootState extends State<_Root> {
  _Phase _phase = _Phase.starting;
  Object? _error;

  @override
  void initState() {
    super.initState();
    _start();
  }

  Future<void> _start() async {
    setState(() => _error = null);
    try {
      await widget.session.start();
      final profile = await widget.session.storage.readProfile();
      if (!mounted) return;
      setState(() => _phase = profile == null ? _Phase.onboarding : _Phase.home);
    } on Object catch (e) {
      if (mounted) setState(() => _error = e);
    }
  }

  Future<void> _finishOnboarding(Profile profile) async {
    setState(() => _phase = _Phase.home);
    await widget.session.storage.writeProfile(profile);
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedSwitcher(
      duration: const Duration(milliseconds: 400),
      child: switch (_phase) {
        _Phase.starting => StartupScreen(key: const ValueKey(_Phase.starting), error: _error, onRetry: _start),
        _Phase.onboarding => OnboardingScreen(key: const ValueKey(_Phase.onboarding), onDone: _finishOnboarding),
        _Phase.home => HomeShell(key: const ValueKey(_Phase.home), api: widget.session.api),
      },
    );
  }
}
