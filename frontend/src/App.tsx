import { useEffect, useState } from 'react';

import { deleteTask, listTasks, updateTaskStatus } from './api';
import { TaskForm } from './TaskForm';
import { STATUS_LABELS, Task, TaskStatus } from './types';

const byDueDate = (a: Task, b: Task) => a.dueDate.localeCompare(b.dueDate);

const formatDate = (value: string) =>
  new Date(value).toLocaleString('en-GB', { dateStyle: 'medium', timeStyle: 'short' });

export function App() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [error, setError] = useState<string>();

  useEffect(() => {
    listTasks().then(
      loaded => setTasks(loaded.sort(byDueDate)),
      () => setError('Could not load tasks...')
    );
  }, []);

  async function changeStatus(id: number, status: TaskStatus) {
    try {
      const updated = await updateTaskStatus(id, status);
      setTasks(tasks.map(task => (task.id === id ? updated : task)));
    } catch {
      setError('Could not update the task.');
    }
  }

  async function remove(task: Task) {
    if (!window.confirm(`Delete "${task.title}"?`)) {
      return;
    }
    try {
      await deleteTask(task.id);
      setTasks(tasks.filter(t => t.id !== task.id));
    } catch {
      setError('Could not delete the task.');
    }
  }

  return (
    <>
      <header className="govuk-header">
        <div className="govuk-header__container govuk-width-container">
          <span className="govuk-header__link govuk-!-font-size-24 govuk-!-font-weight-bold">
            Caseworker task manager
          </span>
        </div>
      </header>

      <main className="govuk-width-container govuk-main-wrapper">
        <h1 className="govuk-heading-xl">Tasks</h1>

        {error && (
          <div className="govuk-error-summary" role="alert">
            <p className="govuk-body govuk-!-margin-bottom-0">{error}</p>
          </div>
        )}

        {tasks.length === 0 ? (
          <p className="govuk-body">No tasks yet.</p>
        ) : (
          <table className="govuk-table">
            <thead className="govuk-table__head">
              <tr className="govuk-table__row">
                <th className="govuk-table__header">Title</th>
                <th className="govuk-table__header">Description</th>
                <th className="govuk-table__header">Due</th>
                <th className="govuk-table__header">Status</th>
                <th className="govuk-table__header" />
              </tr>
            </thead>
            <tbody className="govuk-table__body">
              {tasks.map(task => (
                <tr className="govuk-table__row" key={task.id}>
                  <td className="govuk-table__cell">{task.title}</td>
                  <td className="govuk-table__cell">{task.description}</td>
                  <td className="govuk-table__cell">{formatDate(task.dueDate)}</td>
                  <td className="govuk-table__cell">
                    <select
                      className="govuk-select"
                      aria-label={`Status of ${task.title}`}
                      value={task.status}
                      onChange={e => changeStatus(task.id, e.target.value as TaskStatus)}
                    >
                      {Object.values(TaskStatus).map(status => (
                        <option key={status} value={status}>
                          {STATUS_LABELS[status]}
                        </option>
                      ))}
                    </select>
                  </td>
                  <td className="govuk-table__cell">
                    <button
                      type="button"
                      className="govuk-button govuk-button--warning govuk-!-margin-bottom-0"
                      onClick={() => remove(task)}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        <TaskForm onCreated={task => setTasks([...tasks, task].sort(byDueDate))} />
      </main>
    </>
  );
}
