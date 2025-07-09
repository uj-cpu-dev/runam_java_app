package rum_am_app.run_am.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import rum_am_app.run_am.dtorequest.MessageRequest;
import rum_am_app.run_am.service.MessageService;

import java.security.Principal;

@Controller
public class MessageWebSocketController {
    private final MessageService messageService;

    public MessageWebSocketController(MessageService messageService) {
        this.messageService = messageService;
    }

    @MessageMapping("/chat/{conversationId}")
    @SendTo("/topic/messages/{conversationId}")
    public MessageRequest.MessageDto handleMessage(
            @DestinationVariable String conversationId,
            MessageRequest request,
            Principal principal) {

        return new MessageRequest.MessageDto(
                messageService.sendMessage(
                        conversationId,
                        principal.getName(),
                        request
                )
        );
    }
}
