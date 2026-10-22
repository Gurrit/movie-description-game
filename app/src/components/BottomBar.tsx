import type { ReactNode } from 'react';

/**
 * Pins its content to the bottom of the screen on phones, where the thumb is, and lets it flow
 * inline from the `sm` breakpoint up. Sections using it add `pb-28 sm:pb-0` so nothing hides behind it.
 */
export function BottomBar({ children }: { children: ReactNode }) {
  return (
    <div className="fixed inset-x-0 bottom-0 z-20 border-t border-slate-200 bg-white/95 backdrop-blur sm:static sm:border-0 sm:bg-transparent sm:backdrop-blur-none dark:border-slate-800 dark:bg-slate-950/95 sm:dark:bg-transparent">
      <div className="mx-auto max-w-xl px-4 pt-3 pb-[max(0.75rem,env(safe-area-inset-bottom))] sm:p-0">
        {children}
      </div>
    </div>
  );
}
