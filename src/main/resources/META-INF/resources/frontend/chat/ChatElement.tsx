import React, { useMemo, useRef } from 'react';

import { ReactAdapterElement, RenderHooks } from 'Frontend/generated/flow/ReactAdapter.js';
import { AiChatService, Chat, Message, Subscription } from './Chat.js';
import { ReactElement } from 'react';

class ChatElement extends ReactAdapterElement {
  private errorCallback?: (message: string) => void;
  private completeCallback?: () => void;
  private nextCallback?: (value: string) => void;

  updateStream(value: string): void {
    this.nextCallback?.(value);
  }

  handleError(message: string): void {
    this.errorCallback?.(message);
  }

  handleComplete(): void {
    this.completeCallback?.();
  }

  protected render(hooks: RenderHooks): ReactElement {
    const [acceptedFiles] = hooks.useState<string>('acceptedFiles');
    const [options] = hooks.useState<string>('options');
    const [chatId] = hooks.useState<string>('chatId');
    const [history] = hooks.useState<Message[]>('history');
    const pollInterval = useRef<number | null>(null);

    const streamEvent = hooks.useCustomEvent<string>('stream');
    const removeAttachmentEvent = hooks.useCustomEvent<string>('removeAttachment');

    const service = useMemo<AiChatService<string>>(() => {
      return {
        stream: (_chatId: string, userMessage: string, _options?: string | undefined) => {
          const subscription: Subscription<string> = {
            onNext: (callback: (value: string) => void): Subscription<string> => {
              this.nextCallback = callback;
              return subscription;
            },
            onError: (callback: (message: string) => void): Subscription<string> => {
              this.errorCallback = (message: string) => {
                callback(message);
                clearInterval(pollInterval.current!);
              };
              return subscription;
            },
            onComplete: (callback: () => void): Subscription<string> => {
              this.completeCallback = () => {
                callback();
                clearInterval(pollInterval.current!);
              };
              return subscription;
            },
          };

          pollInterval.current = window.setInterval(() => this.dispatchEvent(new Event('poll')), 100);
          streamEvent(userMessage);

          return subscription;
        },
        getHistory: (_chatId: string) => {
          return Promise.resolve(history || []);
        },
        closeChat: (_chatId: string) => {
          return Promise.resolve();
        },
        uploadAttachment: (_chatId: string, file: File) => {
          const uploadUrl = this.getAttribute('target');
          if (uploadUrl) {
            const formData = new FormData();
            formData.append('file', file);
            return fetch(uploadUrl, {
              method: 'POST',
              body: formData,
            }).then((response) => {
              if (!response.ok) {
                throw new Error('Failed to upload file');
              }
              return file.name;
            });
          }

          return Promise.resolve('');
        },
        removeAttachment: (_chatId: string, attachmentId: string) => {
          removeAttachmentEvent(attachmentId);
          return Promise.resolve();
        },
      };
    }, [history]);

    return <Chat service={service} chatId={chatId} acceptedFiles={acceptedFiles} options={options} />;
  }
}

customElements.define('chat-element', ChatElement);
