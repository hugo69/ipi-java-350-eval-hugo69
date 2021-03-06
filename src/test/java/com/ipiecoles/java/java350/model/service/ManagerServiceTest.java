package com.ipiecoles.java.java350.model.service;

import com.ipiecoles.java.java350.model.Employe;
import com.ipiecoles.java.java350.model.Manager;
import com.ipiecoles.java.java350.model.Technicien;
import com.ipiecoles.java.java350.repository.ManagerRepository;
import com.ipiecoles.java.java350.repository.TechnicienRepository;
import com.ipiecoles.java.java350.service.ManagerService;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityNotFoundException;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.AdditionalAnswers.returnsFirstArg;

/**
 * Created by HCHARBONNEYR on 13/03/2018.
 */
@RunWith(MockitoJUnitRunner.class)
public class ManagerServiceTest {
    @InjectMocks
    ManagerService managerService;

    @Mock
    ManagerRepository managerRepository;

    @Mock
    TechnicienRepository technicienRepository;

    private HashSet<Technicien> equipe = new HashSet<>();

    @Test
    public void testDeleteTechniciens(){
        //Given
        Manager m = new Manager();
        Technicien t = new Technicien();
        m.getEquipe().add(t);
        Mockito.when(managerRepository.findOne(1L)).thenReturn(m);
        Mockito.when(technicienRepository.findOne(2L)).thenReturn(t);

        //When
        managerService.deleteTechniciens(1L,2L);
        //Then
        ArgumentCaptor<Manager> argManager = ArgumentCaptor.forClass(Manager.class);
        Mockito.verify(managerRepository).save(argManager.capture());
        Assertions.assertThat(argManager.getValue().getEquipe()).isEmpty();

        ArgumentCaptor<Technicien> argTechnicien = ArgumentCaptor.forClass(Technicien.class);
        Mockito.verify(technicienRepository).save(argTechnicien.capture());
        Assertions.assertThat(argTechnicien.getValue().getManager()).isNull();
    }

    @Test
    public void testAddTechnicienOK(){
        //Given
        Manager m = new Manager();
        Technicien t = new Technicien();

        Mockito.when(managerRepository.findOneWithEquipeById(1L)).thenReturn(m);
        Mockito.when(managerRepository.save(Mockito.any(Manager.class))).then(returnsFirstArg());
        Mockito.when(technicienRepository.findByMatricule("2L")).thenReturn(t);

        //when
        managerService.addTechniciens(1L,"2L");

        //then
        ArgumentCaptor<Manager> argManager = ArgumentCaptor.forClass(Manager.class);
        Mockito.verify(managerRepository).save(argManager.capture());
        Assertions.assertThat(argManager.getValue().getEquipe()).isNotEmpty();
        Assertions.assertThat(argManager.getValue().getEquipe()).contains(t);

        ArgumentCaptor<Technicien> argTechnicien = ArgumentCaptor.forClass(Technicien.class);
        Mockito.verify(technicienRepository).save(argTechnicien.capture());
        Assertions.assertThat(argTechnicien.getValue().getManager()).isNotNull();
        Assertions.assertThat(argTechnicien.getValue().getManager()).isEqualTo(m);
    }

    @Test
    public void testAddTechnicienNOTOKManager(){
        //Given
        Manager m = new Manager();
        Technicien t = new Technicien();

        Mockito.when(managerRepository.findOneWithEquipeById(1L)).thenReturn(null);
        //Mockito.when(managerRepository.save(Mockito.any(Manager.class))).then(returnsFirstArg());
        //Mockito.when(technicienRepository.findByMatricule("2L")).thenReturn(null);

        //when
        try {
            managerService.addTechniciens(1L, "2L");
            Assertions.fail("Cela aurait dû planter");
        }catch (Exception e){
            Assertions.assertThat(e).isInstanceOf(EntityNotFoundException.class);
            Assertions.assertThat(e).hasMessage("Impossible de trouver le manager d'identifiant 1");
            //Assertions.assertThat(e).hasMessage("Impossible de trouver le technicien de matricule 2L");
        }
    }
    @Test
    public void testAddTechnicienNOTOKTech(){
        //Given
        Manager m = new Manager();
        Technicien t = new Technicien();

        Mockito.when(managerRepository.findOneWithEquipeById(1L)).thenReturn(m);
        Mockito.when(managerRepository.save(Mockito.any(Manager.class))).then(returnsFirstArg());
        Mockito.when(technicienRepository.findByMatricule("2L")).thenReturn(null);

        //when
        try {
            managerService.addTechniciens(1L, "2L");
            Assertions.fail("Cela aurait dû planter");
        }catch (Exception e){
            Assertions.assertThat(e).isInstanceOf(EntityNotFoundException.class);
            //Assertions.assertThat(e).hasMessage("Impossible de trouver le manager d'identifiant 4");
            Assertions.assertThat(e).hasMessage("Impossible de trouver le technicien de matricule 2L");
        }
    }

    //Je voulais tester le cas ou le technicien possède déjà un manager (malheureusement je ne penses pas traiter tous les cas) ----
    @Test
    public void testAddTechnicienTechWithManager(){
        Manager manager1 = new Manager("bob","john","1L",null,1300d, equipe);
        Manager manager2 = new Manager("richard","martin","2L",null,1400d,equipe);
        Technicien technicien = new Technicien();

        Mockito.when(managerRepository.findOneWithEquipeById(1L)).thenReturn(manager1);
        Mockito.when(managerRepository.findOneWithEquipeById(2L)).thenReturn(manager2);
        Mockito.when(managerRepository.save(Mockito.any(Manager.class))).then(returnsFirstArg());
        Mockito.when(technicienRepository.findByMatricule("3L")).thenReturn(technicien);

        managerService.addTechniciens(1L,"3L");

        try{
            managerService.addTechniciens(2L, "3L");
            Assertions.fail("Cela aurait dû planter");
        }catch(Exception e) {
            Assertions.assertThat(e).isInstanceOf(IllegalArgumentException.class);
            //Assertions.assertThat(e).hasMessage("Impossible de trouver le manager d'identifiant 4");
            Assertions.assertThat(e).hasMessage("Le technicien de matricule 3L a déjà un manager : " + technicien.getManager().getPrenom() + " " + technicien.getManager().getNom()
                    + " (matricule " + technicien.getManager().getMatricule() + ")");
        }
    }
}
