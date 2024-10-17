package main.web.controllers;

import lombok.RequiredArgsConstructor;
import main.services.SignorConsoleService;
import main.web.model.SimpleIdRequest;
import main.web.model.SimpleResponse;
import main.web.model.signor.BlockUserRequest;
import main.web.model.signor.CreateNewChatRequest;
import main.web.model.signor.UserBlockResponse;
import main.web.model.chat.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/signorConsole")
@PreAuthorize("hasAnyRole('SIGNOR', 'ADMIN')")
@RequiredArgsConstructor
public class SignorConsoleController {
    private final SignorConsoleService signorConsoleService;

    @GetMapping("/user")
    public ResponseEntity<List<UserDTO>> getAllUsersRequest(){
        return ResponseEntity.ok(signorConsoleService.getAllUsers());
    }

    @PostMapping("/user/warnings")
    public ResponseEntity<List<String>> getUserWarningRequest(@RequestBody SimpleIdRequest request){
        return ResponseEntity.ok(signorConsoleService.getUserWarning(request));
    }

    @GetMapping("/user/block")
    public ResponseEntity<UserBlockResponse> getBlockUserRequest(@RequestParam Long id){
        return ResponseEntity.ok(signorConsoleService.getBlockUser(id));
    }

    @Transactional
    @PostMapping("/user/block")
    public ResponseEntity<SimpleResponse> blockUserRequest(@RequestBody BlockUserRequest request){
        return ResponseEntity.ok(new SimpleResponse(signorConsoleService.blockUserRequest(request)));
    }

    @PostMapping("/user/unblock")
    public ResponseEntity<SimpleResponse> unblockUserRequest(@RequestBody SimpleIdRequest request){
        return ResponseEntity.ok(new SimpleResponse(signorConsoleService.unblockUserRequest(request)));
    }

    @PostMapping("/chat")
    public ResponseEntity<SimpleResponse> createNewChat(@RequestBody CreateNewChatRequest request){
        return ResponseEntity.ok(new SimpleResponse(signorConsoleService.createNewChat(request)));
    }
}
