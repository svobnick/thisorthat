import 'dart:math' as math;
import 'dart:ui' show lerpDouble;

import 'package:flutter/material.dart';

import '../../../api/api_client.dart';
import '../../../api/models.dart';
import '../../../l10n/l10n.dart';
import '../../components/action_bar.dart';
import '../../components/app_dialog.dart';
import '../../components/app_icon.dart';
import '../../components/app_logo.dart';
import '../../components/app_toast.dart';
import '../../components/gradient_button.dart';
import '../../components/question_card.dart';
import '../../components/round_icon_button.dart';
import '../../components/share_question.dart';
import '../../theme/app_colors.dart';
import '../../theme/app_text.dart';
import '../comments/comments_screen.dart';
import 'feed_controller.dart';
import 'report_sheet.dart';

/// The main screen: one question at a time. Tap an answer to vote, swipe left
/// for the next question, right for the one before.
class FeedScreen extends StatefulWidget {
  const FeedScreen({super.key, required this.api});

  final ApiClient api;

  @override
  State<FeedScreen> createState() => _FeedScreenState();
}

class _FeedScreenState extends State<FeedScreen> with SingleTickerProviderStateMixin {
  static const _dotCount = 7;

  late final FeedController _feed = FeedController(widget.api)..start();
  late final AppLifecycleListener _lifecycle;
  late final AnimationController _slide = AnimationController(vsync: this);
  final _toast = GlobalKey<AppToastState>();
  final _cardKey = GlobalKey();

  // Where the card is, in pixels off its resting place.
  double _dx = 0;
  bool _entering = false;
  bool _sliding = false;

  @override
  void initState() {
    super.initState();
    // Answers still on the device go out before the app can be killed.
    _lifecycle = AppLifecycleListener(onHide: _feed.flush, onPause: _feed.flush);
  }

  @override
  void dispose() {
    _lifecycle.dispose();
    _slide.dispose();
    _feed.dispose();
    super.dispose();
  }

  double get _travel => MediaQuery.sizeOf(context).width * 1.1;

  Future<void> _slideTo(double target, Duration duration, Curve curve) async {
    final from = _dx;
    final animation = CurvedAnimation(parent: _slide, curve: curve);
    void tick() => setState(() => _dx = lerpDouble(from, target, animation.value)!);
    _slide
      ..duration = duration
      ..reset()
      ..addListener(tick);
    await _slide.forward();
    _slide.removeListener(tick);
  }

  /// Slides the card away, lets [move] change the question, and slides the
  /// new one in from the other side.
  Future<void> _turn({required bool forward, required VoidCallback move}) async {
    if (_sliding) return;
    _sliding = true;
    final exit = forward ? -_travel : _travel;
    await _slideTo(exit, const Duration(milliseconds: 320), Curves.easeInOut);
    if (!mounted) return;
    move();
    setState(() {
      _dx = -exit;
      _entering = true;
    });
    await _slideTo(0, const Duration(milliseconds: 450), const Cubic(0.16, 1, 0.3, 1));
    if (!mounted) return;
    setState(() => _entering = false);
    _sliding = false;
  }

  void _next() {
    if (_feed.current == null) return;
    _turn(forward: true, move: _feed.next);
  }

  void _onDragUpdate(DragUpdateDetails details) {
    if (_sliding) return;
    setState(() => _dx += details.delta.dx);
  }

  void _onDragEnd(DragEndDetails details) {
    if (_sliding) return;
    final velocity = details.primaryVelocity ?? 0;
    if ((_dx < -60 || velocity < -500) && _feed.current != null) {
      _turn(forward: true, move: _feed.next);
    } else if ((_dx > 60 || velocity > 500) && _feed.canGoBack) {
      _turn(forward: false, move: _feed.previous);
    } else {
      _slideTo(0, const Duration(milliseconds: 220), Curves.easeOut);
    }
  }

  void _vote(FeedEntry entry, Choice choice) {
    _feed.vote(choice);
    final answer = choice == Choice.first ? entry.question.firstText : entry.question.lastText;
    _toast.currentState?.show(context.l10n.youVoted(answer));
  }

  Future<void> _report() async {
    if (_feed.current == null || _sliding) return;
    final l10n = context.l10n;
    final reason = await showReportSheet(context);
    if (reason == null || !mounted) return;
    // The failure, if any, is told after the thank-you is read.
    final sending = _feed.report(reason).then<Object?>((_) => null, onError: (Object e) => e);
    _turn(forward: true, move: _feed.next);
    await showAppDialog(
      context,
      title: l10n.reportResultTitle,
      text: l10n.reportResultText,
      confirmLabel: l10n.ok,
    );
    final failure = await sending;
    if (failure != null) _toast.currentState?.show(failureMessage(l10n, failure));
  }

  Future<void> _toggleFavorite() async {
    final l10n = context.l10n;
    try {
      await _feed.toggleFavorite();
    } on Object catch (e) {
      _toast.currentState?.show(failureMessage(l10n, e));
    }
  }

  void _openComments(FeedEntry entry) {
    Navigator.of(context).push(
      MaterialPageRoute<void>(
        builder: (_) => CommentsScreen(
          api: widget.api,
          question: entry.question,
          firstVotes: entry.firstVotes,
          lastVotes: entry.lastVotes,
          showStats: !entry.canVote,
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: _feed,
      builder: (context, _) {
        final entry = _feed.current;
        final progress = (_dx.abs() / _travel).clamp(0.0, 1.0);
        return Stack(
          children: [
            SafeArea(
              bottom: false,
              child: Column(
                children: [
                  _Header(onReport: entry == null ? null : _report),
                  _SwipeHint(active: _feed.position % _dotCount, count: _dotCount),
                  Expanded(
                    child: Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 16),
                      child: Column(
                        children: [
                          Expanded(
                            child: GestureDetector(
                              behavior: HitTestBehavior.opaque,
                              onHorizontalDragUpdate: _onDragUpdate,
                              onHorizontalDragEnd: _onDragEnd,
                              child: Transform.translate(
                                offset: Offset(_dx, 0),
                                child: Transform.rotate(
                                  angle: _entering ? 0 : _dx / _travel * 5 * math.pi / 180,
                                  child: Transform.scale(
                                    scale: _entering ? 1 - 0.08 * progress : 1,
                                    child: Opacity(opacity: 1 - progress, child: _card(entry)),
                                  ),
                                ),
                              ),
                            ),
                          ),
                          Container(
                            height: 14,
                            margin: const EdgeInsets.fromLTRB(16, 4, 16, 0),
                            decoration: BoxDecoration(
                              color: AppColors.ink(0.04),
                              borderRadius: const BorderRadius.vertical(bottom: Radius.circular(18)),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                  Builder(
                    builder: (shareContext) => ActionBar(
                      onComments: entry == null ? null : () => _openComments(entry),
                      liked: entry?.favorite ?? false,
                      onLike: entry == null ? null : _toggleFavorite,
                      onShare: entry == null ? null : () => shareQuestionCard(shareContext, _cardKey),
                    ),
                  ),
                ],
              ),
            ),
            Positioned(
              left: 16,
              right: 16,
              bottom: 86,
              child: Center(child: AppToast(key: _toast)),
            ),
          ],
        );
      },
    );
  }

  Widget _card(FeedEntry? entry) {
    if (entry == null) {
      return _Placeholder(
        loading: _feed.loading,
        message: _feed.error != null
            ? failureMessage(context.l10n, _feed.error!)
            : context.l10n.feedEmpty,
        onRetry: _feed.retry,
      );
    }
    return RepaintBoundary(
      key: _cardKey,
      child: QuestionCard(
        key: ValueKey(entry.question.id),
        firstText: entry.question.firstText,
        lastText: entry.question.lastText,
        firstVotes: entry.firstVotes,
        lastVotes: entry.lastVotes,
        choice: entry.choice,
        revealed: !entry.canVote,
        onVote: (choice) => _vote(entry, choice),
        onTapRevealed: _next,
      ),
    );
  }
}

class _Header extends StatelessWidget {
  const _Header({required this.onReport});

  final VoidCallback? onReport;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(18, 4, 18, 4),
      child: Stack(
        alignment: Alignment.center,
        children: [
          const AppLogo(),
          Align(
            alignment: Alignment.centerRight,
            child: RoundIconButton(icon: AppIcons.report, label: context.l10n.reportAction, onPressed: onReport),
          ),
        ],
      ),
    );
  }
}

/// The row of dots that walks along as questions go by, and the nudge to
/// swipe.
class _SwipeHint extends StatefulWidget {
  const _SwipeHint({required this.active, required this.count});

  final int active;
  final int count;

  @override
  State<_SwipeHint> createState() => _SwipeHintState();
}

class _SwipeHintState extends State<_SwipeHint> with SingleTickerProviderStateMixin {
  late final AnimationController _nudge =
      AnimationController(vsync: this, duration: const Duration(seconds: 1))..repeat(reverse: true);

  @override
  void dispose() {
    _nudge.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final nudge = CurvedAnimation(parent: _nudge, curve: Curves.easeInOut);
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          for (var i = 0; i < widget.count; i++)
            AnimatedContainer(
              duration: const Duration(milliseconds: 300),
              width: 6,
              height: 6,
              margin: const EdgeInsets.only(right: 5),
              transform: Matrix4.diagonal3Values(i == widget.active ? 1.3 : 1, i == widget.active ? 1.3 : 1, 1),
              transformAlignment: Alignment.center,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: i == widget.active ? AppColors.first : AppColors.ink(0.18),
              ),
            ),
          const SizedBox(width: 3),
          Text(
            context.l10n.swipeHint.toUpperCase(),
            style: AppText.body(size: 10, weight: FontWeight.w800, color: AppColors.ink(0.25), letterSpacing: 1.5),
          ),
          const SizedBox(width: 4),
          AnimatedBuilder(
            animation: nudge,
            builder: (context, child) => Transform.translate(
              offset: Offset(6 * nudge.value, 0),
              child: Opacity(opacity: 0.3 + 0.5 * nudge.value, child: child),
            ),
            child: AppIcon(AppIcons.arrowRight, size: 12, strokeWidth: 2.5, color: AppColors.ink(0.8)),
          ),
        ],
      ),
    );
  }
}

/// Stands where the card would be while there is no question to show.
class _Placeholder extends StatelessWidget {
  const _Placeholder({required this.loading, required this.message, required this.onRetry});

  final bool loading;
  final String message;
  final VoidCallback onRetry;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(32),
      decoration: BoxDecoration(
        color: AppColors.ink(0.04),
        borderRadius: BorderRadius.circular(QuestionCard.radius),
        border: Border.all(color: AppColors.ink(0.08)),
      ),
      child: loading
          ? const Center(child: CircularProgressIndicator(strokeWidth: 3, color: AppColors.first))
          : Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text(
                  message,
                  textAlign: TextAlign.center,
                  style: AppText.body(size: 15, weight: FontWeight.w800, color: AppColors.ink(0.6), height: 1.4),
                ),
                const SizedBox(height: 24),
                GradientButton(label: context.l10n.retry, onPressed: onRetry),
              ],
            ),
    );
  }
}
