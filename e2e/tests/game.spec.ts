import { expect, test } from '@playwright/test';
import { ATTEMPTS, closeDb, guess, ROUNDS, startGame, titleOf, todaysAnswers, wrongIds } from './support';

test.afterAll(closeDb);

test('plays a whole game: reveal, wrong guess, solve, fail, summary, stats', async ({ page }) => {
  await startGame(page);
  const answers = await todaysAnswers();
  expect(answers).toHaveLength(ROUNDS);
  const wrong = wrongIds(answers, 3);

  // Round 1: clues start hidden; revealing is free
  await expect(page.getByText('A story about a man')).toBeHidden();
  await page.getByRole('button', { name: 'Reveal plot' }).click();
  await expect(page.getByText('A story about a man')).toBeVisible();
  await expect(page.getByText(`${ATTEMPTS} attempts left`)).toBeVisible();

  await guess(page, wrong[0]!);
  await expect(page.getByText('1 attempt left')).toBeVisible();
  await expect(page.getByRole('list', { name: 'Your wrong guesses' })).toContainText(titleOf(wrong[0]!));

  await guess(page, answers[0]!);
  const result = page.getByRole('region', { name: 'Round result' });
  await expect(result).toContainText('Correct! Attempt 2');
  await expect(result).toContainText(titleOf(answers[0]!));
  await expect(result.getByRole('img', { name: `Poster of ${titleOf(answers[0]!)}` })).toBeVisible();
  await page.getByRole('button', { name: 'Next movie' }).click();

  // Round 2: out of attempts
  await expect(page.getByText(`Movie 2 of ${ROUNDS}`)).toBeVisible();
  await guess(page, wrong[1]!);
  await expect(page.getByText('1 attempt left')).toBeVisible();
  await guess(page, wrong[2]!);
  await expect(page.getByRole('region', { name: 'Round result' })).toContainText('Out of attempts');
  await expect(page.getByRole('region', { name: 'Round result' })).toContainText(titleOf(answers[1]!));
  await page.getByRole('button', { name: 'Next movie' }).click();

  // Round 3: first try, then the summary
  await guess(page, answers[2]!);
  await expect(page.getByRole('region', { name: 'Round result' })).toContainText('Correct! First try');
  await page.getByRole('button', { name: 'See results' }).click();
  const summary = page.getByRole('region', { name: 'Game summary' });
  await expect(summary).toContainText(`2 / ${ROUNDS}`);
  for (const id of answers) await expect(summary).toContainText(titleOf(id));

  // A finished game stays finished after a reload
  await page.reload();
  await expect(page.getByRole('region', { name: 'Game summary' })).toBeVisible();
  await expect(page.getByRole('combobox')).toHaveCount(0);

  // The stats page counts it
  await page.getByRole('navigation', { name: 'Main' }).getByRole('link', { name: 'Stats' }).click();
  const stats = page.getByRole('region', { name: 'Your stats' });
  await expect(stats.getByText('Games played').locator('xpath=preceding-sibling::dd')).toHaveText('1');
  await expect(stats.getByText('Fully solved').locator('xpath=preceding-sibling::dd')).toHaveText('0');
  await expect(stats.getByText('Movies solved').locator('xpath=preceding-sibling::dd')).toHaveText('2');
});

test('resumes mid-game after a reload', async ({ page }) => {
  await startGame(page);
  const answers = await todaysAnswers();
  await page.getByRole('button', { name: 'Reveal tagline' }).click();
  await expect(page.getByText('Every journey ends somewhere')).toBeVisible();
  await guess(page, wrongIds(answers, 1)[0]!);
  await expect(page.getByText('1 attempt left')).toBeVisible();

  await page.reload();

  await expect(page.getByText(`Movie 1 of ${ROUNDS}`)).toBeVisible();
  await expect(page.getByText('Every journey ends somewhere')).toBeVisible();
  await expect(page.getByText('1 attempt left')).toBeVisible();
  await expect(page.getByRole('region', { name: 'Round result' })).toHaveCount(0);
});

test('every player gets the same movies', async ({ browser }) => {
  const players = await Promise.all([browser.newContext(), browser.newContext()]);
  const pages = await Promise.all(players.map((c) => c.newPage()));
  for (const page of pages) await startGame(page);
  const answers = await todaysAnswers();

  for (const page of pages) {
    await guess(page, answers[0]!);
    await expect(page.getByRole('region', { name: 'Round result' })).toContainText(titleOf(answers[0]!));
  }
  await Promise.all(players.map((c) => c.close()));
});

test('a player the server no longer knows gets a fresh start instead of an error', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByRole('button', { name: /play today/i })).toBeVisible();
  await page.evaluate(() => localStorage.setItem('mdg.token', 'a-token-the-server-has-never-seen'));

  await page.reload();

  await expect(page.getByRole('button', { name: /play today/i })).toBeVisible();
  await expect(page.getByRole('alert')).toHaveCount(0);
  const token = await page.evaluate(() => localStorage.getItem('mdg.token'));
  expect(token).not.toBe('a-token-the-server-has-never-seen');
});

test('the proxy exposes health but not the operational endpoint', async ({ request }) => {
  expect((await request.get('/api/healthz/readiness')).status()).toBe(200);
  expect((await request.post('/api/internal/generate', { headers: { 'X-Internal-Token': 'e2e-internal' } })).status()).toBe(404);
});

test('the guess box is reachable at the bottom of a phone screen', async ({ page, isMobile }) => {
  test.skip(!isMobile, 'phone layout only');
  await startGame(page);
  const box = await page.getByRole('combobox').boundingBox();
  const viewport = page.viewportSize()!;
  expect(box).not.toBeNull();
  expect(box!.y + box!.height).toBeGreaterThan(viewport.height - 100);
  expect(box!.y + box!.height).toBeLessThanOrEqual(viewport.height);

  // Opening the suggestions must not push the box off screen or hide it
  await page.getByRole('combobox').fill('Movie 1');
  await expect(page.getByRole('option').first()).toBeVisible();
  await expect(page.getByRole('combobox')).toBeInViewport();
});

test('the SPA fallback serves deep links', async ({ page }) => {
  await page.goto('/stats');
  await expect(page.getByRole('region', { name: 'Your stats' })).toBeVisible();
});
