package upeu.edu.pe.auth_service.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.auth_service.dtos.CodeDto;
import upeu.edu.pe.auth_service.dtos.RegisterDto;
import upeu.edu.pe.auth_service.dtos.TokenDto;
import upeu.edu.pe.auth_service.dtos.UserDto;
import upeu.edu.pe.auth_service.entities.UserEntity;
import upeu.edu.pe.auth_service.repositories.UserRepository;
import upeu.edu.pe.auth_service.services.AuthService;

@RestController
@RequestMapping(path = "auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final Logger log = LoggerFactory.getLogger(AuthController.class);


    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping(path = "login") //password = secret2025
    public ResponseEntity<TokenDto> jwtCreate(@RequestBody UserDto user) {
        return ResponseEntity.ok(this.authService.login(user));
    }

    @PostMapping(path = "register")
    public ResponseEntity<Void> registerUser(@RequestBody RegisterDto registerDto) {
        authService.register(registerDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping(path = "jwt")
    public ResponseEntity<TokenDto> jwtValidate(@RequestHeader String accessToken) {
        log.info("Auth_controller:"+accessToken);

        return
                ResponseEntity.ok(
                        this.authService.validateToken(TokenDto.builder().accessToken(accessToken).build()));
    }

    @GetMapping(path = "admin-exists")
    public boolean adminExists() {
        return userRepository.findAll().stream().anyMatch(u -> u.getEmail() != null);
    }

    @PostMapping(path = "verify-code") // Valida código y genera JWT real
    public ResponseEntity<TokenDto> verifyAccessCode(@RequestBody CodeDto codeDto) {
        return ResponseEntity.ok(authService.verifyAccessCode(codeDto));
    }

    @GetMapping(path = "admin-email")
    public String getAdminEmail() {
        return userRepository.findAll().stream()
                .filter(u -> u.getEmail() != null && !u.getEmail().isEmpty())
                .map(UserEntity::getEmail)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No admin email found"));
    }

}
