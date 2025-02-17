import React from 'react';
import { describe, it, expect, vi } from 'vitest';
import { userEvent } from '@vitest/browser/context';
import { findByText, render, screen, within } from '@testing-library/react';
import { Chat, Message, Subscription } from '../../../resources/META-INF/resources/frontend/chat/Chat.js';

const getSubscriptionMock = () => ({
  onNext: vi.fn().mockReturnThis(),
  onError: vi.fn().mockReturnThis(),
  onComplete: vi.fn().mockReturnThis(),
});

const getAiChatServiceMock = (
  messages: Message[] = [
    { role: 'user', content: 'Hello World' },
    { role: 'assistant', content: 'Hi there' },
  ],
) => {
  return {
    stream: vi.fn().mockReturnValue(getSubscriptionMock() as unknown as Subscription<string>),
    getHistory: vi.fn().mockResolvedValue(messages),
    closeChat: vi.fn(),
    uploadAttachment: vi.fn().mockResolvedValue('attachmentId'),
    removeAttachment: vi.fn(),
  };
};

const renderChat = (props: Partial<React.ComponentProps<typeof Chat>> = {}) =>
  render(
    <Chat
      service={getAiChatServiceMock()}
      chatId="1"
      options={{
        systemMessage: '',
      }}
      {...props}
    />,
  );

const getInputElement = () => screen.getByLabelText<HTMLTextAreaElement>('Message input').querySelector('textarea')!;

const getSendButton = () => screen.getByLabelText<HTMLButtonElement>('Send');

const getUploadButton = () => screen.getByLabelText<HTMLButtonElement>('Upload attachment');

const getFilesInput = () => document.querySelector<HTMLInputElement>('input[type=file]')!;

describe('Chat', () => {
  it('shows existing messages', async () => {
    const service = getAiChatServiceMock([
      { content: 'Hello World', role: 'user' },
      { content: 'Hi there', role: 'assistant' },
    ]);
    renderChat({ service });

    const messages = screen.getByLabelText('Chat messages');

    expect(await findByText(messages, 'Hello World')).to.exist;
    expect(await findByText(messages, 'Hi there')).to.exist;
  });

  it('calls stream when clicking send', async () => {
    const service = getAiChatServiceMock();
    renderChat({ service });

    await userEvent.type(getInputElement(), 'Test message');
    await userEvent.click(getSendButton());

    expect(service.stream).toHaveBeenCalledWith('1', 'Test message', {
      systemMessage: '',
    });
  });

  it('calls stream when pressing enter', async () => {
    const service = getAiChatServiceMock();
    renderChat({ service });

    await userEvent.type(getInputElement(), 'Test message');
    await userEvent.type(getInputElement(), '{enter}');

    expect(service.stream).toHaveBeenCalledWith('1', 'Test message', {
      systemMessage: '',
    });
  });

  it('prevents sending an empty message', async () => {
    const service = getAiChatServiceMock();
    renderChat({ service });
    expect(getSendButton().disabled).to.be.true;
    await userEvent.type(getInputElement(), '{enter}');
    expect(service.stream).not.toHaveBeenCalled();
  });

  it('clears the input on send', async () => {
    renderChat();
    await userEvent.type(getInputElement(), 'Test message');
    await userEvent.click(getSendButton());
    expect(getInputElement().value).to.equal('');
  });

  it('hides upload if acceptedFiles not provided', () => {
    renderChat();
    expect(getUploadButton().hidden).to.be.true;
  });

  it('shows upload if acceptedFiles provided', () => {
    renderChat({ acceptedFiles: 'image/*' });
    expect(getUploadButton().hidden).to.be.false;
  });

  it('renders placeholder while waiting for assistant', async () => {
    const service = getAiChatServiceMock([{ role: 'user', content: 'Hello' }]);
    renderChat({ service });

    await findByText(screen.getByLabelText('Chat messages'), 'Hello');

    expect(screen.getByLabelText('Chat messages').querySelector('.waiting-message')).to.exist;
  });

  it('removes placeholder after assistant responds', async () => {
    const service = getAiChatServiceMock([
      { role: 'user', content: 'Hello' },
      { role: 'assistant', content: 'Hi' },
    ]);
    renderChat({ service });

    await findByText(screen.getByLabelText('Chat messages'), 'Hi');

    expect(screen.getByLabelText('Chat messages').querySelector('.waiting-message')).to.not.exist;
  });

  it('hides Vaadin loading indicator while waiting', async () => {
    const service = getAiChatServiceMock([{ role: 'user', content: 'Hello' }]);
    renderChat({ service });
    render(<div className="v-loading-indicator">Vaadin loading indicator</div>);

    await findByText(screen.getByLabelText('Chat messages'), 'Hello');

    expect(screen.queryByText('Vaadin loading indicator')?.offsetHeight).to.equal(0);
  });

  it('does not hide vaadin loading indicator by default', async () => {
    const service = getAiChatServiceMock([
      { role: 'user', content: 'Hello' },
      { role: 'assistant', content: 'Hi' },
    ]);
    renderChat({ service });
    render(<div className="v-loading-indicator">Vaadin loading indicator</div>);

    await findByText(screen.getByLabelText('Chat messages'), 'Hi');

    expect(screen.queryByText('Vaadin loading indicator')?.offsetHeight).to.not.equal(0);
  });

  it('disables input while working', async () => {
    renderChat();
    await userEvent.type(getInputElement(), 'Test message');
    await userEvent.click(getSendButton());

    expect(getInputElement().disabled).to.be.true;
  });

  it('disables upload while working', async () => {
    renderChat();
    await userEvent.type(getInputElement(), 'Test message');
    await userEvent.click(getSendButton());

    expect(getUploadButton().disabled).to.be.true;
  });

  it('calls uploadAttachment for supported files', async () => {
    const service = getAiChatServiceMock();
    renderChat({ service, acceptedFiles: 'image/*' });

    const file1 = new File(['hello'], 'hello.png', { type: 'image/png' });
    const file2 = new File(['world'], 'world.png', { type: 'image/png' });
    await userEvent.upload(getFilesInput(), [file1, file2]);

    expect(service.uploadAttachment).toHaveBeenCalledWith('1', expect.any(File));
    expect(service.uploadAttachment).toHaveBeenCalledWith('1', expect.any(File));
  });

  it.skip('ignores unsupported files', async () => {
    const service = getAiChatServiceMock();
    renderChat({ service, acceptedFiles: 'image/*' });

    const file = new File(['hello'], 'hello.txt', { type: 'text/plain' });
    await userEvent.upload(getFilesInput(), [file]);

    expect(service.uploadAttachment).not.toHaveBeenCalled();
  });

  it('calls stream with files when sending', async () => {
    const service = getAiChatServiceMock();
    renderChat({ service, acceptedFiles: 'image/*' });

    const file = new File(['hello'], 'hello.png', { type: 'image/png' });
    await userEvent.upload(getFilesInput(), [file]);
    await userEvent.type(getInputElement(), 'Test message');
    await userEvent.click(getSendButton());

    expect(service.stream).toHaveBeenCalledWith('1', 'Test message', {
      systemMessage: '',
    });
  });

  it('lists file uploads in chat', async () => {
    renderChat({ acceptedFiles: 'image/*' });

    const file = new File(['hello'], 'hello.png', { type: 'image/png' });
    await userEvent.upload(getFilesInput(), [file]);

    expect(screen.getByText('hello.png')).to.exist;
    expect(screen.getByText('Remove file')).to.exist;
  });

  it('removes file upload when clicking remove', async () => {
    renderChat({ acceptedFiles: 'image/*' });

    const file = new File(['hello'], 'hello.png', { type: 'image/png' });
    await userEvent.upload(getFilesInput(), [file]);
    await userEvent.click(screen.getByText('Remove file'));

    expect(screen.queryByAltText('hello.png')).to.not.exist;
  });

  it('removes file upload when sending', async () => {
    renderChat({ acceptedFiles: 'image/*' });

    const file = new File(['hello'], 'hello.png', { type: 'image/png' });
    await userEvent.upload(getFilesInput(), [file]);
    await userEvent.type(getInputElement(), 'Test message');
    await userEvent.click(getSendButton());

    const inputContainer = screen.getByLabelText('Input container');
    expect(within(inputContainer!).queryByAltText('hello.png')).to.not.exist;
  });
});
