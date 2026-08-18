import React, { useEffect, useRef } from 'react';
import { Message as MessageType } from '../types/chat';
import { Message } from './Message';

interface MessageListProps {
  messages: MessageType[];
  isLoading: boolean;
}

export const MessageList: React.FC<MessageListProps> = ({
  messages,
  isLoading,
}) => {
  const endRef = useRef<HTMLDivElement>(null);

  // Auto‑scroll to bottom when messages change
  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isLoading]);

  return (
    <div className="message-list">
      {messages.map((msg) => (
        <Message key={msg.id} message={msg} />
      ))}
      {isLoading && (
        <div className="message loading">
          <div className="message-bubble">
            <span className="loading-dots">…</span>
          </div>
        </div>
      )}
      <div ref={endRef} />
    </div>
  );
};