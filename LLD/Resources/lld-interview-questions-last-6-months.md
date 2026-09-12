# Recent LLD Interview Questions (approx. Mar–Sep 2026)

Compiled from **LeetCode Discuss**, **Reddit**, and **TeamBlind**.  
Window: ~last 6 months relative to **2026-09-12**.

**How to read this**
- **First-hand** = someone reported they were asked this in an interview.
- **Compilation** = a 2026 forum list aggregating many experiences (not a single interview).
- Blind is thin on *dated, named* first-hand LLD prompts in public search; prep threads dominate.

---

## A. First-hand questions (with source)

| # | Question | Company / role | Approx. when | Source |
|---|----------|----------------|--------------|--------|
| 1 | Design a **multi-floor Parking Lot** (class structure + implementation discussion) | Microsoft SDE II | Feb 2026 | [LeetCode Discuss](https://leetcode.com/discuss/post/7620569/microsoft-sde-ii-interview-experience-re-hqua/) |
| 2 | Design **Undo/Redo for a text editor** (configurable history limits; DS-heavy) | Microsoft SDE II | Mar 2026 | Same LeetCode post as #1 |
| 3 | Design a **Vending Machine** *or* **Parking Lot** (SOLID, State/Strategy) | Microsoft SDE 2 (L61/L62), India | 2026 | [LeetCode Discuss](https://leetcode.com/discuss/post/7769548/microsoft-interview-experience-sde-2-l61-xdc2/) |
| 4 | Design a **multi-level Parking Lot** (entry/exit, dedicated spots, levels) | Microsoft SDE2 | Mar 2026 | [Medium — Chanchal Rai](https://medium.com/@raichanchal00/microsoft-interview-experience-sde2-mar-26-248ffe379fd2) *(cross-posted experience; also implements LRU in same loop)* |
| 5 | Implement **LRU Cache** (HashMap + DLL); follow-up **LFU** | Microsoft SDE2 | Mar 2026 | Same Medium post as #4 |
| 6 | Design a **Post Office / mail distribution system** (workers, queues, concurrency) | Microsoft SDE-II (L61) | ~2026 (recent Reddit write-up) | [r/interviews](https://www.reddit.com/r/interviews/comments/1thw9qv/microsoft_sdeii_l61_selected_3_yoe_full_process/) |
| 7 | Design an **Elevator system** | Microsoft SDE-II (L61) | same loop as #6 | Same Reddit post as #6 |
| 8 | Pseudo-code **Google Docs** LLD (real-time collab + conflict handling) | Microsoft (AA / applied round) | same loop as #6 | Same Reddit post as #6 |
| 9 | Design a **Shipping Cost Calculator** (weight, distance, delivery type, region, extensible rules / Strategy) | Amazon SDE-2, India | recent (2026 Discuss post) | [LeetCode Discuss](https://leetcode.com/discuss/post/8312943/amazon-sde-2-india-interview-experience-a2s2q/) |
| 10 | Design an **Auction system** (classes + near-complete service code on Bluescape) | Amazon LLD | ~Jun 2026 (“last week” relative to Jun 16 post) | [r/LeetcodeDesi](https://www.reddit.com/r/LeetcodeDesi/comments/1u7b6tn/anyone_who_has_recently_appeared_for_the_lld/) |
| 11 | Design **Uber Eats pricing** (base prices, upgrades, taxes; working code expected) | Uber SDE-2 | recent Discuss post | [LeetCode Discuss](https://leetcode.com/discuss/post/7682158/uber-sde-2-interview-experience-offer-re-3u58/) |
| 12 | Design an **e-commerce cart** (UML + pseudocode; follow-up: multiple carts per user) | Swiggy SDE-1 (1–3 YOE) | recent | [r/LeetcodeDesi](https://www.reddit.com/r/LeetcodeDesi/comments/1rakedc/lld_interview_at_swiggy_for_sde113_year_of_exp/) |
| 13 | Design a **Wiki Management System** (requirements, schema, APIs) | PayTM SSE | Jan 2026 | [LeetCode Discuss](https://leetcode.com/discuss/post/7649720/paytm-interview-experience-sse-jan-2026-r44h2/) |
| 14 | Design **Ticketmaster**-style ticket booking (concurrency, indexing noted in reports) | PayPay India SDE 2 | Feb 2026 | [LeetCode Discuss](https://leetcode.com/discuss/post/7603671/paypay-sde-2-interview-experience-feb-20-uelh/) |
| 15 | **Ride dispatch / matching** style LLD (constraints, surge, events) — commenter first-hand | Uber SWE 2 Backend | recent | [r/leetcode](https://www.reddit.com/r/leetcode/comments/1tdomb9/uber_swe_2_backend_onsite_what_lld_questions/) |
| 16 | **Notification system for a stock broker** — friend’s first-hand | Uber SWE 2 Backend | recent | Same Reddit thread as #15 |
| 17 | Reported recently at Uber machine coding (commenter): **Parking Lot**, **Logger Rate Limiter**, **Truck Tracking System**, **Train Platform Scheduler** | Uber | recent (prep comments) | Same Reddit thread as #15 |

### Blind (public indexed)

| # | Question | Notes | Source |
|---|----------|-------|--------|
| 18 | **Parking Lot**, **Rate Limiter** cited as common Amazon LLD | Advice thread; original post is older, still surfaces in Blind search | [TeamBlind — Amazon SDE 2 LLD questions](https://www.teamblind.com/post/amazon-sde-2-lld-interview-questions-bok3uhlo) |
| 19 | **Design Amazon Locker** (LLD class design) | Axon loop description (also cites LC Design Underground System for coding) | [TeamBlind — Axon interview pattern](https://www.teamblind.com/post/axon-interview-pattern-xmejov4b) |
| 20 | Confirms Amazon India loops often include a **dedicated LLD round with coding** | No single named prompt in-thread | [TeamBlind — Amazon SDE3 L6 India](https://www.teamblind.com/post/amazon-sde3-l6-loop-starts-in-3-days-india-ie2fa4sq) |

---

## B. Reddit 2026 compilations (frequently reported; not one interview)

### B1. Top LLD questions asked in 2026  
Source: [r/LowLevelDesign — Top LLD Round Interview Questions Asked in 2026](https://www.reddit.com/r/LowLevelDesign/comments/1tn1264/top_low_level_design_round_interview_questions/) (May 2026)

1. Parking Lot  
2. In-Memory Cache / LRU / LFU (incl. TTL variants)  
3. Rate Limiter  
4. Movie Ticket Booking (BookMyShow)  
5. Expense Sharing (Splitwise)  
6. Elevator Management System  
7. Pub/Sub / Messaging Queue / Notification System  
8. HashMap  
9. Chess  
10. Meeting Scheduler / Calendar  
11. Food Delivery (Zomato / Uber Eats)  
12. File System  
13. Leaderboard  
14. Text Editor  
15. Library Management System  
16. Tic Tac Toe  
17. Search Autocomplete  
18. Snake and Ladder  
19. Snake Game (food + score)  
20. Hit Counter  

### B2. Amazon LLD questions (2026 compilation)  
Source: [r/LowLevelDesign — Amazon LLD Interview Questions asked in 2026](https://www.reddit.com/r/LowLevelDesign/comments/1ujfvhd/amazon_low_level_design_interview_questions_asked/)

1. Pizza Pricing System  
2. Unix `find` for file search  
3. Parking Lot  
4. LRU Cache with time constraint  
5. WhatsApp Read Receipts  
6. Backup System for file storage  
7. Digital Wellbeing / screen-time tracker  
8. Warehouse Locker Management  
9. Chess  
10. Splitwise-style expense sharing  
11. Restaurant ordering (Zomato/Swiggy/DoorDash)  
12. Stock broker platform (Zerodha/Groww style)  
13. Movie ticket booking  
14. Elevator  
Plus DSA-flavored: File System, Log Storage, LRU/LFU, Snake, Hit Counter, Tic Tac Toe, Autocomplete, Excel Sum Formula  

### B3. Uber LLD / Depth-in-Specialization (2026 compilation)  
Source: [r/LowLevelDesign — Uber LLD questions asked in 2026](https://www.reddit.com/r/LowLevelDesign/comments/1uhlo2f/uber_low_level_design_and_depth_in_specialization/)

1. Single-queue Pub/Sub  
2. File System (`cd` with `*`)  
3. Parking Lot (simple)  
4. Website customer visit tracking  
5. Splitwise-style expense sharing  
6. MCQ voting system (vote share)  
7. Uber Eats ads schedule dashboard  
8. Uber Eats pricing calculator  
9. Rate-limit circuit breaker  
10. Key-value store with O(1) insert/delete/get-first  
11. Meeting room reservation  
12. Movie ticket booking  
13. Fantasy-team leaderboard  
14. Train platform management  

### B4. Flipkart machine coding (recent rounds compilation)  
Source: [r/LowLevelDesign — Flipkart LLD from recent machine coding](https://www.reddit.com/r/LowLevelDesign/comments/1r3vll8/flipkart_low_level_design_interview_questions/)

1. Ecommerce billing & discounts  
2. Food order management (command-driven)  
3. Gym / fitness slot booking  
4. Peer-to-peer parcel delivery  
5. Payment wallet + transaction history  
6. Doctor appointment booking (Practo-like)  
7. Bug bounty program management  
8. Buy Now Pay Later  
9. Library Management System  
10. Customer loyalty program  
11. Delivery service  
12. Order & inventory management  
13. Dating app (“Gumble”)  

### B5. Amazon question bank scraped from LeetCode Discuss (prep list)  
Source: [LeetCode Discuss — Amazon HLD/LLD/DSA Questions](https://leetcode.com/discuss/post/6906753/amazon-hld-lld-dsa-questions-by-anonymou-sepk/)

LLD items called out: Vending Machine, Quick Commerce, Parking Lot, Amazon Locker, Splitwise, Hotel Reservation, Customer Reviews, Rate Limiter, IRCTC booking module, Seller Experience app.

---

## C. Gaps & caveats

1. **Blind**: public search rarely surfaces *dated first-hand* named LLD prompts from the last 6 months; most hits are “what should I prep?” threads. Login-walled replies may have more.
2. **LeetCode Discuss / Reddit**: full page fetches often hit Cloudflare / network blocks; titles and snippets above are from search indexes + one Medium mirror.
3. **“All” is impossible**: only public posts are visible; many NDA’d interviews never appear.
4. Compilations on r/LowLevelDesign / Codezym are useful frequency signals but are **not** proof every item was asked in the last 6 months.

---

## D. Highest-signal prep set (from first-hand + frequency)

If you only drill a short list from this haul:

1. Parking Lot (multi-floor)  
2. LRU / LFU Cache  
3. Rate Limiter  
4. Elevator  
5. Pricing / shipping / pizza-style rule engines (Strategy)  
6. Splitwise / expense sharing  
7. Movie / ticket booking concurrency  
8. Pub/Sub or notification service  
9. Uber Eats / food-order pricing  
10. Auction or locker / package systems (Amazon flavor)  

---

*Generated 2026-09-12 for Resource Aggregator research request.*
