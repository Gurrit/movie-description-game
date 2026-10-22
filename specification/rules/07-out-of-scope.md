# Out of Scope (for now) and Open Items

## Explicitly out of scope
- A scoring system (points, penalties for clue use). Only raw facts are recorded.
- Leaderboards, shared scoreboards, accounts, nicknames, social sharing.
- Playing past days, restarting, or extra games on the same day.
- Streaks and attempt-distribution stats (data is kept, UI is not).
- Account/token recovery across browsers.
- Localization (UI and TMDB content are in English).

## Designed-for (kept easy)
- Alternative reveal policies, such as one clue per attempt, or a reveal costing an attempt ([02](02-clues.md#generic-clue-model)).
- Per-item reveals within PEOPLE.
- New clue types (e.g. poster crop, keywords, production company).
- Scoring derived from stored attempts and reveals.

## Open items to settle during implementation
1. Whether the guess response auto-advances rounds or a separate `next` call is used ([05](05-api.md)).
2. Final defaults for `minVoteCount` and `maxPoolPages`, after trying the real TMDB pool size.
3. Whether stats use counters or aggregation.
4. Whether `PLOT` redaction needs smarter handling for franchise titles (e.g. "Star Wars" appearing as a common noun).
