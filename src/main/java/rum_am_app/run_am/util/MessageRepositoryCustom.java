package rum_am_app.run_am.util;

public interface MessageRepositoryCustom {

    void markMessagesAsRead(String conversationId, String userId);
}
