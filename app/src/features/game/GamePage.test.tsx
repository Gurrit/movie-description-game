import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { describe, expect, it } from 'vitest';
import { createFakeBackend, MOVIES } from '../../test/fakeBackend';
import { renderApp } from '../../test/render';
import { server } from '../../test/server';

const [ALIEN, , HEAT] = MOVIES;

async function guess(user: ReturnType<typeof userEvent.setup>, search: string, title: RegExp) {
  await user.type(await screen.findByRole('combobox'), search);
  await user.click(await screen.findByRole('option', { name: title }));
}

describe('game page', () => {
  it('plays a whole game: start, reveal, wrong guess, solve, fail, summary', async () => {
    const backend = createFakeBackend({ answers: [ALIEN!, HEAT!], attempts: 2 });
    server.use(...backend.handlers);
    const user = userEvent.setup();
    renderApp();

    // Not started yet
    await user.click(await screen.findByRole('button', { name: /play today/i }));

    // Round 1: clues are hidden until revealed, and revealing costs nothing
    expect(await screen.findByText('Movie 1 of 2')).toBeInTheDocument();
    expect(screen.queryByText('Plot of 101')).not.toBeInTheDocument();
    expect(screen.getByText('2 attempts left')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: /reveal plot/i }));
    expect(await screen.findByText('Plot of 101')).toBeInTheDocument();
    expect(screen.getByText('2 attempts left')).toBeInTheDocument();

    // A wrong guess uses an attempt and is remembered
    await guess(user, 'heat', /^Heat/);
    expect(await screen.findByText('1 attempt left')).toBeInTheDocument();
    expect(
      within(screen.getByRole('list', { name: 'Your wrong guesses' })).getByText(/Heat \(1995\)/),
    ).toBeInTheDocument();

    // The right guess shows the result, with the answer
    await guess(user, 'alien', /^Alien\s*\(1979\)/);
    const result = await screen.findByRole('region', { name: 'Round result' });
    expect(within(result).getByText(/Correct! Attempt 2/)).toBeInTheDocument();
    expect(within(result).getByText('Alien')).toBeInTheDocument();
    expect(within(result).getByAltText('Poster of Alien')).toBeInTheDocument();

    // Round 2: run out of attempts
    await user.click(screen.getByRole('button', { name: 'Next movie' }));
    expect(await screen.findByText('Movie 2 of 2')).toBeInTheDocument();
    await guess(user, 'casa', /^Casablanca/);
    await screen.findByText('1 attempt left');
    await guess(user, 'aliens', /^Aliens/);
    const failed = await screen.findByRole('region', { name: 'Round result' });
    expect(within(failed).getByText('Out of attempts')).toBeInTheDocument();
    expect(within(failed).getByText('Heat')).toBeInTheDocument();

    // Summary
    await user.click(screen.getByRole('button', { name: 'See results' }));
    const summary = await screen.findByRole('region', { name: 'Game summary' });
    expect(within(summary).getByText('1 / 2')).toBeInTheDocument();
    expect(within(summary).getByText(/Solved:/)).toBeInTheDocument();
    expect(within(summary).getByText(/Failed:/)).toBeInTheDocument();
    expect(within(summary).getByText(/Next game in/)).toBeInTheDocument();
    expect(backend.calls.guess).toEqual([103, 101, 104, 102]);
  });

  it('resumes where the player left off after a reload, without replaying results', async () => {
    const backend = createFakeBackend({ answers: [ALIEN!, HEAT!], attempts: 2 });
    server.use(...backend.handlers);
    const user = userEvent.setup();
    const first = renderApp();
    await user.click(await screen.findByRole('button', { name: /play today/i }));
    await guess(user, 'alien', /^Alien\s*\(1979\)/);
    await screen.findByRole('region', { name: 'Round result' });
    first.unmount();

    renderApp(); // same token in localStorage

    expect(await screen.findByText('Movie 2 of 2')).toBeInTheDocument();
    expect(screen.queryByRole('region', { name: 'Round result' })).not.toBeInTheDocument();
    expect(backend.calls.createPlayer).toBe(1);
  });

  it('shows the summary directly when a finished game is reloaded', async () => {
    const backend = createFakeBackend({ answers: [ALIEN!], attempts: 1 });
    server.use(...backend.handlers);
    const user = userEvent.setup();
    const first = renderApp();
    await user.click(await screen.findByRole('button', { name: /play today/i }));
    await guess(user, 'alien', /^Alien\s*\(1979\)/);
    first.unmount();

    renderApp();

    expect(await screen.findByRole('region', { name: 'Game summary' })).toBeInTheDocument();
    expect(screen.queryByRole('combobox')).not.toBeInTheDocument();
  });

  it('shows an error with a retry when the game cannot be loaded', async () => {
    const backend = createFakeBackend();
    let failing = true;
    server.use(
      http.get('http://localhost:3000/api/game/today', () => {
        if (failing)
          return HttpResponse.json(
            { title: 'Boom', status: 400, code: 'BAD', detail: 'Bad request' },
            { status: 400 },
          );
        return undefined;
      }),
      ...backend.handlers,
    );
    const user = userEvent.setup();
    renderApp();

    expect(await screen.findByRole('alert')).toHaveTextContent('Bad request');
    failing = false;
    await user.click(screen.getByRole('button', { name: 'Try again' }));

    expect(await screen.findByRole('button', { name: /play today/i })).toBeInTheDocument();
  });

  it('shows a clear error when starting the game fails', async () => {
    const backend = createFakeBackend();
    server.use(
      http.post('http://localhost:3000/api/game/today', () =>
        HttpResponse.json(
          {
            title: 'Could not generate',
            status: 503,
            code: 'GENERATION_FAILED',
            detail: 'Could not generate today’s game',
          },
          { status: 503 },
        ),
      ),
      ...backend.handlers,
    );
    const user = userEvent.setup();
    renderApp();

    await user.click(await screen.findByRole('button', { name: /play today/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Could not generate today’s game');
  });
});

describe('stats page', () => {
  it('shows the three figures', async () => {
    server.use(...createFakeBackend().handlers);
    renderApp('/stats');

    expect(await screen.findByText('Games played')).toBeInTheDocument();
    const stats = screen.getByRole('region', { name: 'Your stats' });
    expect(within(stats).getByText('3').nextSibling).toHaveTextContent('Games played');
    expect(within(stats).getByText('15').nextSibling).toHaveTextContent('Movies solved');
  });
});
