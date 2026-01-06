package com.atablood.iWindoor_api.auth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class AuthenticationResponse {
    private String token;
    private String errorMessage; // Hata durumunda mesaj dönebilmek için
}