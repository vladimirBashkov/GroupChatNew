package main.services;

import lombok.AllArgsConstructor;
import main.entity.Chat;
import main.entity.RoleType;
import main.entity.Status;
import main.entity.User;
import main.entity.UserWarning;
import main.exception.EntityNotFoundException;
import main.repository.ChatRepository;
import main.repository.UserRepository;
import main.security.SecurityService;
import main.web.model.SimpleIdRequest;
import main.web.model.chat.UserDTO;
import main.web.model.signor.BlockUserRequest;
import main.web.model.signor.CreateNewChatRequest;
import main.web.model.signor.UserBlockResponse;
import main.web.model.signor.UserToChatDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class SignorConsoleService {
    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final ChatRepository chatRepository;
    private final String USER_NOT_EXIST_WARN = "User not exist";

    public List<UserDTO> getAllUsers(){
        return userRepository.findAll().stream()
                .map(u -> UserDTO.builder()
                        .id(u.getId())
                        .login(u.getLogin())
                        .email(u.getEmail())
                        .roles(u.getRoles().stream().map(roleType -> roleType.name().replaceAll("ROLE_", "")).toList())
                        .firstName(u.getFirstName())
                        .lastName(u.getLastName())
                        .sex(u.getSex())
                        .blockTime(u.getBlockTime())
                        .reasonOfBlocking(u.getReasonForBlocking())
                        .startOfBlocking(u.getTimeStartBlock() == null ? "0" : u.getTimeStartBlock().toString())
                        .build())
                .toList();
    }

    public List<String> getUserWarning(SimpleIdRequest request){
        return userRepository.findById(request.getId()).get().getWarnings().stream()
                .map(UserWarning::getWarningMessage)
                .toList();
    }

    public UserBlockResponse getBlockUser(Long id){
        if(userRepository.existsById(id)){
            User user = userRepository.findById(id).get();
            return UserBlockResponse.builder()
                    .blockTime(checkUserBlock(user))
                    .reason(user.getReasonForBlocking())
                    .build();
        } else throw new EntityNotFoundException(USER_NOT_EXIST_WARN);
    }

    private Long checkUserBlock(User user){
        Long blockTime = user.getBlockTime();
        if(blockTime == null || blockTime == 0L){
            return 0L;
        }
        LocalDateTime timeStartBlock = user.getTimeStartBlock();
        if(timeStartBlock.plusHours(blockTime).isBefore(LocalDateTime.now())){
            unblockUser(user);
            return 0L;
        }
        long timeBetween = ChronoUnit.HOURS.between(LocalDateTime.now(), timeStartBlock.plusHours(blockTime));
        if(timeBetween == 0) {
            return 1L;
        }
        return timeBetween;
    }

    private void unblockUser(User user){
        user.setBlockTime(0L);
        user.setReasonForBlocking("");
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);
    }

    @Transactional
    public String blockUserRequest(BlockUserRequest request){
        if(userRepository.existsById(request.getId())){
            User user = userRepository.findById(request.getId()).get();
            if(user.getRoles().contains(RoleType.ROLE_ADMIN) && securityService.checkLastAdmin()){
                return "You are the last ADMIN. Cancelled!!!";
            }
            if(request.getBlockTime() > 0){
                user.setStatus(Status.BANNED);
                user.setTimeStartBlock(LocalDateTime.now());
                user.setBlockTime(request.getBlockTime());
                user.setReasonForBlocking(request.getReasonOfBlocking());
                userRepository.save(user);
                securityService.deleteToken(user.getId());
                return user.getLogin() + " is blocked!";
            } else return "Wrong time";
        } else return USER_NOT_EXIST_WARN;
    }

    public String unblockUserRequest(SimpleIdRequest request){
        if(userRepository.existsById(request.getId())){
            User user = userRepository.findById(request.getId()).get();
            unblockUser(user);
            return "User - " + user.getLogin() + " unblocked";
        } else throw new EntityNotFoundException(USER_NOT_EXIST_WARN);
    }

    public String createNewChat( CreateNewChatRequest request){
        String chatName = request.getChatName();
        if(chatRepository.existsByName(chatName)){
            return "ChatName - " + request.getChatName() + " -  is busy";
        }
        ArrayList<UserToChatDTO> usersToChat = new ArrayList<>(request.getUsersList());
        if(usersToChat.size() < 3){
            return "Too few members";
        }
        ArrayList<User> users = new ArrayList<>();
        usersToChat.forEach(userToChatDTO -> {
            User user = userRepository.findById(userToChatDTO.getId()).orElseThrow(() ->
                    new EntityNotFoundException("Exception trying to get User for userId: " + userToChatDTO.getId()));
            users.add(user);
        });
        Chat chat = Chat.builder()
                .name(chatName).build();
        chatRepository.save(chat);
        Chat chatFromDB = chatRepository.findByName(chatName).get();
        users.forEach(user -> {
            user.addChat(chatFromDB);
            userRepository.save(user);
        });
        return "Chat - " + request.getChatName() + " - created";
    }
}
