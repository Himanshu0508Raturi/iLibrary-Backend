package com.DBMS.iLibrary.service;

import com.DBMS.iLibrary.entity.User;
import com.DBMS.iLibrary.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    private static final PasswordEncoder passwordencoder = new BCryptPasswordEncoder();

    @Transactional
    public RuntimeException saveUser(User user , Set<String> role)
    {
        try
        {
            user.setPassword(passwordencoder.encode(user.getPassword()));
            user.setCreatedAt(new Date());
            user.setRoles(role);
            userRepository.save(user);
        } catch (Exception e) {
            return new RuntimeException("Error while saving user");
        }
        return null;
    }
    public Optional<User> findByUsername(String username)
    {
        return userRepository.findByUsername(username);
    }
    public void deleteById(Long id)
    {
        userRepository.deleteById(id);
    }
}
