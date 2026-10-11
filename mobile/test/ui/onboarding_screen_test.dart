import 'package:flutter_test/flutter_test.dart';
import 'package:thisorthat/session/app_storage.dart';
import 'package:thisorthat/ui/components/gradient_button.dart';
import 'package:thisorthat/ui/screens/onboarding/onboarding_screen.dart';

import '../support/pump_app.dart';

void main() {
  testWidgets('each step waits for an answer, and a declined one is stored as nothing', (tester) async {
    Profile? result;
    await pumpApp(tester, OnboardingScreen(onDone: (profile) => result = profile));
    final l10n = l10nOf(tester);
    GradientButton button() => tester.widget<GradientButton>(find.byType(GradientButton));

    expect(button().onPressed, isNull);
    await tester.tap(find.text(l10n.age13to15));
    await settle(tester);
    await tester.tap(find.byType(GradientButton));
    await settle(tester);

    expect(find.text(l10n.onboardingGenderTitle), findsOneWidget);
    expect(button().onPressed, isNull);
    await tester.tap(find.text(l10n.genderSkip));
    await settle(tester);
    await tester.tap(find.byType(GradientButton));

    expect((result?.age, result?.gender), ('13-15', null));
  });
}
