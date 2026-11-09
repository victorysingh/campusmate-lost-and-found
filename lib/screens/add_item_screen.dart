import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:provider/provider.dart';

import '../config.dart';
import '../models/item.dart';
import '../providers/auth_provider.dart';
import '../providers/item_provider.dart';
import '../routes.dart';
import '../services/image_service.dart';
import '../utils/formatters.dart';
import '../utils/validators.dart';

/// Form to post a Lost or Found item.
/// Route argument: the ItemType to pre-select (from the open tab).
class AddItemScreen extends StatefulWidget {
  const AddItemScreen({super.key});

  @override
  State<AddItemScreen> createState() => _AddItemScreenState();
}

class _AddItemScreenState extends State<AddItemScreen> {
  final _formKey = GlobalKey<FormState>();
  final _titleController = TextEditingController();
  final _descriptionController = TextEditingController();
  final _locationController = TextEditingController();
  final _imageService = ImageService();

  ItemType? _type; // set from the route argument on first build
  String? _category;
  DateTime _date = DateTime.now();
  String? _imageBase64;
  bool _pickingImage = false;
  bool _saving = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    // Route arguments are only available once the screen is in the tree.
    _type ??= ModalRoute.of(context)?.settings.arguments as ItemType? ?? ItemType.lost;
  }

  @override
  void dispose() {
    _titleController.dispose();
    _descriptionController.dispose();
    _locationController.dispose();
    super.dispose();
  }

  Future<void> _pickImage(ImageSource source) async {
    setState(() => _pickingImage = true);
    try {
      final base64 = await _imageService.pickImage(source);
      if (base64 != null) setState(() => _imageBase64 = base64);
    } catch (e) {
      // e.g. camera permission denied, or an unreadable file
      _showMessage('Could not get photo: $e');
    } finally {
      if (mounted) setState(() => _pickingImage = false);
    }
  }

  Future<void> _pickDate() async {
    final now = DateTime.now();
    final picked = await showDatePicker(
      context: context,
      initialDate: _date,
      firstDate: DateTime(now.year - 1),
      lastDate: now, // cannot lose or find something in the future
    );
    if (picked != null) setState(() => _date = picked);
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;
    final user = context.read<AuthProvider>().user!;
    final items = context.read<ItemProvider>();
    final navigator = Navigator.of(context);

    setState(() => _saving = true);
    try {
      final saved = await items.addItem(Item(
        id: '',
        type: _type!,
        title: _titleController.text.trim(),
        category: _category!,
        description: _descriptionController.text.trim(),
        location: _locationController.text.trim(),
        date: _date,
        imageBase64: _imageBase64,
        ownerId: user.uid,
        ownerName: user.name,
        ownerEmail: user.email,
        createdAt: DateTime.now(),
      ));
      final matchCount = items.matchesFor(saved).length;
      _showMessage(matchCount == 0
          ? 'Posted!'
          : 'Posted! $matchCount possible match${matchCount == 1 ? '' : 'es'} found.');
      // Replace the form with the new post's detail page.
      navigator.pushReplacementNamed(AppRoutes.itemDetail, arguments: saved);
    } catch (e) {
      _showMessage('Could not save: $e');
      if (mounted) setState(() => _saving = false);
    }
  }

  void _showMessage(String text) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(text)));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Post an item')),
      body: SafeArea(
        child: Form(
          key: _formKey,
          child: ListView(
            padding: const EdgeInsets.all(16),
            children: [
              SegmentedButton<ItemType>(
                segments: const [
                  ButtonSegment(value: ItemType.lost, label: Text('I lost something')),
                  ButtonSegment(value: ItemType.found, label: Text('I found something')),
                ],
                selected: {_type!},
                onSelectionChanged: (s) => setState(() => _type = s.first),
              ),
              const SizedBox(height: 16),
              _buildPhotoSection(),
              const SizedBox(height: 16),
              TextFormField(
                controller: _titleController,
                textCapitalization: TextCapitalization.sentences,
                maxLength: 60,
                decoration: const InputDecoration(
                    labelText: 'Title', hintText: 'e.g. Black leather wallet'),
                validator: (v) => validateRequired(v, 'a title'),
              ),
              const SizedBox(height: 8),
              DropdownButtonFormField<String>(
                initialValue: _category,
                decoration: const InputDecoration(labelText: 'Category'),
                items: [
                  for (final c in itemCategories) DropdownMenuItem(value: c, child: Text(c)),
                ],
                onChanged: (v) => setState(() => _category = v),
                validator: (v) => v == null ? 'Choose a category' : null,
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _descriptionController,
                textCapitalization: TextCapitalization.sentences,
                maxLines: 3,
                maxLength: 300,
                decoration: const InputDecoration(
                  labelText: 'Description',
                  hintText: 'Colour, brand, stickers, what was inside…',
                  alignLabelWithHint: true,
                ),
                validator: (v) => validateRequired(v, 'a description'),
              ),
              const SizedBox(height: 8),
              TextFormField(
                controller: _locationController,
                decoration: const InputDecoration(
                  labelText: 'Location',
                  hintText: 'e.g. Library 2nd floor',
                  prefixIcon: Icon(Icons.place_outlined),
                ),
                validator: (v) => validateRequired(v, 'where it was lost or found'),
              ),
              const SizedBox(height: 16),
              ListTile(
                contentPadding: EdgeInsets.zero,
                leading: const Icon(Icons.event),
                title: Text(_type == ItemType.lost ? 'Date lost' : 'Date found'),
                subtitle: Text(formatDate(_date)),
                trailing: TextButton(onPressed: _pickDate, child: const Text('Change')),
              ),
              const SizedBox(height: 16),
              FilledButton.icon(
                onPressed: _saving || _pickingImage ? null : _save,
                icon: _saving
                    ? const SizedBox.square(
                        dimension: 18, child: CircularProgressIndicator(strokeWidth: 2))
                    : const Icon(Icons.check),
                label: Text(_saving ? 'Saving…' : 'Post'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildPhotoSection() {
    if (_pickingImage) {
      return const SizedBox(height: 180, child: Center(child: CircularProgressIndicator()));
    }
    if (_imageBase64 != null) {
      return Stack(
        children: [
          ClipRRect(
            borderRadius: BorderRadius.circular(12),
            child: Image.memory(base64Decode(_imageBase64!),
                height: 220, width: double.infinity, fit: BoxFit.cover),
          ),
          Positioned(
            top: 8,
            right: 8,
            child: IconButton.filled(
              tooltip: 'Remove photo',
              icon: const Icon(Icons.close),
              onPressed: () => setState(() => _imageBase64 = null),
            ),
          ),
        ],
      );
    }
    return Row(
      children: [
        Expanded(
          child: OutlinedButton.icon(
            onPressed: () => _pickImage(ImageSource.camera),
            icon: const Icon(Icons.photo_camera),
            label: const Text('Camera'),
          ),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: OutlinedButton.icon(
            onPressed: () => _pickImage(ImageSource.gallery),
            icon: const Icon(Icons.photo_library),
            label: const Text('Gallery'),
          ),
        ),
      ],
    );
  }
}
