import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../config.dart';
import '../providers/auth_provider.dart';
import '../utils/validators.dart';
import '../widgets/primary_button.dart';

/// Create an account with name, college email and password.
class SignupScreen extends StatefulWidget {
  const SignupScreen({super.key});

  @override
  State<SignupScreen> createState() => _SignupScreenState();
}

class _SignupScreenState extends State<SignupScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();

  @override
  void dispose() {
    _nameController.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    final error = await context.read<AuthProvider>().signUp(
        _nameController.text, _emailController.text, _passwordController.text);
    if (!mounted) return;
    if (error != null) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(error)));
    } else {
      // Signed up and logged in. Close this screen; AuthGate (underneath)
      // is already showing Home.
      Navigator.pop(context);
    }
  }

  @override
  Widget build(BuildContext context) {
    final busy = context.watch<AuthProvider>().isBusy;
    return Scaffold(
      appBar: AppBar(title: const Text('Create account')),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                TextFormField(
                  controller: _nameController,
                  textCapitalization: TextCapitalization.words,
                  decoration: const InputDecoration(
                      labelText: 'Full name', prefixIcon: Icon(Icons.person_outline)),
                  validator: (v) => validateRequired(v, 'your name'),
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _emailController,
                  keyboardType: TextInputType.emailAddress,
                  decoration: InputDecoration(
                    labelText: 'College email',
                    prefixIcon: const Icon(Icons.email_outlined),
                    helperText: ALLOWED_EMAIL_DOMAIN.isEmpty
                        ? null
                        : 'Must end with @$ALLOWED_EMAIL_DOMAIN',
                  ),
                  validator: validateEmail,
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _passwordController,
                  obscureText: true,
                  decoration: const InputDecoration(
                      labelText: 'Password (min 6 characters)',
                      prefixIcon: Icon(Icons.lock_outline)),
                  validator: validatePassword,
                ),
                const SizedBox(height: 16),
                TextFormField(
                  obscureText: true,
                  decoration: const InputDecoration(
                      labelText: 'Confirm password',
                      prefixIcon: Icon(Icons.lock_outline)),
                  validator: (v) =>
                      v == _passwordController.text ? null : 'Passwords do not match',
                ),
                const SizedBox(height: 24),
                PrimaryButton(label: 'Sign up', loading: busy, onPressed: _submit),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
