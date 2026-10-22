import { createBrowserRouter } from 'react-router-dom';
import { Layout } from './components/Layout';
import { GamePage } from './features/game/GamePage';
import { StatsPage } from './features/stats/StatsPage';

export const routes = [
  {
    element: <Layout />,
    children: [
      { path: '/', element: <GamePage /> },
      { path: '/stats', element: <StatsPage /> },
    ],
  },
];

export const router = () => createBrowserRouter(routes);
