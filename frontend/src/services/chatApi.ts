import {
  Conversation,
  ConversationResponse,
  Message,
  MessageRole,
} from '../types/chat';

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api';

interface BackendMessage {
  id: string;
  role: 'USER' | 'ASSISTANT';
  content: string;
  createdAt: string;
}

const request = async <T>(
  endpoint: string,
  options?: RequestInit,
): Promise<T> => {
  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers: {
      ...(options?.body
        ? { 'Content-Type': 'text/plain' }
        : {}),
      ...options?.headers,
    },
  });

  if (!response.ok) {
    const errorText = await response.text();

    throw new Error(
      errorText || `Request failed with status ${response.status}`,
    );
  }

  return response.json();
};

const normalizeMessage = (
  message: BackendMessage,
): Message => ({
  id: message.id,
  role: message.role.toLowerCase() as MessageRole,
  content: message.content,
  createdAt: message.createdAt,
});

const toConversation = (
  conversation: ConversationResponse,
): Conversation => ({
  id: conversation.id,
  title: 'New Conversation',
  messages: [],
});

export const chatApi = {
  getConversations: async (): Promise<Conversation[]> => {
    const conversations =
      await request<ConversationResponse[]>('/conversations');

    return conversations.map(toConversation);
  },

  createConversation: async (): Promise<Conversation> => {
    const conversation =
      await request<ConversationResponse>('/conversations', {
        method: 'POST',
      });

    return toConversation(conversation);
  },

  getMessages: async (
    conversationId: string,
  ): Promise<Message[]> => {
    const messages = await request<BackendMessage[]>(
      `/conversations/${conversationId}/messages`,
    );

    return messages.map(normalizeMessage);
  },

  sendMessage: async (
    conversationId: string,
    userMessage: string,
  ): Promise<Message> => {
    const message = await request<BackendMessage>(
      `/conversations/${conversationId}/messages`,
      {
        method: 'POST',
        body: userMessage,
      },
    );

    return normalizeMessage(message);
  },
};