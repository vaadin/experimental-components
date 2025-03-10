import React, { type ReactElement } from 'react';
import { ReactAdapterElement, RenderHooks } from 'Frontend/generated/flow/ReactAdapter.js';
import Markdown from '../markdown/Markdown.js';

class MarkdownElement extends ReactAdapterElement {
  protected render(hooks: RenderHooks): ReactElement {
    const [content] = hooks.useState<string>('content');

    return <Markdown content={content}></Markdown>;
  }
}

customElements.define('markdown-element', MarkdownElement);
