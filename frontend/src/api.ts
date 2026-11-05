import { Task, TaskInput, TaskStatus } from './types';

// Proxied to the backend by Vite (see vite.config.ts)
const BASE_URL = '/api/tasks';

export class ApiError extends Error {
  constructor(
    public status: number,
    public errors?: Record<string, string>
  ) {
    super(`Request failed with status ${status}`);
  }
}

async function request<T>(path: string, method = 'GET', body?: unknown): Promise<T> {
  const response = await fetch(BASE_URL + path, {
    method,
    headers: body ? { 'Content-Type': 'application/json' } : undefined,
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!response.ok) {
    const problem = await response.json().catch(() => undefined);
    throw new ApiError(response.status, problem?.errors);
  }
  return response.status === 204 ? (undefined as T) : response.json();
}

export const listTasks = () => request<Task[]>('');
export const createTask = (input: TaskInput) => request<Task>('', 'POST', input);
export const updateTaskStatus = (id: number, status: TaskStatus) => request<Task>(`/${id}/status`, 'PATCH', { status });
export const deleteTask = (id: number) => request<void>(`/${id}`, 'DELETE');
