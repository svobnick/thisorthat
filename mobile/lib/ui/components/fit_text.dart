import 'package:flutter/widgets.dart';

/// Centered text that shrinks until it fits the box it is given. Answers run
/// from one word to 150 characters and the halves of a card change height, so
/// no single size works.
class FitText extends StatelessWidget {
  const FitText(this.text, {super.key, required this.style, this.minSize = 10});

  final String text;
  final TextStyle style;
  final double minSize;

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final scaler = MediaQuery.textScalerOf(context);
        final direction = Directionality.of(context);
        var size = style.fontSize!;
        var fitted = style;
        int? maxLines;
        while (true) {
          fitted = style.copyWith(fontSize: size);
          final painter = TextPainter(
            text: TextSpan(text: text, style: fitted),
            textAlign: TextAlign.center,
            textDirection: direction,
            textScaler: scaler,
          )..layout(maxWidth: constraints.maxWidth);
          final fits = painter.height <= constraints.maxHeight;
          final lineHeight = painter.preferredLineHeight;
          painter.dispose();
          if (fits) break;
          if (size <= minSize) {
            // Even the smallest size is too tall: cut it off.
            maxLines = (constraints.maxHeight / lineHeight).floor().clamp(1, 99);
            break;
          }
          size -= 1;
        }
        return Text(
          text,
          style: fitted,
          textAlign: TextAlign.center,
          maxLines: maxLines,
          overflow: maxLines == null ? TextOverflow.clip : TextOverflow.ellipsis,
        );
      },
    );
  }
}
