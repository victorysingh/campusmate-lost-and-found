import 'dart:math';
import 'dart:typed_data';

import 'package:campusmate/services/image_service.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:image/image.dart' as img;

void main() {
  test('large photo is compressed under the limit', () {
    // Random noise compresses badly, so this is a worst-case "photo".
    final random = Random(42);
    final big = img.Image(width: 1600, height: 1200);
    for (final pixel in big) {
      pixel.setRgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }
    final input = img.encodePng(big);
    expect(input.length, greaterThan(300 * 1024)); // really is too big

    final output = compressToLimit(input, 300 * 1024);

    expect(output.length, lessThanOrEqualTo(300 * 1024));
    expect(img.decodeJpg(output), isNotNull); // still a valid image
  });

  test('small image is returned unchanged', () {
    final small = Uint8List.fromList(img.encodePng(img.Image(width: 10, height: 10)));
    expect(compressToLimit(small, 300 * 1024), same(small));
  });
}
