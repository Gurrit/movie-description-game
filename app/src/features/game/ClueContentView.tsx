import type { Clue } from '../../api/types';

export const CLUE_LABEL: Record<string, string> = {
  METADATA: 'Details',
  WRITER: 'Writer',
  STUDIO: 'Studio',
  DIRECTOR: 'Director',
  CAST: 'Top billing',
  TAGLINE: 'Tagline',
  PLOT: 'Plot',
};

export const clueLabel = (clue: Clue) => CLUE_LABEL[clue.type] ?? 'Clue';

/** Renders the content of a revealed clue. Unknown clue types fall back to whatever text they carry. */
export function ClueContentView({ clue }: { clue: Clue }) {
  const content = clue.content;
  if (!content) return null;

  switch (clue.type) {
    case 'METADATA':
      return (
        <div className="space-y-2">
          <div className="flex flex-wrap gap-2">
            {content.genres?.map((genre) => (
              <span
                key={genre}
                className="rounded-full bg-slate-200 px-2.5 py-0.5 text-sm font-medium dark:bg-slate-800"
              >
                {genre}
              </span>
            ))}
          </div>
          <p className="text-sm text-slate-600 dark:text-slate-300">
            {[
              content.decade != null && `${content.decade}s`,
              content.runtimeMinutes != null && `${content.runtimeMinutes} min`,
            ]
              .filter(Boolean)
              .join(' · ')}
          </p>
        </div>
      );
    case 'WRITER':
    case 'STUDIO':
    case 'DIRECTOR':
    case 'CAST':
      return <p>{content.names?.join(', ')}</p>;
    default:
      return content.text ? <p>{content.text}</p> : null;
  }
}
