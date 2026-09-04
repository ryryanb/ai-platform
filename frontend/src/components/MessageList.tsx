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
    endRef.current?.scrollIntoView({
      behavior: 'auto',
    });
  }, [messages]);

  return (
    <div className="message-list">
      {messages.map((msg) => {
  const isEmptyAssistant =
    msg.role === 'assistant' && !msg.content;

  return (
    <Message
      key={msg.id}
      message={msg}
      showLoading={isLoading && isEmptyAssistant}
    />
  );
})}

      <div ref={endRef} />
    </div>
  );
};