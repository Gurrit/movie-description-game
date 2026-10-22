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

  it('renders metadata as genres, year and runtime', () => {
    render(
      <ClueContentView
        clue={clue({
          type: 'METADATA',
          content: { genres: ['Drama', 'Crime'], releaseYear: 1972, runtimeMinutes: 175 },
        })}
      />,
    );
    expect(screen.getByText('Drama')).toBeInTheDocument();
    expect(screen.getByText('Crime')).toBeInTheDocument();
    expect(screen.getByText('1972 · 175 min')).toBeInTheDocument();
  });

  it('renders people grouped by role', () => {
    render(
      <ClueContentView
        clue={clue({
          type: 'PEOPLE',
          content: {
            people: [
              { role: 'CAST', name: 'Al Pacino', character: 'Michael' },
              { role: 'CAST', name: 'James Caan' },
              { role: 'DIRECTOR', name: 'Francis Coppola' },
              { role: 'WRITER', name: 'Mario Puzo' },
            ],
          },
        })}
      />,
    );
    expect(screen.getByText('Al Pacino (as Michael), James Caan')).toBeInTheDocument();
    expect(screen.getByText('Francis Coppola')).toBeInTheDocument();
    expect(screen.getByText('Mario Puzo')).toBeInTheDocument();
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
