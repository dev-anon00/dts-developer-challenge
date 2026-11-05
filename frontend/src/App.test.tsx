import { render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, test, vi } from 'vitest';

import * as api from './api';
import { App } from './App';
import { Task, TaskStatus } from './types';

vi.mock('./api', async importOriginal => ({
  ...(await importOriginal<typeof import('./api')>()),
  listTasks: vi.fn(),
}));

const mocked = vi.mocked(api);

const task: Task = {
  id: 1,
  title: 'Review case',
  description: 'Check the bundle',
  status: TaskStatus.TODO,
  dueDate: '2026-12-01T10:00:00',
};

describe('App', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    mocked.listTasks.mockResolvedValue([task]);
  });

  test('lists tasks', async () => {
    render(<App />);

    expect(await screen.findByText('Review case')).toBeInTheDocument();
    expect(screen.getByText('Check the bundle')).toBeInTheDocument();
    expect(screen.getByText('1 Dec 2026, 10:00')).toBeInTheDocument();
    expect(screen.getByText('To do')).toBeInTheDocument();
  });

  test('shows a message when there are no tasks', async () => {
    mocked.listTasks.mockResolvedValue([]);
    render(<App />);

    expect(await screen.findByText('No tasks yet.')).toBeInTheDocument();
  });

  test('shows a message when the backend is down', async () => {
    mocked.listTasks.mockRejectedValue(new Error('down'));
    render(<App />);

    expect(await screen.findByText('Could not load tasks...')).toBeInTheDocument();
  });
});
