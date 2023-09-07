package com.driver;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class WhatsappRepository {

    private int groupCount = 0;
    private int messageCount = 0;

    HashMap<String, User> userMap = new HashMap<>(); //key-mobile
    HashMap<Group, List<User>> groupMap = new HashMap<>(); //key - group name
    HashMap<Group, List<Message>> messageInGroup = new HashMap<>(); // key -group name
    HashMap<User, List<Message>> userMessageListMap = new HashMap<>(); // key - User
    List<Message> messageList = new ArrayList<>(); //list of messages

    public void createUser(String name, String mobile) throws Exception {
        if(userMap.containsKey(mobile)){
            throw new Exception("user already exists");
        }
        User user = new User(name, mobile);
        userMap.put(mobile, user);
    }

    public Group createGroup(List<User> users){
        if(users.size()==2){
            Group group = new Group(users.get(1).getName(),2);
            groupMap.put(group,users);
            return group;
        }
        Group group = new Group("Group "+ ++groupCount,users.size());
        groupMap.put(group,users);
        return group;
    }

    public int createMessage(String content){
        Message message = new Message(++messageCount, content);
        message.setTimestamp(new Date());
        messageList.add(message);
        return messageCount;
    }

    public int sendMessage(Message message,User sender,Group group)throws Exception{
        if(!groupMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        boolean checker = false;
        for(User user : groupMap.get(group)){
            if(user.equals(sender)){
                checker = true;
                break;
            }
        }
        if(!checker){
            throw new Exception("You are not allowed to send message");
        }

        if(messageInGroup.containsKey(group)){
            messageInGroup.get(group).add(message);
        }else{
            List<Message> messages = new ArrayList<>();
            messages.add(message);
            messageInGroup.put(group, messages);
        }

        if(userMessageListMap.containsKey(sender)){
            userMessageListMap.get(sender).add(message);
        }else{
            List<Message> messages = new ArrayList<>();
            messages.add(message);
            userMessageListMap.put(sender, messages);
        }

        return messageInGroup.get(group).size();
    }
    public void changeAdmin(User approver, User user, Group group)throws Exception{
        if(!groupMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        User pastAdmin = groupMap.get(group).get(0);
        if(!approver.equals(pastAdmin)){
            throw new Exception("Approver does not have rights");
        }
        boolean check= false;
        for(User user1 : groupMap.get(group)){
            if(user1.equals(user)){
                check = true;
            }
        }
        if(!check){
            throw new Exception("User is not a participant");
        }
        User newAdmin = null;
        Iterator<User> userIterator = groupMap.get(group).iterator();
        while(userIterator.hasNext()){
            User u = userIterator.next();
            if(u.equals(user)){
                newAdmin = u;
                userIterator.remove();
            }
        }
        groupMap.get(group).add(0,newAdmin);
    }

    public int removeUser(User user)throws Exception{

        boolean userFound = false;
        int groupSize = 0;
        int messageCount = 0;
        int overallMessageCount = messageList.size();
        Group groupToRemoveFrom = null;
        for (Map.Entry<Group, List<User>> entry : groupMap.entrySet()) {
            List<User> groupUsers = entry.getValue();
            if (groupUsers.contains(user))
            {
                userFound = true;
                groupToRemoveFrom = entry.getKey();
                if (groupUsers.get(0).equals(user))
                {
                    throw new Exception("Cannot remove admin");
                }
                groupUsers.remove(user);
                groupSize = groupUsers.size();
                break;
            }
        }
        if (!userFound)
        {
            throw new Exception("User not found");
        }

        if (userMessageListMap.containsKey(user))
        {
            messageCount = userMessageListMap.get(user).size() - 2;
            userMessageListMap.remove(user);
        }


        return groupSize + messageCount + overallMessageCount;

    }
    public String findMessage(Date start, Date end, int k) {

        return "Wait";
    }
}