import 'package:flutter/widgets.dart';

import 'app_colors.dart';

/// The two faces of the mock-up: Nunito for everything read, Unbounded for
/// everything shouted (the logo, answers, numbers, buttons).
abstract final class AppText {
  static TextStyle body({
    double size = 14,
    FontWeight weight = FontWeight.w700,
    Color color = AppColors.white,
    double? height,
    double? letterSpacing,
  }) =>
      TextStyle(
        fontFamily: 'Nunito',
        fontSize: size,
        fontWeight: weight,
        color: color,
        height: height,
        letterSpacing: letterSpacing,
        decoration: TextDecoration.none,
      );

  static TextStyle display({
    double size = 17,
    FontWeight weight = FontWeight.w900,
    Color color = AppColors.white,
    double? height,
    double? letterSpacing,
    List<Shadow>? shadows,
  }) =>
      TextStyle(
        fontFamily: 'Unbounded',
        fontSize: size,
        fontWeight: weight,
        color: color,
        height: height,
        letterSpacing: letterSpacing,
        shadows: shadows,
        decoration: TextDecoration.none,
      );
}
