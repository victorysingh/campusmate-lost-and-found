import 'package:campusmate/utils/validators.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  group('isAllowedEmail', () {
    test('empty domain allows any email', () {
      expect(isAllowedEmail('someone@gmail.com', domain: ''), isTrue);
    });

    test('only college emails pass when a domain is set', () {
      expect(isAllowedEmail('ravi@rvce.edu.in', domain: 'rvce.edu.in'), isTrue);
      expect(isAllowedEmail(' Ravi@RVCE.edu.in ', domain: 'rvce.edu.in'), isTrue);
      expect(isAllowedEmail('ravi@gmail.com', domain: 'rvce.edu.in'), isFalse);
      // A look-alike domain must not pass.
      expect(isAllowedEmail('ravi@fakervce.edu.in', domain: 'rvce.edu.in'), isFalse);
    });
  });

  test('validateEmail rejects malformed addresses', () {
    expect(validateEmail(''), isNotNull);
    expect(validateEmail('not-an-email'), isNotNull);
    expect(validateEmail('a@b.com'), isNull);
  });
}
