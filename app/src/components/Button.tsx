import type { ButtonHTMLAttributes } from 'react';

type Props = ButtonHTMLAttributes<HTMLButtonElement> & { variant?: 'primary' | 'secondary' };

export function Button({ variant = 'primary', className = '', ...props }: Props) {
  const styles =
    variant === 'primary'
      ? 'bg-slate-900 text-white hover:bg-slate-700 dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200'
      : 'border-2 border-slate-300 bg-transparent hover:bg-slate-100 dark:border-slate-700 dark:hover:bg-slate-800';
  return (
    <button
      type="button"
      {...props}
      className={`min-h-12 rounded-lg px-5 py-2.5 text-base font-bold transition focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-sky-500 disabled:opacity-50 ${styles} ${className}`}
    />
  );
}
