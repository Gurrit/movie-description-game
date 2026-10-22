/** A clapperboard, drawn inline so it looks the same on every device (no emoji font needed). */
export function ClapperIcon({ className = 'size-6' }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden
      className={className}
    >
      <path d="M4 11h16v9a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1z" />
      <path d="m4 11-.9-3.6a1 1 0 0 1 .7-1.2l15.5-4.2a1 1 0 0 1 1.2.7L21 7" />
      <path d="m7.6 6.1 2.2 3.9M12.2 4.9l2.2 3.9M16.8 3.6l2.2 3.9" />
    </svg>
  );
}
