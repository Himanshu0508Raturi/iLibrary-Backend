package com.DBMS.iLibrary.service;

import com.DBMS.iLibrary.entity.Subscription;
import com.DBMS.iLibrary.entity.SubscriptionPayment;
import com.DBMS.iLibrary.repository.SubscriptionPaymentRepository;
import com.DBMS.iLibrary.repository.SubscriptionRepo;
import com.stripe.model.checkout.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.DBMS.iLibrary.entity.SubscriptionPayment.subscriptionPaymentStatus.*;

@Service
public class subscriptionPaymentService {

    @Autowired
    private SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Autowired
    private SubscriptionRepo subscriptionRepo;

    //method to call before webhook
    public void saveDataBeforePayment(Subscription subscription,String sessionId)
    {
        try
        {
            SubscriptionPayment subscriptionPayment = new SubscriptionPayment();
            subscriptionPayment.setSubscription(subscription);
            subscriptionPayment.setUser(subscription.getUser());
            subscriptionPayment.setAmount(subscription.getAmount());
            subscriptionPayment.setSessionId(sessionId);
            subscriptionPayment.setTransactionDate(LocalDateTime.now());
            subscriptionPayment.setStatus(PENDING);
            subscriptionPaymentRepository.save(subscriptionPayment);
        }catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    //method to call from webhook
    public void saveDataAfterPayment(Session session)
    {
        String sessionId = session.getId();
        Optional<SubscriptionPayment> opPayment = subscriptionPaymentRepository.findBySessionId(sessionId);
        if(opPayment.isPresent())
        {
            SubscriptionPayment subscriptionPayment = opPayment.get();
            Subscription subscription = subscriptionPayment.getSubscription();
            subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
            subscriptionRepo.save(subscription);
            subscriptionPayment.setStatus(COMPLETED);
            subscriptionPaymentRepository.save(subscriptionPayment);
        }else {
            System.err.println("No SubscriptionPayment found with sessionId: " + sessionId);
        }
    }

    public void handleBookingPaymentFailure(Session session)
    {
        Optional<SubscriptionPayment> optionalSubscriptionPayment = subscriptionPaymentRepository.findBySessionId(session.getId());
        if(optionalSubscriptionPayment.isPresent())
        {
            SubscriptionPayment subscriptionPayment = optionalSubscriptionPayment.get();
            subscriptionPayment.setStatus(FAILED);
            subscriptionPaymentRepository.save(subscriptionPayment);
        }else {
            System.err.println("No SubscriptionPayment found with sessionId: " + session.getId());
        }


    }
}
