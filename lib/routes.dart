import 'package:flutter/material.dart';

import 'screens/add_item_screen.dart';
import 'screens/item_detail_screen.dart';
import 'screens/signup_screen.dart';

/// Names of the screens we open with Navigator.pushNamed.
/// Login and Home are not here: AuthGate (app.dart `home:`) picks between them.
class AppRoutes {
  static const signup = '/signup';
  static const addItem = '/add-item'; // argument: ItemType to pre-select
  static const itemDetail = '/item'; // argument: the Item to show
}

/// Route name -> screen builder. Passed to MaterialApp.routes.
final Map<String, WidgetBuilder> appRoutes = {
  AppRoutes.signup: (context) => const SignupScreen(),
  AppRoutes.addItem: (context) => const AddItemScreen(),
  AppRoutes.itemDetail: (context) => const ItemDetailScreen(),
};
