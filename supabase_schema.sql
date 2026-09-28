-- ========================================================================
-- Mznlive Supabase Database Schema & Seed Data
-- Project ID: iyppuawgyelubfohxthj
-- Run this SQL in your Supabase SQL Editor to set up all tables and initial data
-- ========================================================================

-- 1. Intro Slides (Screen 2: 3 consecutive promotional images)
CREATE TABLE IF NOT EXISTS public.intro_slides (
    id BIGSERIAL PRIMARY KEY,
    order_num INT NOT NULL DEFAULT 1,
    title TEXT NOT NULL,
    description TEXT,
    image_url TEXT NOT NULL,
    facebook_post_url TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Enable RLS and public read access
ALTER TABLE public.intro_slides ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Allow public read intro_slides" ON public.intro_slides FOR SELECT USING (true);

-- 2. App Configuration (Screen 3 video link, support links, settings)
CREATE TABLE IF NOT EXISTS public.app_config (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL,
    description TEXT,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.app_config ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Allow public read app_config" ON public.app_config FOR SELECT USING (true);

-- 3. Live News Feed (Scrolling News bar with Hindi & English headlines)
CREATE TABLE IF NOT EXISTS public.live_news (
    id BIGSERIAL PRIMARY KEY,
    title_en TEXT NOT NULL,
    title_hi TEXT NOT NULL,
    source_newspaper TEXT NOT NULL,
    website_url TEXT NOT NULL,
    category TEXT DEFAULT 'Local',
    is_breaking BOOLEAN DEFAULT false,
    published_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.live_news ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Allow public read live_news" ON public.live_news FOR SELECT USING (true);

-- 4. Live News YouTube Video Stream (Screen 4 Live news section)
CREATE TABLE IF NOT EXISTS public.live_stream (
    id BIGSERIAL PRIMARY KEY,
    title_en TEXT NOT NULL,
    title_hi TEXT NOT NULL,
    youtube_video_url TEXT NOT NULL,
    youtube_video_id TEXT NOT NULL,
    channel_name TEXT NOT NULL,
    category TEXT DEFAULT 'News',
    thumbnail_url TEXT,
    is_live BOOLEAN DEFAULT true,
    viewers_count INT DEFAULT 1250,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Ensure columns exist if table was previously created
ALTER TABLE public.live_stream ADD COLUMN IF NOT EXISTS category TEXT DEFAULT 'News';
ALTER TABLE public.live_stream ADD COLUMN IF NOT EXISTS thumbnail_url TEXT;

ALTER TABLE public.live_stream ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Allow public read live_stream" ON public.live_stream FOR SELECT USING (true);

-- 5. Local Events & Community Programs (Screen 4 Events bar)
CREATE TABLE IF NOT EXISTS public.events (
    id BIGSERIAL PRIMARY KEY,
    title_en TEXT NOT NULL,
    title_hi TEXT NOT NULL,
    category TEXT NOT NULL, -- e.g. 'Community Program', 'Local Problem', 'Festival'
    location TEXT NOT NULL,
    event_date TEXT NOT NULL,
    organizer TEXT,
    description_en TEXT,
    description_hi TEXT,
    facebook_event_url TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.events ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Allow public read events" ON public.events FOR SELECT USING (true);

-- 6. Sponsored Adverts & Product Showcase (Sliding 1-sec images, zoom-in details)
CREATE TABLE IF NOT EXISTS public.sponsored_adverts (
    id BIGSERIAL PRIMARY KEY,
    business_name TEXT NOT NULL,
    product_title TEXT NOT NULL,
    product_description TEXT NOT NULL,
    price TEXT,
    discount_tag TEXT,
    image_url TEXT NOT NULL,
    facebook_page_url TEXT NOT NULL,
    whatsapp_contact TEXT,
    display_duration_seconds INT DEFAULT 1,
    active BOOLEAN DEFAULT true,
    order_num INT DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.sponsored_adverts ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Allow public read sponsored_adverts" ON public.sponsored_adverts FOR SELECT USING (true);

-- 7. Instagram Reels Promotions (3-part aspect ratio 9:16 layout)
CREATE TABLE IF NOT EXISTS public.instagram_reels (
    id BIGSERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    shop_name TEXT NOT NULL,
    reel_url TEXT NOT NULL,
    thumbnail_url TEXT NOT NULL,
    likes_count TEXT DEFAULT '2.4K',
    slot_index INT DEFAULT 0, -- 0, 1, 2 for the 3 divided blocks
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.instagram_reels ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Allow public read instagram_reels" ON public.instagram_reels FOR SELECT USING (true);

-- 8. Marketplace Shops & Local Vendors
CREATE TABLE IF NOT EXISTS public.marketplace_shops (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    address TEXT NOT NULL,
    phone TEXT NOT NULL,
    whatsapp TEXT NOT NULL,
    instagram_link TEXT,
    facebook_link TEXT,
    rating REAL DEFAULT 4.8,
    image_url TEXT NOT NULL,
    featured_offer TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.marketplace_shops ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Allow public read marketplace_shops" ON public.marketplace_shops FOR SELECT USING (true);

-- 9. Chat Messages between Users and Shop Owners
CREATE TABLE IF NOT EXISTS public.chat_messages (
    id BIGSERIAL PRIMARY KEY,
    shop_id BIGINT,
    shop_name TEXT NOT NULL,
    sender TEXT NOT NULL, -- 'user' or 'shop'
    message TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.chat_messages ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Allow public read/insert chat_messages" ON public.chat_messages FOR ALL USING (true);

-- 10. Promotion Plans (Promotional Packages for Local Shops & Advertisers)
CREATE TABLE IF NOT EXISTS public.promotion_plans (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    duration_text TEXT NOT NULL,
    duration_days INT NOT NULL,
    price_inr INT NOT NULL,
    price_display TEXT NOT NULL,
    tagline TEXT NOT NULL,
    description TEXT NOT NULL,
    badge TEXT,
    is_popular BOOLEAN DEFAULT false,
    is_best_value BOOLEAN DEFAULT false,
    is_flash_deal BOOLEAN DEFAULT false,
    is_custom BOOLEAN DEFAULT false,
    features JSONB NOT NULL,
    graphic_type TEXT NOT NULL DEFAULT 'rocket',
    order_num INT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.promotion_plans ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Allow public read promotion_plans" ON public.promotion_plans FOR SELECT USING (true);

-- ========================================================================
-- Initial Seed Data for Mznlive
-- ========================================================================

-- Screen 2 Intro Slides (3 images)
INSERT INTO public.intro_slides (order_num, title, description, image_url, facebook_post_url) VALUES
(1, 'Local Live News Daily', 'Stay connected with instant breaking news from local newspapers and live video broadcasts.', 'https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=800&auto=format&fit=crop&q=80', 'https://facebook.com/mznlive'),
(2, 'Promote Local Brands & Shops', 'Grow your business with sponsored Facebook posts and trending Instagram Reels showcase.', 'https://images.unsplash.com/photo-1472851294608-062f824d29cc?w=800&auto=format&fit=crop&q=80', 'https://facebook.com/mznlive/ads'),
(3, 'Online Local Marketplace', 'Connect directly with local shop owners, chat, check offers, and order instantly.', 'https://images.unsplash.com/photo-1555529669-e69e7aa0ba9a?w=800&auto=format&fit=crop&q=80', 'https://facebook.com/mznlive/market')
ON CONFLICT DO NOTHING;

-- App Configuration (Screen 3 video link & support)
INSERT INTO public.app_config (key, value, description) VALUES
('onboarding_video_url', 'https://iyppuawgyelubfohxthj.supabase.co/storage/v1/object/public/MznLive/MznlivepermissionVideo.mp4', 'AI generated video explaining app permissions and login'),
('whatsapp_support_number', '+919876543210', 'Official Mznlive WhatsApp Support and OTP Service'),
('google_ads_banner_unit_id', 'ca-app-pub-3940256099942544/6300978111', 'Sample Google AdMob Banner Unit ID')
ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value;

-- Live News
INSERT INTO public.live_news (title_en, title_hi, source_newspaper, website_url, category, is_breaking) VALUES
('City smart transit expansion project approved with 12 new eco-friendly routes', 'स्मार्ट सिटी ट्रांसपोर्ट विस्तार परियोजना को मिली 12 नए ग्रीन रूट्स की मंजूरी', 'Dainik Jagran', 'https://www.jagran.com', 'Local', true),
('Local farmers market annual harvest carnival starts this Friday at Gandhi Ground', 'गांधी मैदान में शुक्रवार से शुरू होगा वार्षिक किसान उपज महोत्सव', 'Amar Ujala', 'https://www.amarujala.com', 'Agriculture', false),
('New modern digital library and skill center inaugurated in downtown sector', 'शहर के केंद्र में आधुनिक डिजिटल लाइब्रेरी और कौशल विकास केंद्र का उद्घाटन', 'Hindustan Live', 'https://www.livehindustan.com', 'Education', false),
('Municipal corporation launches green clean city drive with neighborhood rewards', 'नगर निगम ने स्वच्छता अभियान के तहत सर्वश्रेष्ठ मोहल्लों को पुरस्कृत करने की घोषणा की', 'Navbharat Times', 'https://navbharattimes.indiatimes.com', 'Civic', false)
ON CONFLICT DO NOTHING;

-- 4. Live Stream (10 Sample Playable Live News & Report Videos)
INSERT INTO public.live_stream (id, title_en, title_hi, youtube_video_url, youtube_video_id, channel_name, category, thumbnail_url, is_live, viewers_count) VALUES
(1, 'Mznlive 24x7 Prime News Bulletin - Ground Report', 'एमजेडएन लाइव 24x7 मुख्य समाचार बुलेटिन - ग्राउंड रिपोर्ट', 'https://www.youtube.com/watch?v=5qap5aO4i9A', '5qap5aO4i9A', 'Mznlive 24x7 News Channel', 'News', 'https://img.youtube.com/vi/5qap5aO4i9A/hqdefault.jpg', true, 5420),
(2, 'Muzaffarnagar Smart City Development & Infrastructure Special', 'मुजफ्फरनगर स्मार्ट सिटी विकास और बुनियादी ढांचा विशेष रिपोर्ट', 'https://www.youtube.com/watch?v=jfKfPfyJRdk', 'jfKfPfyJRdk', 'UP City Samachar', 'Development', 'https://img.youtube.com/vi/jfKfPfyJRdk/hqdefault.jpg', true, 3850),
(3, 'Western UP Kisan Mahapanchayat & Agricultural Mandi Live', 'पश्चिमी यूपी किसान महापंचायत एवं कृषि मंडी लाइव कवरेज', 'https://www.youtube.com/watch?v=21X5lGlDOfg', '21X5lGlDOfg', 'Kisan Bharat News', 'Agriculture', 'https://img.youtube.com/vi/21X5lGlDOfg/hqdefault.jpg', true, 6120),
(4, 'District Sports & Youth Talent Championship Finals', 'जिला खेल एवं युवा प्रतिभा चैंपियनशिप फाइनल मुकाबला', 'https://www.youtube.com/watch?v=DWcJFNfaw9c', 'DWcJFNfaw9c', 'UP Sports Live', 'Sports', 'https://img.youtube.com/vi/DWcJFNfaw9c/hqdefault.jpg', true, 2940),
(5, 'City Traffic & New Bypass Highway Flyover Inspection Report', 'शहर का नया बाईपास हाईवे और फ्लाईओवर निरीक्षण ग्राउंड रिपोर्ट', 'https://www.youtube.com/watch?v=sP-IDy3tq-E', 'sP-IDy3tq-E', 'Mzn Traffic Watch', 'Civic', 'https://img.youtube.com/vi/sP-IDy3tq-E/hqdefault.jpg', true, 4100),
(6, 'Historic Shukratal Heritage & Ganga Ghat Special Aarti', 'ऐतिहासिक शुक्रताल तीर्थ एवं गंगा आरती विशेष दर्शन', 'https://www.youtube.com/watch?v=7NOSDKb0HlU', '7NOSDKb0HlU', 'Dharmik Darshan Live', 'Culture', 'https://img.youtube.com/vi/7NOSDKb0HlU/hqdefault.jpg', true, 7200),
(7, 'Local Textile & Handloom Bazaar Festive Shopping Buzz', 'लोकल हैंडलूम व कपड़ा बाजार में त्योहारी रौनक की लाइव रिपोर्ट', 'https://www.youtube.com/watch?v=ysz5S6PUM-U', 'ysz5S6PUM-U', 'Vyapar Darpan', 'Business', 'https://img.youtube.com/vi/ysz5S6PUM-U/hqdefault.jpg', true, 3180),
(8, 'Health & Medical College Super-Specialty Wing Inauguration', 'मेडिकल कॉलेज में नए सुपर-स्पेशलिटी विंग का शुभारंभ', 'https://www.youtube.com/watch?v=kJQP7kiw5Fk', 'kJQP7kiw5Fk', 'Swasthya Bharat', 'Health', 'https://img.youtube.com/vi/kJQP7kiw5Fk/hqdefault.jpg', true, 4500),
(9, 'Police Administration Cyber Crime Awareness & Security Briefing', 'साइबर अपराध सुरक्षा व नागरिक जागरूकता पर पुलिस ब्रीफिंग', 'https://www.youtube.com/watch?v=aqz-KE-bpKQ', 'aqz-KE-bpKQ', 'Suraksha Manch Live', 'Safety', 'https://img.youtube.com/vi/aqz-KE-bpKQ/hqdefault.jpg', true, 2650),
(10, 'Weather & Monsoon Forecast: District Rainfall & Crop Advisory', 'मौसम बुलेटिन: मुजफ्फरनगर एवं आसपास के जिलों में बारिश का अलर्ट', 'https://www.youtube.com/watch?v=L_LUpnjgPso', 'L_LUpnjgPso', 'Mausam Live 24', 'Weather', 'https://img.youtube.com/vi/L_LUpnjgPso/hqdefault.jpg', true, 5800)
ON CONFLICT (id) DO UPDATE SET
    title_en = EXCLUDED.title_en,
    title_hi = EXCLUDED.title_hi,
    youtube_video_url = EXCLUDED.youtube_video_url,
    youtube_video_id = EXCLUDED.youtube_video_id,
    channel_name = EXCLUDED.channel_name,
    category = EXCLUDED.category,
    thumbnail_url = EXCLUDED.thumbnail_url,
    is_live = EXCLUDED.is_live,
    viewers_count = EXCLUDED.viewers_count;

-- Events
INSERT INTO public.events (title_en, title_hi, category, location, event_date, organizer, description_en, description_hi, facebook_event_url) VALUES
('Civic Water Pipeline Repair & Community Forum', 'जल आपूर्ति पाइपलाइन सुधार एवं जनसंवाद कार्यक्रम', 'Local Problems', 'Sector 4 Community Hall', 'Tomorrow 10:00 AM', 'City Jal Nigam', 'Interactive public meeting regarding municipal pipeline upgrade and citizen grievances.', 'नागरिकों की पेयजल समस्याओं के त्वरित समाधान हेतु नगर निगम की विशेष जनसंवाद सभा।', 'https://facebook.com/mznlive/events/101'),
('District Youth Entrepreneur & Startup Expo 2026', 'जिला युवा उद्यमी एवं स्टार्टअप महाकुंभ 2026', 'Community Program', 'Town Exhibition Ground', 'Saturday 11:00 AM', 'District Commerce Board', 'Showcase of 80+ local manufacturing units, handicrafts, and tech innovations.', '80 से अधिक स्थानीय विनिर्माताओं और स्टार्टअप्स की प्रदर्शनी और मेंटरशिप सत्र।', 'https://facebook.com/mznlive/events/102'),
('Free Eye & Dental Health Checkup Camp', 'निःशुल्क नेत्र एवं दंत चिकित्सा परामर्श शिविर', 'Health Camp', 'Civil Lines Red Cross Center', 'Sunday 9:00 AM', 'Rotary Club & Lions Club', 'Free checkup, basic medicines, and eye testing for senior citizens and families.', 'वरिष्ठ नागरिकों और बच्चों के लिए विशेषज्ञ डॉक्टरों द्वारा मुफ्त जांच व दवा वितरण।', 'https://facebook.com/mznlive/events/103')
ON CONFLICT DO NOTHING;

-- Sponsored Adverts (1-sec sliding images with zoom-in details)
INSERT INTO public.sponsored_adverts (business_name, product_title, product_description, price, discount_tag, image_url, facebook_page_url, whatsapp_contact, display_duration_seconds, order_num) VALUES
('Royal Heritage Silk Sarees', 'Pure Banarasi Katan Handloom Collection', 'Exclusive bridal silk sarees with genuine gold zari weave. Direct from master weavers with certificate.', '₹4,999', '25% OFF', 'https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=700&auto=format&fit=crop&q=80', 'https://facebook.com/royalheritagesarees', '+919876500001', 1, 1),
('Shree Balaji Sweets & Bakery', 'Special Desi Ghee Dodha & Kaju Katli Gift Boxes', 'Fresh festive sweets prepared in 100% pure cow ghee. Special corporate and wedding gift hampers.', '₹650 / kg', 'Festive Offer', 'https://images.unsplash.com/photo-1599488615731-7e5c2823ff28?w=700&auto=format&fit=crop&q=80', 'https://facebook.com/shreebalajisweets', '+919876500002', 1, 2),
('Galaxy Electronics & Smart Hub', 'Latest 5G Smartphones & Smart 4K TVs', 'Special exchange bonus on older devices, 0% EMI with Bajaj Finserv, free delivery across the city.', 'From ₹12,999', 'Zero Downpayment', 'https://images.unsplash.com/photo-1550009158-9ebf69173e03?w=700&auto=format&fit=crop&q=80', 'https://facebook.com/galaxyelectronicsmzn', '+919876500003', 1, 3),
('Krishna Organic Dairy Farm', 'Farm Fresh A2 Gir Cow Milk & Vedic Bilona Ghee', 'Delivered every morning before 6:30 AM in hygienic glass bottles. 100% adulteration-free tested.', '₹75 / Litre', '1st Week Free Sample', 'https://images.unsplash.com/photo-1527153857715-3908f2ae5e81?w=700&auto=format&fit=crop&q=80', 'https://facebook.com/krishnaorganicfarm', '+919876500004', 1, 4)
ON CONFLICT DO NOTHING;

-- Instagram Reels (3-part aspect ratio 9:16 layout)
INSERT INTO public.instagram_reels (title, shop_name, reel_url, thumbnail_url, likes_count, slot_index) VALUES
('Behind the craft: Making traditional brass lamps', 'Murad Artisans Store', 'https://www.instagram.com/reel/mznlive_art1/', 'https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=500&auto=format&fit=crop&q=80', '3.8K', 0),
('Grand winter footwear collection unboxing', 'StepStyle Footwear Hub', 'https://www.instagram.com/reel/mznlive_shoes/', 'https://images.unsplash.com/photo-1549298916-b41d501d3772?w=500&auto=format&fit=crop&q=80', '6.2K', 1),
('Live tandoori kulcha & dal makhani preparation', 'Amritsari Zayka Restaurant', 'https://www.instagram.com/reel/mznlive_food/', 'https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=500&auto=format&fit=crop&q=80', '11.5K', 2),
('Handcrafted terracotta decor for living room', 'Mitti Mahal Emporium', 'https://www.instagram.com/reel/mznlive_decor/', 'https://images.unsplash.com/photo-1578749556568-bc2c40e68b61?w=500&auto=format&fit=crop&q=80', '4.1K', 0),
('Smart gadgets under ₹999 for students', 'NextGen Gadget Point', 'https://www.instagram.com/reel/mznlive_tech/', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&auto=format&fit=crop&q=80', '8.9K', 1),
('Designer bridal lehengas on display', 'Vogue Fashion Boutique', 'https://www.instagram.com/reel/mznlive_bridal/', 'https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?w=500&auto=format&fit=crop&q=80', '15.4K', 2)
ON CONFLICT DO NOTHING;

-- Marketplace Shops
INSERT INTO public.marketplace_shops (name, category, address, phone, whatsapp, instagram_link, facebook_link, rating, image_url, featured_offer) VALUES
('Royal Heritage Silk Sarees', 'Fashion & Clothing', '14 Court Road, Near Clock Tower', '+919876500001', '+919876500001', 'https://instagram.com/royalheritage', 'https://facebook.com/royalheritagesarees', 4.9, 'https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=600&auto=format&fit=crop&q=80', 'Flat 25% discount for Mznlive app users'),
('Shree Balaji Sweets & Bakery', 'Food & Sweets', 'Opposite G.P.O, Main Market', '+919876500002', '+919876500002', 'https://instagram.com/shreebalajisweets', 'https://facebook.com/shreebalajisweets', 4.8, 'https://images.unsplash.com/photo-1599488615731-7e5c2823ff28?w=600&auto=format&fit=crop&q=80', 'Free sample box on orders above ₹800'),
('Galaxy Electronics & Smart Hub', 'Electronics & Mobiles', 'Shop 22, Nehru Market Complex', '+919876500003', '+919876500003', 'https://instagram.com/galaxyelectronics', 'https://facebook.com/galaxyelectronicsmzn', 4.7, 'https://images.unsplash.com/photo-1550009158-9ebf69173e03?w=600&auto=format&fit=crop&q=80', 'Free tempered glass & cover with all phones'),
('Amritsari Zayka Restaurant', 'Restaurants & Cafes', 'Civil Lines, Near Subhash Chowk', '+919876500005', '+919876500005', 'https://instagram.com/amritsarizayka', 'https://facebook.com/amritsarizayka', 4.9, 'https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=600&auto=format&fit=crop&q=80', 'Complimentary sweet lassi on family dining'),
('Murad Artisans Store', 'Home Decor & Handicrafts', 'Purani Tehsil Market', '+919876500006', '+919876500006', 'https://instagram.com/muradartisans', 'https://facebook.com/muradartisans', 4.6, 'https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=600&auto=format&fit=crop&q=80', '15% off on hand-carved wooden items')
ON CONFLICT DO NOTHING;

-- Initial Chat Messages for Interactive Shop Support
INSERT INTO public.chat_messages (shop_id, shop_name, sender, message) VALUES
(1, 'Royal Heritage Silk Sarees', 'shop', 'Namaste! Welcome to Royal Heritage Silk Sarees on Mznlive. How can we assist you with our bridal collections today?'),
(2, 'Shree Balaji Sweets & Bakery', 'shop', 'Hello! Looking for fresh desi ghee sweets or custom gift hampers? Let us know your requirements!')
ON CONFLICT DO NOTHING;

-- Promotion Plans (Promotional packages with Buy now buttons and full specs)
INSERT INTO public.promotion_plans (id, title, duration_text, duration_days, price_inr, price_display, tagline, description, badge, is_popular, is_best_value, is_flash_deal, is_custom, features, graphic_type, order_num) VALUES
(
    'plan_3_days',
    'Micro Test Boost',
    '( 3 days )',
    3,
    11,
    'INR 11 only',
    'Super Flash Trial Deal',
    'Perfect introductory test drive to showcase your shop products on Mznlive feed with live buyer traffic.',
    '⚡ FLASH DEAL',
    false,
    false,
    true,
    false,
    '["1 High-visibility sponsored sliding advert", "Direct WhatsApp click-to-chat button", "Local city feed broadcast for 72 hours", "Instant listing in Mznlive Marketplace"]'::jsonb,
    'flash',
    1
),
(
    'plan_7_days',
    'Weekly Flash Sprint',
    '( 7 Days )',
    7,
    399,
    'INR 399 only',
    'Rapid Footfall Catalyst',
    'Targeted 7-day high impact promotion to drive weekend shoppers and immediate inquiries directly to your store.',
    '🔥 POPULAR WEEKLY',
    false,
    false,
    false,
    false,
    '["Top rotation in sliding sponsorship banner", "1 Instagram Reel promotion slot (9:16 layout)", "Direct call & WhatsApp enquiry button", "Featured banner in Marketplace directory", "Push highlight during peak evening hours"]'::jsonb,
    'fire',
    2
),
(
    'plan_15_days',
    'Bi-Weekly Growth Blast',
    '( 15 Days )',
    15,
    599,
    'INR 599 only',
    'Steady Customer Magnet',
    'Two weeks of continuous merchant visibility across live news and advert carousels with verified seller badge.',
    '🚀 VALUE ACCELERATOR',
    false,
    false,
    false,
    false,
    '["Guaranteed 1,500+ daily banner impressions", "2 Instagram Reels rotating in 9:16 showcase", "Verified Local Merchant Blue Badge", "Direct Facebook page & Instagram link", "Priority customer lead routing on WhatsApp"]'::jsonb,
    'rocket',
    3
),
(
    'plan_30_days',
    'Monthly Pro Dominance',
    '( 30 Days )',
    30,
    899,
    'INR 899 only',
    'Most Loved by Local Merchants',
    'Complete monthly dominance in local search, news broadcasts, and sliding banner rotation across the city.',
    '⭐ MOST POPULAR',
    true,
    false,
    false,
    false,
    '["Prime top spot in sliding adverts banner", "3 Instagram Reels featured in looping grid", "Full digital storefront in Marketplace tab", "Verified Gold Merchant Badge", "Direct lead capture & dedicated chat support", "Weekly performance summary report"]'::jsonb,
    'star',
    4
),
(
    'plan_3_months',
    'Quarterly Market Leader',
    '( 3 Months )',
    90,
    1299,
    'INR 1299 only',
    'Seasonal Sales Multiplier',
    '90 days of sustained presence across festival seasons, summer sales, and city-wide events at just ₹433/mo.',
    '💎 HIGH ROI',
    false,
    false,
    false,
    false,
    '["Uninterrupted 90-day carousel ad placement", "Unlimited rotation of promotional reels", "Live news ticker sponsor branding tag", "Exclusive festive festival campaign boost", "Dedicated merchant manager support", "Free graphic design support for banners"]'::jsonb,
    'diamond',
    5
),
(
    'plan_6_months',
    'Half-Year Super Saver',
    '( 6 Months )',
    180,
    1599,
    'INR 1599 only',
    'Maximum Savings & Brand Equity',
    'Long-term local brand authority for top retailers, doctors, coaching centers, and manufacturers.',
    '🏆 BEST VALUE',
    false,
    true,
    false,
    false,
    '["Unbeatable value at just ₹266 per month!", "Permanent Gold Featured Partner listing", "Top position in Marketplace directory", "5 Custom Instagram Reels production assistance", "Special live news stream banner mentions", "Direct CRM lead forwarding & WhatsApp VIP desk"]'::jsonb,
    'trophy',
    6
),
(
    'plan_1_year',
    'Annual VIP City Partner',
    '( 1 Year )',
    365,
    2599,
    'INR 2599 only',
    'The Ultimate Brand Domination',
    'Year-round brand ownership on Mznlive. Become the household name in your category across the entire region.',
    '👑 VIP ANNUAL',
    false,
    false,
    false,
    false,
    '["365 Days complete presence across all tabs", "Permanent VIP crown badge on storefront", "Priority ad slots during breaking live news", "Unlimited Instagram reels & ad banner updates", "Quarterly video promotion showcase", "Dedicated 24/7 business account manager"]'::jsonb,
    'crown',
    7
),
(
    'plan_custom',
    'Customized Plans',
    'Customized Plans also available',
    0,
    0,
    'Tailored Quote',
    'Tailored for Brands & Multi-Outlets',
    'Need custom durations, multiple showroom branches, custom video ads, or omni-channel campaigns? We craft it for you.',
    '🛠️ CUSTOM SOLUTIONS',
    false,
    false,
    false,
    true,
    '["Custom duration (10 days, 45 days, 2 years)", "Multi-branch & franchise coverage", "Professional video shoot & scriptwriting", "Interactive survey & contest campaigns", "Custom analytics & dedicated growth advisor"]'::jsonb,
    'custom',
    8
)
ON CONFLICT (id) DO UPDATE SET
    title = EXCLUDED.title,
    duration_text = EXCLUDED.duration_text,
    duration_days = EXCLUDED.duration_days,
    price_inr = EXCLUDED.price_inr,
    price_display = EXCLUDED.price_display,
    tagline = EXCLUDED.tagline,
    description = EXCLUDED.description,
    badge = EXCLUDED.badge,
    is_popular = EXCLUDED.is_popular,
    is_best_value = EXCLUDED.is_best_value,
    is_flash_deal = EXCLUDED.is_flash_deal,
    is_custom = EXCLUDED.is_custom,
    features = EXCLUDED.features,
    graphic_type = EXCLUDED.graphic_type,
    order_num = EXCLUDED.order_num;

-- ========================================================================
-- 10. Plan Purchases & Merchant Orders (Buy Plan Screen Transactions)
-- ========================================================================
CREATE TABLE IF NOT EXISTS public.plan_purchases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number TEXT NOT NULL UNIQUE,
    customer_name TEXT NOT NULL,
    shop_name TEXT NOT NULL,
    address TEXT NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    mobile_number TEXT NOT NULL,
    plan_id TEXT NOT NULL,
    plan_title TEXT NOT NULL,
    plan_duration TEXT NOT NULL,
    amount_inr NUMERIC NOT NULL,
    payment_method TEXT DEFAULT 'UPI_PHONEPE',
    payment_status TEXT DEFAULT 'completed',
    transaction_ref TEXT,
    whatsapp_notified BOOLEAN DEFAULT true,
    merchant_vpa TEXT DEFAULT 'tarunsharma@phonepe',
    merchant_name TEXT DEFAULT 'TARUN SHARMA SO PSHARMA',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Enable RLS and public permissions
ALTER TABLE public.plan_purchases ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Allow public read plan_purchases" ON public.plan_purchases FOR SELECT USING (true);
CREATE POLICY "Allow public insert plan_purchases" ON public.plan_purchases FOR INSERT WITH CHECK (true);

-- Index for fast lookup by mobile and order number
CREATE INDEX IF NOT EXISTS idx_plan_purchases_mobile ON public.plan_purchases(mobile_number);
CREATE INDEX IF NOT EXISTS idx_plan_purchases_created_at ON public.plan_purchases(created_at DESC);

-- Seed SQL table data for initial plan purchases
INSERT INTO public.plan_purchases (
    order_number,
    customer_name,
    shop_name,
    address,
    latitude,
    longitude,
    mobile_number,
    plan_id,
    plan_title,
    plan_duration,
    amount_inr,
    payment_method,
    payment_status,
    transaction_ref,
    whatsapp_notified,
    created_at
) VALUES
(
    'MZN-2026-78412',
    'Rajesh Kumar Sharma',
    'Sharma Sweets & Bakers',
    'Shiv Chowk, Main Roorkee Road, Muzaffarnagar',
    29.4727,
    77.7085,
    '+91 98370 12345',
    'plan_30_days',
    'Monthly Growth Pro',
    '( 30 Days )',
    899,
    'UPI_PHONEPE',
    'completed',
    'UPI/324109827101/PhonePe',
    true,
    NOW() - INTERVAL '2 hours'
),
(
    'MZN-2026-89104',
    'Vipin Aggarwal',
    'Aggarwal Silk & Sarees',
    'Bhagatsingh Road, Cloth Market, Muzaffarnagar',
    29.4715,
    77.7021,
    '+91 94122 54321',
    'plan_7_days',
    'Weekly Launchpad',
    '( 7 Days )',
    399,
    'UPI_PHONEPE',
    'completed',
    'UPI/324188204912/PhonePe',
    true,
    NOW() - INTERVAL '6 hours'
),
(
    'MZN-2026-92147',
    'Amit Tyagi',
    'Tyagi Auto Spares & Service',
    'Near Company Bagh, Meerut Road, Muzaffarnagar',
    29.4650,
    77.7120,
    '+91 97580 98765',
    'plan_3_days',
    'Micro Test Boost',
    '( 3 days )',
    11,
    'UPI_PHONEPE',
    'completed',
    'UPI/324201948210/PhonePe',
    true,
    NOW() - INTERVAL '1 day'
),
(
    'MZN-2026-99520',
    'Pradeep Singhal',
    'Singhal Electronics & Mobiles',
    'Court Road, Near District Hospital, Muzaffarnagar',
    29.4780,
    77.7055,
    '+91 98971 45678',
    'plan_3_months',
    'Quarterly Festive Champion',
    '( 3 Months)',
    1299,
    'UPI_PHONEPE',
    'completed',
    'UPI/324219481023/PhonePe',
    true,
    NOW() - INTERVAL '3 days'
)
ON CONFLICT (order_number) DO NOTHING;

-- ========================================================================
-- 10. Users Table (Registered Users & Shop Owners Database)
-- Stores account profile, role ('USER' or 'SHOP_OWNER'), and contact data
-- ========================================================================
CREATE TABLE IF NOT EXISTS public.users (
    id BIGSERIAL PRIMARY KEY,
    role TEXT NOT NULL DEFAULT 'USER', -- 'USER' or 'SHOP_OWNER'
    name TEXT NOT NULL,
    business_name TEXT DEFAULT '',
    category TEXT DEFAULT '',
    address TEXT DEFAULT '',
    mobile TEXT NOT NULL UNIQUE,
    whatsapp TEXT DEFAULT '',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Allow public read users" ON public.users FOR SELECT USING (true);
CREATE POLICY "Allow public insert users" ON public.users FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow public update users" ON public.users FOR UPDATE USING (true);

-- Seed Initial Users
INSERT INTO public.users (
    role, name, business_name, category, address, mobile, whatsapp
) VALUES
(
    'SHOP_OWNER',
    'Rajesh Kumar Sharma',
    'Sharma Sweets & Bakers',
    'Food, Sweets & Bakery (मिठाई और बेकरी)',
    'Shiv Chowk, Main Roorkee Road, Muzaffarnagar',
    '+91 98370 12345',
    '+91 98370 12345'
),
(
    'SHOP_OWNER',
    'Vipin Aggarwal',
    'Aggarwal Silk & Sarees',
    'Fashion & Clothing (कपड़े और फैशन)',
    'Bhagatsingh Road, Cloth Market, Muzaffarnagar',
    '+91 94122 54321',
    '+91 94122 54321'
),
(
    'USER',
    'Pooja Verma',
    '',
    '',
    'New Mandi, Circular Road, Muzaffarnagar',
    '+91 91234 56789',
    '+91 91234 56789'
)
ON CONFLICT (mobile) DO NOTHING;

-- ========================================================================
-- 11. Shops Table (Local Businesses & Verified Shops Directory)
-- ========================================================================
CREATE TABLE IF NOT EXISTS public.shops (
    id BIGSERIAL PRIMARY KEY,
    owner_mobile TEXT NOT NULL,
    owner_name TEXT NOT NULL,
    shop_name TEXT NOT NULL,
    category TEXT NOT NULL,
    address TEXT NOT NULL,
    phone TEXT NOT NULL,
    whatsapp TEXT NOT NULL,
    description TEXT DEFAULT '',
    rating NUMERIC(3, 2) DEFAULT 4.8,
    image_url TEXT DEFAULT '',
    banner_url TEXT DEFAULT '',
    featured_offer TEXT DEFAULT '',
    is_verified BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.shops ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Allow public read shops" ON public.shops FOR SELECT USING (true);
CREATE POLICY "Allow public insert shops" ON public.shops FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow public update shops" ON public.shops FOR UPDATE USING (true);

-- Seed Initial Shops
INSERT INTO public.shops (
    owner_mobile, owner_name, shop_name, category, address, phone, whatsapp, description, rating, image_url, featured_offer
) VALUES
(
    '+91 98370 12345',
    'Rajesh Kumar Sharma',
    'Sharma Sweets & Bakers',
    'Food, Sweets & Bakery (मिठाई और बेकरी)',
    'Shiv Chowk, Main Roorkee Road, Muzaffarnagar',
    '+91 98370 12345',
    '+91 98370 12345',
    'Famous pure desi ghee jalebi, kaju katli, fresh paneer, and delicious birthday cakes since 1984 in Muzaffarnagar.',
    4.9,
    'https://images.unsplash.com/photo-1599785209707-a456fc1337bb?w=800&auto=format&fit=crop&q=80',
    'Festival Special: 15% OFF on Kaju Katli & Fresh Rasgulla boxes!'
),
(
    '+91 94122 54321',
    'Vipin Aggarwal',
    'Aggarwal Silk & Sarees',
    'Fashion & Clothing (कपड़े और फैशन)',
    'Bhagatsingh Road, Cloth Market, Muzaffarnagar',
    '+91 94122 54321',
    '+91 94122 54321',
    'Muzaffarnagar premier ethnic fashion destination with authentic Banarasi, bridal lehengas, and designer suits.',
    4.8,
    'https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=800&auto=format&fit=crop&q=80',
    'Wedding Collection: Flat 20% OFF on Designer Bridal Sarees!'
);

-- ========================================================================
-- 12. Products Table (Shop Products Catalog)
-- Listed by Shop Owners with title, price, description, and images
-- ========================================================================
CREATE TABLE IF NOT EXISTS public.products (
    id BIGSERIAL PRIMARY KEY,
    shop_id BIGINT DEFAULT 0,
    shop_name TEXT NOT NULL,
    title TEXT NOT NULL,
    title_hi TEXT DEFAULT '',
    category TEXT NOT NULL,
    description TEXT DEFAULT '',
    price_inr INT NOT NULL,
    original_mrp_inr INT NOT NULL,
    image_url TEXT DEFAULT '',
    phone TEXT DEFAULT '',
    whatsapp TEXT DEFAULT '',
    is_featured BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Allow public read products" ON public.products FOR SELECT USING (true);
CREATE POLICY "Allow public insert products" ON public.products FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow public update products" ON public.products FOR UPDATE USING (true);
CREATE POLICY "Allow public delete products" ON public.products FOR DELETE USING (true);

-- Seed Initial Products
INSERT INTO public.products (
    shop_name, title, title_hi, category, description, price_inr, original_mrp_inr, image_url, phone, whatsapp, is_featured
) VALUES
(
    'Sharma Sweets & Bakers',
    'Pure Desi Ghee Kaju Katli (500g)',
    'शुद्ध देसी घी काजू कतली (500 ग्राम)',
    'Food, Sweets & Bakery',
    'Made with 100% premium Goan cashews and rich organic silver vark. Melt in the mouth goodness prepared fresh every morning.',
    480,
    600,
    'https://images.unsplash.com/photo-1599785209707-a456fc1337bb?w=800&auto=format&fit=crop&q=80',
    '+91 98370 12345',
    '+91 98370 12345',
    true
),
(
    'Sharma Sweets & Bakers',
    'Hot Crispy Desi Ghee Jalebi (1 Kg)',
    'गर्मा-गर्म कुरकुरी देसी घी जलेबी (1 किलो)',
    'Food, Sweets & Bakery',
    'The iconic morning breakfast of Muzaffarnagar. Crisp saffron spirals soaked in rose-infused sugar syrup.',
    320,
    400,
    'https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=800&auto=format&fit=crop&q=80',
    '+91 98370 12345',
    '+91 98370 12345',
    true
),
(
    'Aggarwal Silk & Sarees',
    'Royal Banarasi Silk Saree with Zari',
    'शाही बनारसी सिल्क साड़ी जरी बॉर्डर सहित',
    'Fashion & Clothing',
    'Handcrafted heavy golden zari work on pure crimson red silk. Includes matching unstitched blouse piece.',
    2499,
    4999,
    'https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=800&auto=format&fit=crop&q=80',
    '+91 94122 54321',
    '+91 94122 54321',
    true
);

-- ========================================================================
-- 13. Chat Messages Table (In-App Messenger & SMS / WhatsApp Logs)
-- Logs communication between customers and shop owners
-- ========================================================================
CREATE TABLE IF NOT EXISTS public.chat_messages (
    id BIGSERIAL PRIMARY KEY,
    shop_id BIGINT DEFAULT 0,
    shop_name TEXT NOT NULL,
    customer_phone TEXT DEFAULT '',
    sender TEXT NOT NULL, -- 'user' or 'shop'
    message TEXT NOT NULL,
    channel TEXT DEFAULT 'APP', -- 'APP', 'SMS', 'WHATSAPP'
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.chat_messages ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Allow public read chat_messages" ON public.chat_messages FOR SELECT USING (true);
CREATE POLICY "Allow public insert chat_messages" ON public.chat_messages FOR INSERT WITH CHECK (true);


