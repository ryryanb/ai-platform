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
      const data = await chatApi.getConversations();
      setConversations(data);
      if (data.length > 0) setSelectedId(data[0].id);
    };
    load();
  }, []);

  const selectedConversation = conversations.find((c) => c.id === selectedId) || null;

  const refreshConversation = async (id: string) => {
    const msgs = await chatApi.getMessages(id);
    setConversations((prev) =>
      prev.map((c) => (c.id === id ? { ...c, messages: msgs } : c)),
    );
  };

  const handleSelect = (id: string) => {
    setSelectedId(id);
  };

  const handleNewConversation = async () => {
    const newConv = await chatApi.createConversation();
    setConversations((prev) => [newConv, ...prev]);
    setSelectedId(newConv.id);
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

    await refreshConversation(conversationId);
  } catch (err) {
    setError(
      err instanceof Error
        ? err.message
        : 'Failed to generate response.',
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