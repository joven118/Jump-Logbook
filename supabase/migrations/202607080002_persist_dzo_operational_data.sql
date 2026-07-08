-- DZO-entered operational data must be writable by the owning DZ operator
-- and readable after a fresh install hydrates the local Room cache.
alter table if exists public.dropzones enable row level security;
alter table if exists public.dz_facilities enable row level security;
alter table if exists public.dz_inventory enable row level security;
alter table if exists public.dz_waivers enable row level security;
alter table if exists public.dz_ratings enable row level security;
alter table if exists public.incident_reports enable row level security;
alter table if exists public.jumper_waiver_signatures enable row level security;

drop policy if exists dropzones_read_all on public.dropzones;
create policy dropzones_read_all on public.dropzones
  for select
  to authenticated
  using (true);

drop policy if exists dropzones_owner_write on public.dropzones;
create policy dropzones_owner_write on public.dropzones
  for all
  to authenticated
  using (owner_id = (select auth.uid())::text or id = (select auth.uid())::text or (select private.is_admin()))
  with check (owner_id = (select auth.uid())::text or id = (select auth.uid())::text or (select private.is_admin()));

drop policy if exists dz_facilities_read_all on public.dz_facilities;
create policy dz_facilities_read_all on public.dz_facilities
  for select
  to authenticated
  using (true);

drop policy if exists dz_facilities_dz_write on public.dz_facilities;
create policy dz_facilities_dz_write on public.dz_facilities
  for all
  to authenticated
  using (dz_id = (select auth.uid())::text or (select private.is_active_dz_staff(dz_id)) or (select private.is_admin()))
  with check (dz_id = (select auth.uid())::text or (select private.is_active_dz_staff(dz_id)) or (select private.is_admin()));

drop policy if exists dz_inventory_read_all on public.dz_inventory;
create policy dz_inventory_read_all on public.dz_inventory
  for select
  to authenticated
  using (true);

drop policy if exists dz_inventory_dz_write on public.dz_inventory;
create policy dz_inventory_dz_write on public.dz_inventory
  for all
  to authenticated
  using (dz_id = (select auth.uid())::text or (select private.is_active_dz_staff(dz_id)) or (select private.is_admin()))
  with check (dz_id = (select auth.uid())::text or (select private.is_active_dz_staff(dz_id)) or (select private.is_admin()));

drop policy if exists dz_waivers_read_all on public.dz_waivers;
create policy dz_waivers_read_all on public.dz_waivers
  for select
  to authenticated
  using (true);

drop policy if exists dz_waivers_dz_write on public.dz_waivers;
create policy dz_waivers_dz_write on public.dz_waivers
  for all
  to authenticated
  using (dz_id = (select auth.uid())::text or (select private.is_active_dz_staff(dz_id)) or (select private.is_admin()))
  with check (dz_id = (select auth.uid())::text or (select private.is_active_dz_staff(dz_id)) or (select private.is_admin()));

drop policy if exists dz_ratings_read_all on public.dz_ratings;
create policy dz_ratings_read_all on public.dz_ratings
  for select
  to authenticated
  using (true);

drop policy if exists dz_ratings_user_insert on public.dz_ratings;
create policy dz_ratings_user_insert on public.dz_ratings
  for insert
  to authenticated
  with check (user_id = (select auth.uid())::text or (select private.is_admin()));

drop policy if exists dz_ratings_user_update_delete on public.dz_ratings;
create policy dz_ratings_user_update_delete on public.dz_ratings
  for update
  to authenticated
  using (user_id = (select auth.uid())::text or dz_id = (select auth.uid())::text or (select private.is_admin()))
  with check (user_id = (select auth.uid())::text or dz_id = (select auth.uid())::text or (select private.is_admin()));

drop policy if exists incident_reports_dz_read on public.incident_reports;
create policy incident_reports_dz_read on public.incident_reports
  for select
  to authenticated
  using (user_id = (select auth.uid())::text or dz_id = (select auth.uid())::text or (select private.is_active_dz_staff(dz_id)) or (select private.is_admin()));

drop policy if exists incident_reports_dz_write on public.incident_reports;
create policy incident_reports_dz_write on public.incident_reports
  for all
  to authenticated
  using (user_id = (select auth.uid())::text or dz_id = (select auth.uid())::text or (select private.is_active_dz_staff(dz_id)) or (select private.is_admin()))
  with check (user_id = (select auth.uid())::text or dz_id = (select auth.uid())::text or (select private.is_active_dz_staff(dz_id)) or (select private.is_admin()));

drop policy if exists waiver_signatures_participant_read on public.jumper_waiver_signatures;
create policy waiver_signatures_participant_read on public.jumper_waiver_signatures
  for select
  to authenticated
  using (user_id = (select auth.uid())::text or dz_id = (select auth.uid())::text or (select private.is_active_dz_staff(dz_id)) or (select private.is_admin()));

drop policy if exists waiver_signatures_participant_insert on public.jumper_waiver_signatures;
create policy waiver_signatures_participant_insert on public.jumper_waiver_signatures
  for insert
  to authenticated
  with check (user_id = (select auth.uid())::text or dz_id = (select auth.uid())::text or (select private.is_active_dz_staff(dz_id)) or (select private.is_admin()));
