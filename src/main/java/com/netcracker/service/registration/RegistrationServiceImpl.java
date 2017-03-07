package com.netcracker.service.registration;

import com.netcracker.exception.BadEmployeeException;
import com.netcracker.exception.ResourceAlreadyExistsException;
import com.netcracker.exception.ResourceNotFoundException;
import com.netcracker.model.entity.Person;
import com.netcracker.model.entity.Role;
import com.netcracker.model.entity.Token;
import com.netcracker.model.entity.TokenType;
import com.netcracker.model.event.NewPasswordEvent;
import com.netcracker.model.event.PersonRegistrationEvent;
import com.netcracker.model.event.ResetPasswordEvent;
import com.netcracker.repository.data.interfaces.PersonRepository;
import com.netcracker.repository.data.interfaces.RoleRepository;
import com.netcracker.repository.data.interfaces.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    static final int EXPIRATION = 60 * 24;

    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public Token registerPerson(Person person, String requestLink, int roleId) throws Exception {

        Optional<Person> savedOptional = this.personRepository.findPersonByEmail(person.getEmail());
        if (savedOptional.isPresent()) {
            if (savedOptional.get().isEnabled()) {
                throw new ResourceAlreadyExistsException(Person.TABLE_NAME);
            } else {
                Optional<Token> oldTokenOptional = this.tokenRepository.findRegistrationTokenByPerson(savedOptional.get().getId());
                if (oldTokenOptional.isPresent()) {
                    oldTokenOptional.get().setDateExpired(this.calculateDateExpired());
                    this.publishOnRegistrationCompleteEvent(savedOptional.get(), this.tokenRepository.save(oldTokenOptional.get()), requestLink);
                }
                if (!oldTokenOptional.isPresent()) {
                    Token token = this.createVerificationToken(savedOptional.get());
                    Optional<Token> newTokenOptional = this.tokenRepository.save(token);
                    this.publishOnRegistrationCompleteEvent(savedOptional.get(), newTokenOptional, requestLink);
                    return newTokenOptional.orElse(null);
                }
                return oldTokenOptional.get();
            }
        }

        person.setRole(this.loadRoleById(roleId));
        if (person.getRole().getId()!= EMPLOYEEID )
            person.setEnabled(true);
        else
            person.setEnabled(false);
        person.setPassword(passwordEncoder.encode(person.getPassword()));
        Optional<Person> personOptional = this.personRepository.save(person);

        if (personOptional.isPresent()) {
            Optional<Token> verificationTokenOptional = this.tokenRepository.save(this.createVerificationToken(person));
            this.publishOnRegistrationCompleteEvent(personOptional.get(), verificationTokenOptional, requestLink);
            return verificationTokenOptional.orElse(null);
        } else return null;
    }


    @Transactional
    @Override
    public List<Role> findAllRolesForAdminRegistration() throws Exception{
        List<Role> allRoles = this.roleRepository.findAllRoles();
        List<Role> allRolesForAdminRegistration = new ArrayList<>();
        for (Role role : allRoles){
            if (role.getId()!= EMPLOYEEID){
                allRolesForAdminRegistration.add(role);
            }
        }

        return allRolesForAdminRegistration;
     }

    @Transactional
    @Override
    public Person confirmEmail(String token) throws BadEmployeeException, ResourceNotFoundException {

        Token verificationToken = this.tokenRepository.findTokenByValueAndExpiredDate(token)
                .orElseThrow(() -> new BadEmployeeException(token));

        Person person = personRepository.findOne(verificationToken.getPerson().getId())
                .orElseThrow(() -> new ResourceNotFoundException(Person.TABLE_NAME));
        person.setEnabled(true);

        return personRepository.save(person).orElse(null);
    }



    private Role loadRoleByName(String roleName) throws ResourceNotFoundException {
        return this.roleRepository.findRoleByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException(Role.TABLE_NAME));
    }

    private Role loadRoleById(int roleId) throws ResourceNotFoundException {
        return this.roleRepository.findRoleById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException(Role.TABLE_NAME));
    }

    private Token createVerificationToken(Person person) {
        Token token = new Token();
        token.setTokenValue(this.generateToken());
        token.setDateExpired(this.calculateDateExpired());
        token.setTokenType(TokenType.REGISTRATION);
        token.setPerson(person);
        return token;
    }

    private Date calculateDateExpired() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, EXPIRATION);
        return new Date(cal.getTime().getTime());
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    private void publishOnRegistrationCompleteEvent(Person person, Optional<Token> verificationToken, String requestLink) {
        verificationToken.ifPresent((token) -> {
            if (person.getRole().getId() == EMPLOYEEID) {
                PersonRegistrationEvent event = new PersonRegistrationEvent(requestLink, person, token);
                eventPublisher.publishEvent(event);
            }else{
                ResetPasswordEvent event = new ResetPasswordEvent(requestLink, person, token);
                eventPublisher.publishEvent(event);
            }

        });

    }
}