package upeu.edu.pe.auth_service.services;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import upeu.edu.pe.auth_service.dtos.CodeDto;
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
    private final JavaMailSender mailSender;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtHelper jwtHelper, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtHelper = jwtHelper;
        this.mailSender = mailSender;
    }

    @Override
    public TokenDto login(UserDto user) {
        final var userFromDB = this.userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_EXCEPTION_MSG));

        this.validPassword(user, userFromDB);

        // Generar código de acceso
        String code = String.valueOf((int) ((Math.random() * 900000) + 100000));
        userFromDB.setAccessCode(code);
        userRepository.save(userFromDB);

        // Enviar email si existe
        String adminEmail = userRepository.findAll().stream()
                .filter(u -> u.getEmail() != null)
                .map(UserEntity::getEmail)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Admin email not found"));

        enviarEmail(adminEmail, code, userFromDB);

        return TokenDto.builder().accessToken("Código enviado. Verifica tu correo.").build();
    }

    @Override
    public void register(RegisterDto registerDto) {
        if (userRepository.findByUsername(registerDto.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }

        boolean adminExists = userRepository.findAll().stream().anyMatch(u -> u.getEmail() != null);

        if (adminExists && registerDto.getEmail() != null && !registerDto.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only first user can register with email");
        }

        if (!adminExists && (registerDto.getEmail() == null || registerDto.getEmail().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "First user must provide an email");
        }

        UserEntity newUser = new UserEntity();
        newUser.setUsername(registerDto.getUsername());
        newUser.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        newUser.setWhatsapp(registerDto.getWhatsapp());
        if (!adminExists) {
            newUser.setEmail(registerDto.getEmail());
        }

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

    @Override
    public TokenDto verifyAccessCode(CodeDto codeDto) {
        UserEntity user = userRepository.findByUsername(codeDto.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_EXCEPTION_MSG));

        if (user.getAccessCode() == null || !user.getAccessCode().equals(codeDto.getAccessCode())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid access code");
        }

        // Limpiar el código una vez usado
        user.setAccessCode(null);
        userRepository.save(user);

        return TokenDto.builder()
                .accessToken(jwtHelper.createToken(user.getUsername()))
                .build();
    }

    private void validPassword(UserDto userDto, UserEntity userEntity) {
        if (!this.passwordEncoder.matches(userDto.getPassword(), userEntity.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_EXCEPTION_MSG);
        }
    }

    private void enviarEmail(String to, String code, UserEntity user) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress("levidiaz222@gmail.com", "CrediAhorro Soporte 🚀"));
            helper.setTo(to);
            helper.setSubject("🔐 Nuevo Código de Acceso - CrediAhorro");

            String htmlContent = """
            <div style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 8px; padding: 20px;">
                <h2 style="color: #007bff; text-align: center;">🔑 Verificación de Acceso</h2>
                <p>Hola Administrador,</p>
                <p>El siguiente usuario está intentando iniciar sesión:</p>
                <ul style="list-style: none; padding-left: 0;">
                    <li><strong>👤 Usuario:</strong> %s</li>
                    <li><strong>📱 WhatsApp:</strong> %s</li>
                </ul>
                <p style="margin-top: 20px;">Su código de acceso es:</p>
                <div style="background: #007bff; color: #fff; font-size: 24px; font-weight: bold; text-align: center; padding: 10px; border-radius: 4px;">
                    %s
                </div>
                <p style="margin-top: 20px;">Este código es válido por unos minutos. Si no reconoces esta actividad, por favor revisa tu panel de administración.</p>
                <p style="text-align: center; margin-top: 30px; font-size: 12px; color: #888;">© 2025 CrediAhorro - Todos los derechos reservados</p>
            </div>
        """.formatted(user.getUsername(), user.getWhatsapp(), code);

            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            log.error("Error enviando correo: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo enviar el correo.");
        }
    }

}