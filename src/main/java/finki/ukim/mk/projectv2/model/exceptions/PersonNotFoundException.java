package finki.ukim.mk.projectv2.model.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.NOT_FOUND)
public class PersonNotFoundException  extends RuntimeException{
    public PersonNotFoundException(Long id) {
        super(String.format("Person with id %d is not found ",id));
    }
}
