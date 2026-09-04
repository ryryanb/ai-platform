import React from 'react';

import { Conversation } from '../types/chat';

interface ConversationListProps {
  conversations: Conversation[];
  selectedId: string | null;
  onSelect: (id: string) => void;
  onNew: () => void;
}

export const ConversationList: React.FC<ConversationListProps> = ({
  conversations,
  selectedId,
  onSelect,
  onNew,
}) => {
  return (
    <aside className="conversation-list">
      <header className="conversation-list-header">
        <h2>Conversations</h2>

        <button
          type="button"
          className="new-conversation-button"
          onClick={onNew}
          aria-label="Create a new conversation"
        >
          + New
        </button>
      </header>

      {conversations.length === 0 ? (
        <div className="conversation-empty">
          <p>No conversations yet.</p>

          <button
            type="button"
            onClick={onNew}
            className="conversation-empty-button"
          >
            Start a conversation
          </button>
        </div>
      ) : (
        <nav
          className="conversation-items"
          aria-label="Conversation history"
        >
          {conversations.map((conversation) => (
            <button
              key={conversation.id}
              type="button"
              className={`conversation-item ${
                conversation.id === selectedId ? 'active' : ''
              }`}
              onClick={() => onSelect(conversation.id)}
              aria-current={
                conversation.id === selectedId ? 'page' : undefined
              }
            >
              <span className="conversation-title">
                {conversation.title}
              </span>
            </button>
          ))}
        </nav>
      )}
    </aside>
  );
};