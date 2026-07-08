# Jump Logbook Web Dashboard

Web dashboard for `app.jumplogbook.com`.

This is a separate Cloudflare Pages app from the public landing website. It uses the existing Supabase project URL directly.

## Local Development

Create `dashboard/.env.local`:

```text
VITE_SUPABASE_URL=https://<project-ref>.supabase.co
VITE_SUPABASE_ANON_KEY=<supabase-anon-key>
```

Run:

```powershell
npm.cmd run dashboard:dev
```

## Cloudflare Pages

Create a separate Pages project for the dashboard.

Recommended settings:

- Project name: `jumplogbook-app`
- Framework preset: `Vite`
- Build command: `npm run dashboard:build`
- Build output directory: `dashboard/dist`
- Custom domain: `app.jumplogbook.com`

Environment variables:

- `VITE_SUPABASE_URL`
- `VITE_SUPABASE_ANON_KEY`

Keep Supabase on its native URL. Do not proxy it through Cloudflare.

## Current Scope

The dashboard supports:

- Email/password sign-in against Supabase Auth
- Profile editing
- DZO facilities
- DZO inventory
- DZO waivers
- DZO flight schedules

It intentionally shares the same Supabase tables as the mobile app.
