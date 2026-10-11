import 'package:flutter/material.dart';

import '../../api/models.dart';
import '../../l10n/l10n.dart';
import '../theme/app_colors.dart';
import '../theme/app_text.dart';
import 'app_icon.dart';
import 'fit_text.dart';

/// The card of the mock-up: the first answer on red above, the last on purple
/// below, an "or" bubble between them.
///
/// Until it is [revealed] the halves are equal and each one is a button. Once
/// revealed they resize to how people voted and each shows its percentage and
/// its count. A card that becomes revealed while it is on screen animates the
/// change; one built already revealed just shows it.
class QuestionCard extends StatefulWidget {
  const QuestionCard({
    super.key,
    required this.firstText,
    required this.lastText,
    required this.firstVotes,
    required this.lastVotes,
    this.choice,
    this.revealed = false,
    this.onVote,
    this.onTapRevealed,
  });

  final String firstText;
  final String lastText;
  final int firstVotes;
  final int lastVotes;

  /// The answer this person picked, which gets the emphasis. Anything but
  /// first or last means there is none.
  final Choice? choice;
  final bool revealed;

  /// Called with the tapped half while the card is not revealed.
  final ValueChanged<Choice>? onVote;

  /// Called when a revealed card is tapped anywhere.
  final VoidCallback? onTapRevealed;

  static const radius = 28.0;

  @override
  State<QuestionCard> createState() => _QuestionCardState();
}

class _QuestionCardState extends State<QuestionCard> with SingleTickerProviderStateMixin {
  static const _stripHeight = 56.0;
  static const _bubbleSize = 60.0;
  static const _overshoot = Cubic(0.34, 1.56, 0.64, 1);

  // The tick that pops over the chosen half and fades away.
  late final AnimationController _tick =
      AnimationController(vsync: this, duration: const Duration(milliseconds: 1600));

  // The reveal happened in front of the person, so the numbers count up.
  bool _live = false;

  @override
  void didUpdateWidget(QuestionCard oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (!oldWidget.revealed && widget.revealed) {
      _live = true;
      if (_hasChoice) _tick.forward(from: 0);
    }
  }

  @override
  void dispose() {
    _tick.dispose();
    super.dispose();
  }

  bool get _hasChoice => widget.choice == Choice.first || widget.choice == Choice.last;

  @override
  Widget build(BuildContext context) {
    final share = voteShare(widget.firstVotes, widget.lastVotes);
    // A landslide would squeeze the losing answer out of sight; the numbers
    // tell the real split, the heights only hint at it.
    final split = widget.revealed ? (share.first / 100).clamp(0.35, 0.65).toDouble() : 0.5;

    return DecoratedBox(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(QuestionCard.radius),
        boxShadow: [BoxShadow(color: AppColors.shade(0.55), offset: const Offset(0, 24), blurRadius: 70)],
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(QuestionCard.radius),
        child: Stack(
          fit: StackFit.expand,
          children: [
            LayoutBuilder(
              builder: (context, constraints) => TweenAnimationBuilder<double>(
                tween: Tween<double>(end: split),
                duration: const Duration(milliseconds: 550),
                curve: const Cubic(0.34, 1.2, 0.64, 1),
                builder: (context, value, _) {
                  final top = (constraints.maxHeight - _stripHeight) * value;
                  return Stack(
                    children: [
                      Column(
                        children: [
                          SizedBox(height: top, child: _half(Choice.first, share.first)),
                          const _OrStrip(height: _stripHeight),
                          Expanded(child: _half(Choice.last, share.last)),
                        ],
                      ),
                      Positioned(
                        top: top + (_stripHeight - _bubbleSize) / 2,
                        left: 0,
                        right: 0,
                        child: const Center(child: _OrBubble(size: _bubbleSize)),
                      ),
                    ],
                  );
                },
              ),
            ),
            IgnorePointer(
              child: DecoratedBox(
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(QuestionCard.radius),
                  border: Border.all(color: AppColors.ink(0.08)),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _half(Choice side, int percent) {
    final isFirst = side == Choice.first;
    final revealed = widget.revealed;
    final chosen = widget.choice == side;
    final text = isFirst ? widget.firstText : widget.lastText;
    final votes = isFirst ? widget.firstVotes : widget.lastVotes;

    VoidCallback? onTap;
    if (!revealed && widget.onVote != null) {
      onTap = () => widget.onVote!(side);
    } else if (revealed) {
      onTap = widget.onTapRevealed;
    }

    return Stack(
      fit: StackFit.expand,
      children: [
        DecoratedBox(
          decoration: BoxDecoration(gradient: isFirst ? AppColors.firstGradient : AppColors.lastGradient),
        ),
        // The gloss: a sheen along the outer edge and a warm spot off-center.
        DecoratedBox(
          decoration: BoxDecoration(
            gradient: LinearGradient(
              begin: isFirst ? Alignment.topCenter : Alignment.bottomCenter,
              end: isFirst ? Alignment.bottomCenter : Alignment.topCenter,
              colors: [AppColors.ink(0.18), AppColors.ink(0)],
              stops: const [0, 0.45],
            ),
          ),
        ),
        DecoratedBox(
          decoration: BoxDecoration(
            gradient: RadialGradient(
              center: isFirst ? const Alignment(-0.4, -0.5) : const Alignment(0.4, 0.5),
              radius: 1.3,
              colors: isFirst
                  ? const [Color.fromRGBO(255, 160, 130, 0.3), Color.fromRGBO(255, 160, 130, 0)]
                  : const [Color.fromRGBO(220, 130, 255, 0.3), Color.fromRGBO(220, 130, 255, 0)],
              stops: const [0, 0.65],
            ),
          ),
        ),
        Semantics(
          button: onTap != null,
          child: Material(
            type: MaterialType.transparency,
            child: InkWell(
              onTap: onTap,
              splashColor: AppColors.ink(0.22),
              highlightColor: Colors.transparent,
              child: AnimatedPadding(
                // Once revealed the text moves clear of the numbers above it.
                padding: EdgeInsets.fromLTRB(36, revealed ? 66 : 16, 36, 16),
                duration: const Duration(milliseconds: 550),
                curve: Curves.easeOutCubic,
                child: Center(
                  child: FitText(
                    text,
                    style: AppText.display(
                      size: 17,
                      weight: FontWeight.w700,
                      color: AppColors.ink(0.93),
                      height: 1.45,
                      letterSpacing: -0.3,
                      shadows: [Shadow(color: AppColors.shade(0.3), offset: const Offset(0, 2), blurRadius: 14)],
                    ),
                  ),
                ),
              ),
            ),
          ),
        ),
        // The answer not picked steps back.
        IgnorePointer(
          child: AnimatedOpacity(
            opacity: revealed && _hasChoice && !chosen ? 1 : 0,
            duration: const Duration(milliseconds: 300),
            child: ColoredBox(color: AppColors.shade(0.22)),
          ),
        ),
        Positioned(
          top: 14,
          right: 14,
          child: IgnorePointer(
            child: AnimatedScale(
              scale: revealed ? 1 : 0.7,
              duration: const Duration(milliseconds: 400),
              curve: _overshoot,
              alignment: Alignment.topRight,
              child: AnimatedOpacity(
                opacity: revealed ? 1 : 0,
                duration: const Duration(milliseconds: 400),
                child: revealed
                    ? _Stats(
                        percent: percent,
                        votes: votes,
                        countUp: _live,
                        emphasis: !_hasChoice ? _Emphasis.even : (chosen ? _Emphasis.strong : _Emphasis.weak),
                      )
                    : const SizedBox.shrink(),
              ),
            ),
          ),
        ),
        Align(
          alignment: Alignment.bottomLeft,
          child: AnimatedFractionallySizedBox(
            widthFactor: revealed ? percent / 100 : 0,
            alignment: Alignment.bottomLeft,
            duration: const Duration(milliseconds: 950),
            curve: const Interval(0.16, 1, curve: Cubic(0.16, 1, 0.3, 1)),
            child: Container(
              height: 4,
              decoration: BoxDecoration(color: AppColors.ink(0.6), borderRadius: BorderRadius.circular(2)),
            ),
          ),
        ),
        if (chosen) IgnorePointer(child: _Tick(animation: _tick)),
      ],
    );
  }
}

enum _Emphasis { strong, even, weak }

class _Stats extends StatelessWidget {
  const _Stats({required this.percent, required this.votes, required this.countUp, required this.emphasis});

  final int percent;
  final int votes;
  final bool countUp;
  final _Emphasis emphasis;

  @override
  Widget build(BuildContext context) {
    final (size, opacity) = switch (emphasis) {
      _Emphasis.strong => (42.0, 0.95),
      _Emphasis.even => (38.0, 0.9),
      _Emphasis.weak => (34.0, 0.65),
    };
    final shadows = [Shadow(color: AppColors.shade(0.35), offset: const Offset(0, 3), blurRadius: 16)];
    return TweenAnimationBuilder<double>(
      tween: Tween<double>(begin: countUp ? 0 : 1, end: 1),
      duration: const Duration(milliseconds: 500),
      builder: (context, t, _) => Column(
        crossAxisAlignment: CrossAxisAlignment.end,
        mainAxisSize: MainAxisSize.min,
        children: [
          Text.rich(
            TextSpan(
              text: '${(percent * t).round()}',
              children: [
                TextSpan(
                  text: '%',
                  style: TextStyle(fontSize: 18, color: AppColors.ink(opacity * 0.75)),
                ),
              ],
            ),
            style: AppText.display(size: size, color: AppColors.ink(opacity), height: 1, shadows: shadows),
          ),
          const SizedBox(height: 2),
          Text(
            context.l10n.votesCount((votes * t).round()),
            style: AppText.body(size: 11, weight: FontWeight.w800, color: AppColors.ink(0.5), letterSpacing: 0.8),
          ),
        ],
      ),
    );
  }
}

class _Tick extends StatelessWidget {
  const _Tick({required this.animation});

  final Animation<double> animation;

  @override
  Widget build(BuildContext context) {
    final fade = TweenSequence<double>([
      TweenSequenceItem(tween: Tween(begin: 0.0, end: 1.0), weight: 15),
      TweenSequenceItem(tween: ConstantTween(1.0), weight: 55),
      TweenSequenceItem(tween: Tween(begin: 1.0, end: 0.0), weight: 30),
    ]).animate(animation);
    final pop = CurvedAnimation(parent: animation, curve: const Interval(0, 0.28, curve: Cubic(0.34, 1.56, 0.64, 1)));
    return FadeTransition(
      opacity: fade,
      child: ColoredBox(
        color: AppColors.shade(0.12),
        child: Center(
          child: RotationTransition(
            turns: Tween(begin: -20 / 360, end: 0.0).animate(pop),
            child: ScaleTransition(
              scale: Tween(begin: 0.3, end: 1.0).animate(pop),
              child: const AppIcon(AppIcons.check, size: 58, strokeWidth: 3, color: AppColors.white),
            ),
          ),
        ),
      ),
    );
  }
}

class _OrStrip extends StatelessWidget {
  const _OrStrip({required this.height});

  final double height;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: height,
      child: const Column(
        children: [
          Expanded(child: DecoratedBox(decoration: BoxDecoration(gradient: AppColors.firstBandGradient), child: SizedBox.expand())),
          Expanded(child: DecoratedBox(decoration: BoxDecoration(gradient: AppColors.lastBandGradient), child: SizedBox.expand())),
        ],
      ),
    );
  }
}

class _OrBubble extends StatelessWidget {
  const _OrBubble({required this.size});

  final double size;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: size,
      height: size,
      alignment: Alignment.center,
      decoration: BoxDecoration(
        color: AppColors.bg,
        shape: BoxShape.circle,
        boxShadow: [
          BoxShadow(color: AppColors.ink(0.07), spreadRadius: 5),
          BoxShadow(color: AppColors.shade(0.6), offset: const Offset(0, 10), blurRadius: 35),
        ],
      ),
      child: Text(context.l10n.orDivider, style: AppText.display(size: 11, letterSpacing: 1)),
    );
  }
}
