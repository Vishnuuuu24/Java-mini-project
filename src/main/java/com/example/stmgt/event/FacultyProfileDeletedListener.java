package com.example.stmgt.event;

import com.example.stmgt.domain.enums.UserRole;
import com.example.stmgt.repository.CustomUserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class FacultyProfileDeletedListener {

    private final CustomUserRepository customUserRepository;

    public FacultyProfileDeletedListener(CustomUserRepository customUserRepository) {
        this.customUserRepository = customUserRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFacultyProfileDeleted(FacultyProfileDeletedEvent event) {
        Long linkedUserId = event.getLinkedUserId();
        if (linkedUserId == null) {
            return;
        }

        customUserRepository.findById(linkedUserId).ifPresent(user -> {
            if (user.getRole() == UserRole.FACULTY) {
                customUserRepository.delete(user);
            }
        });
    }
}
