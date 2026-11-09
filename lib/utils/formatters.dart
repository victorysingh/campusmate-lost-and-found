const List<String> _months = [
  'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
  'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec',
];

/// 10 Nov 2026
String formatDate(DateTime date) => '${date.day} ${_months[date.month - 1]} ${date.year}';
