-- Keep the remote user profile schema aligned with UserEntity so profile
-- details survive app reinstall and local Room database deletion.
alter table if exists public.users
  add column if not exists screen_name text,
  add column if not exists license_number text not null default '',
  add column if not exists role text not null default 'JUMPER',
  add column if not exists base_jump_count integer not null default 0,
  add column if not exists exit_weight double precision,
  add column if not exists selected_gear_ids_for_weight text,
  add column if not exists wallet_balance double precision not null default 0,
  add column if not exists profile_picture_url text,
  add column if not exists background_picture_url text,
  add column if not exists birthdate bigint,
  add column if not exists nationality text,
  add column if not exists city text,
  add column if not exists province text,
  add column if not exists country text,
  add column if not exists weight double precision,
  add column if not exists weight_unit text default 'kg',
  add column if not exists gender text,
  add column if not exists mobile_number text,
  add column if not exists email text,
  add column if not exists is_verified boolean not null default false,
  add column if not exists emergency_contact_name text,
  add column if not exists emergency_contact_number text,
  add column if not exists dz_name text,
  add column if not exists dz_location text,
  add column if not exists dz_street text,
  add column if not exists dz_city text,
  add column if not exists dz_province text,
  add column if not exists dz_country text,
  add column if not exists dz_mobile_number text,
  add column if not exists dz_email text,
  add column if not exists operating_days text,
  add column if not exists operating_hours text,
  add column if not exists dz_website text,
  add column if not exists referral_code text,
  add column if not exists nfc_tag_id text,
  add column if not exists wind_limit_kts double precision not null default 25,
  add column if not exists student_wind_limit_kts double precision not null default 15,
  add column if not exists rigger_seal_symbol text,
  add column if not exists rigger_license text,
  add column if not exists membership_level text not null default 'Standard',
  add column if not exists membership_tier text not null default 'STANDARD',
  add column if not exists subscription_expiry bigint;

drop policy if exists users_insert_own_profile on public.users;
create policy users_insert_own_profile on public.users
  for insert
  to authenticated
  with check (
    user_id = (select auth.uid())::text
    and coalesce(is_verified, false) = false
  );
