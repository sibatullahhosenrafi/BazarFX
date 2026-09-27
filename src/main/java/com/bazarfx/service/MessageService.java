package com.bazarfx.service;

import com.bazarfx.db.MessageDao;
import com.bazarfx.model.Message;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * Service layer for the in-app buyer/seller chat feature. Wraps
 * {@link MessageDao} (relational storage) and groups raw rows into
 * per-conversation summaries the UI can list, the same "DAO does SQL,
 * Service does business logic" split already used by ReviewService.
 */
public class MessageService {

    private final MessageDao messageDao = new MessageDao();

    /** Sends a message. No-ops on blank text or a user messaging themselves. */
    public void send(String sender, String receiver, String productId, String productTitle, String content) {
        if (sender == null || receiver == null || sender.equals(receiver)) return;
        if (content == null || content.isBlank()) return;
        messageDao.insert(new Message(sender, receiver, productId, productTitle, content.trim()));
    }

    /** Full message history between two users about one product (oldest first). */
    public List<Message> getConversation(String userA, String userB, String productId) {
        return messageDao.findConversation(userA, userB, productId);
    }

    /** Marks every message the other user sent me (about this product) as read. */
    public void markConversationRead(String me, String otherUsername, String productId) {
        messageDao.markRead(me, otherUsername, productId);
    }

    /** Total unread messages waiting for this user across every conversation. */
    public int countUnread(String username) {
        return messageDao.countUnread(username);
    }

    /**
     * Groups every message involving this user into one summary per
     * (other user, product) pair, newest conversation first. Because
     * findAllForUser already returns rows newest-first, the first message
     * seen for a given key is that conversation's latest message - so a
     * LinkedHashMap's insertion order alone gives the correct ordering.
     */
    public List<ConversationSummary> getConversationsForUser(String username) {
        List<Message> all = messageDao.findAllForUser(username);
        LinkedHashMap<String, ConversationSummary> byKey = new LinkedHashMap<>();

        for (Message m : all) {
            boolean iAmSender = m.getSenderUsername().equals(username);
            String otherUser = iAmSender ? m.getReceiverUsername() : m.getSenderUsername();
            String key = otherUser + "::" + (m.getProductId() == null ? "" : m.getProductId());

            ConversationSummary summary = byKey.get(key);
            if (summary == null) {
                summary = new ConversationSummary(otherUser, m.getProductId(), m.getProductTitle());
                byKey.put(key, summary);
            }
            summary.considerMessage(m, username);
        }
        return new ArrayList<>(byKey.values());
    }

    /** One row in the "Messages" conversation list: who, about what, and how many are unread. */
    public static class ConversationSummary {
        public final String partnerUsername;
        public final String productId;
        public final String productTitle;
        public String lastMessage = "";
        public String lastTimestamp = "";
        public int unreadCount = 0;

        public ConversationSummary(String partnerUsername, String productId, String productTitle) {
            this.partnerUsername = partnerUsername;
            this.productId = productId;
            this.productTitle = productTitle;
        }

        /** messages arrive newest-first, so only the FIRST call sets the preview text. */
        void considerMessage(Message m, String meUsername) {
            if (lastMessage.isEmpty()) {
                lastMessage = m.getContent();
                lastTimestamp = m.getCreatedAt();
            }
            if (!m.isRead() && m.getReceiverUsername().equals(meUsername)) {
                unreadCount++;
            }
        }

        public boolean matches(String otherUsername, String otherProductId) {
            return partnerUsername.equals(otherUsername) && Objects.equals(productId, otherProductId);
        }
    }
}
