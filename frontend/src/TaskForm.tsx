import { FormEvent, useState } from 'react';

import { ApiError, createTask } from './api';
import { STATUS_LABELS, Task, TaskStatus } from './types';

const EMPTY = { title: '', description: '', status: TaskStatus.TODO, dueDate: '' };

function validate(form: typeof EMPTY) {
  const errors: Record<string, string> = {};
  if (!form.title.trim()) {
    errors.title = 'Enter a title';
  } else if (form.title.length > 255) {
    errors.title = 'Title must be 255 characters or fewer';
  }
  if (form.description.length > 3000) {
    errors.description = 'Description must be 3000 characters or fewer';
  }
  if (!form.dueDate) {
    errors.dueDate = 'Enter a due date and time';
  }
  return errors;
}

export function TaskForm({ onCreated }: { onCreated: (task: Task) => void }) {
  const [form, setForm] = useState(EMPTY);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const field = (name: keyof typeof EMPTY) => ({
    id: name,
    name,
    value: form[name],
    onChange: (e: { target: { value: string } }) => setForm({ ...form, [name]: e.target.value }),
  });

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    const formErrors = validate(form);
    setErrors(formErrors);
    if (Object.keys(formErrors).length) {
      return;
    }
    try {
      onCreated(await createTask({ ...form, title: form.title.trim(), description: form.description || undefined }));
      setForm(EMPTY);
    } catch (err) {
      setErrors(err instanceof ApiError && err.errors ? err.errors : { form: 'Could not create the task. Try again.' });
    }
  }

  const groupClass = (name: string) => `govuk-form-group${errors[name] ? ' govuk-form-group--error' : ''}`;
  const errorMessage = (name: string) => errors[name] && <p className="govuk-error-message">{errors[name]}</p>;

  return (
    <form onSubmit={onSubmit} noValidate>
      <h2 className="govuk-heading-l">Create a task</h2>

      {Object.keys(errors).length > 0 && (
        <div className="govuk-error-summary" role="alert">
          <h2 className="govuk-error-summary__title">There is a problem</h2>
          <ul className="govuk-list govuk-error-summary__list">
            {Object.entries(errors).map(([name, message]) => (
              <li key={name}>
                <a href={`#${name}`}>{message}</a>
              </li>
            ))}
          </ul>
        </div>
      )}

      <div className={groupClass('title')}>
        <label className="govuk-label" htmlFor="title">
          Title
        </label>
        {errorMessage('title')}
        <input className="govuk-input" type="text" {...field('title')} />
      </div>

      <div className={groupClass('description')}>
        <label className="govuk-label" htmlFor="description">
          Description (optional)
        </label>
        {errorMessage('description')}
        <textarea className="govuk-textarea" rows={3} {...field('description')} />
      </div>

      <div className="govuk-form-group">
        <label className="govuk-label" htmlFor="status">
          Status
        </label>
        <select className="govuk-select" {...field('status')}>
          {Object.values(TaskStatus).map(status => (
            <option key={status} value={status}>
              {STATUS_LABELS[status]}
            </option>
          ))}
        </select>
      </div>

      <div className={groupClass('dueDate')}>
        <label className="govuk-label" htmlFor="dueDate">
          Due date and time
        </label>
        {errorMessage('dueDate')}
        <input className="govuk-input govuk-input--width-20" type="datetime-local" {...field('dueDate')} />
      </div>

      <button type="submit" className="govuk-button">
        Create task
      </button>
    </form>
  );
}
