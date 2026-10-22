import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { createFakeBackend } from '../../test/fakeBackend';
import { renderWithQuery } from '../../test/render';
import { server } from '../../test/server';
import { GuessInput } from './GuessInput';

function setup(guessed: number[] = []) {
  const backend = createFakeBackend();
  server.use(...backend.handlers);
  const onGuess = vi.fn();
  renderWithQuery(<GuessInput guessedIds={new Set(guessed)} onGuess={onGuess} />);
  return { backend, onGuess, user: userEvent.setup() };
}

describe('GuessInput', () => {
  it('does not search for fewer than two characters', async () => {
    const { backend, user } = setup();
    await user.type(screen.getByRole('combobox'), 'a');
    await new Promise((r) => setTimeout(r, 400));
    expect(backend.calls.search).toEqual([]);
    expect(screen.queryByRole('listbox')).not.toBeInTheDocument();
  });

  it('debounces typing into a single search', async () => {
    const { backend, user } = setup();
    await user.type(screen.getByRole('combobox'), 'alien');
    expect(await screen.findByRole('option', { name: /^Alien\s*\(1979\)/ })).toBeInTheDocument();
    expect(backend.calls.search).toEqual(['alien']);
  });

  it('reports the picked movie and clears the box', async () => {
    const { onGuess, user } = setup();
    const input = screen.getByRole('combobox');
    await user.type(input, 'alien');
    await user.click(await screen.findByRole('option', { name: /^Aliens/ }));
    expect(onGuess).toHaveBeenCalledWith({ tmdbId: 102, title: 'Aliens', year: 1986 });
    expect(input).toHaveValue('');
  });

  it('supports the keyboard: arrows move, enter picks', async () => {
    const { onGuess, user } = setup();
    await user.type(screen.getByRole('combobox'), 'alien');
    await screen.findAllByRole('option');
    await user.keyboard('{ArrowDown}{Enter}');
    expect(onGuess).toHaveBeenCalledWith(expect.objectContaining({ tmdbId: 102 }));
  });

  it('does not allow picking a movie that was already guessed', async () => {
    const { onGuess, user } = setup([101]);
    await user.type(screen.getByRole('combobox'), 'alien');
    const option = await screen.findByRole('option', { name: /^Alien\s*\(1979\)/ });
    expect(option).toHaveAttribute('aria-disabled', 'true');
    await user.click(option);
    expect(onGuess).not.toHaveBeenCalled();
  });

  it('says when nothing matches', async () => {
    const { user } = setup();
    await user.type(screen.getByRole('combobox'), 'zzzz');
    await waitFor(() => expect(screen.getByText('No matches')).toBeInTheDocument());
  });

  it('closes the list with escape', async () => {
    const { user } = setup();
    await user.type(screen.getByRole('combobox'), 'alien');
    await screen.findAllByRole('option');
    await user.keyboard('{Escape}');
    expect(screen.queryByRole('listbox')).not.toBeInTheDocument();
  });
});
