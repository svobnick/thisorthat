import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../../api/api_client.dart';
import '../../../l10n/l10n.dart';
import '../../components/app_dialog.dart';
import '../../components/gradient_button.dart';
import '../../components/question_card.dart';
import '../../theme/app_colors.dart';
import '../../theme/app_text.dart';

/// The limits the API puts on each answer of a question.
const answerMinLength = 4;
const answerMaxLength = 150;

bool isValidAnswer(String text) {
  final length = text.trim().length;
  return length >= answerMinLength && length <= answerMaxLength;
}

/// The form for a question of one's own: the card of the feed with a text
/// field in each half.
class CreateScreen extends StatefulWidget {
  const CreateScreen({super.key, required this.api, required this.onAdded});

  final ApiClient api;

  /// Called after the "question added" message is closed.
  final VoidCallback onAdded;

  @override
  State<CreateScreen> createState() => _CreateScreenState();
}

class _CreateScreenState extends State<CreateScreen> {
  final _first = TextEditingController();
  final _last = TextEditingController();
  final _lastFocus = FocusNode();
  bool _sending = false;

  @override
  void dispose() {
    _first.dispose();
    _last.dispose();
    _lastFocus.dispose();
    super.dispose();
  }

  void _clear() {
    _first.clear();
    _last.clear();
  }

  Future<void> _send() async {
    final l10n = context.l10n;
    FocusScope.of(context).unfocus();
    if (!isValidAnswer(_first.text) || !isValidAnswer(_last.text)) {
      await showAppDialog(context, title: l10n.errorTitle, text: l10n.createValidation, confirmLabel: l10n.ok);
      return;
    }
    setState(() => _sending = true);
    try {
      await widget.api.addItem(firstText: _first.text.trim(), lastText: _last.text.trim());
      if (!mounted) return;
      _clear();
      await showAppDialog(
        context,
        title: l10n.createdTitle,
        text: l10n.createdText,
        confirmLabel: l10n.createdOk,
      );
      widget.onAdded();
    } on ApiException catch (e) {
      if (!mounted) return;
      final cloneId = e.cloneId;
      if (cloneId == null) {
        await showErrorDialog(context, e);
      } else {
        await _offerFavorite(cloneId);
      }
    } finally {
      if (mounted) setState(() => _sending = false);
    }
  }

  // The question exists already: the next best thing is to keep it.
  Future<void> _offerFavorite(int questionId) async {
    final l10n = context.l10n;
    final add = await showAppDialog(
      context,
      emblem: l10n.cloneShrug,
      title: l10n.cloneTitle,
      text: l10n.cloneText,
      confirmLabel: l10n.yes,
      cancelLabel: l10n.no,
    );
    _clear();
    if (add != true) return;
    try {
      await widget.api.addFavorite(questionId);
    } on Object catch (e) {
      if (mounted) await showErrorDialog(context, e);
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;
    return GestureDetector(
      behavior: HitTestBehavior.translucent,
      onTap: () => FocusScope.of(context).unfocus(),
      child: SafeArea(
        bottom: false,
        child: SingleChildScrollView(
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 8),
                child: Text(l10n.createTitle, style: AppText.display(size: 20, height: 1.3)),
              ),
              const SizedBox(height: 6),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 8),
                child: Text(
                  l10n.createSubtitle,
                  style: AppText.body(size: 13, color: AppColors.ink(0.4), height: 1.5),
                ),
              ),
              const SizedBox(height: 20),
              _EditorCard(
                first: _AnswerField(
                  key: const ValueKey('create-first'),
                  controller: _first,
                  hint: l10n.createFirstHint,
                  action: TextInputAction.next,
                  onSubmitted: _lastFocus.requestFocus,
                ),
                last: _AnswerField(
                  key: const ValueKey('create-last'),
                  controller: _last,
                  focusNode: _lastFocus,
                  hint: l10n.createLastHint,
                  action: TextInputAction.done,
                  onSubmitted: () => FocusScope.of(context).unfocus(),
                ),
              ),
              const SizedBox(height: 24),
              GradientButton(label: l10n.createSend, busy: _sending, onPressed: _send),
            ],
          ),
        ),
      ),
    );
  }
}

class _EditorCard extends StatelessWidget {
  const _EditorCard({required this.first, required this.last});

  final Widget first;
  final Widget last;

  static const _halfHeight = 132.0;
  static const _stripHeight = 40.0;
  static const _bubbleSize = 52.0;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(QuestionCard.radius),
        boxShadow: [BoxShadow(color: AppColors.shade(0.45), offset: const Offset(0, 18), blurRadius: 50)],
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(QuestionCard.radius),
        child: Stack(
          alignment: Alignment.center,
          children: [
            Column(
              children: [
                Container(
                  height: _halfHeight,
                  decoration: const BoxDecoration(gradient: AppColors.firstGradient),
                  child: first,
                ),
                const SizedBox(
                  height: _stripHeight,
                  child: Column(
                    children: [
                      Expanded(child: DecoratedBox(decoration: BoxDecoration(gradient: AppColors.firstBandGradient), child: SizedBox.expand())),
                      Expanded(child: DecoratedBox(decoration: BoxDecoration(gradient: AppColors.lastBandGradient), child: SizedBox.expand())),
                    ],
                  ),
                ),
                Container(
                  height: _halfHeight,
                  decoration: const BoxDecoration(gradient: AppColors.lastGradient),
                  child: last,
                ),
              ],
            ),
            Container(
              width: _bubbleSize,
              height: _bubbleSize,
              alignment: Alignment.center,
              decoration: BoxDecoration(
                color: AppColors.bg,
                shape: BoxShape.circle,
                boxShadow: [BoxShadow(color: AppColors.ink(0.07), spreadRadius: 5)],
              ),
              child: Text(context.l10n.orDivider, style: AppText.display(size: 10, letterSpacing: 1)),
            ),
          ],
        ),
      ),
    );
  }
}

class _AnswerField extends StatelessWidget {
  const _AnswerField({
    super.key,
    required this.controller,
    required this.hint,
    required this.action,
    required this.onSubmitted,
    this.focusNode,
  });

  final TextEditingController controller;
  final FocusNode? focusNode;
  final String hint;
  final TextInputAction action;
  final VoidCallback onSubmitted;

  @override
  Widget build(BuildContext context) {
    final style = AppText.display(size: 14, weight: FontWeight.w700, color: AppColors.ink(0.95), height: 1.4);
    return Stack(
      children: [
        Center(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 14),
            child: TextField(
              controller: controller,
              focusNode: focusNode,
              minLines: 1,
              // The longest answer the API takes fills five lines.
              maxLines: 5,
              textAlign: TextAlign.center,
              textCapitalization: TextCapitalization.sentences,
              textInputAction: action,
              onSubmitted: (_) => onSubmitted(),
              inputFormatters: [
                // An answer is one line; Enter moves on instead.
                FilteringTextInputFormatter.deny('\n'),
                LengthLimitingTextInputFormatter(answerMaxLength),
              ],
              style: style,
              decoration: InputDecoration(
                isDense: true,
                border: InputBorder.none,
                contentPadding: EdgeInsets.zero,
                hintText: hint,
                hintStyle: style.copyWith(color: AppColors.ink(0.45)),
              ),
            ),
          ),
        ),
        Positioned(
          right: 14,
          bottom: 8,
          child: ListenableBuilder(
            listenable: controller,
            builder: (context, _) => Text(
              '${controller.text.trim().length}/$answerMaxLength',
              style: AppText.body(size: 10, weight: FontWeight.w800, color: AppColors.ink(0.45)),
            ),
          ),
        ),
      ],
    );
  }
}
