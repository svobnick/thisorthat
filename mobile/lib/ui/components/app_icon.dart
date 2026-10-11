import 'package:flutter/widgets.dart';
import 'package:flutter_svg/flutter_svg.dart';

/// The mock-up's icons: Feather outlines, kept as their original SVG so the
/// stroke is the same on both platforms.
enum AppIcons {
  comment('<path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>'),
  heart(
    '<path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06'
    'L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>',
  ),
  share(
    '<circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/>'
    '<line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>',
  ),
  feed(
    '<circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>'
    '<line x1="12" y1="17" x2="12.01" y2="17"/>',
  ),
  plus('<line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>'),
  user('<path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>'),
  report(
    '<circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/>'
    '<line x1="12" y1="16" x2="12.01" y2="16"/>',
  ),
  back('<polyline points="15 18 9 12 15 6"/>'),
  send('<line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/>'),
  check('<polyline points="20 6 9 17 4 12"/>'),
  arrowRight('<line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/>'),
  // Drawn rather than typed: as emoji the two signs are plain white on iOS
  // and coloured on Android.
  male('<circle cx="10" cy="14" r="6"/><line x1="14.5" y1="9.5" x2="21" y2="3"/><polyline points="15 3 21 3 21 9"/>'),
  female('<circle cx="12" cy="9" r="6"/><line x1="12" y1="15" x2="12" y2="22"/><line x1="8.5" y1="18.5" x2="15.5" y2="18.5"/>');

  const AppIcons(this.body);

  final String body;
}

class AppIcon extends StatelessWidget {
  const AppIcon(
    this.icon, {
    super.key,
    this.size = 22,
    required this.color,
    this.strokeWidth = 2,
    this.fillOpacity = 0,
    this.glow,
  });

  final AppIcons icon;
  final double size;
  final Color color;
  final double strokeWidth;

  /// How much of [color] fills the shape; the liked heart is a quarter full.
  final double fillOpacity;

  /// A soft halo of this color behind the icon.
  final Color? glow;

  @override
  Widget build(BuildContext context) {
    final svg = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#fff" '
        'fill-opacity="$fillOpacity" stroke="#fff" stroke-width="$strokeWidth" '
        'stroke-linecap="round" stroke-linejoin="round">${icon.body}</svg>';
    Widget picture(Color tint) => SvgPicture.string(
          svg,
          width: size,
          height: size,
          colorFilter: ColorFilter.mode(tint, BlendMode.srcIn),
        );
    final glow = this.glow;
    if (glow == null) return picture(color);
    // A blurred copy of the icon would be truer to the mock-up's drop-shadow,
    // but an image filter under a fading parent trips the renderer; a soft
    // disc behind the icon reads the same at this size.
    return DecoratedBox(
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        boxShadow: [BoxShadow(color: glow.withValues(alpha: glow.a * 0.45), blurRadius: size * 0.45)],
      ),
      child: picture(color),
    );
  }
}
