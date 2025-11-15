--
-- PostgreSQL database dump
--

-- Dumped from database version 14.18 (Homebrew)
-- Dumped by pg_dump version 14.18 (Homebrew)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: cleanup_expired_tokens(); Type: FUNCTION; Schema: public; Owner: yuxin
--

CREATE FUNCTION public.cleanup_expired_tokens() RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM refresh_tokens 
    WHERE expires_at < NOW() - INTERVAL '7 days'
       OR created_at < NOW() - INTERVAL '31 days'
       OR is_active = false;
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    
    -- Optional: Log the cleanup
    INSERT INTO cleanup_logs (table_name, deleted_rows, cleanup_date)
    VALUES ('refresh_tokens', deleted_count, NOW());
    
    RETURN deleted_count;
END;
$$;


ALTER FUNCTION public.cleanup_expired_tokens() OWNER TO yuxin;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: conversations; Type: TABLE; Schema: public; Owner: yuxin
--

CREATE TABLE public.conversations (
    id bigint NOT NULL,
    user_one uuid NOT NULL,
    user_two uuid NOT NULL,
    user_one_msg jsonb DEFAULT '[]'::jsonb,
    user_two_msg jsonb DEFAULT '[]'::jsonb,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ordered_participants CHECK ((user_one < user_two))
);


ALTER TABLE public.conversations OWNER TO yuxin;

--
-- Name: conversations_id_seq; Type: SEQUENCE; Schema: public; Owner: yuxin
--

CREATE SEQUENCE public.conversations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.conversations_id_seq OWNER TO yuxin;

--
-- Name: conversations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: yuxin
--

ALTER SEQUENCE public.conversations_id_seq OWNED BY public.conversations.id;


--
-- Name: listings; Type: TABLE; Schema: public; Owner: yuxin
--

CREATE TABLE public.listings (
    item_id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    listing_type character varying(255) NOT NULL,
    description character varying(255),
    created_at timestamp without time zone NOT NULL,
    brand character varying(255) NOT NULL,
    condition character varying(255) NOT NULL,
    price numeric(38,2),
    pick_up_location character varying(255) NOT NULL,
    trade_for character varying(255),
    return_by date,
    pick_up_by date,
    title character varying(255) NOT NULL,
    category character varying(255) NOT NULL,
    image_urls json
);


ALTER TABLE public.listings OWNER TO yuxin;

--
-- Name: refresh_tokens; Type: TABLE; Schema: public; Owner: yuxin
--

CREATE TABLE public.refresh_tokens (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone,
    expires_at timestamp(6) without time zone NOT NULL,
    is_active boolean,
    token character varying(500) NOT NULL,
    user_id uuid NOT NULL
);


ALTER TABLE public.refresh_tokens OWNER TO yuxin;

--
-- Name: saved_listings; Type: TABLE; Schema: public; Owner: yuxin
--

CREATE TABLE public.saved_listings (
    id bigint NOT NULL,
    user_id uuid NOT NULL,
    listing_id uuid NOT NULL,
    saved_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.saved_listings OWNER TO yuxin;

--
-- Name: saved_listings_id_seq; Type: SEQUENCE; Schema: public; Owner: yuxin
--

CREATE SEQUENCE public.saved_listings_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.saved_listings_id_seq OWNER TO yuxin;

--
-- Name: saved_listings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: yuxin
--

ALTER SEQUENCE public.saved_listings_id_seq OWNED BY public.saved_listings.id;


--
-- Name: user_profiles; Type: TABLE; Schema: public; Owner: yuxin
--

CREATE TABLE public.user_profiles (
    profile_id uuid NOT NULL,
    user_id uuid NOT NULL,
    profile_image character varying(255),
    rating double precision,
    created_at timestamp with time zone,
    follower_count integer,
    following_count integer,
    listings uuid[] DEFAULT '{}'::uuid[],
    saved_listings uuid[] DEFAULT '{}'::uuid[],
    purchased uuid[] DEFAULT '{}'::uuid[],
    followers uuid[] DEFAULT '{}'::uuid[],
    following uuid[] DEFAULT '{}'::uuid[]
);


ALTER TABLE public.user_profiles OWNER TO yuxin;

--
-- Name: users; Type: TABLE; Schema: public; Owner: yuxin
--

CREATE TABLE public.users (
    user_id uuid DEFAULT gen_random_uuid() NOT NULL,
    first_name character varying(255) NOT NULL,
    last_name character varying(255) NOT NULL,
    dorm character varying(255),
    email character varying(255) NOT NULL,
    username character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    enabled boolean DEFAULT false NOT NULL,
    verification_code character varying(255),
    verification_expiration timestamp without time zone
);


ALTER TABLE public.users OWNER TO yuxin;

--
-- Name: users_seq; Type: SEQUENCE; Schema: public; Owner: yuxin
--

CREATE SEQUENCE public.users_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.users_seq OWNER TO yuxin;

--
-- Name: conversations id; Type: DEFAULT; Schema: public; Owner: yuxin
--

ALTER TABLE ONLY public.conversations ALTER COLUMN id SET DEFAULT nextval('public.conversations_id_seq'::regclass);


--
-- Name: saved_listings id; Type: DEFAULT; Schema: public; Owner: yuxin
--

ALTER TABLE ONLY public.saved_listings ALTER COLUMN id SET DEFAULT nextval('public.saved_listings_id_seq'::regclass);


--
-- Data for Name: conversations; Type: TABLE DATA; Schema: public; Owner: yuxin
--

COPY public.conversations (id, user_one, user_two, user_one_msg, user_two_msg, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: listings; Type: TABLE DATA; Schema: public; Owner: yuxin
--

COPY public.listings (item_id, user_id, listing_type, description, created_at, brand, condition, price, pick_up_location, trade_for, return_by, pick_up_by, title, category, image_urls) FROM stdin;
d9e76989-b2a6-4605-81dc-7d97d35609d9	b0bad584-5f9b-431b-b646-dd368d627115	Selling	testing testing testing	2025-06-30 20:18:25.01471	Apple	Like New	699.99	Harleston Hall	\N	\N	\N	Macbook 13 Inch Light Blue	Electronics	["https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1751329103980-6cb55325-56c6-4555-b819-6877071ea8d0.png", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1751329104449-a4796e9c-089e-4849-80a8-fbe925bbd1b9.png", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1751329104629-96b9bd54-ebdf-48af-a0f7-90d5740854b1.png", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1751329104828-76776834-5956-4be8-812a-1a97be048c71.png"]
56e35011-4cad-4440-b512-2be5cc58905b	b0bad584-5f9b-431b-b646-dd368d627115	Selling	testing frontend 	2025-06-30 21:09:56.347047	Apple	Like New	699.99	Harleston Hall		\N	\N		Electronics	[]
763fc484-ed94-4981-8431-80bc48ff3969	b0bad584-5f9b-431b-b646-dd368d627115	Selling	testing frontend pt2	2025-07-01 16:55:32.206187	Apple	Like New	699.99	Harleston Hall		\N	\N	MacBook 13 Inch 2025 Ver.	Electronics	[]
879476bc-a029-41cd-990a-02dfe5011ec4	b0bad584-5f9b-431b-b646-dd368d627115	Selling	Testing fronyend pt3	2025-07-01 17:08:40.454349	Apple	Like New	699.99	Harleston Hall		\N	\N	MacBook 13 Inch 2025 Light Blue	Electronics	["https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1751404119336-4ca09ce0-3cfc-4a95-9d3d-0d0f323bea88.jpg", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1751404120174-ab157f85-56d5-4d6c-ba53-832bba9fc70a.jpg"]
1a6880c0-78b1-47c1-8f3d-b1646017f6e1	b0bad584-5f9b-431b-b646-dd368d627115	Selling	Pepepe	2025-07-07 22:41:56.85752	Apple	New	0.01	Harleston Hall		\N	\N	Testing_User_Id	Electronics	["https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1751942516409-185128ad-3b53-4f32-b9f1-6af85a71d6c5.jpg"]
1717ceee-8b76-42be-adca-906cd69028ab	1c326c3d-7124-491f-ab9c-fa87e8f0ac29	Selling	Pepepep	2025-07-07 22:54:46.186997	Apple	New	0.05	Harleston Hall		\N	\N	Testing_User_Id_3	Electronics	["https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1751943285781-576f76db-e542-4af3-b700-c223e50b43b6.jpg"]
2a334a12-6c9a-4e92-804e-880daf69b846	fbb712e7-02a2-4aa0-a94a-7f2ad874c7af	Selling	Super cute Patagonia fleece jacket. Kids L fits like XXS/XS. Sleeves too short for me now. Message me with questions!	2025-07-09 01:30:20.789388	Arc'teryx	Like New	45.50	Carmichael Hall	\N	\N	\N	Arc'teryx Women's Cream and Grey Jacket	Clothing	["https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1752039019295-e819e2f4-a9f5-49af-acac-e45dd35466c1.jpg", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1752039019754-b9833d78-b0cc-4fe4-9f9e-ea17714d5718.jpg", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1752039019891-b69a3e26-3022-4213-8a44-7cb756df7e84.jpg", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1752039020098-ba1ccb6e-dbe5-4beb-b56f-d2c9b0bda77c.jpg", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1752039020419-720df54c-c07e-4778-aa63-03f1f51d70bb.jpg"]
224730ec-1fd1-4bd5-89d3-96424f9de190	fbb712e7-02a2-4aa0-a94a-7f2ad874c7af	Selling	Capture precious moments with this silver Nikon COOLPIX S6000 digital camera. 	2025-07-09 01:34:58.609574	Nikon	Like New	120.00	Carmichael Hall	\N	\N	\N	Silver Nikon COOLPIX S6000 14.2MP Digital Camera Lens Cover Defective Untested	Electronics	["https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1752039296740-37ad4336-ae3a-4658-8e4f-9659cb242e82.jpg", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1752039297176-e4d819cd-2298-4e0c-913f-05398c641a17.jpg", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1752039297454-8037ea10-85cc-460e-96b6-a68d82e7fee2.jpg", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1752039297742-559fa7ba-401f-4226-be59-df856904d069.jpg", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1752039298088-f7e00ef0-c062-40ee-8bf3-ecca2f978f3d.jpg", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1752039298312-2cc97b3a-1f61-45c5-ae4a-fe3ae6ba029e.jpg"]
35a71d05-d6a2-432f-b3ba-20790bc6569d	fbb712e7-02a2-4aa0-a94a-7f2ad874c7af	Giveaway	Capture precious moments with this silver Nikon COOLPIX S6000 digital camera. 	2025-07-09 01:36:04.259081	iKEA	Good	\N	Carmichael Hall	\N	\N	\N	Large Maroon Couch	Furniture	["https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1752039363972-46edd9c3-8dc4-444f-8f88-51795f5c379c.jpg"]
6a37a3c3-aaca-4524-a334-0b17a7237526	fbb712e7-02a2-4aa0-a94a-7f2ad874c7af	Trading	Minor ware and tare, as you can see on the pictures. Looking for a Chemistry textbook!	2025-07-09 01:39:40.967711	Campbell	Like New	\N	Carmichael Hall	Campbell Chemistry	\N	\N	Campbell Biology (Campbell Biology Series)	Books	["https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1752039577170-88718a91-49f8-4d57-9696-162590b436a2.jpg", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1752039577981-1db0a0fd-5408-4b24-82fc-6187d8aa9a0c.jpg", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1752039578457-681d03ea-9207-465c-aa12-dc14c9506f9d.jpg", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1752039579258-23232959-24d7-4cfa-93a5-101068c9d0bc.jpg", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1752039580059-4a3f12af-b96d-4a4b-aaf2-590869966de4.jpg", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1752039580332-9aaf75ed-400f-4f30-99b0-3b6b6dfb745d.jpg"]
19bbc4c0-acbc-4ab3-8567-9735a26e7004	fbb712e7-02a2-4aa0-a94a-7f2ad874c7af	Lending	Come pick up any time. I'm on Carm 2nd floor.	2025-07-09 01:42:49.882288	Dirt Devil	Good	\N	Carmichael Hall	\N	2025-09-29	\N	Dirt Devil Endura Reach Upright Vacuum Cleaner - Red (UD20124)	Appliances	["https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1752039766303-70d9814b-4071-469e-a0ef-a4889ab5f550.webp", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1752039768453-522d6ddd-442c-44bb-b28a-cde0135fe9d2.webp", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1752039769026-e69c3c13-3d44-4f3d-8560-4c0eb895a925.webp"]
83a5ecc7-0a26-4c9b-9cda-43f53a0742d2	fbb712e7-02a2-4aa0-a94a-7f2ad874c7af	Selling	gaming chair. The only flaw is it does squeak pretty easily but it's not too loud, it reclines	2025-07-09 01:44:14.228762	Unknown	Good	60.00	Carmichael Hall	\N	\N	\N	Red Reclining Gaming Chair	Furniture	["https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1752039853521-e3516ef1-34ca-43f8-9f80-f3b362827989.webp"]
5f013065-93ae-4b80-b0f6-78430e3b500f	1c326c3d-7124-491f-ab9c-fa87e8f0ac29	Giveaway	test	2025-07-26 23:38:02.978007	Apple	Good	\N	Harleston Hall		\N	2025-11-21	iPhone 4	Electronics	["https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1753587481920-21c8cc52-7dfd-4ba7-bedf-440810f95a24.jpg", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1753587482520-8fee2109-4a6b-41e2-bccc-017a6fb8e878.jpg", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1753587482757-ca69fad4-9229-42ed-a360-892231ac0af1.jpg"]
b38f2faf-85f1-4c76-a1e3-3cdc2a881338	1c326c3d-7124-491f-ab9c-fa87e8f0ac29	Giveaway	test2	2025-07-26 23:48:58.447763	Apple	Good	\N	Harleston Hall		\N	2025-11-21	iPhone 5 SE	Electronics	["https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1753588137385-66006e90-9a43-4bf7-b2b1-ef17e0f2a828.jpg", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1753588137999-ea427b91-eef8-4b64-a97a-b5a5b45772f7.jpg", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1753588138228-eeb484d0-1a62-4fd1-acc0-3bd8b71787d2.jpg"]
b177b371-08a5-45d5-be6a-4b302d8e3aaa	1c326c3d-7124-491f-ab9c-fa87e8f0ac29	Giveaway	testing whether save to user_profile	2025-07-27 16:18:15.570775	Apple	Good	\N	Harleston Hall		\N	2025-11-21	iPhone 6	Electronics	["https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1753647493947-e9800307-0708-478c-814c-f5550d5a0cfe.jpg", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1753647494845-fd85a34b-e2a4-439b-ba25-8ebae27eef0f.jpg", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1753647495105-616c5660-2bb8-45e7-a7dc-b76e3646e176.jpg"]
d76e2b2a-b6c9-43a9-8be2-8a984f2af8c0	1c326c3d-7124-491f-ab9c-fa87e8f0ac29	Giveaway	testing whether save to user_profile	2025-07-27 16:23:24.932222	Apple	Good	\N	Harleston Hall		\N	2025-11-21	iPhone 6	Electronics	["https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1753647803713-236edf88-1abc-4b4b-a6e9-439061b06b81.jpg", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1753647804492-e02b3985-1896-40a2-8398-7522eff9f046.jpg", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1753647804773-339c8c6e-0226-4af1-a081-0a441936a4d5.jpg"]
1bc57b81-f61a-4958-a362-95e8bcb33a4f	1c326c3d-7124-491f-ab9c-fa87e8f0ac29	Giveaway	testing whether save to user_profile	2025-07-27 16:25:51.419663	Apple	Good	\N	Harleston Hall		\N	2025-11-21	iPhone 6	Electronics	["https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1753647950170-f1ddb3ae-d8d4-488c-a1e5-1d48309c5ea4.jpg", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1753647951057-e340256c-9846-4eee-93d0-26d2fd04571e.jpg", "https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1753647951261-bece63a0-a1a7-4296-b65e-80df08391671.jpg"]
053386bc-5f4f-469b-8368-293ab370fd27	1c326c3d-7124-491f-ab9c-fa87e8f0ac29	Giveaway	Test	2025-07-27 16:26:58.296739	Apple	Good	\N	Harleston Hall		\N	2025-11-21	iPhone 7	Electronics	["https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1753648017803-b9845436-3fb1-498f-b362-e7e69cc99b8a.jpg"]
d7d31d06-f2fc-4f6b-8d39-109fb0294e43	1c326c3d-7124-491f-ab9c-fa87e8f0ac29	Giveaway	Test	2025-07-27 16:28:35.6696	Apple	Good	\N	Harleston Hall		\N	2025-11-21	iPhone 7	Electronics	["https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/listings/1753648115071-4e597819-b0ac-4f04-a838-351479f90a66.jpg"]
\.


--
-- Data for Name: refresh_tokens; Type: TABLE DATA; Schema: public; Owner: yuxin
--

COPY public.refresh_tokens (id, created_at, expires_at, is_active, token, user_id) FROM stdin;
3422ccaa-9248-4652-8889-2b593b247293	2025-07-06 15:07:16.943643	2025-08-05 15:07:16.943627	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MTgyODgzNiwiZXhwIjoxNzU0NDIwODM2fQ.wgaE2I00MVNUesLJyd-_59dnQBPN-JPtVrBjyRFFDFQ	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
0563aae4-234f-4f34-8e04-80cabe1b1810	2025-07-06 15:23:56.610395	2025-08-05 15:23:56.610388	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MTgyOTgzNiwiZXhwIjoxNzU0NDIxODM2fQ.OGvUmPMMeX4ufZOgAXlNrn2D9I_uwL8OcHpg2m2BjEQ	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
c403df28-c3a0-4d61-9a6f-4a292ed75422	2025-07-06 16:39:50.065735	2025-08-05 16:39:50.065723	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MTgzNDM5MCwiZXhwIjoxNzU0NDI2MzkwfQ.ZjampIjL0DVmVuPhMgwY_jlzjUkDyTAGj325u-tCgfs	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
704ab491-6fad-4474-9448-e342634323f1	2025-07-28 16:18:00.33123	2025-08-27 16:18:00.331215	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MzczMzg4MCwiZXhwIjoxNzU2MzI1ODgwfQ.V8aPM1-JrZThKcrzTiE7wvlRAw51vKsMG3DW6zJINEE	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
0782b220-bfe4-4380-959a-cdc807aa7047	2025-08-15 20:23:55.182138	2025-09-14 20:23:55.182122	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1NTMwMzgzNSwiZXhwIjoxNzU3ODk1ODM1fQ.jBJ_TSzKqRx87rNCgq22VwoPELV6BDSKFGh7uEa0yus	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
25ebbdc6-2d3b-4180-89d1-4d8c505a6f87	2025-07-06 14:10:31.980173	2025-08-05 14:10:31.98015	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MTgyNTQzMSwiZXhwIjoxNzU0NDE3NDMxfQ.-CtuGgkRq69s2M5Sw2-vcERd2EJ9dylNAmVq1y6KUlI	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
d905f6b0-a4ed-4970-9a58-f9bd76c864b4	2025-07-06 14:10:45.569102	2025-08-05 14:10:45.569095	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MTgyNTQ0NSwiZXhwIjoxNzU0NDE3NDQ1fQ.B_05rTnFK--2y25yB1awjHI7iZUaSWuGPTWa5bXpIHE	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
3b227847-6563-455c-b9e7-8e6632571441	2025-07-26 21:39:08.289249	2025-08-25 21:39:08.289243	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MzU4MDM0OCwiZXhwIjoxNzU2MTcyMzQ4fQ.KlwBuZQiERJ6poJO5-5NRY1g2U8vpo-sfy_EpgrZB-I	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
67581271-de2f-468a-ab87-3a06c75d7833	2025-07-27 16:15:24.279393	2025-08-26 16:15:24.279378	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MzY0NzMyNCwiZXhwIjoxNzU2MjM5MzI0fQ.EzDxwyFcjxX6GdW4e6LQ_dMPnUzYn0X7YPz4OmjKkNQ	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
5635c3f4-9af5-45b1-912d-7fb92bea210a	2025-07-31 14:15:47.019578	2025-08-30 14:15:47.019556	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1Mzk4NTc0NywiZXhwIjoxNzU2NTc3NzQ3fQ._1jK1R2cDYYzUKClUMc4O-R2kgpgi35mJSC5BnpIzCQ	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
8e0cc350-b786-4e18-8dac-928b64625414	2025-08-21 15:21:58.987073	2025-09-20 15:21:58.987061	t	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1NTgwNDExOCwiZXhwIjoxNzU4Mzk2MTE4fQ.BdqzPravixgd2Qn2h1LSBSM9gcWNlZWeFOPiKWNSj3o	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
de46dbd6-2029-4b22-9464-fb773c75ef22	2025-07-05 21:01:23.373303	2025-08-04 21:01:23.373283	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6Inl1LnplbmdAdHVmdHMuZWR1IiwiaWF0IjoxNzUxNzYzNjgzLCJleHAiOjE3NTQzNTU2ODN9.yYkV9Vi1DKPTb_n4UHHYpo_9_IrKt2U58GWTgVzKyis	b0bad584-5f9b-431b-b646-dd368d627115
82788a68-9d13-4efd-a5b9-eedd4c39da4f	2025-07-05 21:01:42.020972	2025-08-04 21:01:42.020963	t	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6Inl1LnplbmdAdHVmdHMuZWR1IiwiaWF0IjoxNzUxNzYzNzAyLCJleHAiOjE3NTQzNTU3MDJ9.82P-2bB2sfDY1F22fZ1oY-U26rLMtKi88dubHx4uq_c	b0bad584-5f9b-431b-b646-dd368d627115
1aec5114-6bcd-48bb-a68b-5b8368d790c5	2025-07-07 22:32:18.607953	2025-08-06 22:32:18.607948	t	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6ImZiYjcxMmU3LTAyYTItNGFhMC1hOTRhLTdmMmFkODc0YzdhZiIsImlhdCI6MTc1MTk0MTkzOCwiZXhwIjoxNzU0NTMzOTM4fQ.flQHPLLk8NjahDkc6y4wvJ2sLdjCDpn-W2dDMofOhtA	fbb712e7-02a2-4aa0-a94a-7f2ad874c7af
44975635-c286-40ca-8802-7430cc617f4e	2025-07-05 22:15:01.110798	2025-08-04 22:15:01.110773	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MTc2ODEwMSwiZXhwIjoxNzU0MzYwMTAxfQ.OjspeeI4IQEU48SCCia2THzsSfqw6kQC3ie6Fe3F1CE	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
aff28675-d240-45d8-8001-b5a1a90d1738	2025-07-05 22:26:33.433277	2025-08-04 22:26:33.433255	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MTc2ODc5MywiZXhwIjoxNzU0MzYwNzkzfQ.vm2mQJrXHzBQef5coCwmtKBV12q5AvAjr7oMZtVme5I	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
c8fecb0f-0902-49b4-8ba7-08f463f5d58a	2025-07-05 22:47:33.733852	2025-08-04 22:47:33.733827	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MTc3MDA1MywiZXhwIjoxNzU0MzYyMDUzfQ.zmUOB1gaERtYyACuZC8MkvmvN545rSyORr-w5zwSQpw	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
48b4357c-a8a6-4b32-b2d9-7f62298e2260	2025-07-19 22:48:27.716283	2025-08-18 22:48:27.716264	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1Mjk3OTcwNywiZXhwIjoxNzU1NTcxNzA3fQ.6hUx9vA9SSkJB-UC4ADPzHbizhwwe_S70o0q3vnxB5Y	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
bc7ac803-3f84-4fc6-bff2-1477e83d5e74	2025-07-23 00:54:58.714192	2025-08-22 00:54:58.714175	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MzI0NjQ5OCwiZXhwIjoxNzU1ODM4NDk4fQ.4cJF-DAVv1qc9zWnLHekFfGD1vAHQrsQtrm8B2tlf9E	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
0656ea2a-1f9a-4df9-aa4b-18df3ebe9c56	2025-07-07 21:18:28.957082	2025-08-06 21:18:28.95707	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MTkzNzUwOCwiZXhwIjoxNzU0NTI5NTA4fQ.nb2Yp6NpgoWjfPfAt6taA3J4Nx5pHQaaYlz9VITZ8_Y	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
3ce5675a-1dae-4774-80d4-0c954902dc70	2025-07-07 21:24:41.205046	2025-08-06 21:24:41.205033	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MTkzNzg4MSwiZXhwIjoxNzU0NTI5ODgxfQ._fd0EYYFSJoqrM9Atv5t8IjHeAQfT19G9TRB0rLjcB4	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
ebe2fb79-2833-4b3b-86cc-5b8a5d218d27	2025-07-09 20:36:58.123438	2025-08-08 20:36:58.123423	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MjEwNzgxOCwiZXhwIjoxNzU0Njk5ODE4fQ.6sAY_z9q8UVgg93JT63RpNxD5SW3JyI-C6OYXJ3oNIY	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
15667311-a301-4ad7-b3ac-72c25247f382	2025-07-19 15:43:09.656414	2025-08-18 15:43:09.656369	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1Mjk1NDE4OSwiZXhwIjoxNzU1NTQ2MTg5fQ.pMjsCZ1Nk7kmotp8Sr6Xyz9CM82-80j83Wt7Ky6W3Lo	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
4616c2d0-da28-4f94-8590-f151ab019019	2025-07-21 23:07:01.338343	2025-08-20 23:07:01.338322	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MzE1MzYyMSwiZXhwIjoxNzU1NzQ1NjIxfQ.HPBEteVQVsNV1X321GYXGsvTqTzucWKZv5KUk4003oA	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
af5a9a94-450f-419f-953f-06b10ac026c4	2025-07-22 23:40:56.237389	2025-08-21 23:40:56.237364	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MzI0MjA1NiwiZXhwIjoxNzU1ODM0MDU2fQ.69ummOGcJwxAsa_Hpq6o4kMiHIlJBN1CJN7odzkW5r8	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
1fdab9a2-4a61-4fd7-aed2-d2e07ba4a842	2025-07-10 13:53:14.319722	2025-08-09 13:53:14.319708	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MjE2OTk5NCwiZXhwIjoxNzU0NzYxOTk0fQ.JwTED72ZsoJIr6Pi6H5g7jn_UJvtouICVh0bcn6AsSE	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
b824c2bf-8731-4361-b6e9-40e8d53c44c0	2025-07-05 23:12:54.226172	2025-08-04 23:12:54.22615	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MTc3MTU3NCwiZXhwIjoxNzU0MzYzNTc0fQ.RI6nysVGl6TRj-d7JIVOUv1_JN_jKc2Jz500wIOKwq8	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
2a2bcacc-150d-4932-977a-9de9c8aff013	2025-07-06 15:54:42.088632	2025-08-05 15:54:42.08862	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MTgzMTY4MiwiZXhwIjoxNzU0NDIzNjgyfQ.5rR7On0F0xEKeawDKsCYBaTA06ScBiP6u006UNIo_7o	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
747deb2c-05e9-411c-9555-d5d9acf7241d	2025-07-09 19:48:30.625138	2025-08-08 19:48:30.625116	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MjEwNDkxMCwiZXhwIjoxNzU0Njk2OTEwfQ.nzjjNDxcuicWG2Zajo-_mxy5ToXl3GsgL-KjHORGAdg	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
290ca6ed-3f82-44cf-aeb9-c2f566c2bb63	2025-07-06 15:36:20.061866	2025-08-05 15:36:20.061851	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MTgzMDU4MCwiZXhwIjoxNzU0NDIyNTgwfQ.lCxmcIWKWwpIopwGShhJEoccxHSRV8O7y09SCZ_pw5I	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
425f697d-1a14-4803-aa6d-4bd785398aaa	2025-07-06 15:42:22.962004	2025-08-05 15:42:22.961997	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MTgzMDk0MiwiZXhwIjoxNzU0NDIyOTQyfQ.KzC81KtO2BiTBjJVuZuIyZKAWHe52J1xkFifP7-oe4M	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
e44e74c2-02ce-438c-b396-ace4c72ebbd9	2025-07-17 14:03:56.980312	2025-08-16 14:03:56.980293	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1Mjc3NTQzNiwiZXhwIjoxNzU1MzY3NDM2fQ.ydS8h7ZY0KSc50qY456EfFrzz3NCxfunQ3_d3ESY6Bw	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
f060dfa4-9b8a-4084-a75d-6cc4a63d7ee8	2025-07-23 17:59:08.781652	2025-08-22 17:59:08.781632	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MzMwNzk0OCwiZXhwIjoxNzU1ODk5OTQ4fQ.yi1QhA3aQEMtBm6NkHN_9VmyM9nXrdx3bEZJJukq6co	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
947772cf-a9eb-48d9-ac53-edd93f6050b4	2025-07-23 18:00:44.602368	2025-08-22 18:00:44.602361	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MzMwODA0NCwiZXhwIjoxNzU1OTAwMDQ0fQ.Gj_TOlGnSyRB7a1rIqyusKSEfznzYOYk4ynbheRCTIU	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
c5e004a7-137f-4cd4-9b08-f9b8b0cb68d0	2025-07-26 21:37:37.500455	2025-08-25 21:37:37.50044	f	eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjFjMzI2YzNkLTcxMjQtNDkxZi1hYjljLWZhODdlOGYwYWMyOSIsImlhdCI6MTc1MzU4MDI1NywiZXhwIjoxNzU2MTcyMjU3fQ.Esps7_b8x40oozVkOEiPIh7iEdLuqQoMUUu2_1gTuks	1c326c3d-7124-491f-ab9c-fa87e8f0ac29
\.


--
-- Data for Name: saved_listings; Type: TABLE DATA; Schema: public; Owner: yuxin
--

COPY public.saved_listings (id, user_id, listing_id, saved_at) FROM stdin;
\.


--
-- Data for Name: user_profiles; Type: TABLE DATA; Schema: public; Owner: yuxin
--

COPY public.user_profiles (profile_id, user_id, profile_image, rating, created_at, follower_count, following_count, listings, saved_listings, purchased, followers, following) FROM stdin;
7c967567-51f6-4ee7-843f-0cf36f205c6f	fbb712e7-02a2-4aa0-a94a-7f2ad874c7af	https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/profile-images/1752530315908-3db666b4-26dc-422b-918d-72dd89f05bc6.webp	0	2025-07-14 17:58:36.483896-04	0	0	\N	\N	\N	\N	\N
b3ecb70e-7219-4a8c-a715-b93658d6eaac	1c326c3d-7124-491f-ab9c-fa87e8f0ac29	https://unicycle-app-bucket.s3.us-east-1.amazonaws.com/profile-images/1753582199218-792c21ab-f13f-4667-87bd-05ca0a0dba7c.jpg	0	2025-07-14 17:43:46.123628-04	0	0	{1bc57b81-f61a-4958-a362-95e8bcb33a4f,053386bc-5f4f-469b-8368-293ab370fd27,d7d31d06-f2fc-4f6b-8d39-109fb0294e43}	\N	\N	\N	\N
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: yuxin
--

COPY public.users (user_id, first_name, last_name, dorm, email, username, password, enabled, verification_code, verification_expiration) FROM stdin;
1c326c3d-7124-491f-ab9c-fa87e8f0ac29	Yuxin	Zeng	Harleston Hall	yu.zeng@tufts.edu	yuxin.zeng	$2a$10$dpBUVp2Q1aZ1Vj.q1HLMM.tBjO7aZMDmIHf7BJUBJHYNoGLzUMO3.	t	\N	\N
fbb712e7-02a2-4aa0-a94a-7f2ad874c7af	John	Doe	Harleston Hall	john.doe@tufts.edu	jdoe	$2a$10$4CVbINmuvJq/BNL4wTibUO7uu6c88JW127I5ThmT1QEI32I5b/1eq	t	\N	\N
\.


--
-- Name: conversations_id_seq; Type: SEQUENCE SET; Schema: public; Owner: yuxin
--

SELECT pg_catalog.setval('public.conversations_id_seq', 1, false);


--
-- Name: saved_listings_id_seq; Type: SEQUENCE SET; Schema: public; Owner: yuxin
--

SELECT pg_catalog.setval('public.saved_listings_id_seq', 1, false);


--
-- Name: users_seq; Type: SEQUENCE SET; Schema: public; Owner: yuxin
--

SELECT pg_catalog.setval('public.users_seq', 1, true);


--
-- Name: conversations conversations_pkey; Type: CONSTRAINT; Schema: public; Owner: yuxin
--

ALTER TABLE ONLY public.conversations
    ADD CONSTRAINT conversations_pkey PRIMARY KEY (id);


--
-- Name: listings listings_pkey; Type: CONSTRAINT; Schema: public; Owner: yuxin
--

ALTER TABLE ONLY public.listings
    ADD CONSTRAINT listings_pkey PRIMARY KEY (item_id);


--
-- Name: refresh_tokens refresh_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: yuxin
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_pkey PRIMARY KEY (id);


--
-- Name: saved_listings saved_listings_pkey; Type: CONSTRAINT; Schema: public; Owner: yuxin
--

ALTER TABLE ONLY public.saved_listings
    ADD CONSTRAINT saved_listings_pkey PRIMARY KEY (id);


--
-- Name: user_profiles uk_user_profiles_user_id; Type: CONSTRAINT; Schema: public; Owner: yuxin
--

ALTER TABLE ONLY public.user_profiles
    ADD CONSTRAINT uk_user_profiles_user_id UNIQUE (user_id);


--
-- Name: refresh_tokens ukghpmfn23vmxfu3spu3lfg4r2d; Type: CONSTRAINT; Schema: public; Owner: yuxin
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT ukghpmfn23vmxfu3spu3lfg4r2d UNIQUE (token);


--
-- Name: conversations unique_participants; Type: CONSTRAINT; Schema: public; Owner: yuxin
--

ALTER TABLE ONLY public.conversations
    ADD CONSTRAINT unique_participants UNIQUE (user_one, user_two);


--
-- Name: saved_listings unique_user_listing; Type: CONSTRAINT; Schema: public; Owner: yuxin
--

ALTER TABLE ONLY public.saved_listings
    ADD CONSTRAINT unique_user_listing UNIQUE (user_id, listing_id);


--
-- Name: users unique_username; Type: CONSTRAINT; Schema: public; Owner: yuxin
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT unique_username UNIQUE (username);


--
-- Name: user_profiles user_profiles_pkey; Type: CONSTRAINT; Schema: public; Owner: yuxin
--

ALTER TABLE ONLY public.user_profiles
    ADD CONSTRAINT user_profiles_pkey PRIMARY KEY (profile_id);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: yuxin
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: yuxin
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- Name: idx_conversations_user_one_msg; Type: INDEX; Schema: public; Owner: yuxin
--

CREATE INDEX idx_conversations_user_one_msg ON public.conversations USING gin (user_one_msg);


--
-- Name: idx_conversations_user_two_msg; Type: INDEX; Schema: public; Owner: yuxin
--

CREATE INDEX idx_conversations_user_two_msg ON public.conversations USING gin (user_two_msg);


--
-- Name: conversations conversations_user_one_fkey; Type: FK CONSTRAINT; Schema: public; Owner: yuxin
--

ALTER TABLE ONLY public.conversations
    ADD CONSTRAINT conversations_user_one_fkey FOREIGN KEY (user_one) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- Name: conversations conversations_user_two_fkey; Type: FK CONSTRAINT; Schema: public; Owner: yuxin
--

ALTER TABLE ONLY public.conversations
    ADD CONSTRAINT conversations_user_two_fkey FOREIGN KEY (user_two) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- Name: saved_listings saved_listings_listing_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: yuxin
--

ALTER TABLE ONLY public.saved_listings
    ADD CONSTRAINT saved_listings_listing_id_fkey FOREIGN KEY (listing_id) REFERENCES public.listings(item_id) ON DELETE CASCADE;


--
-- Name: saved_listings saved_listings_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: yuxin
--

ALTER TABLE ONLY public.saved_listings
    ADD CONSTRAINT saved_listings_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- Name: user_profiles user_profiles_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: yuxin
--

ALTER TABLE ONLY public.user_profiles
    ADD CONSTRAINT user_profiles_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

