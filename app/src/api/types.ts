import type { components } from './schema';

type Schemas = components['schemas'];

export type Game = Schemas['Game'];
export type Round = Schemas['Round'];
export type Clue = Schemas['Clue'];
export type ClueContent = Schemas['ClueContent'];
export type PersonClue = Schemas['PersonClue'];
export type Answer = Schemas['Answer'];
export type GuessResult = Schemas['GuessResult'];
export type MovieSuggestion = Schemas['MovieSuggestion'];
export type Stats = Schemas['Stats'];
export type Problem = Schemas['Problem'];

export const isEnded = (round: Round) => round.status === 'SOLVED' || round.status === 'FAILED';
