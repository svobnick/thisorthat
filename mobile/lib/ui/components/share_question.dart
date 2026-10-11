import 'dart:io';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/rendering.dart';
import 'package:flutter/widgets.dart';
import 'package:path_provider/path_provider.dart';
import 'package:share_plus/share_plus.dart';

import '../../l10n/l10n.dart';
import '../theme/app_colors.dart';
import '../theme/app_text.dart';

/// Hands the OS share sheet a picture of the card behind [cardKey] under the
/// logo. [context] is the button that was pressed; an iPad anchors the sheet
/// to it.
Future<void> shareQuestionCard(BuildContext context, GlobalKey cardKey) async {
  final l10n = context.l10n;
  final boundary = cardKey.currentContext?.findRenderObject();
  if (boundary is! RenderRepaintBoundary) return;
  final button = context.findRenderObject();
  final origin = button is RenderBox ? button.localToGlobal(Offset.zero) & button.size : null;

  const scale = 3.0;
  final card = await boundary.toImage(pixelRatio: scale);
  final bytes = await _compose(card, l10n, scale);
  card.dispose();

  final directory = await getTemporaryDirectory();
  final file = File('${directory.path}/thisorthat.png');
  await file.writeAsBytes(bytes, flush: true);
  await SharePlus.instance.share(
    ShareParams(
      files: [XFile(file.path, mimeType: 'image/png')],
      subject: l10n.shareSubject,
      sharePositionOrigin: origin,
    ),
  );
}

Future<Uint8List> _compose(ui.Image card, AppLocalizations l10n, double scale) async {
  final margin = 24 * scale;
  final logo = TextPainter(
    textDirection: TextDirection.ltr,
    text: TextSpan(
      children: [
        TextSpan(text: l10n.logoFirst, style: AppText.display(size: 30 * scale, color: AppColors.first)),
        TextSpan(
          text: '  ${l10n.logoOr}  ',
          style: AppText.body(size: 18 * scale, color: AppColors.ink(0.35), letterSpacing: 3 * scale),
        ),
        TextSpan(text: l10n.logoLast, style: AppText.display(size: 30 * scale, color: AppColors.last)),
      ],
    ),
  )..layout();

  final width = card.width + margin * 2;
  final height = margin + logo.height + margin + card.height + margin;
  final recorder = ui.PictureRecorder();
  final canvas = Canvas(recorder)..drawColor(AppColors.bg, BlendMode.src);
  logo.paint(canvas, Offset((width - logo.width) / 2, margin));
  canvas.drawImage(card, Offset(margin, margin + logo.height + margin), Paint());
  logo.dispose();

  final picture = recorder.endRecording();
  final image = await picture.toImage(width.round(), height.round());
  picture.dispose();
  final data = await image.toByteData(format: ui.ImageByteFormat.png);
  image.dispose();
  return data!.buffer.asUint8List();
}
