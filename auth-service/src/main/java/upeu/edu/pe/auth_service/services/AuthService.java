package upeu.edu.pe.auth_service.services;

import upeu.edu.pe.auth_service.dtos.CodeDto;
import upeu.edu.pe.auth_service.dtos.RegisterDto;
import upeu.edu.pe.auth_service.dtos.TokenDto;
import upeu.edu.pe.auth_service.dtos.UserDto;

public interface AuthService {
    TokenDto login(UserDto user);
    TokenDto validateToken(TokenDto token);
    TokenDto verifyAccessCode(CodeDto codeDto);
    void register(RegisterDto registerDto);
}
