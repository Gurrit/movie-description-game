import type { Clue, PersonClue } from '../../api/types';

const ROLE_LABEL: Record<PersonClue['role'], string> = {
  CAST: 'Cast',
  DIRECTOR: 'Director',
  WRITER: 'Writer',
};

export const CLUE_LABEL: Record<string, string> = {
  PLOT: 'Plot',
  TAGLINE: 'Tagline',
  METADATA: 'Details',
  PEOPLE: 'People',
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
              content.releaseYear,
              content.runtimeMinutes != null && `${content.runtimeMinutes} min`,
            ]
              .filter(Boolean)
              .join(' · ')}
          </p>
        </div>
      );
    case 'PEOPLE':
      return (
        <dl className="space-y-1 text-sm">
          {(['CAST', 'DIRECTOR', 'WRITER'] as const).map((role) => {
            const people = content.people?.filter((p) => p.role === role) ?? [];
            if (people.length === 0) return null;
            return (
              <div key={role} className="flex gap-2">
                <dt className="w-16 shrink-0 text-slate-500">{ROLE_LABEL[role]}</dt>
                <dd>
                  {people
                    .map((p) => (p.character ? `${p.name} (as ${p.character})` : p.name))
                    .join(', ')}
                </dd>
              </div>
            );
          })}
        </dl>
      );
    default:
      return content.text ? <p>{content.text}</p> : null;
  }
}
