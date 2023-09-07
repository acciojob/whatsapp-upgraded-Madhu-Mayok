package com.driver;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WhatsappService {
    WhatsappRepository whatsappRepository = new WhatsappRepository();

    public void createUser(String name, String mobile) throws Exception{
        whatsappRepository.createUser(name, mobile);
    }
    public Group createGroup(List<User> user){
        Group group = whatsappRepository.createGroup(user);
        return group;
    }
    public int createMessage(String content){
        return whatsappRepository.createMessage(content);
    }
    public int sendMessage(Message message, User sender, Group group) throws Exception{
        return whatsappRepository.sendMessage(message, sender, group);
    }
    public void changeAdmin(User approver, User user, Group group)throws Exception{
        whatsappRepository.changeAdmin(approver, user, group);
    }
    public int removeUser(User user)throws Exception{
        return whatsappRepository.removeUser(user);
    }
}