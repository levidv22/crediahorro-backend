package upeu.edu.pe.admin_core_service.helpers;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtHelperAdmin jwtHelper;

    public JwtAuthFilter(JwtHelperAdmin jwtHelper) {
        this.jwtHelper = jwtHelper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.replace("Bearer ", "");
            try {
                String role = jwtHelper.extractRole(token);

                String path = request.getRequestURI();

                // 👮 Restricción: si es USUARIO, solo puede entrar a /clientes/mis-prestamos o /clientes/mis-cuotas/**
                if ("USUARIO".equalsIgnoreCase(role)) {
                    boolean rutaPermitida =
                            path.startsWith("/admin-service/usuarios/mis-prestamos") ||
                                    path.startsWith("/admin-service/usuarios/mis-cuotas") ||
                                    path.startsWith("/admin-service/usuarios-solicitudes-pago");

                    if (!rutaPermitida) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("Acceso denegado para usuarios");
                        return;
                    }
                }

                // ADMIN: role vacío -> puede acceder a todo
                if (role == null || role.isBlank()) {
                    // sin restricciones
                }

            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token inválido");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
