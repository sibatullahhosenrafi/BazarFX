package com.bazarfx.controller;

import com.bazarfx.AppContext;
import com.bazarfx.model.Message;
import com.bazarfx.service.MessageService;
import com.bazarfx.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;


public class MessagesController {

    @FXML private ListView<String> conversationListView;
    @FXML private Label chatHeaderLabel;
    @FXML private Label emptyStateLabel;
    @FXML private ListView<String> chatListView;
    @FXML private TextField messageField;

    private MainController mainController;
    private String me;
    private List<MessageService.ConversationSummary> conversations = new ArrayList<>();
    private MessageService.ConversationSummary activeConversation;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        me = SessionManager.getCurrentUser().getUsername();

        conversationListView.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            int idx = newIdx.intValue();
            if (idx >= 0 && idx < conversations.size()) {
                openConversation(conversations.get(idx));
            }
        });

        // Distinguishes "my" bubbles from "their" bubbles with a background-color CSS class.
        chatListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("chat-bubble-mine", "chat-bubble-theirs");
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setWrapText(true);
                setText(item);
                getStyleClass().add(item.startsWith("You:") ? "chat-bubble-mine" : "chat-bubble-theirs");
            }
        });

        refreshConversationList();
    }

    public void openWith(String partnerUsername, String productId, String productTitle) {
        refreshConversationList();

        MessageService.ConversationSummary match = findConversation(partnerUsername, productId);
        if (match == null) {
            match = new MessageService.ConversationSummary(partnerUsername, productId, productTitle);
            conversations.add(0, match);
        }
        activeConversation = match;
        rebuildConversationListView();
        openConversation(match);
    }

    private MessageService.ConversationSummary findConversation(String partnerUsername, String productId) {
        for (MessageService.ConversationSummary c : conversations) {
            if (c.matches(partnerUsername, productId)) return c;
        }
        return null;
    }

    private void refreshConversationList() {
        conversations = AppContext.get().messageService.getConversationsForUser(me);
        rebuildConversationListView();
    }

    private void rebuildConversationListView() {
        List<String> lines = new ArrayList<>();
        for (MessageService.ConversationSummary c : conversations) {
            String productPart = (c.productTitle == null || c.productTitle.isBlank()) ? "" : "  (" + c.productTitle + ")";
            String unread = c.unreadCount > 0 ? "   \u2022 " + c.unreadCount + " new" : "";
            String preview = c.lastMessage.isEmpty() ? "No messages yet - say hello!" : c.lastMessage;
            lines.add(c.partnerUsername + productPart + unread + "\n" + preview);
        }
        conversationListView.setItems(FXCollections.observableArrayList(lines));
        emptyStateLabel.setVisible(conversations.isEmpty());
        emptyStateLabel.setManaged(conversations.isEmpty());

        if (activeConversation != null) {
            for (int i = 0; i < conversations.size(); i++) {
                if (conversations.get(i).matches(activeConversation.partnerUsername, activeConversation.productId)) {
                    conversationListView.getSelectionModel().select(i);
                    break;
                }
            }
        }
    }

    private void openConversation(MessageService.ConversationSummary conv) {
        activeConversation = conv;
        chatHeaderLabel.setText(conv.partnerUsername
                + ((conv.productTitle == null || conv.productTitle.isBlank()) ? "" : "  -  " + conv.productTitle));

        AppContext.get().messageService.markConversationRead(me, conv.partnerUsername, conv.productId);
        AppContext.get().conversationPoller.poll(
                AppContext.get().messageService, me, conv.partnerUsername, conv.productId, this::renderMessages);
    }

    private void renderMessages(List<Message> messages) {
        List<String> lines = new ArrayList<>();
        for (Message m : messages) {
            String who = m.getSenderUsername().equals(me) ? "You" : m.getSenderUsername();
            lines.add(who + ": " + m.getContent());
        }
        chatListView.setItems(FXCollections.observableArrayList(lines));
        if (!lines.isEmpty()) chatListView.scrollTo(lines.size() - 1);
    }

    @FXML
    private void onSend() {
        String text = messageField.getText();
        if (text == null || text.isBlank() || activeConversation == null) return;

        AppContext.get().messageService.send(
                me, activeConversation.partnerUsername, activeConversation.productId,
                activeConversation.productTitle, text);
        messageField.clear();

        // Refresh immediately rather than waiting for the next poll tick, so
        // the sender always sees their own message appear right away.
        renderMessages(AppContext.get().messageService.getConversation(
                me, activeConversation.partnerUsername, activeConversation.productId));
        refreshConversationList();
    }
}
