package com.adamfgcross.concurrentcomputations.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "username")
	private String username;
	
	@Column(unique = true)
	private String tempKey;
	
	public String getTempKey() {
		return tempKey;
	}
	public void setTempKey(String tempKey) {
		this.tempKey = tempKey;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	private String password;
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.emptyList();
	}
	
	private Boolean isEnabled;
	
	public Boolean isEnabled() {
		return isEnabled;
	}
	public void setIsEnabled(Boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	@Override
    public boolean equals(Object o) {
        if (this == o) return true; // Check if they are the same instance
        if (o == null || getClass() != o.getClass()) return false; // Check if o is null or not of the same class
        User user = (User) o; // Cast o to User and compare relevant fields
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Generate hash code based on relevant fields
    }
	
}
