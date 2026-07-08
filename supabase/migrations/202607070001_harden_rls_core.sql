-- Harden core app tables with RLS policies and indexes.
-- Review table/column names against the live Supabase schema before applying.

create schema if not exists private;

create or replace function private.is_admin()
returns boolean
language sql
security definer
set search_path = ''
as $$
  select exists (
    select 1
    from public.users
    where user_id = (select auth.uid())::text
      and role = 'ADMIN'
  );
$$;

create or replace function private.is_active_dz_staff(target_dz_id text)
returns boolean
language sql
security definer
set search_path = ''
as $$
  select exists (
    select 1
    from public.dz_staff_memberships
    where dz_id = target_dz_id
      and user_id = (select auth.uid())::text
      and is_active = true
  );
$$;

revoke execute on function private.is_admin() from public, anon, authenticated;
revoke execute on function private.is_active_dz_staff(text) from public, anon, authenticated;

alter table if exists public.users enable row level security;
alter table if exists public.user_follows enable row level security;
alter table if exists public.jump_logs enable row level security;
alter table if exists public.chat_messages enable row level security;
alter table if exists public.user_badges enable row level security;
alter table if exists public.flight_schedules enable row level security;
alter table if exists public.dz_staff_memberships enable row level security;

create index if not exists users_user_id_role_idx on public.users (user_id, role);
create index if not exists user_follows_follower_followed_idx on public.user_follows (follower_id, followed_id);
create index if not exists user_follows_followed_idx on public.user_follows (followed_id);
create index if not exists jump_logs_user_id_date_idx on public.jump_logs (user_id, date desc);
create index if not exists chat_messages_sender_receiver_timestamp_idx on public.chat_messages (sender_id, receiver_id, timestamp);
create index if not exists chat_messages_receiver_read_idx on public.chat_messages (receiver_id, is_read);
create index if not exists dz_staff_memberships_dz_user_active_idx on public.dz_staff_memberships (dz_id, user_id, is_active);
create index if not exists flight_schedules_dz_date_idx on public.flight_schedules (dz_id, date_of_flight);

drop policy if exists users_select_registered on public.users;
create policy users_select_registered on public.users
  for select
  to authenticated
  using (true);

drop policy if exists users_update_own_profile on public.users;
create policy users_update_own_profile on public.users
  for update
  to authenticated
  using (user_id = (select auth.uid())::text)
  with check (
    user_id = (select auth.uid())::text
    and role = (select role from public.users where user_id = (select auth.uid())::text)
    and is_verified = (select is_verified from public.users where user_id = (select auth.uid())::text)
  );

drop policy if exists users_admin_update on public.users;
create policy users_admin_update on public.users
  for update
  to authenticated
  using ((select private.is_admin()))
  with check ((select private.is_admin()));

drop policy if exists follows_owner_all on public.user_follows;
create policy follows_owner_all on public.user_follows
  for all
  to authenticated
  using (follower_id = (select auth.uid())::text)
  with check (follower_id = (select auth.uid())::text);

drop policy if exists jumps_owner_select on public.jump_logs;
create policy jumps_owner_select on public.jump_logs
  for select
  to authenticated
  using (
    user_id = (select auth.uid())::text
    or (dz_id is not null and (select private.is_active_dz_staff(dz_id)))
    or (select private.is_admin())
  );

drop policy if exists jumps_owner_insert on public.jump_logs;
create policy jumps_owner_insert on public.jump_logs
  for insert
  to authenticated
  with check (
    user_id = (select auth.uid())::text
    or (dz_id is not null and (select private.is_active_dz_staff(dz_id)))
    or (select private.is_admin())
  );

drop policy if exists jumps_owner_update_delete on public.jump_logs;
create policy jumps_owner_update_delete on public.jump_logs
  for update
  to authenticated
  using (
    user_id = (select auth.uid())::text
    or (dz_id is not null and (select private.is_active_dz_staff(dz_id)))
    or (select private.is_admin())
  )
  with check (
    user_id = (select auth.uid())::text
    or (dz_id is not null and (select private.is_active_dz_staff(dz_id)))
    or (select private.is_admin())
  );

drop policy if exists chat_participant_all on public.chat_messages;
create policy chat_participant_all on public.chat_messages
  for all
  to authenticated
  using (
    sender_id = (select auth.uid())::text
    or receiver_id = (select auth.uid())::text
  )
  with check (sender_id = (select auth.uid())::text);

drop policy if exists badges_owner_select on public.user_badges;
create policy badges_owner_select on public.user_badges
  for select
  to authenticated
  using (user_id = (select auth.uid())::text or (select private.is_admin()));

drop policy if exists badges_owner_insert on public.user_badges;
create policy badges_owner_insert on public.user_badges
  for insert
  to authenticated
  with check (user_id = (select auth.uid())::text);

drop policy if exists badges_admin_update on public.user_badges;
create policy badges_admin_update on public.user_badges
  for update
  to authenticated
  using ((select private.is_admin()))
  with check ((select private.is_admin()));

drop policy if exists flight_schedules_public_read on public.flight_schedules;
create policy flight_schedules_public_read on public.flight_schedules
  for select
  to authenticated
  using (true);

drop policy if exists flight_schedules_dz_staff_write on public.flight_schedules;
create policy flight_schedules_dz_staff_write on public.flight_schedules
  for all
  to authenticated
  using ((select private.is_active_dz_staff(dz_id)) or dz_id = (select auth.uid())::text or (select private.is_admin()))
  with check ((select private.is_active_dz_staff(dz_id)) or dz_id = (select auth.uid())::text or (select private.is_admin()));

drop policy if exists dz_staff_visible on public.dz_staff_memberships;
create policy dz_staff_visible on public.dz_staff_memberships
  for select
  to authenticated
  using (user_id = (select auth.uid())::text or (select private.is_active_dz_staff(dz_id)) or (select private.is_admin()));

drop policy if exists dz_staff_admin_write on public.dz_staff_memberships;
create policy dz_staff_admin_write on public.dz_staff_memberships
  for all
  to authenticated
  using ((select private.is_admin()) or dz_id = (select auth.uid())::text)
  with check ((select private.is_admin()) or dz_id = (select auth.uid())::text);
