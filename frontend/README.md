# AI Platform Frontend

React + TypeScript + Vite frontend for the AI Platform.

The frontend provides the web interface for the AI Platform's Spring Boot backend, which integrates Spring AI, Ollama, PostgreSQL, conversation persistence, and streaming LLM responses.

The frontend is maintained as a separate application within the same repository.

## Overview

The frontend provides:

* AI chat interface
* Conversation list and conversation selection
* Conversation history
* User and assistant message rendering
* Markdown rendering for assistant responses
* Syntax highlighting for code blocks
* Streaming LLM responses
* Backend API communication
* Responsive chat interface

The Spring Boot backend remains responsible for:

* REST API endpoints
* LLM integration
* Spring AI
* Ollama
* Conversation persistence
* PostgreSQL
* Conversation context
* Streaming LLM responses

The frontend does not contain LLM or persistence logic. It communicates with the backend through HTTP APIs.

## Technology Stack

| Technology               | Purpose                                      |
| ------------------------ | -------------------------------------------- |
| React 18                 | UI framework                                 |
| TypeScript 5             | Type-safe frontend development               |
| Vite 4                   | Development server and production build tool |
| react-markdown           | Markdown rendering                           |
| react-syntax-highlighter | Syntax highlighting for code blocks          |
| Chart.js                 | Client-side charting                         |
| date-fns                 | Date formatting and manipulation             |
| Lodash                   | Utility functions                            |
| Native Fetch API         | Backend HTTP communication                   |
| CSS                      | Application styling                          |

Frontend dependencies are defined in `package.json` and locked through `package-lock.json`.

## Architecture

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
┌───────────────────────────────────────────────┐
│             Spring Boot Backend               │
│                                               │
│ REST API                                      │
│      │                                        │
│      ▼                                        │
│ Chat Service                                  │
│      │                                        │
│      ├── Spring AI                            │
│      │      │                                 │
│      │      ▼                                 │
│      │   Ollama                               │
│      │                                        │
│      ├── Conversation Repository              │
│      │                                        │
│      └── Message Repository                   │
│             │                                 │
│             ▼                                 │
│         PostgreSQL                            │
└───────────────────────────────────────────────┘
```

This separation allows the frontend and backend to evolve independently while maintaining a clear API boundary.

## Project Structure

```text
ai-platform/
├── src/                         # Spring Boot backend
├── frontend/
│   ├── src/
│   │   ├── components/          # React UI components
│   │   ├── services/            # Backend API communication
│   │   ├── types/               # TypeScript types
│   │   ├── App.tsx              # Root React component
│   │   └── main.tsx             # React entry point
│   ├── package.json
│   ├── package-lock.json
│   ├── tsconfig.json
│   ├── vite.config.ts
│   └── README.md
├── pom.xml
└── README.md
```

## Frontend Components

The frontend separates the user interface into focused React components.

### ChatWindow

Coordinates the main chat interface, including the current conversation, messages, message input, and streaming state.

### ConversationList

Displays available conversations and allows the user to select an existing conversation or create a new one.

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
* Appropriate disabled/loading states

### Markdown Rendering

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

## Backend Integration

The frontend communicates with the Spring Boot backend through HTTP APIs.

The backend provides functionality for:

* Creating conversations
* Retrieving conversations
* Retrieving conversation history
* Sending messages
* Streaming AI responses

The backend is the source of truth for conversation data.

The frontend does not duplicate:

* Conversation persistence
* Message persistence
* LLM integration
* Conversation context management

## Conversation Flow

A typical conversation follows this flow:

```text
User
 │
 │ Create conversation
 ▼
Spring Boot
 │
 ▼
PostgreSQL
 │
 │ Conversation ID
 ▼
React Frontend
 │
 │ Send message
 ▼
Spring Boot
 │
 ├── Save USER message
 │
 ├── Retrieve conversation history
 │
 ├── Send context to LLM
 │
 ├── Receive AI response
 │
 └── Save ASSISTANT message
 │
 ▼
React Frontend
 │
 └── Display assistant response
```

## Streaming

The backend supports streaming LLM responses.

The frontend consumes the streaming response and progressively updates the assistant message.

```text
Ollama
   │
   ▼
Spring AI
   │
   ▼
Spring Boot Streaming Endpoint
   │
   ▼
React Streaming Client
   │
   ▼
Accumulated Assistant Response
   │
   ▼
Markdown Renderer
   │
   ▼
Formatted Chat Message
```

Incoming chunks are accumulated into a single assistant message rather than being treated as separate messages.

This allows the response to be rendered progressively while the model is generating it.

## API Separation

Backend communication is isolated in:

```text
src/services/chatApi.ts
```

React components use the API abstraction rather than constructing backend requests throughout the UI.

```text
React Component
       │
       ▼
   chatApi.ts
       │
       ▼
Spring Boot REST API
```

This keeps backend communication separate from presentation logic and makes future API changes easier to manage.

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

## Environment Configuration

The frontend uses a Vite environment variable for the backend API base URL.

```text
VITE_API_BASE_URL=http://localhost:8080
```

For local development, create a `.env` file in the `frontend/` directory when a custom API URL is required:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Environment files containing secrets must not be committed.

A template may be provided through:

```text
.env.example
```

The frontend does not require backend services to perform its production build. The Spring Boot backend, PostgreSQL, and Ollama are required when running the complete application.

## Prerequisites

For frontend development:

* Node.js
* npm

For running the complete application:

* Spring Boot backend
* PostgreSQL
* Ollama

The frontend itself does not run the LLM. LLM processing is handled by the backend through Ollama.

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

## Development Workflow

### 1. Start infrastructure

Start PostgreSQL and Ollama as required by the backend.

### 2. Start the Spring Boot backend

From the repository root:

```bash
./mvnw spring-boot:run
```

The backend normally runs on:

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

The frontend communicates with the Spring Boot API through the configured `VITE_API_BASE_URL`.

## Production Build

The frontend uses Vite to create an optimized production build.

From the `frontend/` directory:

```bash
npm run build
```

The generated production assets are written to:

```text
frontend/dist/
```

The production build does not require:

* Spring Boot
* PostgreSQL
* Ollama
* A running backend

The build validates that the frontend source code can be compiled and bundled successfully.

To preview the production build locally:

```bash
npm run preview
```

## Production Build Validation

The frontend uses `package-lock.json` to provide reproducible dependency installation.

For a clean installation:

```bash
npm ci
```

Then build the frontend:

```bash
npm run build
```

A successful build confirms that the frontend can be installed from the lockfile and compiled in a clean environment.

### Validation Command

The frontend provides a dedicated validation command:

```bash
npm run validate
```

The validation command runs the production build:

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

The generated `dist/` directory is build output and must not be committed to Git.

### Automated Validation

The frontend production build is also validated through GitHub Actions.

The CI workflow performs the following steps:

1. Checks out the repository
2. Sets up the required Node.js runtime
3. Installs the exact dependency versions from `package-lock.json`
4. Runs the frontend validation command
5. Fails the workflow if the production build fails

The frontend build is intentionally independent of the Spring Boot backend and AI infrastructure.

This allows frontend build failures to be detected automatically without requiring PostgreSQL, Ollama, or a running backend.

## Build Reproducibility

The frontend build is designed to be reproducible from a clean checkout.

The reproducible build process is:

```text
Clean Repository
      │
      ▼
frontend/package.json
      │
      ▼
frontend/package-lock.json
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

The committed `package-lock.json` should remain in the repository so that CI and other developers can reproduce the frontend dependency installation.

## Engineering Goals

The frontend is designed to demonstrate:

1. Modern frontend development using React and TypeScript
2. Component-based UI architecture
3. Integration with a Spring Boot REST API
4. Streaming UI for LLM-generated responses
5. Markdown rendering for AI responses
6. Syntax highlighting for generated source code
7. Type-safe frontend development
8. Separation of UI and backend communication
9. Reproducible production builds
10. Automated frontend build validation

The goal is to provide a focused web client for an AI backend without introducing unnecessary frontend complexity.

## Future Improvements

Potential future improvements include:

* Conversation renaming
* Conversation deletion
* Message regeneration
* Stop/cancel streaming
* Copy code button
* Copy message button
* Dark/light theme
* Frontend automated tests
* Error boundary
* Authentication
* Multi-model selection
* RAG interface
* Production deployment

These features should be introduced incrementally as the application evolves.

## License

MIT License - See the root project license for details.
