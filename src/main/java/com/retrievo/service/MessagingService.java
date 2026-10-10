package com.retrievo.service;

import com.retrievo.entity.*;
import com.retrievo.repository.ConversationRepository;
import com.retrievo.repository.ItemRepository;
import com.retrievo.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessagingService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public Conversation startOrGetConversation(Long itemId, User currentUser) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found."));

        User reporter = item.getReporter();
        if (reporter.getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You cannot start a conversation with yourself.");
        }

        return conversationRepository.findExistingConversation(currentUser.getId(), reporter.getId(), itemId)
                .orElseGet(() -> {
                    Conversation conv = new Conversation(item, currentUser, reporter);
                    return conversationRepository.save(conv);
                });
    }

    public List<Conversation> getUserConversations(User user) {
        return conversationRepository.findUserConversations(user.getId());
    }

    public List<Message> getConversationMessages(Long conversationId, User user) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found."));

        if (!conversation.getParticipant1().getId().equals(user.getId()) &&
            !conversation.getParticipant2().getId().equals(user.getId()) &&
            user.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Unauthorized access to conversation.");
        }

        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Transactional
    public Message sendMessage(Long conversationId, User sender, String content) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found."));

        if (!conversation.getParticipant1().getId().equals(sender.getId()) &&
            !conversation.getParticipant2().getId().equals(sender.getId())) {
            throw new IllegalStateException("Unauthorized to send message in this thread.");
        }

        Message message = new Message(conversation, sender, content);
        Message saved = messageRepository.save(message);

        conversation.setLastMessageContent(content);
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        User recipient = conversation.getParticipant1().getId().equals(sender.getId())
                ? conversation.getParticipant2()
                : conversation.getParticipant1();

        notificationService.createNotification(
                recipient,
                "New Message Received",
                sender.getFullName() + ": " + (content.length() > 40 ? content.substring(0, 40) + "..." : content),
                "MESSAGE",
                "/dashboard/messages?id=" + conversationId
        );

        return saved;
    }
}
