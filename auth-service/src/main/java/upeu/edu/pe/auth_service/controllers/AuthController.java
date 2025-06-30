package upeu.edu.pe.auth_service.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.auth_service.dtos.RegisterDto;
import upeu.edu.pe.auth_service.dtos.TokenDto;
import upeu.edu.pe.auth_service.dtos.UserDto;
import upeu.edu.pe.auth_service.services.AuthService;

@RestController
@RequestMapping(path = "auth")
public class AuthController {

    private final AuthService authService;
    private final Logger log = LoggerFactory.getLogger(AuthController.class);


    public AuthController(AuthService authService) {
        this.authService = authService;
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
}
