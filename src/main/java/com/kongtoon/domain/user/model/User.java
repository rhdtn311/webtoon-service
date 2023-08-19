package com.kongtoon.domain.user.model;

import com.kongtoon.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Embedded
	private LoginId loginId;

	@Column(name = "name", length = 20, nullable = false)
	private String name;

	@Column(name = "email", unique = true, length = 320, nullable = false)
	private String email;

	@Column(name = "nickname", length = 15, nullable = false)
	private String nickname;

	@Column(name = "password", length = 255, nullable = false)
	private String password;

	@Enumerated(value = EnumType.STRING)
	@Column(name = "authority", length = 20, nullable = false)
	private UserAuthority authority;

	@Column(name = "set_alarm", nullable = false)
	private boolean setAlarm;

	@Column(name = "deleted_at", nullable = true)
	private LocalDateTime deletedAt;

	public User(LoginId loginId, String name, String email, String nickname, String password, UserAuthority authority,
			boolean setAlarm) {
		this.loginId = loginId;
		this.name = name;
		this.email = email;
		this.nickname = nickname;
		this.password = password;
		this.authority = authority;
		this.setAlarm = setAlarm;
	}

	public void setAuthority(UserAuthority authority) {
		this.authority = authority;
	}

	public boolean isAuthor() {
		return this.authority == UserAuthority.AUTHOR;
	}
}
