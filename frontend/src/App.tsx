import React, { useEffect, useState } from 'react';
import { ChatWindow } from './components/ChatWindow';
import { ConversationList } from './components/ConversationList';
import { chatApi } from './services/chatApi';
import {
  Conversation,
  Message,
} from './types/chat';

export const App: React.FC = () => {
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Load conversations on mount
  useEffect(() => {
    const load = async () => {
      try {
        const data = await chatApi.getConversations();

        setConversations(data);

        if (data.length > 0) {
          setSelectedId(data[0].id);
          await loadConversation(data[0].id);
        }
      } catch (err) {
        setError(
          err instanceof Error
            ? err.message
            : 'Failed to load conversations.',
        );
      }
    };

    load();
  }, []);

  const selectedConversation =
    conversations.find((c) => c.id === selectedId) || null;

  const loadConversation = async (id: string) => {
    const messages = await chatApi.getMessages(id);

    setConversations((prev) =>
      prev.map((conversation) =>
        conversation.id === id
          ? { ...conversation, messages }
          : conversation,
      ),
    );
  };

  const refreshConversation = async (id: string) => {
    const msgs = await chatApi.getMessages(id);

    setConversations((prev) =>
      prev.map((c) =>
        c.id === id
          ? { ...c, messages: msgs }
          : c,
      ),
    );
  };

  const handleSelect = async (id: string) => {
    setSelectedId(id);

    try {
      setError(null);
      await loadConversation(id);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'Failed to load conversation.',
      );
    }
  };

  const handleNewConversation = async () => {
    try {
      setError(null);

      const newConv = await chatApi.createConversation();

      setConversations((prev) => [newConv, ...prev]);
      setSelectedId(newConv.id);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'Failed to create conversation.',
      );
    }
  };

  const handleSendMessage = async (content: string) => {
    if (!selectedId) return;

    setLoading(true);
    setError(null);

    const conversationId = selectedId;

    const userMessage: Message = {
      id: crypto.randomUUID(),
      role: 'user',
      content,
      createdAt: new Date().toISOString(),
    };

    const assistantMessage: Message = {
      id: crypto.randomUUID(),
      role: 'assistant',
      content: '',
      createdAt: new Date().toISOString(),
    };

    // Optimistically display the user's message and an empty assistant message.
    setConversations((prev) =>
      prev.map((conversation) =>
        conversation.id === conversationId
          ? {
              ...conversation,
              messages: [
                ...conversation.messages,
                userMessage,
                assistantMessage,
              ],
            }
          : conversation,
      ),
    );

    try {
      await chatApi.streamMessage(
        conversationId,
        content,
        (chunk) => {
          setConversations((prev) =>
            prev.map((conversation) =>
              conversation.id === conversationId
                ? {
                    ...conversation,
                    messages: conversation.messages.map((message) =>
                      message.id === assistantMessage.id
                        ? {
                            ...message,
                            content: message.content + chunk,
                          }
                        : message,
                    ),
                  }
                : conversation,
            ),
          );
        },
      );

      // Refresh the messages from the backend after streaming completes.
      await refreshConversation(conversationId);

      // Refresh conversations so the newly generated title appears.
      const updatedConversations =
        await chatApi.getConversations();

      setConversations((prev) =>
        updatedConversations.map((updated) => {
          const existing = prev.find(
            (conversation) => conversation.id === updated.id,
          );

          return {
            ...updated,
            messages: existing?.messages ?? [],
          };
        }),
      );
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'Failed to send message.',
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app-container">
      <ConversationList
        conversations={conversations}
        selectedId={selectedId}
        onSelect={handleSelect}
        onNew={handleNewConversation}
      />

      <ChatWindow
        conversation={selectedConversation}
        onSendMessage={handleSendMessage}
        isLoading={loading}
      />
    </div>
  );
};