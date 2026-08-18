import React from 'react';
import { Message as MessageType } from '../types/chat';
import { MarkdownRenderer } from './MarkdownRenderer';

interface MessageProps {
  message: MessageType;
}

export const Message: React.FC<MessageProps> = ({ message }) => {
  const isUser = message.role === 'user';

  return (
    <div className={`message ${isUser ? 'user' : 'assistant'}`}>
      <div className="message-bubble">
        {isUser ? (
          <p>{message.content}</p>
        ) : (
          <MarkdownRenderer content={message.content} />
        )}
        <span className="timestamp">
          {new Date(message.createdAt).toLocaleTimeString([], {
            hour: '2-digit',
            minute: '2-digit',
          })}
        </span>
      </div>
    </div>
  );
};