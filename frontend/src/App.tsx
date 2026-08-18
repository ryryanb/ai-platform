import React, { useEffect, useState } from 'react';
import {
  Conversation,
  Message,
} from './types/chat';
import { ConversationList } from './components/ConversationList';
import { ChatWindow } from './components/ChatWindow';
import { chatApi } from './services/chatApi';

export const App: React.FC = () => {
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

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
    await chatApi.sendMessage(selectedId, content);
    await refreshConversation(selectedId);
    setLoading(false);
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