// App-wide settings the team may need to change. Nothing secret goes here.

/// true  = MOCK MODE: login and posts live in memory only (no internet or
///         Firebase needed). Use this for demos and while Firebase is not set up.
/// false = use Firebase. If Firebase is not configured yet the app still
///         falls back to mock mode and shows the yellow banner.
// ignore: constant_identifier_names
const bool USE_MOCK_DATA = true;

/// Only emails ending in "@" + this domain can sign up or log in.
///
/// TODO(team): put your college email domain here, for example 'dsce.edu.in'.
/// Leave it empty ('') to allow any email address (useful while testing).
// ignore: constant_identifier_names
const String ALLOWED_EMAIL_DOMAIN = '';

/// Name shown in the app bar and on the login screen.
const String appName = 'Campusmate';

/// Categories shown as chips and in the post form. Matching only compares
/// posts in the same category, so keep this list short and clear.
/// (Each one has an icon in widgets/category_chip.dart.)
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

/// Places on campus offered in the post form's location step.
const List<String> campusLocations = [
  'Main gate',
  'Library',
  'Block A canteen',
  'Seminar hall',
  'Auditorium',
  'CSE block',
  'Mechanical block',
  'Basketball court',
  'Parking area',
  'Hostel',
  'Other',
];

/// Photos are compressed until they are smaller than this (300 KB).
const int maxImageBytes = 300 * 1024;

/// The feed loads at most this many of the newest posts.
const int feedLimit = 100;
