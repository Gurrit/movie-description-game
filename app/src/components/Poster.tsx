const IMAGE_BASE: string =
  import.meta.env.VITE_TMDB_IMAGE_BASE ?? 'https://image.tmdb.org/t/p/w342';

export function Poster({ path, title }: { path?: string | null; title: string }) {
  if (!path) {
    return (
      <div className="flex aspect-[2/3] w-32 items-center justify-center rounded-lg bg-slate-200 p-2 text-center text-sm text-slate-500 dark:bg-slate-800">
        No poster
      </div>
    );
  }
  return (
    <img
      src={`${IMAGE_BASE}${path}`}
      alt={`Poster of ${title}`}
      loading="lazy"
      className="aspect-[2/3] w-32 rounded-lg object-cover shadow"
    />
  );
}
