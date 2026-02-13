package com.blog.platform.user.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserMetricsService {

    private final Counter userRegistrationCounter;
    private final Counter userLoginCounter;
    private final Counter userLoginFailureCounter;
    private final Counter userProfileUpdateCounter;
    private final Timer userRegistrationTimer;
    private final Timer userLoginTimer;
    private final UserMetricsConfiguration metricsConfiguration;
    private final MeterRegistry meterRegistry;

    @Autowired
    public UserMetricsService(MeterRegistry meterRegistry,
                             UserMetricsConfiguration metricsConfiguration) {
        this.meterRegistry = meterRegistry;
        this.metricsConfiguration = metricsConfiguration;
        
        this.userRegistrationCounter = Counter.builder("user.registrations")
            .description("Number of user registrations")
            .register(meterRegistry);
        this.userLoginCounter = Counter.builder("user.logins")
            .description("Number of user logins")
            .register(meterRegistry);
        this.userLoginFailureCounter = Counter.builder("user.login.failures")
            .description("Number of failed login attempts")
            .register(meterRegistry);
        this.userProfileUpdateCounter = Counter.builder("user.profile.updates")
            .description("Number of profile updates")
            .register(meterRegistry);
        this.userRegistrationTimer = Timer.builder("user.registration.time")
            .description("Time taken for user registration")
            .register(meterRegistry);
        this.userLoginTimer = Timer.builder("user.login.time")
            .description("Time taken for user login")
            .register(meterRegistry);
    }

    public void recordUserRegistration() {
        userRegistrationCounter.increment();
        metricsConfiguration.getTotalRegistrations().incrementAndGet();
    }

    public void recordUserLogin(boolean success) {
        userLoginCounter.increment();
        if (!success) {
            userLoginFailureCounter.increment();
            metricsConfiguration.getFailedLogins().incrementAndGet();
        }
    }

    public void recordUserProfileUpdate() {
        userProfileUpdateCounter.increment();
    }

    public Timer.Sample startRegistrationTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopRegistrationTimer(Timer.Sample sample) {
        sample.stop(userRegistrationTimer);
    }
    
    public Timer.Sample startLoginTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopLoginTimer(Timer.Sample sample) {
        sample.stop(userLoginTimer);
    }

    public void incrementActiveUsers() {
        metricsConfiguration.getActiveUsers().incrementAndGet();
    }

    public void decrementActiveUsers() {
        metricsConfiguration.getActiveUsers().decrementAndGet();
    }
}
