import 'package:flutter/material.dart';

/// Selectable chip with a category icon, used in the Home filter row.
class CategoryChip extends StatelessWidget {
  const CategoryChip({
    super.key,
    required this.label,
    required this.icon,
    required this.selected,
    required this.onTap,
  });

  final String label;
  final IconData icon;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    return ChoiceChip(
      avatar: Icon(icon, size: 18, color: selected ? colors.onPrimary : colors.primary),
      label: Text(label),
      selected: selected,
      showCheckmark: false,
      selectedColor: colors.primary,
      labelStyle: TextStyle(
        color: selected ? colors.onPrimary : colors.onSurface,
        fontWeight: selected ? FontWeight.w600 : FontWeight.w400,
      ),
      onSelected: (_) => onTap(),
    );
  }
}

/// Icon for each category in config.dart (fallback: a box).
IconData iconForCategory(String category) {
  switch (category) {
    case 'Electronics':
      return Icons.headphones_rounded;
    case 'ID Card':
      return Icons.badge_rounded;
    case 'Wallet':
      return Icons.account_balance_wallet_rounded;
    case 'Keys':
      return Icons.key_rounded;
    case 'Books & Notes':
      return Icons.menu_book_rounded;
    case 'Bags':
      return Icons.backpack_rounded;
    case 'Clothing':
      return Icons.checkroom_rounded;
    case 'Water Bottle':
      return Icons.water_drop_rounded;
    default:
      return Icons.inventory_2_rounded;
  }
}
