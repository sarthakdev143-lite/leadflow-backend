package com.leadflow.leadflow_backend.model;
import com.leadflow.leadflow_backend.domain.Role;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String name;

    @Indexed(unique = true)
    private String phone;

    private String password;

    private Role role;

    private LocalDateTime createdAt;
}