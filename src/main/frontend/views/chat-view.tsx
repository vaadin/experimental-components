import { AiChatService, Chat, Message, Subscription } from '../../resources/META-INF/resources/frontend/chat/Chat.js';
import './chat-view.css';

const testService: AiChatService<string> = {
  stream: function (chatId: string, userMessage: string, options?: string | undefined): Subscription<string> {
    return {
      onNext: function (callback: (value: string) => void): Subscription<string> {
        callback('Hello ');
        callback('world');
        return this;
      },
      onError: function (callback: (message: string) => void): Subscription<string> {
        return this;
      },
      onComplete: function (callback: () => void): Subscription<string> {
        callback();
        return this;
      },
    };
  },
  getHistory: async function (chatId: string): Promise<Message[]> {
    return [];
  },
  closeChat: async function (chatId: string): Promise<void> {
    console.log(`Closing chat with id: ${chatId}`);
  },
  uploadAttachment: function (chatId: string, file: File): Promise<string> {
    return Promise.resolve('attachmentId');
  },
  removeAttachment: function (chatId: string, attachmentId: string): Promise<void> {
    return Promise.resolve();
  },
};

export default function ChatView() {
  return <Chat chatId="1" service={testService} acceptedFiles="image/*" />;
}
