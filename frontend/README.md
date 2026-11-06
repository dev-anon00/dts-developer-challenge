# Task manager frontend

A single page React + TypeScript app (Vite) for creating, viewing, updating and deleting tasks. It uses the
[GOV.UK Frontend](https://frontend.design-system.service.gov.uk/) CSS for styling.

## Running

Requires Node 22 and Yarn via Corepack. Use Yarn, not npm!!

```
yarn install
yarn dev
```

The app runs on http://localhost:3100. Requests to `/api/*` are proxied to the backend via `http://localhost:4000`, to start the backend (see [`../backend/README.md`](../backend/README.md)).

## Scripts

| Script          | What it does                              |
| --------------- | ----------------------------------------- |
| `yarn dev`      | Start the dev server                      |
| `yarn build`    | Type-check and build to `dist/`           |
| `yarn preview`  | Serve the built app (with the same proxy) |
| `yarn test`     | Run the Vitest + Testing Library tests    |
| `yarn lint`     | ESLint and Prettier checks                |
| `yarn lint:fix` | Fix lint and formatting issues            |

## Structure

- `src/App.tsx`: the task table, with status changes and delete.
- `src/TaskForm.tsx`: the create form, with client-side validation and backend field errors.
- `src/api.ts`: `fetch` calls to the backend and `ApiError`.
- `src/types.ts`: types matching the API's request and response.

## Editor SDKs

`.yarn/sdks/` and `.vscode/` are auto-generated for Yarn Plug'n'Play. Can be regenerate by running
`yarn dlx @yarnpkg/sdks vscode`.
