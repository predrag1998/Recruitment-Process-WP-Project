package finki.ukim.mk.projectv2.service;

import finki.ukim.mk.projectv2.bootstrap.DataHolder;
import finki.ukim.mk.projectv2.model.Application;
import finki.ukim.mk.projectv2.model.OpenJobPosition;
import finki.ukim.mk.projectv2.model.Person;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface ApplicationService {
    List<Application> findAll();
    List<Application> findAllByPhase(Long phaseNumber);
    Optional<Application> save(Person person, OpenJobPosition jobPosition);
    Optional<Application> findById(Long id);
    Optional<Application> findByPersonId(Long personId);
    Optional<Application> containMailAndId(String mail,Long id);
    void dropApplication(Long id);
    void dropApplicationByPersonId(Long personId);
}

