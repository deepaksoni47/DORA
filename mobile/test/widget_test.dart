import 'package:dora_mobile/main.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  testWidgets('renders DORA mobile landing content', (WidgetTester tester) async {
    await tester.pumpWidget(const DoraApp());

    expect(find.text('Academic Meta Search'), findsOneWidget);
    expect(find.text('Search'), findsOneWidget);
    expect(find.text('Research Papers'), findsOneWidget);
  });
}
