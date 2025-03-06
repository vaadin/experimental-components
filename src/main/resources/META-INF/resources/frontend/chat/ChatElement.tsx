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

  // Called by the Java component to handle errors
  handleError(message: string): void {
    this.errorCallback?.(message);
  }

  // Called by the Java component to handle completion
  handleComplete(): void {
    this.completeCallback?.();
  }

  // uploadAttachment(chatId: string, file: File): Promise<string> {
  //   this.dispatchEvent(
  //     new CustomEvent('uploadAttachment', {
  //       detail: { chatId, file },
  //     }),
  //   );
  //   // In a real implementation, this would return an ID from the server
  //   return Promise.resolve(`attachment-${Date.now()}`);
  // }

  // removeAttachment(chatId: string, attachmentId: string): Promise<void> {
  //   this.dispatchEvent(
  //     new CustomEvent('removeAttachment', {
  //       detail: { chatId, attachmentId },
  //     }),
  //   );
  //   return Promise.resolve();
  // }

  protected render(hooks: RenderHooks): ReactElement {
    const [acceptedFiles] = hooks.useState<string>('acceptedFiles');
    const [options] = hooks.useState<string>('options');
    const [history] = hooks.useState<Message[]>('history');
    const pollInterval = useRef<number | null>(null);

    const streamEvent = hooks.useCustomEvent<string>('stream');
    const getHistoryEvent = hooks.useCustomEvent('getHistory');
    const removeAttachmentEvent = hooks.useCustomEvent<string>('removeAttachment');

    const service = useMemo<AiChatService<string>>(() => {
      return {
        stream: (chatId: string, userMessage: string, options?: string | undefined) => {
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
          getHistoryEvent();
          return Promise.resolve(history || []);
        },
        closeChat: (chatId: string) => {
          return Promise.resolve();
        },
        uploadAttachment: (chatId: string, file: File) => {
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

    return (
      <Chat service={service} chatId={String(history?.length || 0)} acceptedFiles={acceptedFiles} options={options} />
    );
  }
}

customElements.define('chat-element', ChatElement);
