// App-wide settings the team may need to change. Nothing secret goes here.

/// Only emails ending in "@" + this domain can sign up or log in.
///
/// TODO(team): put your college email domain here, for example 'rvce.edu.in'.
/// Leave it empty ('') to allow any email address (useful while testing).
// ignore: constant_identifier_names
const String ALLOWED_EMAIL_DOMAIN = '';

/// Name shown in the app bar and on the login screen.
const String appName = 'Campusmate';

/// Categories shown in the dropdowns. Matching only compares posts in the
/// same category, so keep this list short and clear.
const List<String> itemCategories = [
  'Electronics',
  'ID Card',
  'Wallet',
  'Keys',
  'Books & Notes',
  'Bags',
  'Clothing',
  'Water Bottle',
  'Other',
];

/// Photos are compressed until they are smaller than this (300 KB).
const int maxImageBytes = 300 * 1024;

/// The feed loads at most this many of the newest posts.
const int feedLimit = 100;
