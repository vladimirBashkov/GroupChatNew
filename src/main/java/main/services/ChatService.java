package main.services;

import lombok.RequiredArgsConstructor;
import main.entity.Chat;
import main.entity.Message;
import main.entity.RoleType;
import main.entity.Status;
import main.entity.User;
import main.entity.UserWarning;
import main.exception.DuplicateException;
import main.exception.EntityNotFoundException;
import main.repository.ChatRepository;
import main.repository.MessageRepository;
import main.repository.UserRepository;
import main.repository.UserWarningRepository;
import main.security.SecurityService;
import main.web.model.chat.ChatDTO;
import main.web.model.chat.CreateWarnRequest;
import main.web.model.chat.DeleteMessageRequest;
import main.web.model.chat.MessageDTO;
import main.web.model.chat.SendMessageRequest;
import main.web.model.chat.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    @Value("${firstChat.name}")
    private String nameFirstChat;

    private final SecurityService securityService;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserWarningRepository userWarningRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final Long TIME_CORRECTION = 3l;
    private final String EMPTY_MESSAGE_WARN = "Empty message";
    private final String CHAT_NOT_FOUND_WARN = "Chat not found";
    private final String USER_NOT_MEMBER_WARN = "User is not member of chat";
    private final String DONE_MESSAGE = "Done";
    private final String MESSAGE_NOT_FOUND_WARN = "Message not found";
    private final String DUPLICATE_CAT_WARN = "Duplicate chat";
    private final String EX_GET_USER_BY_ID = "Exception trying to get User for userId: ";

    public String sendMessage(SendMessageRequest request){
        String message = request.getMessage();
        if(message.isEmpty()){
            return EMPTY_MESSAGE_WARN;
        }
        String refreshToken = request.getRefreshToken();
        Long chatId = request.getChatId();
        Long messageIdReply = request.getReplyByMessageId();
        User user = securityService.getUserByRefreshToken(refreshToken);
        if(user.getStatus().equals(Status.BANNED)){
            return user.getLogin() + " are BANNED";
        }

        Chat chat;
        if (chatRepository.existsById(chatId)){
            chat = chatRepository.findById(chatId).get();
        } else throw new EntityNotFoundException(CHAT_NOT_FOUND_WARN);
        if (!checkMember(user, chat)){
            throw new EntityNotFoundException(USER_NOT_MEMBER_WARN);
        }
        if(request.getReplyByMessageId() != 0){
            Message messageReply;
            if (messageRepository.existsById(messageIdReply)){
                messageReply = messageRepository.findById(messageIdReply).get();
                if(!chat.getName().equals(messageReply.getChat().getName())){
                    throw new EntityNotFoundException("Message not this chat");
                }
            } else throw new EntityNotFoundException(MESSAGE_NOT_FOUND_WARN);
        }

        Message messageEntity = Message.builder()
                .user(user)
                .chat(chat)
                .time(LocalDateTime.now().plusHours(TIME_CORRECTION))
                .message(message)
                .messageIdReply(messageIdReply)
                .build();
        messageRepository.save(messageEntity);
        return DONE_MESSAGE;
    }

    public String deleteMessage(DeleteMessageRequest request){
        String refreshToken = request.getRefreshToken();
        Long messageId = request.getMessageId();
        if(messageId == 0 || messageId == null){
            return EMPTY_MESSAGE_WARN;
        }
        User user = securityService.getUserByRefreshToken(refreshToken);
        Message message;
        if(messageRepository.existsById(messageId)){
            message = messageRepository.findById(messageId).get();
        } else return MESSAGE_NOT_FOUND_WARN;
        if(!user.getRoles().contains(RoleType.ROLE_ADMIN)){
            if(!user.getId().equals(message.getUser().getId())){
                return "The Message is not yours!";
            }
        }
        messageRepository.delete(message);
        return DONE_MESSAGE;
    }

    public List<ChatDTO> getChatsByUser(Long id){
        User user = userRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(EX_GET_USER_BY_ID + id));
        return user.getChats().stream().map(chat -> ChatDTO.builder()
                .id(chat.getId())
                .chatName(chat.getName())
                .length(messageRepository.countByChat(chat))
                .build()).toList();
    }

    public List<MessageDTO> getChatMessages(Long id, Long chatId){
        User user = userRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(EX_GET_USER_BY_ID + id));
        Chat chat = chatRepository.findById(chatId).orElseThrow(() ->
                new EntityNotFoundException(EX_GET_USER_BY_ID + chatId));

        if (!checkMember(user, chat)){
            throw new EntityNotFoundException(USER_NOT_MEMBER_WARN);
        }
        return chat.getMessages().stream().map(message -> MessageDTO.builder()
                .id(message.getId())
                .userId(message.getUser().getId())
                .login(message.getUser().getLogin())
                .time(message.getTime().format(formatter))
                .text(message.getMessage())
                .replyByMessageId(message.getMessageIdReply())
                .build()).toList();
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

    public List<UserDTO> getListOfChatUsers(Long id, Long chatId){
        User user = userRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(EX_GET_USER_BY_ID + id));
        Chat chat = chatRepository.findById(chatId).orElseThrow(() ->
                new EntityNotFoundException(EX_GET_USER_BY_ID + chatId));

        if (!checkMember(user, chat)){
            throw new EntityNotFoundException(USER_NOT_MEMBER_WARN);
        }
        return chat.getUsers().stream().map(u -> {
            return UserDTO.builder()
                    .id(u.getId())
                    .login(u.getLogin())
                    .firstName(u.getFirstName())
                    .sex(u.getSex())
                    .age(u.getAge())
                    .iconType(user.getIconType())
                    .build();
        }).toList();
    }

    public String createNewChat(Long id, Long toUserId){
        if(id == toUserId){
            return "You trying to write you";
        }
        User user = userRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(EX_GET_USER_BY_ID + id));
        User targetUser = userRepository.findById(toUserId).orElseThrow(() ->
                new EntityNotFoundException(EX_GET_USER_BY_ID + id));
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
                        throw new DuplicateException(DUPLICATE_CAT_WARN);
                    }
                }
            });
        }catch (DuplicateException ex){
            return DUPLICATE_CAT_WARN;
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
        return DONE_MESSAGE;
    }

    public String createNewWarn(CreateWarnRequest request){
        Long targetUserId = request.getUserId();
        String warnMessage = request.getWarnMessage();
        User targetUser;
        if(userRepository.existsById(targetUserId)){
            targetUser = userRepository.findById(targetUserId).get();
        } else return "User not found";
        UserWarning warning = UserWarning.builder()
                .user(targetUser)
                .warningMessage(warnMessage)
                .build();
        userWarningRepository.save(warning);
        return "Warning added";
    }

    public String deleteUserFromChat(Long id, Long userId, Long chatId){
        if(id.equals(userId)){
            return "You trying delete you";
        }
        User targetUser = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException(EX_GET_USER_BY_ID + id));

        Chat chat;
        if(chatRepository.existsById(chatId)){
            chat = chatRepository.findById(chatId).get();
        } else return CHAT_NOT_FOUND_WARN;
        if(targetUser.getChats().remove(chat)){
            userRepository.save(targetUser);
            return DONE_MESSAGE;
        }else return "Error in delete";
    }
}
