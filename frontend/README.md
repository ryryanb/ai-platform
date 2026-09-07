# AI Platform Frontend

React + TypeScript + Vite frontend for the AI Platform.

The frontend provides the web interface for the AI Platform's Spring Boot backend. It provides the conversational user interface, conversation navigation, message rendering, Markdown support, syntax highlighting, and progressive display of streaming AI responses.

The frontend is maintained as a separate application within the same repository.

For the overall system architecture, backend, database, Ollama, API, and complete application setup, see the [root README](../README.md).

---

## Overview

The frontend provides:

* AI chat interface
* Conversation list and selection
* Conversation history
* User and assistant message rendering
* Markdown rendering
* Syntax highlighting for code blocks
* Streaming AI responses
* Backend API communication
* Responsive chat interface

The frontend does **not** contain:

* LLM integration
* Ollama integration
* Conversation persistence
* PostgreSQL access
* Conversation context management

Those responsibilities belong to the Spring Boot backend.

The frontend communicates with the backend through HTTP APIs.

---

## Technology Stack

| Technology               | Purpose                                 |
| ------------------------ | --------------------------------------- |
| React 18                 | UI framework                            |
| TypeScript 5             | Type-safe frontend development          |
| Vite 4                   | Development server and production build |
| react-markdown           | Markdown rendering                      |
| react-syntax-highlighter | Syntax highlighting                     |
| Chart.js                 | Client-side charting                    |
| date-fns                 | Date formatting and manipulation        |
| Lodash                   | Utility functions                       |
| Fetch API                | Backend HTTP communication              |
| CSS                      | Application styling                     |

Dependencies are defined in `package.json` and locked through `package-lock.json`.

---

## Frontend Architecture

The frontend acts as a client of the Spring Boot backend.

```text
┌───────────────────────────────────────────────┐
│                 React Frontend                │
│                                               │
│  ┌────────────────┐   ┌────────────────────┐ │
│  │ Conversation   │   │    Chat Window     │ │
│  │ List           │   │                    │ │
│  │                │   │ Messages           │ │
│  │ Conversations  │   │ Markdown           │ │
│  │                │   │ Code Highlighting  │ │
│  └────────────────┘   │ Message Input      │ │
│                       └────────────────────┘ │
└───────────────────────┬───────────────────────┘
                        │
                        │ HTTP / Streaming
                        ▼
               Spring Boot Backend
```

The frontend maintains a clear API boundary with the backend. UI components are responsible for presentation and interaction, while backend communication is isolated in the frontend API layer.

---

## Project Structure

```text
frontend/
├── src/
│   ├── components/
│   │   ├── ChatWindow
│   │   ├── ConversationList
│   │   ├── MessageList
│   │   ├── Message
│   │   ├── MessageInput
│   │   └── ...
│   │
│   ├── services/
│   │   └── chatApi.ts
│   │
│   ├── types/
│   │   └── ...
│   │
│   ├── App.tsx
│   └── main.tsx
│
├── package.json
├── package-lock.json
├── tsconfig.json
├── vite.config.ts
└── README.md
```

---

## Components

The frontend separates the user interface into focused React components.

### ChatWindow

Coordinates the main chat interface, including:

* Current conversation
* Messages
* Message input
* Loading state
* Streaming state

### ConversationList

Displays available conversations and allows the user to:

* Select an existing conversation
* Create a new conversation

### MessageList

Renders the messages belonging to the selected conversation.

### Message

Represents an individual user or assistant message.

User and assistant messages are visually differentiated.

### MessageInput

Provides the chat input interface.

The input supports:

* Multiline text
* Sending messages
* Enter-to-send behavior
* Shift+Enter for a new line
* Disabled/loading states

---

## Markdown Rendering

Assistant responses are rendered as Markdown rather than displaying raw Markdown syntax.

Supported content includes:

* Headings
* Paragraphs
* Bold and italic text
* Ordered lists
* Unordered lists
* Blockquotes
* Inline code
* Fenced code blocks

Code blocks are rendered using `react-syntax-highlighter`.

Common language identifiers include:

```text
java
javascript
typescript
python
sql
json
bash
xml
yaml
```

Code blocks without a language identifier are rendered as plain code.

This allows AI-generated technical responses to remain readable while preserving source-code formatting.

---

## Streaming Responses

The backend supports streaming AI responses, and the frontend progressively updates the assistant message as response chunks arrive.

The frontend streaming flow is:

```text
Spring Boot Streaming Endpoint
             │
             │ response chunks
             ▼
       Fetch API
             │
             ▼
     React Streaming Client
             │
             ▼
   Accumulated Response
             │
             ▼
      Markdown Renderer
             │
             ▼
     Formatted Message
```

Incoming chunks are accumulated into a single assistant message rather than being displayed as separate messages.

This allows the user to see the response while the AI model is still generating it.

---

## Backend API Integration

Backend communication is isolated in:

```text
src/services/chatApi.ts
```

React components use the API abstraction rather than constructing backend requests throughout the UI.

The general interaction is:

```text
React Component
       │
       ▼
   chatApi.ts
       │
       ▼
Spring Boot REST API
```

The backend API provides functionality for:

* Creating conversations
* Listing conversations
* Retrieving conversation history
* Sending messages
* Streaming AI responses

The backend remains the source of truth for conversation and message data.

The frontend does not duplicate persistence or LLM logic.

---

## Type Safety

TypeScript is used throughout the frontend.

Application data is represented using explicit TypeScript types.

For example:

```typescript
interface Conversation {
  id: string;
  title: string | null;
  createdAt: string;
  updatedAt: string;
}

interface Message {
  id: string;
  conversationId: string;
  role: 'USER' | 'ASSISTANT';
  content: string;
  createdAt: string;
}
```

TypeScript strict mode is enabled.

Avoid using `any` unless there is a specific technical reason.

Explicit types help maintain a clear contract between the backend API and the React application.

---

## Environment Configuration

The frontend uses the Vite environment variable:

```text
VITE_API_BASE_URL
```

to determine the backend API URL.

For local development:

```text
VITE_API_BASE_URL=http://localhost:8080
```

If a custom API URL is required, create:

```text
frontend/.env
```

with:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Restart the Vite development server after changing environment variables.

Environment files containing secrets must not be committed.

The frontend build itself does not require PostgreSQL, Ollama, or a running Spring Boot backend.

Those services are required when running the complete application.

---

## Prerequisites

For frontend development:

* Node.js
* npm

For running the complete AI Platform:

* Node.js
* npm
* Spring Boot backend
* PostgreSQL
* Ollama

The frontend does not run the LLM. LLM processing is handled by the backend through Ollama.

For complete application setup, see the [root README](../README.md).

---

## Running the Frontend

From the repository root:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the Vite development server:

```bash
npm run dev
```

Vite will display the local development URL in the terminal.

Open that URL in a browser.

The frontend communicates with the Spring Boot API using the configured `VITE_API_BASE_URL`.

---

## Development Workflow

To run the complete application locally:

### 1. Start PostgreSQL and Ollama

Start the infrastructure required by the backend.

See the [root README](../README.md) for the complete setup.

### 2. Start Spring Boot

From the repository root:

```bash
./mvnw spring-boot:run
```

The backend normally runs at:

```text
http://localhost:8080
```

### 3. Start the frontend

In another terminal:

```bash
cd frontend
npm run dev
```

### 4. Open the application

Use the URL provided by Vite.

The frontend connects to the backend through:

```text
VITE_API_BASE_URL
```

---

## Production Build

The frontend uses Vite to create an optimized production build.

From the `frontend/` directory:

```bash
npm run build
```

The generated assets are written to:

```text
frontend/dist/
```

The production build does not require:

* Spring Boot
* PostgreSQL
* Ollama
* A running backend

A successful build confirms that the frontend source can be compiled and bundled successfully.

To preview the production build locally:

```bash
npm run preview
```

---

## Build Validation

The frontend provides a dedicated validation command:

```bash
npm run validate
```

The validation command currently runs the production build:

```text
npm run validate
       │
       ▼
npm run build
       │
       ▼
Vite production build
       │
       ▼
frontend/dist/
```

The `validate` script is defined in `package.json` as:

```json
"validate": "npm run build"
```

This gives local development and CI a single command for validating that the frontend can be built successfully.

---

## Reproducible Builds

The frontend uses `package-lock.json` to provide reproducible dependency installation.

For a clean installation:

```bash
npm ci
```

Then validate the frontend:

```bash
npm run validate
```

The recommended clean validation process is:

```text
Clean Repository
       │
       ▼
package.json
       │
       ▼
package-lock.json
       │
       ▼
npm ci
       │
       ▼
Installed Dependencies
       │
       ▼
npm run validate
       │
       ▼
Vite Production Build
       │
       ▼
frontend/dist/
```

`npm ci` should be preferred for CI and clean-environment validation because it installs dependencies from the committed lockfile.

---

## Continuous Integration

The repository uses a unified GitHub Actions workflow:

```text
.github/workflows/ci.yml
```

The workflow validates both the backend and frontend.

It runs on:

* Pushes to `main`
* Pushes to `development`
* Pull requests targeting `main`
* Pull requests targeting `development`

The frontend portion of CI performs:

```text
GitHub Actions
      │
      ▼
Node.js 20
      │
      ▼
npm ci
      │
      ▼
npm run validate
      │
      ▼
Vite Production Build
```

The frontend build does not require PostgreSQL, Ollama, or a running Spring Boot backend.

This allows frontend compilation failures to be detected automatically as part of the repository CI pipeline.

---

## Repository Hygiene

Generated and environment-specific files should not be committed.

In particular:

```text
node_modules/
dist/
.env
.env.local
.env.*.local
```

The following file should remain committed:

```text
package-lock.json
```

The lockfile allows developers and CI to reproduce the frontend dependency installation.

Build output should not be committed:

```text
frontend/dist/
```

---

## Engineering Characteristics

The frontend is designed around several engineering principles:

1. **Component-based UI architecture**
2. **Type-safe development with TypeScript**
3. **Clear separation between UI and API communication**
4. **Backend-owned persistence and business logic**
5. **Progressive rendering of streaming AI responses**
6. **Structured Markdown rendering**
7. **Syntax highlighting for generated source code**
8. **Reproducible dependency installation**
9. **Automated production-build validation**
10. **Minimal frontend complexity**

The frontend is intentionally focused on being a clear, maintainable client for the AI backend.

---

## Future Frontend Improvements

Potential future improvements include:

* Conversation renaming
* Conversation deletion
* Message regeneration
* Stop/cancel streaming
* Copy code button
* Copy message button
* Dark/light theme
* Frontend automated tests
* Error boundaries
* Authentication UI
* Multi-model selection UI
* RAG interface
* Production deployment

These features should be introduced incrementally as the application evolves.

---

## License

MIT License — see the root project license for details.
