import React from 'react';

import { Conversation } from '../types/chat';

import { MessageInput } from './MessageInput';
import { MessageList } from './MessageList';

interface ChatWindowProps {
  conversation: Conversation | null;
  onSendMessage: (content: string) => void;
  isLoading: boolean;
}

export const ChatWindow: React.FC<ChatWindowProps> = ({
  conversation,
  onSendMessage,
  isLoading,
}) => {
  if (!conversation) {
    return (
      <main className="chat-window empty" aria-label="Chat">
        <div className="chat-empty">
          <h1>AI Platform</h1>
          <p>Select a conversation or create a new one to get started.</p>
        </div>
      </main>
    );
  }

  return (
    <main className="chat-window" aria-label="Chat">
      <header className="chat-header">
        <h1>{conversation.title}</h1>
      </header>

      <MessageList
        messages={conversation.messages}
        isLoading={isLoading}
      />

      <MessageInput
        onSend={onSendMessage}
        disabled={isLoading}
      />
    </main>
  );
};