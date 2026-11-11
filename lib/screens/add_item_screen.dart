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
import '../theme.dart';
import '../utils/formatters.dart';
import '../utils/validators.dart';
import '../widgets/primary_button.dart';

/// Post a Lost or Found item in 3 steps: Photo → Details → Location.
/// Optional route argument: the ItemType to pre-select.
class AddItemScreen extends StatefulWidget {
  const AddItemScreen({super.key});

  @override
  State<AddItemScreen> createState() => _AddItemScreenState();
}

class _AddItemScreenState extends State<AddItemScreen> {
  // One Form per step, so "Next" only checks the fields on that step.
  final _detailsFormKey = GlobalKey<FormState>();
  final _locationFormKey = GlobalKey<FormState>();
  final _titleController = TextEditingController();
  final _descriptionController = TextEditingController();
  final _placeDetailController = TextEditingController();
  final _imageService = ImageService();

  int _step = 0; // 0 = Photo, 1 = Details, 2 = Location
  ItemType? _type; // set from the route argument on first build
  String? _category;
  String? _place;
  DateTime _date = DateTime.now();
  String? _imageBase64;
  bool _pickingImage = false;
  bool _saving = false;
  bool _detailsHasError = false;

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
    _placeDetailController.dispose();
    super.dispose();
  }

  // ---- actions ----

  Future<void> _pickImage(ImageSource source) async {
    setState(() => _pickingImage = true);
    try {
      final base64 = await _imageService.pickImage(source);
      if (base64 != null) setState(() => _imageBase64 = base64);
    } catch (e) {
      _showMessage('Could not get the photo. Please try another one.');
      debugPrint('Image pick failed: $e');
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

  /// "Next" / "Post" button.
  void _onContinue() {
    if (_step == 0) {
      setState(() => _step = 1);
    } else if (_step == 1) {
      final ok = _detailsFormKey.currentState!.validate();
      setState(() {
        _detailsHasError = !ok;
        if (ok) _step = 2;
      });
    } else if (_locationFormKey.currentState!.validate()) {
      _save();
    }
  }

  void _onBack() {
    if (_step > 0) setState(() => _step -= 1);
  }

  Future<void> _save() async {
    final user = context.read<AuthProvider>().user!;
    final items = context.read<ItemProvider>();
    final navigator = Navigator.of(context);
    final detail = _placeDetailController.text.trim();

    setState(() => _saving = true);
    try {
      final saved = await items.addItem(Item(
        id: '',
        type: _type!,
        title: _titleController.text.trim(),
        category: _category!,
        description: _descriptionController.text.trim(),
        location: detail.isEmpty ? _place! : '$_place · $detail',
        date: _date,
        imageBase64: _imageBase64,
        ownerId: user.uid,
        ownerName: user.name,
        ownerEmail: user.email,
        createdAt: DateTime.now(),
      ));
      final matchCount = items.matchesFor(saved).length;
      _showMessage(matchCount == 0
          ? 'Posted! We will show matches as soon as they appear.'
          : 'Posted! $matchCount possible match${matchCount == 1 ? '' : 'es'} found.');
      // Replace the form with the new post's detail page.
      navigator.pushReplacementNamed(AppRoutes.itemDetail, arguments: saved);
    } catch (e) {
      _showMessage('Could not save your post. Check your internet and try again.');
      debugPrint('Save failed: $e');
      if (mounted) setState(() => _saving = false);
    }
  }

  void _showMessage(String text) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(text)));
  }

  // ---- UI ----

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Post an item')),
      body: SafeArea(
        child: Stepper(
          type: StepperType.horizontal,
          currentStep: _step,
          onStepContinue: _onContinue,
          onStepCancel: _onBack,
          // Tapping a step header only goes back (forward needs "Next").
          onStepTapped: (i) {
            if (i < _step) setState(() => _step = i);
          },
          controlsBuilder: (context, details) => Padding(
            padding: const EdgeInsets.only(top: Space.l),
            child: Row(
              children: [
                if (_step > 0) ...[
                  TextButton(onPressed: details.onStepCancel, child: const Text('Back')),
                  const SizedBox(width: Space.s),
                ],
                Expanded(
                  child: PrimaryButton(
                    label: _step == 2 ? 'Post' : 'Next',
                    icon: _step == 2 ? Icons.check_rounded : Icons.arrow_forward_rounded,
                    loading: _saving,
                    onPressed: _pickingImage ? null : details.onStepContinue,
                  ),
                ),
              ],
            ),
          ),
          steps: [
            Step(
              title: const SizedBox.shrink(),
              label: const Text('Photo'), // label sits under the circle: fits 320 px phones
              isActive: _step >= 0,
              state: _step > 0 ? StepState.complete : StepState.indexed,
              content: _photoStep(),
            ),
            Step(
              title: const SizedBox.shrink(),
              label: const Text('Details'),
              isActive: _step >= 1,
              state: _detailsHasError
                  ? StepState.error
                  : _step > 1
                      ? StepState.complete
                      : StepState.indexed,
              content: _detailsStep(),
            ),
            Step(
              title: const SizedBox.shrink(),
              label: const Text('Location'),
              isActive: _step >= 2,
              content: _locationStep(),
            ),
          ],
        ),
      ),
    );
  }

  Widget _photoStep() {
    final colors = Theme.of(context).colorScheme;
    Widget box;
    if (_pickingImage) {
      box = const Center(child: CircularProgressIndicator());
    } else if (_imageBase64 != null) {
      box = Stack(
        fit: StackFit.expand,
        children: [
          Image.memory(base64Decode(_imageBase64!), fit: BoxFit.cover),
          Positioned(
            top: Space.s,
            right: Space.s,
            child: IconButton.filled(
              tooltip: 'Remove photo',
              icon: const Icon(Icons.close_rounded),
              onPressed: () => setState(() => _imageBase64 = null),
            ),
          ),
        ],
      );
    } else {
      box = Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.add_a_photo_rounded, size: 48, color: colors.primary),
          const SizedBox(height: Space.s),
          const Text('Add a clear photo (optional)'),
          Text('It helps people recognise the item',
              style: TextStyle(color: colors.onSurfaceVariant, fontSize: 12)),
        ],
      );
    }

    return Column(
      children: [
        ClipRRect(
          borderRadius: BorderRadius.circular(kRadius),
          child: Container(
            height: 220,
            width: double.infinity,
            color: colors.surfaceContainerHighest,
            child: box,
          ),
        ),
        const SizedBox(height: Space.m),
        Row(
          children: [
            Expanded(
              child: OutlinedButton.icon(
                onPressed: _pickingImage ? null : () => _pickImage(ImageSource.camera),
                icon: const Icon(Icons.photo_camera_rounded),
                label: const Text('Camera'),
              ),
            ),
            const SizedBox(width: Space.m),
            Expanded(
              child: OutlinedButton.icon(
                onPressed: _pickingImage ? null : () => _pickImage(ImageSource.gallery),
                icon: const Icon(Icons.photo_library_rounded),
                label: const Text('Gallery'),
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _detailsStep() {
    return Form(
      key: _detailsFormKey,
      child: Column(
        children: [
          SizedBox(
            width: double.infinity,
            child: SegmentedButton<ItemType>(
              segments: const [
                ButtonSegment(value: ItemType.lost, label: Text('I lost it')),
                ButtonSegment(value: ItemType.found, label: Text('I found it')),
              ],
              selected: {_type!},
              onSelectionChanged: (s) => setState(() => _type = s.first),
            ),
          ),
          const SizedBox(height: Space.m),
          TextFormField(
            controller: _titleController,
            textCapitalization: TextCapitalization.sentences,
            maxLength: 60,
            decoration: const InputDecoration(
                labelText: 'Title', hintText: 'e.g. Black leather wallet'),
            validator: (v) => validateRequired(v, 'a title'),
          ),
          const SizedBox(height: Space.s),
          DropdownButtonFormField<String>(
            initialValue: _category,
            isExpanded: true,
            decoration: const InputDecoration(labelText: 'Category'),
            items: [
              for (final c in itemCategories) DropdownMenuItem(value: c, child: Text(c)),
            ],
            onChanged: (v) => setState(() => _category = v),
            validator: (v) => v == null ? 'Choose a category' : null,
          ),
          const SizedBox(height: Space.m),
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
          ListTile(
            contentPadding: EdgeInsets.zero,
            leading: const Icon(Icons.event_rounded),
            title: Text(_type == ItemType.lost ? 'Date lost' : 'Date found'),
            subtitle: Text(formatDate(_date)),
            trailing: TextButton(onPressed: _pickDate, child: const Text('Change')),
          ),
        ],
      ),
    );
  }

  Widget _locationStep() {
    return Form(
      key: _locationFormKey,
      child: Column(
        children: [
          DropdownButtonFormField<String>(
            initialValue: _place,
            isExpanded: true,
            decoration: const InputDecoration(
              labelText: 'Where on campus?',
              prefixIcon: Icon(Icons.place_rounded),
            ),
            items: [
              for (final p in campusLocations) DropdownMenuItem(value: p, child: Text(p)),
            ],
            onChanged: (v) => setState(() => _place = v),
            validator: (v) => v == null ? 'Choose a place' : null,
          ),
          const SizedBox(height: Space.m),
          TextFormField(
            controller: _placeDetailController,
            maxLength: 60,
            decoration: const InputDecoration(
              labelText: 'More detail (optional)',
              hintText: 'e.g. 2nd floor, near the window',
            ),
          ),
        ],
      ),
    );
  }
}
