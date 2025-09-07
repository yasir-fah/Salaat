package com.finalproject.tuwaiqfinal.Service;

import com.finalproject.tuwaiqfinal.Api.ApiException;
import com.finalproject.tuwaiqfinal.Model.User;
import com.finalproject.tuwaiqfinal.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public List<User> getAllUsers(){
        log.info("getting all users");
        return userRepository.findAll();
    }

    public User getUser(Integer id){
        log.info("get user:{}",id);
        return userRepository.findById(id)
                .orElseThrow(()->new ApiException("user not found"));
    }


    public void registerUser(User user){
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        log.info("user:{} registered",user.getId());
        userRepository.save(user);
    }

    public void updateUser(Integer id, User user){
        User user1 = userRepository.findById(id).orElseThrow(()->new ApiException("user not found"));
        user1.setUsername(user.getUsername());
        user1.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        log.info("user:{} updated", user1.getId());
        userRepository.save(user1);
    }

    public void deleteUser(Integer id){
        User user = userRepository.findById(id).orElseThrow(()-> new ApiException("user not found"));
        log.info("user:{} deleted",user.getId());
        userRepository.delete(user);
    }
}
