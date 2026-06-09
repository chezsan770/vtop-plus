# Gamma for VTOP

Gamma is a Jetpack Compose Android companion app for the VIT Bhopal VTOP portal. It keeps the original VTOP session as the backend, then presents attendance, timetable, grades, settings, and portal access in a cleaner mobile UI.

## Features

- VTOP login with CAPTCHA support and optional saved credentials.
- Dashboard with greeting, registration number, next class, and attendance grid.
- Next class card with live ongoing-class progress and upcoming-class details.
- Classes tab with timetable cards, class detail popups, and completed-class check marks.
- Grades tab protected by biometric unlock or device PIN/password fallback.
- Current grades, grade history, CGPA, grade distribution, and total credits.
- Profile/settings page with theme mode, appearance palettes, accent colors, logout, feature requests, and VTOP Online.
- Full VTOP opens in a separate in-app portal window using the logged-in session cookies.
- AMOLED dark UI with animated neon background and glass-style cards.
- Start.io banner ads and rewarded-ad gate for VTOP Online, with no-ad fallback.
- Supabase-backed feature request submissions and active-user heartbeat support.
- In-app update metadata support through Supabase.

## Tech Stack

- Kotlin
- Jetpack Compose Material 3
- OkHttp
- Jsoup
- AndroidX Biometric
- Start.io Android SDK
- Supabase PostgREST

## Local Setup

Create `local.properties` in the project root. This file is intentionally ignored by Git.

```properties
sdk.dir=C\:\\Users\\YOUR_NAME\\AppData\\Local\\Android\\Sdk
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-supabase-anon-or-publishable-key
STARTAPP_APP_ID=your-startio-app-id
```

`SUPABASE_URL`, `SUPABASE_ANON_KEY`, and `STARTAPP_APP_ID` can be left blank for local builds, but feature requests, active users, updates, and ads will be unavailable or use fallback behavior.

## Build

```powershell
.\gradlew.bat :app:assembleDebug --console=plain --no-daemon
```

Debug APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Supabase Tables

Feature requests:

```sql
create table feature_requests (
  id uuid primary key default gen_random_uuid(),
  title text not null,
  description text not null,
  category text not null default 'Feature',
  student_name text,
  registration_number text,
  app_version text,
  status text not null default 'new',
  created_at timestamptz not null default now()
);

alter table feature_requests enable row level security;

create policy "allow anonymous inserts"
on feature_requests
for insert
to anon
with check (true);
```

Active users and update metadata are also submitted/read through Supabase. Keep public write policies narrow and do not add public select/update/delete policies for private request data.

## Notes

- This app is not an official VIT or VTOP product.
- VTOP credentials are used only to authenticate with the VTOP portal.
- Saved credentials and session cookies are stored locally on device.
- Android cannot copy app WebView cookies into Chrome, so logged-in Full VTOP opens inside the app's portal window.
- CAPTCHA automation is intentionally not included.

