import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import { ApiError } from './api/ApiError';
import './index.css';
import { router } from './routes';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // Client errors will not fix themselves; transient failures get two more tries
      retry: (count, error) => !(error instanceof ApiError && error.status < 500) && count < 2,
      refetchOnWindowFocus: false,
    },
    mutations: { retry: false },
  },
});

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router()} />
    </QueryClientProvider>
  </StrictMode>,
);
