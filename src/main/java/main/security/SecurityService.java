package main.security;

import lombok.RequiredArgsConstructor;
import main.entity.*;
import main.exception.AlreadyExistException;
import main.exception.RefreshTokenException;
import main.repository.*;
import main.security.jwt.JwtUtils;
import main.services.RefreshTokenService;
import main.web.model.auth.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SecurityService {

    @Value("${firstChat.name}")
    private String nameFirstChat;

    private final AuthenticationManager authenticationManager;

    private final JwtUtils jwtUtils;

    private final RefreshTokenService refreshTokenService;

    private final UserRepository userRepository;

    private final ChatRepository chatRepository;

    private final MessageRepository messageRepository;

    private final UserJoinRequestRepository userJoinRequestRepository;

    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthResponse authenticateUser(LoginRequest loginRequest){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getLogin(),
                loginRequest.getPassword()
        ));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        List<String> roles = securityUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                .toList();

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(securityUser.getId());

        return AuthResponse.builder()
                .id(securityUser.getId())
                .token(jwtUtils.generateJwtToken(securityUser))
                .refreshToken(refreshToken.getToken())
                .login(securityUser.getLogin())
                .email(securityUser.getEmail())
                .roles(roles)
                .build();
    }

    public ApplicationToJoinResponse saveApplicationToJoin(ApplicationToJoinRequest request, String sessionId)
            throws AlreadyExistException{
        String login = request.getLogin().trim();
        String password = request.getPassword();
        String email = request.getEmail().trim();
        if(login.isEmpty() || password.isEmpty() || email.isEmpty()){
            throw new AlreadyExistException("Empty fields, try again");
        }
        //add check login and password by symbols
        if(userRepository.count()==0){
            createFirstUser(request);
            return ApplicationToJoinResponse.builder()
                    .message("Congratulation!! You first user!")
                    .build();
        }
        if(userRepository.existsByLogin(login) || userJoinRequestRepository.existsByLogin(login)){
            throw new AlreadyExistException("This login is busy");
        }
        if(userRepository.existsByEmail(email)){
            throw new AlreadyExistException("This email is busy");
        }
        if(userJoinRequestRepository.existsBySessionId(sessionId)){
            throw new AlreadyExistException("You created request. Please wait admin answer");
        }

        userJoinRequestRepository.save(UserJoinRequest.builder()
                .login(login)
                .password(passwordEncoder.encode(password))
                .email(email)
                .messageToAdmin(request.getMessage())
                .sessionId(sessionId)
                .build());
        return ApplicationToJoinResponse.builder()
                .message("Your application is created. The administrator will review your application as soon as possible")
                .build();
    }

    private void createFirstUser(ApplicationToJoinRequest request) {
        Set<RoleType> roles = new HashSet<>();
        roles.add(RoleType.ROLE_ADMIN);
        List<Chat> chats = new ArrayList<>();
        Chat firstChatFromDB;
        if(!chatRepository.existsByName(nameFirstChat)){
            Chat firstChat = new Chat();
            firstChat.setName(nameFirstChat);
            chatRepository.save(firstChat);
        }
        firstChatFromDB = chatRepository.findByName(nameFirstChat).get();
        chats.add(firstChatFromDB);

        User user = User.builder()
                .login(request.getLogin())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .registrationDate(LocalDateTime.now())
                .status(Status.ACTIVE)
                .roles(roles)
                .chats(chats)
                .build();
        userRepository.save(user);
    }

    public void register(CreateUserRequest createUserRequest){
        if(userRepository.existsByLogin(createUserRequest.getLogin())){
            throw new AlreadyExistException("Login already exist!");
        }
        if(userRepository.existsByEmail(createUserRequest.getLogin())){
            throw new AlreadyExistException("Email already exist!");
        }
        Chat firstChat = chatRepository.findByName(nameFirstChat).get();
        List<Chat> chats = new ArrayList<>();
        chats.add(firstChat);
        var user = User.builder()
                .login(createUserRequest.getLogin())
                .email(createUserRequest.getEmail())
                .password(createUserRequest.getPassword())
                .registrationDate(LocalDateTime.now())
                .status(Status.ACTIVE)
                .roles(createUserRequest.getRoles())
                .chats(chats)
                .build();
        userRepository.save(user);
    }

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request){
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByRefreshToken(requestRefreshToken)
                .map(refreshTokenService::chekRefreshToken)
                .map(RefreshToken::getUserId)
                .map(userId -> {
                    User tokenOwner = userRepository.findById(userId).orElseThrow(() ->
                            new RefreshTokenException("Exception trying to get token for userId: " + userId));
                    String token = jwtUtils.generateTokenFromLogin(tokenOwner.getLogin());
                    return new RefreshTokenResponse(token, refreshTokenService.createRefreshToken(userId).getToken());
                }).orElseThrow(() -> new RefreshTokenException(requestRefreshToken, "Refresh token not found: "));
    }

    public User getUserByRefreshToken(String requestRefreshToken){
        long userId = refreshTokenRepository.findByToken(requestRefreshToken).orElseThrow(() ->
                new RefreshTokenException("Exception trying to get token for userId: " + requestRefreshToken)).getUserId();
        return userRepository.findById(userId).orElseThrow(() ->
                new RefreshTokenException("Exception trying to get token for userId: " + userId));
    }

    public void updateUser(User user){
        userRepository.save(user);
    }

    @Transactional
    public void logout(){
        var currentPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(currentPrincipal instanceof SecurityUser securityUser) {
            Long userId = securityUser.getId();
            deleteToken(userId);
        }
        SecurityContextHolder.clearContext();
    }

    @Transactional
    public void deleteUser(User user){
        deleteUserChatMessages(user);
        messageRepository.deleteByUser(user);
        deleteToken(user.getId());
        userRepository.delete(user);
    }

    @Transactional
    public void deleteToken(Long userId){
        refreshTokenService.deleteTokenByUserId(userId);
    }

    @Transactional
    public void deleteUserChatMessages(User user){
        user.getChats().forEach(chat -> {
            if(!chat.getName().equals(nameFirstChat)){
                if(chat.getUsers().size() == 2) {
                    chat.getUsers().forEach(chatUser -> {
                        ArrayList<Chat> chats = new ArrayList<>(chatUser.getChats());
                        System.out.println(chats.size());
                        chats.remove(chat);
                        System.out.println(chats.size());
                        chatUser.setChats(chats);
                        userRepository.save(chatUser);
                    });
                    chatRepository.delete(chat);
                }
            }
        });
    }

    public boolean checkLastAdmin(){
        if(userRepository.countByRoles(RoleType.ROLE_ADMIN) == 1){
            return true;
        } else return false;
    }

}
