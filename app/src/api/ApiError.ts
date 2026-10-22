import type { Problem } from './types';

/** An error response of the API (problem+json) or a failure to reach it. */
export class ApiError extends Error {
  readonly status: number;
  readonly code: string;

  constructor(status: number, code: string, message: string) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
  }

  static fromProblem(status: number, problem: Partial<Problem> | undefined): ApiError {
    return new ApiError(
      status,
      problem?.code ?? 'UNKNOWN',
      problem?.detail ?? problem?.title ?? `Request failed (${status})`,
    );
  }
}
