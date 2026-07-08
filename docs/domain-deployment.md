# Domain Deployment Plan

Use Cloudflare for public web hosting and DNS, but keep Supabase on its native Supabase URL.

## Domains

| Hostname | Purpose | Deployment |
| --- | --- | --- |
| `jumplogbook.com` | Public website, support, app info, early access | Cloudflare Pages project `jumplogbook`, output directory `landing` |
| `www.jumplogbook.com` | Alias for public website | Redirect to `https://jumplogbook.com` |
| `app.jumplogbook.com` | Web app/dashboard | Separate Cloudflare Pages project `jumplogbook-app`, output directory `dashboard/dist` |
| `<project-ref>.supabase.co` | Supabase API/Auth/Storage/Realtime | Keep as-is; do not CNAME/proxy through Cloudflare |

## Cloudflare Pages: Public Website

Project settings:

- Project name: `jumplogbook`
- Framework preset: `None`
- Build command: blank
- Build output directory: `landing`

Custom domains:

- `jumplogbook.com`
- `www.jumplogbook.com`

The landing site includes `_redirects` to send `www.jumplogbook.com` traffic to `jumplogbook.com`.

## Cloudflare Pages: Dashboard

Project settings:

- Project name: `jumplogbook-app`
- Framework preset: `Vite`
- Build command: `npm run dashboard:build`
- Build output directory: `dashboard/dist`

Custom domain:

- `app.jumplogbook.com`

Do not attach `app.jumplogbook.com` to the landing Pages project.

## Supabase

Keep the app configured with the Supabase project URL:

```text
https://<project-ref>.supabase.co
```

Do not create `api.jumplogbook.com` unless there is a separate backend gateway later.

Recommended Supabase Auth settings once the dashboard is deployed:

- Site URL: `https://app.jumplogbook.com`
- Redirect URLs:
- `https://app.jumplogbook.com/**`
- `https://jumplogbook.com`
- `jumplogbook://auth-callback` if mobile deep links are added later
