package com.itamarprojects.bookorderapp.users;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequestMapping("/api/users")
@RestController
public class UserController
{
    private UserRepository userRepository;

    public UserController(UserRepository userRepository){
        this.userRepository = userRepository;
    }


    @GetMapping("")
    List<User> findAll(){
        return this.userRepository.findAll();
    }

    //temporary
    @GetMapping("/managers")
    List<User> findAllManagers() {return this.userRepository.findAllManagers(); }

    @GetMapping("/{id}")
    User findById(@PathVariable UUID id)
    {
        Optional<User> user= this.userRepository.findById(id);
        if (user.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return user.get();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void DeleteById(@PathVariable UUID id) { this.userRepository.delete(id);}

    @PatchMapping("/manager")
    void updateWorkersManager(@AuthenticationPrincipal User currentUser, @Valid @RequestBody UpdateManagerRequest managerId)
    {
        this.userRepository.updateWorkersManager(currentUser.getId(),managerId.getManagerId());
    }
}
