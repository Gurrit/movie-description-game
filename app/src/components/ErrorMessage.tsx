import { ApiError } from '../api/ApiError';

export function errorText(error: unknown): string {
  if (error instanceof ApiError) return error.message;
  return 'Something went wrong. Please check your connection and try again.';
}

export function ErrorMessage({ error, onRetry }: { error: unknown; onRetry?: () => void }) {
  return (
    <div
      role="alert"
      className="rounded-lg border border-red-300 bg-red-50 p-4 text-red-900 dark:border-red-900 dark:bg-red-950 dark:text-red-100"
    >
      <p>{errorText(error)}</p>
      {onRetry && (
        <button type="button" onClick={onRetry} className="mt-2 font-medium underline">
          Try again
        </button>
      )}
    </div>
  );
}
