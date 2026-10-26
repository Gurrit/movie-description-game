import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import type { Clue } from '../../api/types';
import { ClueContentView } from './ClueContentView';

const clue = (overrides: Partial<Clue>): Clue => ({
  id: 'x',
  type: 'PLOT',
  order: 1,
  revealed: true,
  content: { text: 'hello' },
  ...overrides,
});

describe('ClueContentView', () => {
  it('renders plain text clues', () => {
    render(<ClueContentView clue={clue({ content: { text: 'A ### story' } })} />);
    expect(screen.getByText('A ### story')).toBeInTheDocument();
  });

  it('renders metadata as genres, decade and runtime', () => {
    render(
      <ClueContentView
        clue={clue({
          type: 'METADATA',
          content: { genres: ['Drama', 'Crime'], decade: 1970, runtimeMinutes: 175 },
        })}
      />,
    );
    expect(screen.getByText('Drama')).toBeInTheDocument();
    expect(screen.getByText('Crime')).toBeInTheDocument();
    expect(screen.getByText('1970s · 175 min')).toBeInTheDocument();
  });

  it.each([
    ['WRITER', ['Mario Puzo'], 'Mario Puzo'],
    ['DIRECTOR', ['Francis Coppola'], 'Francis Coppola'],
    ['CAST', ['Al Pacino', 'James Caan', 'Robert Duvall'], 'Al Pacino, James Caan, Robert Duvall'],
  ] as const)('renders the names of a %s clue', (type, names, text) => {
    render(<ClueContentView clue={clue({ type, content: { names: [...names] } })} />);
    expect(screen.getByText(text)).toBeInTheDocument();
  });

  it('shows the text of an unknown clue type', () => {
    render(
      <ClueContentView
        clue={clue({ type: 'POSTER_CROP' as Clue['type'], content: { text: 'fallback' } })}
      />,
    );
    expect(screen.getByText('fallback')).toBeInTheDocument();
  });

  it('renders nothing for an unrevealed clue', () => {
    const { container } = render(<ClueContentView clue={clue({ content: undefined })} />);
    expect(container).toBeEmptyDOMElement();
  });
});
