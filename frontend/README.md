
# AI Platform Frontend

React + TypeScript + Vite frontend for the AI Platform.

This frontend provides the user interface for the AI Platform's Spring Boot backend, which integrates Spring AI, Ollama, PostgreSQL, conversation history, and streaming LLM responses.

The frontend is intentionally kept separate from the Spring Boot application while remaining part of the same repository.

## Overview

The AI Platform is a full-stack AI application designed to demonstrate production-oriented backend and frontend engineering for LLM-powered applications.

The frontend is responsible for:

- Providing the AI chat user interface
- Displaying conversations and messages
- Rendering assistant responses as Markdown
- Syntax highlighting code blocks
- Communicating with the Spring Boot REST API
- Displaying streaming LLM responses
- Providing a responsive and usable chat experience

The Spring Boot backend remains responsible for:

- REST API endpoints
- LLM integration
- Spring AI
- Ollama
- Conversation persistence
- PostgreSQL
- Conversation context
- Streaming LLM responses

## Technology Stack

| Technology | Purpose |
|---|---|
| React | UI framework |
| TypeScript | Type-safe frontend development |
| Vite | Development server and build tool |
| react-markdown | Markdown rendering |
| react-syntax-highlighter | Syntax highlighting for code blocks |
| Native Fetch API | Backend HTTP communication |
| CSS | Application styling |

The current project uses React 18, TypeScript 5, and Vite 4. The frontend dependencies are defined in `package.json`. 

## Architecture

The frontend is designed as a client of the Spring Boot backend.

```text
┌───────────────────────────────────────────────┐
│                 React Frontend                │
│                                               │
│  ┌─────────────┐     ┌────────────────────┐  │
│  │ Conversation│     │    Chat Window     │  │
│  │    List     │     │                    │  │
│  └─────────────┘     │ Messages           │  │
│                      │ Markdown           │  │
│                      │ Code Highlighting  │  │
│                      │ Message Input      │  │
│                      └────────────────────┘  │
│                                               │
└───────────────────────┬───────────────────────┘
                        │ HTTP / Streaming
                        ▼
┌───────────────────────────────────────────────┐
│             Spring Boot Backend               │
│                                               │
│  REST API                                     │
│       │                                       │
│       ▼                                       │
│  Chat Service                                 │
│       │                                       │
│       ├── Spring AI                           │
│       │       │                               │
│       │       ▼                               │
│       │    Ollama                             │
│       │                                       │
│       ├── Conversation Repository             │
│       │                                       │
│       └── Message Repository                  │
│               │                               │
│               ▼                               │
│           PostgreSQL                          │
└───────────────────────────────────────────────┘
````

## Project Structure

The frontend lives in its own directory within the main repository:

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
│   ├── tsconfig.json
│   ├── vite.config.ts
│   └── README.md
├── pom.xml
└── README.md
```

The frontend uses a standard React/Vite structure and TypeScript is configured in strict mode. ([GitHub][2])

## Frontend Components

The planned component architecture separates the UI into focused responsibilities:

```text
components/
├── ChatWindow.tsx
├── MessageList.tsx
├── Message.tsx
├── MessageInput.tsx
├── ConversationList.tsx
└── MarkdownRenderer.tsx
```

### ChatWindow

Coordinates the main chat interface.

Responsibilities include:

* Displaying the current conversation
* Rendering messages
* Managing the message input
* Displaying loading/streaming state

### ConversationList

Displays available conversations and allows the user to select a conversation or create a new one.

### MessageList

Renders the messages belonging to the currently selected conversation.

### Message

Represents an individual user or assistant message.

User and assistant messages are visually differentiated.

### MessageInput

Provides the chat input interface.

Expected behavior:

* Multiline input
* Send button
* Enter to send
* Shift+Enter for a new line
* Disabled state while appropriate

### MarkdownRenderer

Responsible for rendering assistant responses as Markdown.

Supported Markdown includes:

* Headings
* Paragraphs
* Bold
* Italic
* Ordered lists
* Unordered lists
* Blockquotes
* Inline code
* Fenced code blocks

Code blocks can be syntax highlighted using `react-syntax-highlighter`.

## Markdown Rendering

LLMs commonly return formatted Markdown rather than plain text.

For example:

````markdown
# Java Streams

Java Streams provide a declarative way to process collections.

- Filtering
- Mapping
- Collecting

```java
List<String> names = List.of("Ryan", "John");

List<String> result = names.stream()
        .filter(name -> name.length() > 4)
        .toList();
````

````

The frontend renders this as formatted content instead of displaying the raw Markdown syntax.

## Syntax Highlighting

Fenced code blocks are rendered using `react-syntax-highlighter`.

The frontend is intended to support common languages including:

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
````

Code blocks without a language identifier are rendered as plain code.

## Backend Integration

The frontend communicates with the Spring Boot backend through HTTP APIs.

The backend currently provides functionality for:

* Creating conversations
* Sending messages
* Retrieving conversation history
* Streaming AI responses

The frontend should treat the backend as the source of truth for conversation data.

The frontend should not duplicate conversation persistence or LLM logic.

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

The frontend is designed to consume the streaming response and progressively update the assistant message.

Conceptually:

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

The frontend should accumulate incoming response chunks into a single assistant message rather than treating each chunk as a separate message.

This allows Markdown to be rendered correctly as the response is generated.

## Environment Configuration

The frontend should not hard-code the backend URL.

Use a Vite environment variable:

```text
VITE_API_BASE_URL=http://localhost:8080
```

Create a local `.env` file when needed:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Do not commit `.env` files containing secrets.

A template can be provided as:

```text
.env.example
```

For example:

```env
VITE_API_BASE_URL=http://localhost:8080
```

## Prerequisites

Before running the frontend, install:

* Node.js
* npm
* Spring Boot backend
* Ollama
* PostgreSQL

The frontend itself does not run the LLM.

The LLM is handled by the backend through Ollama.

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

## Building for Production

Create a production build:

```bash
npm run build
```

The build output is generated by Vite.

To preview the production build locally:

```bash
npm run preview
```

## Development Workflow

The recommended development workflow is:

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

The frontend communicates with the Spring Boot API running on port `8080`.

## API Separation

Backend communication should be isolated in:

```text
src/services/chatApi.ts
```

React components should not contain raw API calls throughout the UI.

For example, components should call an API abstraction rather than directly constructing URLs:

```text
React Component
       │
       ▼
chatApi.ts
       │
       ▼
Spring Boot REST API
```

This keeps the UI independent from the backend implementation details and makes future API changes easier to manage.

## Type Safety

TypeScript is used throughout the frontend.

Conversation-related data should use explicit types such as:

```typescript
interface Conversation {
    id: string;
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

The project uses TypeScript's strict mode to catch potential type errors during development. ([GitHub][2])

Avoid using `any` unless there is a specific technical reason.

## Current Status

| Feature                            | Status         |
| ---------------------------------- | -------------- |
| React + TypeScript + Vite scaffold | Implemented    |
| Markdown dependency                | Installed      |
| Syntax highlighting dependency     | Installed      |
| AI chat UI                         | In development |
| Conversation sidebar               | Planned        |
| Conversation history UI            | Planned        |
| Backend API integration            | Planned        |
| Streaming responses                | Planned        |
| Markdown rendering                 | Planned        |
| Syntax-highlighted code            | Planned        |
| Responsive UI                      | Planned        |
| Frontend tests                     | Planned        |

The current repository contains the initial Vite/React scaffold and the Markdown/syntax-highlighting dependencies; the application UI is still being developed. ([GitHub][3])

## Relationship to the Backend

This frontend is intentionally a separate application within the same Git repository.

```text
ai-platform/
│
├── Backend
│   ├── Spring Boot
│   ├── Spring AI
│   ├── Ollama
│   ├── PostgreSQL
│   └── REST APIs
│
└── frontend/
    ├── React
    ├── TypeScript
    ├── Vite
    ├── Markdown rendering
    └── Chat UI
```

This separation allows the backend and frontend to evolve independently while remaining part of the same portfolio project.

## Engineering Goals

The frontend is intended to demonstrate:

1. **Modern frontend development** using React and TypeScript
2. **Component-based architecture** with clear separation of responsibilities
3. **API integration** with a Spring Boot backend
4. **Streaming UI** for LLM-generated responses
5. **Markdown rendering** for rich AI responses
6. **Syntax highlighting** for generated source code
7. **Type safety** using TypeScript
8. **Responsive design** for different screen sizes
9. **Separation of concerns** between UI and backend communication

The goal is not to build a feature-heavy consumer chat application.

Instead, the frontend serves as a clean demonstration of how a modern web client can interact with an AI backend.

## Future Improvements

Potential future improvements include:

* [ ] Conversation list endpoint
* [ ] Conversation renaming
* [ ] Conversation deletion
* [ ] Message regeneration
* [ ] Stop/cancel streaming
* [ ] Copy code button
* [ ] Copy message button
* [ ] Dark/light theme
* [ ] Frontend automated tests
* [ ] Error boundary
* [ ] Authentication
* [ ] Multi-model selection
* [ ] RAG interface
* [ ] Production deployment

These features should be added incrementally rather than introducing unnecessary complexity into the initial implementation.

## License

MIT License - See the root project license for details.




