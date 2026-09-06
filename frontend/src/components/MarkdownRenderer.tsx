import React from 'react';
import ReactMarkdown from 'react-markdown';


import {
  Prism as SyntaxHighlighter,
} from 'react-syntax-highlighter';

import { prism } from 'react-syntax-highlighter/dist/esm/styles/prism';

interface MarkdownRendererProps {
  content: string;
}

/**
 * Renders Markdown content with syntax-highlighted fenced code blocks.
 *
 * Examples:
 *
 * ```java
 * public class Example {
 * }
 * ```
 *
 * ```python
 * print("Hello")
 * ```
 */
export const MarkdownRenderer: React.FC<MarkdownRendererProps> = ({
  content,
}) => {
  return (
    <ReactMarkdown
      components={{
        code({ className, children, ...props }) {
          const match = /language-([\w-]+)/.exec(className || '');

          const code = String(children).replace(/\n$/, '');

          /*
           * react-markdown uses the presence of className="language-*"
           * to identify fenced code blocks with a language.
           *
           * Inline code does not have this class and should remain a
           * normal inline <code> element.
           */
          if (!match) {
            return (
              <code className={className} {...props}>
                {children}
              </code>
            );
          }

          return (
            <SyntaxHighlighter
  style={prism}
  language={match[1]}
  PreTag="div"
  customStyle={{
    margin: '0.75rem 0',
    padding: '1rem',
    borderRadius: '8px',
    background: '#f6f8fa',
    fontSize: '0.9rem',
    lineHeight: '1.5',
    overflowX: 'auto',
  }}
>
  {code}
</SyntaxHighlighter>
          );
        },
      }}
    >
      {content}
    </ReactMarkdown>
  );
};