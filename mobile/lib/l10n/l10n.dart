import 'package:flutter/widgets.dart';

import '../api/api_client.dart';
import 'app_localizations.dart';

export 'app_localizations.dart';

extension AppLocalizationsContext on BuildContext {
  AppLocalizations get l10n => AppLocalizations.of(this);
}

/// What to tell the person when a request failed. The server's own
/// explanation is shown as it came; a transport or server breakdown gets our
/// words, never the exception's.
String failureMessage(AppLocalizations l10n, Object error) {
  if (error is ApiException) {
    switch (error.kind) {
      case ApiFailure.rejected:
        return error.description ?? l10n.errorServer;
      case ApiFailure.server:
        return l10n.errorServer;
      case ApiFailure.network:
        return l10n.errorNetwork;
    }
  }
  return l10n.errorServer;
}
