package com.DBMS.iLibrary.service;

import com.DBMS.iLibrary.entity.Subscription;
import com.DBMS.iLibrary.entity.User;
import com.DBMS.iLibrary.repository.SubscriptionRepo;
import com.DBMS.iLibrary.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SubscriptionService {
    @Autowired
    private SubscriptionRepo subsRepo;

    @Autowired
    private UserRepository userRepository;

    public Long findById(String userName)
    {
        Optional<User> user = userRepository.findByUsername(userName);

        return user.map(User::getId).orElse(null);
    }

    public boolean saveNewSubs(Subscription subs)
    {
        subs.setStartDate(LocalDateTime.now());
        String type = String.valueOf(subs.getType());
        if(type.equals("WEEKLY"))
        {
            subs.setPrice(600);
            subs.setEndDate(LocalDateTime.now().plusDays(7));
        }else if(type.equals("MONTHLY"))
        {
            subs.setPrice(3500);
            subs.setEndDate(LocalDateTime.now().plusMonths(1));
        } else if (type.equals("YEARLY")) {
            subs.setPrice(12000);
            subs.setEndDate(LocalDateTime.now().plusYears(1));
        }else
        {
            return false;
        }
        subs.setStatus(Subscription.SubscriptionStatus.valueOf("ACTIVE"));
        subsRepo.save(subs);
        return true;
    }
    public Optional<Subscription> getActiveSubscription(User user) {
        return subsRepo.findByUserAndStatus(user, Subscription.SubscriptionStatus.ACTIVE);
    }

}
