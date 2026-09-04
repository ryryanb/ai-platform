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

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isLoading]);

  return (
    <div
      className="message-list"
      aria-live="polite"
      aria-busy={isLoading}
    >
      {messages.length === 0 && !isLoading ? (
        <div className="message-empty">
          <h3>Start a conversation</h3>
          <p>
            Send a message below to begin chatting with the AI assistant.
          </p>
        </div>
      ) : (
        messages.map((message) => (
          <Message key={message.id} message={message} />
        ))
      )}

      {isLoading && (
        <div
          className="message assistant loading"
          role="status"
          aria-label="Assistant is generating a response"
        >
          <div className="message-bubble">
            <span className="loading-dots" aria-hidden="true">
              ...
            </span>
            <span className="sr-only">
              Assistant is generating a response
            </span>
          </div>
        </div>
      )}

      <div ref={endRef} />
    </div>
  );
};