import { useEffect, useState } from 'react';

import { listTasks } from './api';
import { STATUS_LABELS, Task } from './types';

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
              </tr>
            </thead>
            <tbody className="govuk-table__body">
              {tasks.map(task => (
                <tr className="govuk-table__row" key={task.id}>
                  <td className="govuk-table__cell">{task.title}</td>
                  <td className="govuk-table__cell">{task.description}</td>
                  <td className="govuk-table__cell">{formatDate(task.dueDate)}</td>
                  <td className="govuk-table__cell">{STATUS_LABELS[task.status]}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </main>
    </>
  );
}
