import 'dart:async';

import 'package:flutter/material.dart';

import '../theme/app_colors.dart';
import '../theme/app_text.dart';

/// The pill that rises over the action bar for a few seconds. Put one in a
/// [Stack] over the screen and call [AppToastState.show] through its key.
class AppToast extends StatefulWidget {
  const AppToast({super.key});

  @override
  State<AppToast> createState() => AppToastState();
}

class AppToastState extends State<AppToast> {
  String _message = '';
  bool _visible = false;
  Timer? _timer;

  void show(String message) {
    _timer?.cancel();
    setState(() {
      _message = message;
      _visible = true;
    });
    _timer = Timer(const Duration(milliseconds: 2800), () {
      if (mounted) setState(() => _visible = false);
    });
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    const curve = Cubic(0.34, 1.56, 0.64, 1);
    return IgnorePointer(
      child: AnimatedSlide(
        offset: _visible ? Offset.zero : const Offset(0, 0.5),
        duration: const Duration(milliseconds: 350),
        curve: curve,
        child: AnimatedOpacity(
          opacity: _visible ? 1 : 0,
          duration: const Duration(milliseconds: 350),
          child: Container(
            constraints: const BoxConstraints(maxWidth: 320),
            padding: const EdgeInsets.symmetric(horizontal: 22, vertical: 10),
            decoration: BoxDecoration(
              color: const Color.fromRGBO(30, 26, 60, 0.95),
              borderRadius: BorderRadius.circular(50),
              border: Border.all(color: AppColors.ink(0.12)),
              boxShadow: [BoxShadow(color: AppColors.shade(0.4), offset: const Offset(0, 8), blurRadius: 30)],
            ),
            child: Text(
              _message,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: AppText.body(size: 13, weight: FontWeight.w800),
            ),
          ),
        ),
      ),
    );
  }
}
