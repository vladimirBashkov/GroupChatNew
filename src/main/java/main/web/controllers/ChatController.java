package main.web.controllers;

import lombok.RequiredArgsConstructor;
import main.services.ChatService;
import main.web.model.SimpleResponse;
import main.web.model.chat.ChatDTO;
import main.web.model.chat.CreateWarnRequest;
import main.web.model.chat.DeleteMessageRequest;
import main.web.model.chat.MessageDTO;
import main.web.model.chat.SendMessageRequest;
import main.web.model.chat.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/chat")
@PreAuthorize("hasAnyRole('USER', 'OLD', 'SIGNOR', 'ADMIN')")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/message")
    public ResponseEntity<SimpleResponse> sendMessage(@RequestBody SendMessageRequest request){
        return ResponseEntity.ok(new SimpleResponse(chatService.sendMessage(request)));
    }

    @DeleteMapping("/delete-message")
    public ResponseEntity<SimpleResponse> deleteMessage(@RequestBody DeleteMessageRequest request){
        return ResponseEntity.ok(new SimpleResponse(chatService.deleteMessage(request)));
    }

    @GetMapping("/chat")
    public ResponseEntity<List<ChatDTO>> getChatsByUser(@RequestParam Long id){
        return ResponseEntity.ok(chatService.getChatsByUser(id));
    }

    @GetMapping("/message")
    public ResponseEntity<List<MessageDTO>> getChatMessages(@RequestParam Long id, @RequestParam Long chatId){
        return ResponseEntity.ok(chatService.getChatMessages(id, chatId));
    }

    @GetMapping("/user")
    public ResponseEntity<List<UserDTO>> getListOfChatUsers(@RequestParam Long id, @RequestParam Long chatId){
        return ResponseEntity.ok(chatService.getListOfChatUsers(id, chatId));
    }


    @GetMapping("/create-new-chat")
    @PreAuthorize("hasAnyRole('OLD', 'SIGNOR', 'ADMIN')")
    public ResponseEntity<SimpleResponse> createNewChat(@RequestParam Long id, @RequestParam Long toUserId){
        if(id == toUserId){
            return ResponseEntity.badRequest().body(new SimpleResponse("You trying to write you"));
        }
        return ResponseEntity.ok(new SimpleResponse(chatService.createNewChat(id,toUserId)));
    }

    @PostMapping("/create-new-warn")
    @PreAuthorize("hasAnyRole('SIGNOR', 'ADMIN')")
    public ResponseEntity<SimpleResponse> createNewWarn(@RequestBody CreateWarnRequest request){
        return ResponseEntity.ok(new SimpleResponse(chatService.createNewWarn(request)));
    }

    @GetMapping("/delete-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SimpleResponse> deleteUserFromChat(@RequestParam Long id, @RequestParam Long userId, @RequestParam Long chatId){
        if(id.equals(userId)){
            return ResponseEntity.badRequest().body(new SimpleResponse("You trying delete you"));
        }
        return ResponseEntity.ok(new SimpleResponse(chatService.deleteUserFromChat(id, userId, chatId)));
    }
}
