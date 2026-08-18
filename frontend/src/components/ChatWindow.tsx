import React from 'react';
import { Conversation } from '../types/chat';
import { MessageList } from './MessageList';
import { MessageInput } from './MessageInput';

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
      <section className="chat-window empty">
        <p>Select or create a conversation to start chatting.</p>
      </section>
    );
  }

  return (
    <section className="chat-window">
      <header className="chat-header">
        <h2>{conversation.title}</h2>
      </header>

      <MessageList messages={conversation.messages} isLoading={isLoading} />

      <MessageInput onSend={onSendMessage} disabled={isLoading} />
    </section>
  );
};