import 'package:flutter/material.dart';

import '../theme.dart';

/// Grey "skeleton" cards with a light band sweeping across them, shown
/// while posts load. Built by hand (no extra package): one repeating
/// animation drives a gradient that is painted over the grey boxes.
class ShimmerList extends StatefulWidget {
  const ShimmerList({super.key, this.count = 3});

  final int count;

  @override
  State<ShimmerList> createState() => _ShimmerListState();
}

class _ShimmerListState extends State<ShimmerList> with SingleTickerProviderStateMixin {
  late final AnimationController _controller =
      AnimationController(vsync: this, duration: const Duration(milliseconds: 1200))..repeat();

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final base = colors.surfaceContainerHighest;
    final highlight = colors.surfaceContainerLow;

    return AnimatedBuilder(
      animation: _controller,
      builder: (context, child) {
        // Moves the bright band from left (-1) to right (+2).
        final x = -1 + 3 * _controller.value;
        return ShaderMask(
          blendMode: BlendMode.srcATop,
          shaderCallback: (rect) => LinearGradient(
            begin: Alignment(x - 1, 0),
            end: Alignment(x, 0),
            colors: [base, highlight, base],
          ).createShader(rect),
          child: child,
        );
      },
      child: Column(
        children: [
          for (var i = 0; i < widget.count; i++)
            Padding(
              padding: const EdgeInsets.only(bottom: Space.m),
              child: _SkeletonCard(color: base),
            ),
        ],
      ),
    );
  }
}

class _SkeletonCard extends StatelessWidget {
  const _SkeletonCard({required this.color});

  final Color color;

  @override
  Widget build(BuildContext context) {
    Widget bar(double width, double height) => Container(
          width: width,
          height: height,
          decoration: BoxDecoration(color: color, borderRadius: BorderRadius.circular(8)),
        );
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        AspectRatio(
          aspectRatio: 16 / 9,
          child: Container(
            decoration:
                BoxDecoration(color: color, borderRadius: BorderRadius.circular(kRadius)),
          ),
        ),
        const SizedBox(height: Space.s),
        bar(180, 16),
        const SizedBox(height: Space.s),
        bar(120, 12),
      ],
    );
  }
}
