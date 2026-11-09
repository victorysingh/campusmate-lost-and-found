import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:image/image.dart' as img;
import 'package:image_picker/image_picker.dart';

import '../config.dart';

/// Picks a photo from the camera or gallery and shrinks it so it fits
/// inside a Firestore document (we have no Firebase Storage).
class ImageService {
  final ImagePicker _picker = ImagePicker();

  /// Returns the photo as a base64 JPEG string under [maxImageBytes],
  /// or null if the user cancelled.
  Future<String?> pickImage(ImageSource source) async {
    // Let the phone do a first resize + JPEG compression. This is fast.
    final file = await _picker.pickImage(
      source: source,
      maxWidth: 1024,
      maxHeight: 1024,
      imageQuality: 70,
    );
    if (file == null) return null;

    final bytes = await file.readAsBytes();
    // If it is still too big, re-compress in the background
    // (compute runs it on another isolate so the UI does not freeze).
    final small = await compute(_compressJob, bytes);
    return base64Encode(small);
  }
}

Uint8List _compressJob(Uint8List bytes) => compressToLimit(bytes, maxImageBytes);

/// Re-encodes [bytes] as JPEG, lowering quality and then size, until the
/// result is at most [maxBytes]. Images already small enough are returned
/// unchanged. Throws FormatException if the bytes are not an image.
Uint8List compressToLimit(Uint8List bytes, int maxBytes) {
  if (bytes.length <= maxBytes) return bytes;

  var image = img.decodeImage(bytes);
  if (image == null) throw const FormatException('Unsupported image format');

  var quality = 80;
  while (true) {
    final jpg = img.encodeJpg(image!, quality: quality);
    // Stop when small enough, or when the image is already tiny.
    if (jpg.length <= maxBytes || image.width <= 200) return jpg;

    if (quality > 50) {
      quality -= 10; // first try lower quality...
    } else {
      // ...then make the picture 25% smaller.
      image = img.copyResize(image, width: (image.width * 0.75).round());
    }
  }
}
