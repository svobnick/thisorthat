import 'dart:math' as math;

import 'package:flutter/widgets.dart';

/// The palette of the mock-up (`to-ili-eto-v5.html`). "First" is the top
/// answer and everything red, "last" the bottom answer and everything purple.
abstract final class AppColors {
  /// The screen ground.
  static const bg = Color(0xFF181430);

  /// A surface lifted off the ground: sheets, dialogs, the comment field.
  static const panel = Color(0xFF1E1A40);

  static const first = Color(0xFFF07060);
  static const firstDeep = Color(0xFFA02818);
  static const last = Color(0xFFCC66EE);
  static const lastDeep = Color(0xFF7018A0);

  static const white = Color(0xFFFFFFFF);

  /// White at [opacity]; nearly every secondary tone of the mock-up is one.
  static Color ink(double opacity) => Color.fromRGBO(255, 255, 255, opacity);

  static Color shade(double opacity) => Color.fromRGBO(0, 0, 0, opacity);

  static const firstGradient = LinearGradient(
    transform: CssAngle(160),
    colors: [Color(0xFFF07060), Color(0xFFD94030), Color(0xFFB82E20)],
    stops: [0, 0.45, 1],
  );

  static const lastGradient = LinearGradient(
    transform: CssAngle(160),
    colors: [Color(0xFFCC66EE), Color(0xFF9930C0), Color(0xFF7018A0)],
    stops: [0, 0.45, 1],
  );

  /// The band around the "or" bubble: the dark end of the first answer above,
  /// the dark end of the last answer below.
  static const firstBandGradient = LinearGradient(
    transform: CssAngle(160),
    colors: [Color(0xFFD94030), Color(0xFFB82E20)],
  );

  static const lastBandGradient = LinearGradient(
    transform: CssAngle(160),
    colors: [Color(0xFF9930C0), Color(0xFF7018A0)],
  );

  /// Primary buttons and selected chips.
  static const accentGradient = LinearGradient(
    transform: CssAngle(135),
    colors: [Color(0xFFE8503A), Color(0xFFCC66EE)],
  );
}

/// Lays a [LinearGradient] out the way CSS `linear-gradient(<angle>, …)`
/// does: 0° points up, 90° right, and the line is just long enough for the
/// first and last colours to reach the corners, whatever the shape of the
/// box. Flutter's own begin/end alignments stretch with the box instead, which
/// turns the mock-up's steep 160° into a sideways sweep on a wide strip.
class CssAngle extends GradientTransform {
  const CssAngle(this.degrees);

  final double degrees;

  @override
  Matrix4 transform(Rect bounds, {TextDirection? textDirection}) {
    final angle = degrees * math.pi / 180;
    final length = (bounds.width * math.sin(angle)).abs() + (bounds.height * math.cos(angle)).abs();
    final center = bounds.center;
    // The gradient starts out running left to right across the box.
    return Matrix4.identity()
      ..translateByDouble(center.dx, center.dy, 0, 1)
      ..rotateZ(angle - math.pi / 2)
      ..scaleByDouble(length / bounds.width, length / bounds.width, 1, 1)
      ..translateByDouble(-center.dx, -center.dy, 0, 1);
  }
}
