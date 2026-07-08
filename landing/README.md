# Jump Logbook Landing Site

Static production landing site for `jumplogbook.com`.

## Domain Plan

- `jumplogbook.com`: public marketing/support website served from this `landing` directory.
- `www.jumplogbook.com`: redirect to `jumplogbook.com`.
- `app.jumplogbook.com`: web app/dashboard, deployed from `dashboard/` as a separate Cloudflare Pages project.
- Supabase API/Auth: keep using the project Supabase URL, for example `https://<project-ref>.supabase.co`. Do not proxy Supabase through Cloudflare.

## Cloudflare Pages

Use these settings:

- Framework preset: `None`
- Build command: leave blank
- Build output directory: `landing`
- Production branch: your main branch

## Domain

In Cloudflare Pages, add these custom domains:

- `jumplogbook.com`
- `www.jumplogbook.com`

Cloudflare will create the required DNS records if the domain is in the same Cloudflare account.

Do not attach `app.jumplogbook.com` to this landing project. It belongs to the dashboard project.

## Dashboard Subdomain

Deploy the web dashboard as a separate Cloudflare Pages project and attach:

- `app.jumplogbook.com`

Dashboard project settings:

- Project name: `jumplogbook-app`
- Framework preset: `Vite`
- Build command: `npm run dashboard:build`
- Build output directory: `dashboard/dist`

Recommended DNS shape:

- `jumplogbook.com` -> landing Pages project
- `www.jumplogbook.com` -> landing Pages project, redirected to apex
- `app.jumplogbook.com` -> dashboard Pages project

## Supabase Auth URLs

In Supabase Auth URL settings, keep the API URL unchanged and only add allowed site/redirect URLs:

- Site URL: `https://app.jumplogbook.com`
- Additional redirect URLs:
- `https://app.jumplogbook.com/**`
- `https://jumplogbook.com`
- Mobile deep link later, if implemented: `jumplogbook://auth-callback`

## Local Preview

Any static file server can preview the site:

```powershell
npx.cmd wrangler pages dev landing
```

or open `landing/index.html` directly in a browser.
