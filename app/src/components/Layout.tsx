import { NavLink, Outlet } from 'react-router-dom';
import { isTokenPersistent } from '../features/player/token';
import { ClapperIcon } from './ClapperIcon';

const link = ({ isActive }: { isActive: boolean }) =>
  `rounded-md px-3 py-2 text-sm font-bold ${
    isActive
      ? 'bg-slate-900 text-white dark:bg-white dark:text-slate-900'
      : 'text-slate-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800'
  }`;

export function Layout() {
  return (
    <div className="mx-auto flex min-h-dvh max-w-xl flex-col px-4">
      <header className="flex items-center justify-between gap-3 border-b border-slate-200 py-3 dark:border-slate-800">
        <h1 className="flex min-w-0 items-center gap-2 text-base leading-tight font-extrabold tracking-tight sm:text-lg">
          <ClapperIcon className="size-6 shrink-0" />
          Movie Description Game
        </h1>
        <nav aria-label="Main" className="flex shrink-0 gap-1">
          <NavLink to="/" end className={link}>
            Play
          </NavLink>
          <NavLink to="/stats" className={link}>
            Stats
          </NavLink>
        </nav>
      </header>
      {!isTokenPersistent() && (
        <p
          role="note"
          className="mt-4 rounded-lg bg-amber-100 p-3 text-sm text-amber-900 dark:bg-amber-950 dark:text-amber-100"
        >
          Your browser blocks local storage, so your progress is lost when you close this page.
        </p>
      )}
      <main className="flex-1 py-5">
        <Outlet />
      </main>
    </div>
  );
}
