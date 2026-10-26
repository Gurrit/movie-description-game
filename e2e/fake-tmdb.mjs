// A fake TMDB for the end-to-end stack: 200 usable movies ("Movie 1 Title" ... "Movie 200 Title"),
// the three endpoints the backend uses, and placeholder poster images. No network, fully deterministic.
import { createServer } from 'node:http';

const MOVIES = 200;
const PER_PAGE = 20;
const PAGES = MOVIES / PER_PAGE;

const title = (id) => `Movie ${id} Title`;
const year = (id) => 1990 + (id % 30);

const listItem = (id) => ({ id, title: title(id), release_date: `${year(id)}-06-01` });

const details = (id) => ({
  id,
  title: title(id),
  original_title: title(id),
  overview: 'A story about a man who wanders far and finds a home somewhere new.',
  tagline: 'Every journey ends somewhere',
  release_date: `${year(id)}-06-01`,
  runtime: 100 + (id % 40),
  poster_path: `/poster${id}.svg`,
  genres: [{ id: 1, name: 'Drama' }, { id: 2, name: 'Adventure' }],
  production_companies: [{ id: 1, name: `Studio ${id} Pictures` }, { id: 2, name: 'Second Company' }],
  credits: {
    cast: [
      { name: `Alice Actor${id}`, character: 'Hero', order: 0 },
      { name: `Bob Actor${id}`, character: 'Sidekick', order: 1 },
      { name: `Carol Actor${id}`, character: 'Villain', order: 2 },
      { name: `Dan Extra${id}`, character: 'Bystander', order: 3 },
    ],
    crew: [
      { name: `Dora Director${id}`, job: 'Director' },
      { name: `Wally Writer${id}`, job: 'Screenplay' },
    ],
  },
});

const send = (res, status, body, type = 'application/json') => {
  res.writeHead(status, { 'Content-Type': type, 'Access-Control-Allow-Origin': '*' });
  res.end(typeof body === 'string' ? body : JSON.stringify(body));
};

createServer((req, res) => {
  const url = new URL(req.url, 'http://fake');
  const path = url.pathname;
  let m;

  if (path === '/health') return send(res, 200, 'ok', 'text/plain');

  if (path === '/3/discover/movie') {
    const page = Math.min(Math.max(Number(url.searchParams.get('page')) || 1, 1), PAGES);
    const first = (page - 1) * PER_PAGE + 1;
    const results = Array.from({ length: PER_PAGE }, (_, i) => listItem(first + i));
    return send(res, 200, { page, total_pages: PAGES, total_results: MOVIES, results });
  }

  if ((m = path.match(/^\/3\/movie\/(\d+)$/))) {
    const id = Number(m[1]);
    return id >= 1 && id <= MOVIES ? send(res, 200, details(id)) : send(res, 404, { success: false });
  }

  if (path === '/3/search/movie') {
    const q = (url.searchParams.get('query') ?? '').toLowerCase();
    const results = Array.from({ length: MOVIES }, (_, i) => i + 1)
      .filter((id) => title(id).toLowerCase().includes(q))
      .slice(0, 20)
      .map(listItem);
    return send(res, 200, { page: 1, total_pages: 1, results });
  }

  if (path.startsWith('/images/')) {
    const svg =
      '<svg xmlns="http://www.w3.org/2000/svg" width="200" height="300"><rect width="200" height="300" fill="#334155"/>' +
      '<text x="100" y="155" fill="white" font-size="20" text-anchor="middle">poster</text></svg>';
    return send(res, 200, svg, 'image/svg+xml');
  }

  send(res, 404, { success: false, status_message: 'not found' });
}).listen(3000, () => console.log('fake TMDB listening on :3000'));
