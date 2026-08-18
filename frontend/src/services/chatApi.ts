import { Conversation, Message } from '../types/chat';

// Utility to generate random IDs
const randomId = () => Math.random().toString(36).substring(2, 9);

// Mock data ---------------------------------------------------------
let mockConversations: Conversation[] = [
  {
    id: randomId(),
    title: 'Demo Conversation',
    messages: [
      {
        id: randomId(),
        role: 'assistant',
        content: `## Welcome!

I’m your AI assistant. Feel free to ask anything.

### Example Java code

\`\`\`java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, world!");
    }
}
\`\`\`

Enjoy!`,
        createdAt: new Date().toISOString(),
      },
    ],
  },
];

// API ----------------------------------------------------------------
export const chatApi = {
  /** Get list of conversations */
  getConversations: async (): Promise<Conversation[]> => {
    // Simulate network latency
    await new Promise((res) => setTimeout(res, 300));
    // Return shallow copy
    return [...mockConversations];
  },

  /** Create a new empty conversation */
  createConversation: async (): Promise<Conversation> => {
    await new Promise((res) => setTimeout(res, 200));
    const newConv: Conversation = {
      id: randomId(),
      title: 'New Conversation',
      messages: [],
    };
    mockConversations = [newConv, ...mockConversations];
    return newConv;
  },

  /** Get messages for a conversation */
  getMessages: async (conversationId: string): Promise<Message[]> => {
    await new Promise((res) => setTimeout(res, 200));
    const conv = mockConversations.find((c) => c.id === conversationId);
    return conv ? [...conv.messages] : [];
  },

  /** Send a user message and receive a mocked assistant reply */
  sendMessage: async (
    conversationId: string,
    userMessage: string,
  ): Promise<Message> => {
    await new Promise((res) => setTimeout(res, 500)); // pretend processing

    const assistantReply: Message = {
      id: randomId(),
      role: 'assistant',
      content: `You said:

> ${userMessage}

Here is a **sample** response with a code block:

\`\`\`python
def greet(name):
    return f"Hello, {name}!"
\`\`\`

Let me know if you need anything else!`,
      createdAt: new Date().toISOString(),
    };

    // Append messages to mock store
    const convIndex = mockConversations.findIndex((c) => c.id === conversationId);
    if (convIndex !== -1) {
      const userMsg: Message = {
        id: randomId(),
        role: 'user',
        content: userMessage,
        createdAt: new Date().toISOString(),
      };
      mockConversations[convIndex].messages.push(userMsg, assistantReply);
    }

    return assistantReply;
  },
};