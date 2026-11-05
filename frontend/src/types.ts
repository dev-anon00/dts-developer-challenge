export enum TaskStatus {
  TODO = 'TODO',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
}

// Matches APIs TaskResponse
export type Task = {
  id: number;
  title: string;
  description?: string | null;
  status: TaskStatus;
  dueDate: string;
};

// Matches APIs TaskRequest
export type TaskInput = {
  title: string;
  description?: string;
  status: TaskStatus;
  dueDate: string;
};

export const STATUS_LABELS: Record<TaskStatus, string> = {
  [TaskStatus.TODO]: 'To do',
  [TaskStatus.IN_PROGRESS]: 'In progress',
  [TaskStatus.COMPLETED]: 'Completed',
};
