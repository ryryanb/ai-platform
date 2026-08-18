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
      <header>
        <h2>Conversations</h2>
        <button onClick={onNew} aria-label="New conversation">
          + New
        </button>
      </header>
      {conversations.length === 0 ? (
        <p className="empty">No conversations yet.</p>
      ) : (
        <ul>
          {conversations.map((conv) => (
            <li
              key={conv.id}
              className={conv.id === selectedId ? 'active' : ''}
              onClick={() => onSelect(conv.id)}
            >
              {conv.title}
            </li>
          ))}
        </ul>
      )}
    </aside>
  );
};