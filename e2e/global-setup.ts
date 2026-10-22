import { execFileSync } from 'node:child_process';

const compose = (...args: string[]) =>
  execFileSync('docker', ['compose', '-p', 'mdg-e2e', ...args], { cwd: import.meta.dirname, stdio: 'inherit' });

/** Starts the whole stack (building the images) and waits until every service is healthy. */
export default function globalSetup() {
  compose('up', '-d', '--build', '--wait', '--wait-timeout', '300');
  // Set E2E_KEEP=1 to leave the stack running, for debugging or faster reruns
  return () => {
    if (!process.env.E2E_KEEP) compose('down', '-v');
  };
}
