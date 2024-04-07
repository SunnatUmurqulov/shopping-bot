package com.company.shoppingbotv2.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.company.shoppingbotv2.utils.AppConstants;

import java.io.IOException;

@Component
public class SecretKeyFilter extends OncePerRequestFilter {
    @Value("${app.secret-key}")
    private String secretKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader(AppConstants.X_API_TELEGRAM_BOT_KEY_HEADER);
        if (!this.secretKey.equals(token)) {
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            response.getWriter().print("Ha ko't bacha mani aldamoqchi bo'ldingmi yirtaman \uD83D\uDD95");
        }
        filterChain.doFilter(request, response);
    }
}
