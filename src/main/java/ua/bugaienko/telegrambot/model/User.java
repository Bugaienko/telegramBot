package ua.bugaienko.telegrambot.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * @author Sergii Bugaienko
 */

@Entity
@Table(name = "usersDataTable")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    private Long chatId;

    private String firstName;
    private String lastName;
    private String username;

    private Timestamp registerAt;

    private boolean isActive;

    @Override
    public String toString() {
        return "User{" +
                "chatId=" + chatId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", registerAt=" + registerAt +
                ", isActive=" + isActive +
                '}';
    }
}
