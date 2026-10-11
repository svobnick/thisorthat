import 'dart:convert';
import 'dart:io';

/// What a person said about themselves in onboarding. A null field is a
/// question they chose not to answer.
class Profile {
  const Profile({this.age, this.gender});

  final String? age;
  final String? gender;

  Map<String, dynamic> toJson() => {'age': age, 'gender': gender};

  factory Profile.fromJson(Map<String, dynamic> json) =>
      Profile(age: json['age'] as String?, gender: json['gender'] as String?);
}

/// The few things the app keeps on the device, as files in one directory.
///
/// The token file is the native Android app's own (`filesDir/authToken`), so
/// an install upgraded from it keeps its favorites and questions.
class AppStorage {
  AppStorage(this.directory);

  final Directory directory;

  File get _tokenFile => File('${directory.path}/authToken');
  File get _profileFile => File('${directory.path}/profile.json');

  Future<String?> readToken() async {
    if (!await _tokenFile.exists()) return null;
    final token = (await _tokenFile.readAsString()).trim();
    return token.isEmpty ? null : token;
  }

  Future<void> writeToken(String token) => _write(_tokenFile, token);

  /// Null until onboarding has been finished once.
  Future<Profile?> readProfile() async {
    if (!await _profileFile.exists()) return null;
    try {
      return Profile.fromJson(jsonDecode(await _profileFile.readAsString()) as Map<String, dynamic>);
    } on Object {
      return null;
    }
  }

  Future<void> writeProfile(Profile profile) => _write(_profileFile, jsonEncode(profile.toJson()));

  Future<void> _write(File file, String content) async {
    await directory.create(recursive: true);
    await file.writeAsString(content, flush: true);
  }
}
