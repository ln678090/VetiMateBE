--
-- PostgreSQL database dump
--

\restrict e6FynWFJQ2CWD7RfYNYcNTiEPvySydzfEuIhGWr8IqH99xHGtF9d33vIGftMfQ1

-- Dumped from database version 17.10 (Debian 17.10-1.pgdg13+1)
-- Dumped by pg_dump version 17.10 (Debian 17.10-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.flyway_schema_history OWNER TO postgres;

--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	1	create users roles	SQL	V1__create_users_roles.sql	-97644564	postgres	2026-07-21 12:07:00.831525	6	t
2	2	catalog	SQL	V2__catalog.sql	-1230228120	postgres	2026-07-21 12:07:00.853217	4	t
3	3	product	SQL	V3__product.sql	1465000989	postgres	2026-07-21 12:07:00.865597	5	t
4	4	clinic	SQL	V4__clinic.sql	1972010124	postgres	2026-07-23 02:02:52.544852	12	t
5	5	clinic seed	SQL	V5__clinic_seed.sql	-2139290233	postgres	2026-07-23 02:41:23.497611	3	t
6	7	add users phone	SQL	V7__add_users_phone.sql	-1316681916	postgres	2026-07-24 20:08:35.061647	12	t
7	8	pet soft delete	SQL	V8__pet_soft_delete.sql	716434114	postgres	2026-08-01 13:42:23.133984	10	t
8	9	auth rbac audit	SQL	V9__auth_rbac_audit.sql	-1347017157	postgres	2026-08-08 10:45:32.349138	38	t
9	10	staff inventory	SQL	V10__staff_inventory.sql	87626217	postgres	2026-08-08 10:47:34.872741	51	t
10	11	medical queue	SQL	V11__medical_queue.sql	-552191613	postgres	2026-08-08 10:51:00.538211	29	t
11	12	billing notifications	SQL	V12__billing_notifications.sql	-1662445574	postgres	2026-08-08 10:53:59.71938	29	t
12	13	seed system roles	SQL	V13__seed_system_roles.sql	243105438	postgres	2026-08-08 11:48:58.663976	17	t
13	14	align staff role type	SQL	V14__align_staff_role_type.sql	-11921068	postgres	2026-08-20 08:08:38.579948	6	t
14	15	pet health snapshot	SQL	V15__pet_health_snapshot.sql	1675986621	postgres	2026-08-20 12:20:05.104415	21	t
15	\N	finalize shop interactions and orders	SQL	R__finalize_shop_interactions_and_orders.sql	975617661	postgres	2026-08-31 21:12:02.494486	35	t
16	\N	seed products	SQL	R__seed_products.sql	345166983	postgres	2026-08-31 21:12:02.546657	10	t
17	\N	seed staff accounts	SQL	R__seed_staff_accounts.sql	1922461756	postgres	2026-08-31 21:12:02.564589	4	t
18	\N	seed suppliers	SQL	R__seed_suppliers.sql	-280667329	postgres	2026-08-31 21:12:02.574579	1	t
19	\N	user product interactions	SQL	R__user_product_interactions.sql	-347792682	postgres	2026-08-31 21:12:02.580266	0	t
20	16	add is read to notifications	SQL	V16__add_is_read_to_notifications.sql	1124088071	postgres	2026-08-31 21:17:06.373926	5	t
21	17	add link to notifications	SQL	V17__add_link_to_notifications.sql	-666327402	postgres	2026-08-31 21:18:35.367315	3	t
22	18	seed inventory	SQL	V18__seed_inventory.sql	1748945474	postgres	2026-09-05 03:33:04.749121	15	t
23	20260903	seed test accounts	SQL	V20260903__seed_test_accounts.sql	-790227223	postgres	2026-09-05 10:08:37.60028	2	t
24	20260903.2	voucher loyalty	SQL	V20260903_2__voucher_loyalty.sql	1403179504	postgres	2026-09-05 10:08:37.613312	14	t
25	20260903.3	invoice review	SQL	V20260903_3__invoice_review.sql	-1823992277	postgres	2026-09-05 10:08:37.636172	1	t
26	20260903.4	invoice review details	SQL	V20260903_4__invoice_review_details.sql	-971503815	postgres	2026-09-05 10:08:37.643053	2	t
27	20260903.5	invoice review add product	SQL	V20260903_5__invoice_review_add_product.sql	1782604178	postgres	2026-09-05 10:08:37.649365	5	t
28	20260904.2	sync product reviews	SQL	V20260904_2__sync_product_reviews.sql	-1002177221	postgres	2026-09-05 10:08:37.658195	4	t
29	20260904.3	add customer tiers	SQL	V20260904_3__add_customer_tiers.sql	-633217285	postgres	2026-09-05 10:08:37.666245	4	t
30	\N	seed staff accounts	SQL	R__seed_staff_accounts.sql	-743464273	postgres	2026-09-05 10:08:37.673987	6	t
31	20260905115738	extend audit logs for admin mvp	SQL	V20260905115738__extend_audit_logs_for_admin_mvp.sql	732331231	postgres	2026-09-05 12:00:20.428707	17	t
32	20260903000000	seed test accounts	SQL	V20260903000000__seed_test_accounts.sql	-790227223	postgres	2026-09-06 13:42:28.788007	8	t
\.


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- PostgreSQL database dump complete
--

\unrestrict e6FynWFJQ2CWD7RfYNYcNTiEPvySydzfEuIhGWr8IqH99xHGtF9d33vIGftMfQ1

