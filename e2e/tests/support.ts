import { expect, type Page } from '@playwright/test';
import pg from 'pg';

export const ROUNDS = 3;
export const ATTEMPTS = 2;
export const MOVIES = 200;

const pool = new pg.Pool({ connectionString: 'postgres://moviegame:moviegame@localhost:5433/moviegame' });

/**
 * The right answers of today's game, in round order. The API deliberately never reveals them, so the tests
 * read them from the database. Empty until the first player starts the game (which generates it).
 */
export async function todaysAnswers(): Promise<number[]> {
  const { rows } = await pool.query(
    `SELECT gm.movie_id FROM game_movie gm JOIN daily_game dg ON dg.game_id = gm.game_id
      WHERE dg.date = (now() AT TIME ZONE 'utc')::date ORDER BY gm.position`,
  );
  return rows.map((r) => r.movie_id as number);
}

export async function closeDb() {
  await pool.end();
}

export const titleOf = (id: number) => `Movie ${id} Title`;

/** Some movie that is certainly not the answer of any round of today's game. */
export function wrongIds(answers: number[], count: number): number[] {
  return Array.from({ length: MOVIES }, (_, i) => i + 1).filter((id) => !answers.includes(id)).slice(0, count);
}

export async function startGame(page: Page) {
  await page.goto('/');
  await page.getByRole('button', { name: /play today/i }).click();
  await expect(page.getByText(`Movie 1 of ${ROUNDS}`)).toBeVisible();
}

/** Types the movie's title into the guess box and picks it from the suggestions, like a player does. */
export async function guess(page: Page, id: number) {
  await page.getByRole('combobox').fill(titleOf(id));
  await page.getByRole('option', { name: new RegExp(`^${titleOf(id)}`) }).click();
}
