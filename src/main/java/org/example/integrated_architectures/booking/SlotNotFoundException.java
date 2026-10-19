package org.example.integrated_architectures.booking;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Also thrown for another coach's slot: we don't reveal that the slot exists.
@ResponseStatus(HttpStatus.NOT_FOUND)
public class SlotNotFoundException extends RuntimeException {

    public SlotNotFoundException(Long slotId) {
        super("Slot " + slotId + " not found");
    }
}
