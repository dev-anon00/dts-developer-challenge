import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, test, vi } from 'vitest';

import * as api from './api';
import { App } from './App';
import { Task, TaskStatus } from './types';

vi.mock('./api', async importOriginal => ({
  ...(await importOriginal<typeof import('./api')>()),
  listTasks: vi.fn(),
  createTask: vi.fn(),
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
    expect(within(screen.getByRole('table')).getByText('To do')).toBeInTheDocument();
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

  test('creates a task', async () => {
    const user = userEvent.setup();
    mocked.createTask.mockResolvedValue({ ...task, id: 2, title: 'New task' });
    render(<App />);

    await user.type(screen.getByLabelText('Title'), 'New task');
    await user.type(screen.getByLabelText('Due date and time'), '2026-12-02T09:30');
    await user.click(screen.getByRole('button', { name: 'Create task' }));

    expect(mocked.createTask).toHaveBeenCalledWith({
      title: 'New task',
      description: undefined,
      status: TaskStatus.TODO,
      dueDate: '2026-12-02T09:30',
    });
    expect(await screen.findByText('New task')).toBeInTheDocument();
  });

  test('validates the form before calling the backend', async () => {
    const user = userEvent.setup();
    render(<App />);

    await user.click(screen.getByRole('button', { name: 'Create task' }));

    expect(screen.getAllByText('Enter a title')).not.toHaveLength(0);
    expect(screen.getAllByText('Enter a due date and time')).not.toHaveLength(0);
    expect(mocked.createTask).not.toHaveBeenCalled();
  });
});
