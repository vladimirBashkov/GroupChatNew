package main.web.controllers;

import lombok.RequiredArgsConstructor;
import main.entity.*;
import main.exception.DuplicateException;
import main.exception.EntityNotFoundException;
import main.repository.ChatRepository;
import main.repository.MessageRepository;
import main.repository.UserRepository;
import main.repository.UserWarningRepository;
import main.security.SecurityService;
import main.web.model.SimpleResponse;
import main.web.model.chat.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/chat")
@PreAuthorize("hasAnyRole('USER', 'OLD', 'SIGNOR', 'ADMIN')")
@RequiredArgsConstructor
public class ChatController {
    @Value("${firstChat.name}")
    private String nameFirstChat;

    private final SecurityService securityService;

    private final UserRepository userRepository;

    private final ChatRepository chatRepository;

    private final MessageRepository messageRepository;

    private final UserWarningRepository userWarningRepository;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @PostMapping("/message")
    public ResponseEntity<SimpleResponse> sendMessage(@RequestBody SendMessageRequest request){
        String message = request.getMessage();
        if(message.isEmpty()){
            return ResponseEntity.badRequest().body(new SimpleResponse("Empty message"));
        }
        String refreshToken = request.getRefreshToken();
        Long chatId = request.getChatId();
        Long messageIdReply = request.getReplyByMessageId();
        User user = securityService.getUserByRefreshToken(refreshToken);
        if(user.getStatus().equals(Status.BANNED)){
            return ResponseEntity.badRequest().body(new SimpleResponse(user.getLogin() + " are BANNED"));
        }

        Chat chat;
        if (chatRepository.existsById(chatId)){
            chat = chatRepository.findById(chatId).get();
        } else throw new EntityNotFoundException("Chat not found");
        if (!checkMember(user, chat)){
            throw new EntityNotFoundException("User is not member of chat");
        }
        if(request.getReplyByMessageId() != 0){
            Message messageReply;
            if (messageRepository.existsById(messageIdReply)){
                messageReply = messageRepository.findById(messageIdReply).get();
                if(!chat.getName().equals(messageReply.getChat().getName())){
                    throw new EntityNotFoundException("Message not this chat");
                }
            } else throw new EntityNotFoundException("Message not found");
        }

        Message messageEntity = Message.builder()
                .user(user)
                .chat(chat)
                .time(LocalDateTime.now())
                .message(message)
                .messageIdReply(messageIdReply)
                .build();
        messageRepository.save(messageEntity);
        return ResponseEntity.ok(new SimpleResponse("Done"));
    }

    @DeleteMapping("/delete-message")
    public ResponseEntity<SimpleResponse> deleteMessage(@RequestBody DeleteMessageRequest request){
        String refreshToken = request.getRefreshToken();
        Long messageId = request.getMessageId();
        if(messageId == 0 || messageId == null){
            return ResponseEntity.badRequest().body(new SimpleResponse("Empty messageId"));
        }
        User user = securityService.getUserByRefreshToken(refreshToken);
        Message message;
        if(messageRepository.existsById(messageId)){
            message = messageRepository.findById(messageId).get();
        } else return ResponseEntity.badRequest().body(new SimpleResponse("Message not found!"));
        if(!user.getRoles().contains(RoleType.ROLE_ADMIN)){
            if(!user.getId().equals(message.getUser().getId())){
                return ResponseEntity.badRequest().body(new SimpleResponse("The Message is not yours!"));
            }
        }
        messageRepository.delete(message);
        return ResponseEntity.ok(new SimpleResponse("Done"));
    }

    @GetMapping("/chat")
    public ResponseEntity<List<ChatDTO>> getChatsByUser(@RequestParam Long id){
        User user = userRepository.findById(id).orElseThrow(() ->
        new EntityNotFoundException("Exception trying to get User for userId: " + id));
        return ResponseEntity.ok(user.getChats().stream().map(chat -> ChatDTO.builder()
                    .id(chat.getId())
                    .chatName(chat.getName())
                    .length(messageRepository.countByChat(chat))
                    .build()).toList());
    }

    @GetMapping("/message")
    public ResponseEntity<List<MessageDTO>> getChatMessages(@RequestParam Long id, @RequestParam Long chatId){
        User user = userRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Exception trying to get User for userId: " + id));
        Chat chat = chatRepository.findById(chatId).orElseThrow(() ->
                new EntityNotFoundException("Exception trying to get Chat for chatId: " + chatId));

        if (!checkMember(user, chat)){
            throw new EntityNotFoundException("User is not member of chat");
        }
        return ResponseEntity.ok(chat.getMessages().stream().map(message -> MessageDTO.builder()
                    .id(message.getId())
                    .userId(message.getUser().getId())
                    .login(message.getUser().getLogin())
                    .time(message.getTime().format(formatter))
                    .text(message.getMessage())
                    .replyByMessageId(message.getMessageIdReply())
                    .build()).toList());
    }

    private boolean checkMember(User user, Chat chat){
        Long userId = user.getId();
        if(chat.getName().equals(nameFirstChat)){
            return true;
        } else {
            List<User> findList = chat.getUsers().stream().filter(userOfSt -> userOfSt.getId().equals(userId)).toList();
            return findList.size() == 1;
        }
    }

    @GetMapping("/user")
    public ResponseEntity<List<UserDTO>> getListOfChatUsers(@RequestParam Long id, @RequestParam Long chatId){
        User user = userRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Exception trying to get User for userId: " + id));
        Chat chat = chatRepository.findById(chatId).orElseThrow(() ->
                new EntityNotFoundException("Exception trying to get Chat for chatId: " + chatId));

        if (!checkMember(user, chat)){
            throw new EntityNotFoundException("User is not member of chat");
        }
        return ResponseEntity.ok(chat.getUsers().stream().map(u -> {
            return UserDTO.builder()
                    .id(u.getId())
                    .login(u.getLogin())
                    .firstName(u.getFirstName())
                    .sex(u.getSex())
                    .age(u.getAge())
                    .iconType(user.getIconType())
                    .build();
        }).toList());
    }


    @GetMapping("/create-new-chat")
    @PreAuthorize("hasAnyRole('OLD', 'SIGNOR', 'ADMIN')")
    public ResponseEntity<SimpleResponse> createNewChat(@RequestParam Long id, @RequestParam Long toUserId){
        if(id == toUserId){
            return ResponseEntity.badRequest().body(new SimpleResponse("You trying to write you"));
        }
        User user = userRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Exception trying to get User for userId: " + id));
        User targetUser = userRepository.findById(toUserId).orElseThrow(() ->
                new EntityNotFoundException("Exception trying to get User for userId: " + id));
        List<Chat> allChats = chatRepository.findByUsers(user);
        try{
            allChats.forEach(chat -> {
                List<User> usersList = chat.getUsers();
                boolean isFindUser = false;
                boolean isFindTargetUser = false;
                if(usersList.size() == 2){
                    for(User u : usersList){
                        if(u.getId().equals(toUserId)){
                            isFindTargetUser = true;
                        }
                        if(u.getId().equals(id)){
                            isFindUser = true;
                        }
                    }
                    if(isFindUser && isFindTargetUser){
                        throw new DuplicateException("Duplicate chat");
                    }
                }
            });
        }catch (DuplicateException ex){
            return ResponseEntity.badRequest().body(new SimpleResponse("Duplicate chat"));
        }

        String nameChat = user.getLogin() + "-to-" + targetUser.getLogin();
        Chat chat = Chat.builder()
                .name(nameChat).build();
        chatRepository.save(chat);
        Chat chatFromDB = chatRepository.findByName(nameChat).get();
        user.addChat(chatFromDB);
        userRepository.save(user);
        targetUser.addChat(chatFromDB);
        userRepository.save(targetUser);
        return ResponseEntity.ok(new SimpleResponse("Done"));
    }

    @PostMapping("/create-new-warn")
    @PreAuthorize("hasAnyRole('SIGNOR', 'ADMIN')")
    public ResponseEntity<SimpleResponse> createNewWarn(@RequestBody CreateWarnRequest request){
        Long targetUserId = request.getUserId();
        String warnMessage = request.getWarnMessage();
        User targetUser;
        if(userRepository.existsById(targetUserId)){
            targetUser = userRepository.findById(targetUserId).get();
        } else return ResponseEntity.badRequest().body(new SimpleResponse("User not found"));
        UserWarning warning = UserWarning.builder()
                .user(targetUser)
                .warningMessage(warnMessage)
                .build();
        userWarningRepository.save(warning);
        return ResponseEntity.ok(new SimpleResponse("Warning added"));
    }

    @GetMapping("/delete-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SimpleResponse> deleteUserFromChat(@RequestParam Long id, @RequestParam Long userId, @RequestParam Long chatId){
        if(id.equals(userId)){
            return ResponseEntity.badRequest().body(new SimpleResponse("You trying delete you"));
        }
        User targetUser = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Exception trying to get User for userId: " + id));

        Chat chat;
        if(chatRepository.existsById(chatId)){
            chat = chatRepository.findById(chatId).get();
        } else return ResponseEntity.badRequest().body(new SimpleResponse("Chat not found"));
        if(targetUser.getChats().remove(chat)){
            userRepository.save(targetUser);
            return ResponseEntity.ok(new SimpleResponse("Done"));
        }else return ResponseEntity.badRequest().body(new SimpleResponse("Error in delete"));
    }
}
