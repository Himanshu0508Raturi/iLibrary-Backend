package com.DBMS.iLibrary.controllers;

import com.DBMS.iLibrary.entity.Booking;
import com.DBMS.iLibrary.entity.User;
import com.DBMS.iLibrary.entity.Userdto;
import com.DBMS.iLibrary.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.spel.ast.OpAnd;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/changeDetail")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/changeUsername/{newUserName}")
    public ResponseEntity<?> changeUsername(@RequestBody Userdto userdto, @PathVariable String newUserName) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> optionalOldUser = userService.findByUsername(userdto.getUsername());
        if (optionalOldUser.isEmpty()) {
            return new ResponseEntity<>("User with username : " + userdto.getUsername() + " not found.", HttpStatus.NO_CONTENT);
        }
        User oldUser = optionalOldUser.get();
        if (newUserName.isEmpty())
            return new ResponseEntity<>("New User Name can't be blank.", HttpStatus.BAD_REQUEST);
        userService.changeUserName(oldUser, newUserName);
        return new ResponseEntity<>("User Name Changed Successfully. New User name: " + newUserName, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/bookingHistory")
    public ResponseEntity<?> getUserBookingHistory() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> OpUser = userService.findByUsername(username);
        if (OpUser.isEmpty()) {
            return new ResponseEntity<>("User with username : " + username + " not found.", HttpStatus.NO_CONTENT);
        }
        List<Booking> all = userService.getUserBookingHistory(OpUser.get());
        return new ResponseEntity<>(all, HttpStatus.OK);
    }
}
