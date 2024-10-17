package main.web.controllers;

import lombok.RequiredArgsConstructor;
import main.services.AdminConsoleService;
import main.web.model.SimpleIdRequest;
import main.web.model.SimpleResponse;
import main.web.model.admin.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminConsoleController {
    private final AdminConsoleService adminConsoleService;

    @GetMapping("/join-request")
    public ResponseEntity<List<UserJoinRequestDTO>> getAllUsersJoinRequests(){
        return ResponseEntity.ok(adminConsoleService.getAllUsersJoin());
    }

    @PostMapping("/add-user")
    public ResponseEntity<SimpleResponse> addNewUserRequest(@RequestBody UJRBasicDataRequest request){
        return ResponseEntity.ok(new SimpleResponse(adminConsoleService.addNewUser(request)));
    }

    @Transactional
    @PostMapping("/delete-request")
    public ResponseEntity<SimpleResponse> deleteUserJoinRequest(@RequestBody UJRBasicDataRequest request){
        return ResponseEntity.ok(new SimpleResponse(adminConsoleService.deleteUserJoin(request)));
    }

    @PostMapping("/user/roles")
    public ResponseEntity<SimpleResponse> updateUserRolesRequest(@RequestBody UpdateUserRolesRequest request){
        return ResponseEntity.ok(new SimpleResponse(adminConsoleService.updateUserRoles(request)));
    }

    @Transactional
    @PostMapping("/user/delete")
    public ResponseEntity<SimpleResponse> deleteUserRequest(@RequestBody SimpleIdRequest request){
        return ResponseEntity.ok(new SimpleResponse(adminConsoleService.deleteUser(request)));
    }
}
