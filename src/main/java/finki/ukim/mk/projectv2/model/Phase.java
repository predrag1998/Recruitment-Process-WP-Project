package finki.ukim.mk.projectv2.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Entity
public class Phase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Long phaseNumber;

    public Phase() {}
    public Phase(String name, String description, Long phaseNumber) {
        this.name = name;
        this.description = description;
        this.phaseNumber = phaseNumber;
    }
}
