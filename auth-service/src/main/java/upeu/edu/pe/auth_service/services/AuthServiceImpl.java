package upeu.edu.pe.auth_service.services;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import upeu.edu.pe.auth_service.dtos.RegisterDto;
import upeu.edu.pe.auth_service.dtos.TokenDto;
import upeu.edu.pe.auth_service.dtos.UserDto;
import upeu.edu.pe.auth_service.entities.UserEntity;
import upeu.edu.pe.auth_service.helpers.JwtHelper;
import upeu.edu.pe.auth_service.repositories.UserRepository;

@Transactional
@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtHelper jwtHelper;
    private final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);


    private static final String USER_EXCEPTION_MSG = "Error to auth user";

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtHelper jwtHelper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtHelper = jwtHelper;
    }

    @Override
    public TokenDto login(UserDto user) {
        final var userFromDB = this.userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_EXCEPTION_MSG));
        this.validPassword(user, userFromDB);
        return TokenDto.builder().accessToken(this.jwtHelper.createToken(userFromDB.getUsername())).build();

    }

    @Override
    public void register(RegisterDto registerDto) {
        if (userRepository.findByUsername(registerDto.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }

        UserEntity newUser = new UserEntity();
        newUser.setUsername(registerDto.getUsername());
        newUser.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        newUser.setWhatsapp(registerDto.getWhatsapp());

        userRepository.save(newUser);
    }

    @Override
    public TokenDto validateToken(TokenDto token) {
        log.info("AuthServiceImpl:"+token);

        if(this.jwtHelper.validateToken(token.getAccessToken())){
            log.info("ingresa al if de AuthServiceImpl:"+token);
            return TokenDto.builder().accessToken(token.getAccessToken()).build();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_EXCEPTION_MSG);

    }

    private void validPassword(UserDto userDto, UserEntity userEntity) {
        if (!this.passwordEncoder.matches(userDto.getPassword(), userEntity.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_EXCEPTION_MSG);
        }
    }

}