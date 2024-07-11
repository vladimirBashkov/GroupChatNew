package main.web.controllers;

import lombok.RequiredArgsConstructor;
import main.entity.Chat;
import main.entity.RoleType;
import main.entity.Status;
import main.entity.User;
import main.exception.EntityNotFoundException;
import main.repository.ChatRepository;
import main.repository.UserRepository;
import main.security.SecurityService;
import main.web.model.SimpleIdRequest;
import main.web.model.SimpleResponse;
import main.web.model.signor.BlockUserRequest;
import main.web.model.signor.CreateNewChatRequest;
import main.web.model.signor.UserBlockResponse;
import main.web.model.chat.UserDTO;
import main.web.model.signor.UserToChatDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin/signorConsole")
@PreAuthorize("hasAnyRole('SIGNOR', 'ADMIN')")
@RequiredArgsConstructor
public class SignorConsoleController {
    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final ChatRepository chatRepository;

    @GetMapping("/user")
    public ResponseEntity<List<UserDTO>> getAllUsersRequest(){
        return ResponseEntity.ok(userRepository.findAll().stream()
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
                        //.startOfBlocking(u.getTimeStartBlock().toString().)
                        .build())
                .toList());
    }

    @PostMapping("/user/warnings")
    public ResponseEntity<List<String>> getUserWarningRequest(@RequestBody SimpleIdRequest request){
        return ResponseEntity.ok(userRepository.findById(request.getId()).get().getWarnings().stream()
                .map(uw -> uw.getWarningMessage())
                .toList());
    }

    @GetMapping("/user/block")
    public ResponseEntity<UserBlockResponse> getBlockUserRequest(@RequestParam Long id){
        if(userRepository.existsById(id)){
            User user = userRepository.findById(id).get();
            return ResponseEntity.ok(UserBlockResponse.builder()
                    .blockTime(checkUserBlock(user))
                    .reason(user.getReasonForBlocking())
                    .build());
        } else throw new EntityNotFoundException("User not exist");
    }

    private Long checkUserBlock(User user){
        Long blockTime = user.getBlockTime();
        if(blockTime == null || blockTime == 0l){
            return 0l;
        }
        LocalDateTime timeStartBlock = user.getTimeStartBlock();
        if(timeStartBlock.plusHours(blockTime).isBefore(LocalDateTime.now())){
            unblockUser(user);
            return 0l;
        }
        Long timeBetween = ChronoUnit.HOURS.between(LocalDateTime.now(), timeStartBlock.plusHours(blockTime));
        if(timeBetween == 0) {
            return 1l;
        }
        return timeBetween;
    }

    private void unblockUser(User user){
        user.setBlockTime(0l);
        user.setReasonForBlocking("");
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);
    }

    @Transactional
    @PostMapping("/user/block")
    public ResponseEntity<SimpleResponse> blockUserRequest(@RequestBody BlockUserRequest request){
        if(userRepository.existsById(request.getId())){
            User user = userRepository.findById(request.getId()).get();
            if(user.getRoles().contains(RoleType.ROLE_ADMIN) && securityService.checkLastAdmin()){
                return ResponseEntity.ok(new SimpleResponse("You are the last ADMIN. Cancelled!!!"));
            }
            if(request.getBlockTime() > 0){
                user.setStatus(Status.BANNED);
                user.setTimeStartBlock(LocalDateTime.now());
                user.setBlockTime(request.getBlockTime());
                user.setReasonForBlocking(request.getReasonOfBlocking());
                userRepository.save(user);
                securityService.deleteToken(user.getId());
                return ResponseEntity.ok(new SimpleResponse(user.getLogin() + " is blocked!"));
            } else return ResponseEntity.ok(new SimpleResponse("Wrong time"));
        } else return ResponseEntity.ok(new SimpleResponse("User not exist"));
    }

    @PostMapping("/user/unblock")
    public ResponseEntity<SimpleResponse> unblockUserRequest(@RequestBody SimpleIdRequest request){
        if(userRepository.existsById(request.getId())){
            User user = userRepository.findById(request.getId()).get();
            unblockUser(user);
            return ResponseEntity.ok(new SimpleResponse("User - " + user.getLogin() + " unblocked"));
        } else throw new EntityNotFoundException("User not exist");
    }


    @PostMapping("/chat")
    public ResponseEntity<SimpleResponse> createNewChat(@RequestBody CreateNewChatRequest request){
        String chatName = request.getChatName();
        if(chatRepository.existsByName(chatName)){
            return ResponseEntity.badRequest().body(new SimpleResponse("ChatName - " + request.getChatName() + " -  is busy"));
        }
        ArrayList<UserToChatDTO> usersToChat = new ArrayList<>(request.getUsersList());
        if(usersToChat.size() < 3){
            return ResponseEntity.badRequest().body(new SimpleResponse("Too few members"));
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
        return ResponseEntity.ok(new SimpleResponse("Chat - " + request.getChatName() + " - created"));
    }







}
