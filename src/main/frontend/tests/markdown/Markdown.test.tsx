import React from 'react';
import { describe, it, expect } from 'vitest';
import CustomMarkdown from '../../../resources/META-INF/resources/frontend/markdown/Markdown.js';
import { render, screen } from '@testing-library/react';

describe('CustomMarkdown', () => {
  it('should render', () => {
    render(<CustomMarkdown content="*foo bar*" />);
    expect(screen.getByText('foo bar')).to.exist;
  });

  it('should render a link', () => {
    render(<CustomMarkdown content="[foo](http://example.com)" />);
    expect(screen.getByRole('link', { name: 'foo' })).to.exist;
  });

  it('should render an image', () => {
    render(<CustomMarkdown content="![foo](http://example.com/image.png)" />);
    expect(screen.getByAltText('foo')).to.exist;
  });

  it('should render a custom code block', () => {
    const content = '```mermaid\ngraph TD; A-->B;\n```';

    render(
      <CustomMarkdown
        content={content}
        renderer={(language, content) => {
          if (language === 'mermaid') {
            return <div className="mermaid">{content}</div>;
          }
          return null;
        }}
      />,
    );

    expect(screen.getByText('graph TD; A-->B;')).to.exist;
    expect(screen.getByText('graph TD; A-->B;')!.classList).toContain('mermaid');
  });
});
