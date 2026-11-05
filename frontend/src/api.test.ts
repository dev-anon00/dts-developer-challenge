import { afterEach, describe, expect, test, vi } from 'vitest';

import { ApiError, createTask, deleteTask, listTasks, updateTaskStatus } from './api';
import { TaskStatus } from './types';

const fetchMock = vi.fn<typeof fetch>();
vi.stubGlobal('fetch', fetchMock);

const respond = (status: number, body?: unknown) =>
  fetchMock.mockResolvedValueOnce(new Response(body === undefined ? null : JSON.stringify(body), { status }));

describe('api', () => {
  afterEach(() => fetchMock.mockReset());

  test('listTasks gets /api/tasks', async () => {
    respond(200, []);

    await expect(listTasks()).resolves.toEqual([]);
    expect(fetchMock).toHaveBeenCalledWith('/api/tasks', expect.objectContaining({ method: 'GET' }));
  });

  test('createTask posts JSON', async () => {
    const input = { title: 'Review case', status: TaskStatus.TODO, dueDate: '2026-12-01T09:00' };
    respond(201, { id: 1, ...input });

    await createTask(input);

    expect(fetchMock).toHaveBeenCalledWith('/api/tasks', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(input),
    });
  });

  test('updateTaskStatus and deleteTask call the right URLs', async () => {
    respond(200, {});
    respond(204);

    await updateTaskStatus(1, TaskStatus.COMPLETED);
    await deleteTask(1);

    expect(fetchMock.mock.calls.map(([url, init]) => [url, init?.method])).toEqual([
      ['/api/tasks/1/status', 'PATCH'],
      ['/api/tasks/1', 'DELETE'],
    ]);
  });

  test('errors become an ApiError with the backend field errors', async () => {
    respond(400, { errors: { title: 'must not be blank' } });

    await expect(listTasks()).rejects.toEqual(new ApiError(400, { title: 'must not be blank' }));
  });
});
