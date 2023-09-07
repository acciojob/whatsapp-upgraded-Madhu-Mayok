package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private HashMap<Group, List<User>> multipleAdminMap;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.multipleAdminMap = new HashMap<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public String createUser(String name, String mobile) throws Exception {
        //If the mobile number exists in database, throw "User already exists" exception
        //Otherwise, create the user and return "SUCCESS"

        if(userMobile.contains(mobile)){
            throw new Exception("User already exists");
        }

        User user = new User(name, mobile);
        userMobile.add(mobile);
        return "SUCCESS";
    }

    public Group createGroup(List<User> users){

        // The list contains at least 2 users where the first user is the admin. A group has exactly one admin.
        // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
        // If there are 2+ users, the name of group should be "Group count". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
        // Note that a personal chat is not considered a group and the count is not updated for personal chats.
        // If group is successfully created, return group.

        //For example: Consider userList1 = {Alex, Bob, Charlie}, userList2 = {Dan, Evan}, userList3 = {Felix, Graham, Hugh}.
        //If createGroup is called for these userLists in the same order, their group names would be "Group 1", "Evan", and "Group 2" respectively.

        String groupName = "";
        if(users.size()>2){
            this.customGroupCount++;
            groupName = "Group "+this.customGroupCount;
        }else{
            groupName = users.get(1).getName();
        }

        Group group = new Group(groupName, users.size());
        groupUserMap.put(group, users);
        adminMap.put(group, users.get(0));
        groupMessageMap.put(group, new ArrayList<Message>());

        return group;
    }

    public int createMessage(String content){
        // The 'i^th' created message has message id 'i'.
        // Return the message id.

        this.messageId++;
        Message message = new Message(messageId, content);
        return message.getId();
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.

        if(!groupUserMap.containsKey(group)) throw new Exception("Group does not exist");

        boolean isMember = false;
        List<User> userList = groupUserMap.get(group);
        for(User user : userList){
            if(user.equals(sender)) {
                isMember = true;
                break;
            }
        }

        if(isMember==false) throw new Exception("You are not allowed to send message");

        List<Message> messageList = new ArrayList<>();

        if(groupMessageMap.containsKey(group)) {
            messageList = groupMessageMap.get(group);
        }

        messageList.add(message);
        groupMessageMap.put(group, messageList);
        return messageList.size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS".
        // Note that at one time there is only one admin and the admin rights are transferred from approver to user.

        if(!groupUserMap.containsKey(group)) throw new Exception("Group does not exist");
        if(!adminMap.get(group).equals(approver)) throw new Exception("Approver does not have rights");

        boolean isMember = false;
        List<User> userList = groupUserMap.get(group);
        for(User user1 : userList){
            if(user1.equals(user)) {
                isMember = true;
                break;
            }
        }

        if(isMember==false) throw new Exception("User is not a participant");

        adminMap.put(group, user);
        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception{
        //This is a bonus problem and does not contains any marks
        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)
        boolean userFound = false;
        Group userGroup = null;
        for(Group group : groupUserMap.keySet()){
            if(groupUserMap.get(group).contains(user)){
                userGroup = group;
                if(adminMap.get(userGroup).equals(user)){
                    throw new Exception("Cannot remove admin");
                }
                userFound = true;
                break;
            }
        }

        if(userFound==false){
            throw new Exception("User not found");
        }

        List<User> userList = groupUserMap.get(userGroup);
        List<User> updatedUserList = new ArrayList<>();

        for(User user1 : userList){
            if(user1.equals(user)){
                continue;
            }
            updatedUserList.add(user1);
        }
        groupUserMap.put(userGroup, updatedUserList);

        List<Message> messageList = groupMessageMap.get(userGroup);
        List<Message> updatedMessageList = new ArrayList<>();

        for(Message message : messageList){
            if(senderMap.get(message).equals(user)){
                continue;
            }
            updatedMessageList.add(message);
        }
        groupMessageMap.put(userGroup, updatedMessageList);

        HashMap<Message, User> updatedSenderMap = new HashMap<>();
        for(Message message : senderMap.keySet()){
            if(senderMap.get(message).equals(user)){
                continue;
            }
            updatedSenderMap.put(message, senderMap.get(message));
        }

        senderMap = updatedSenderMap;

        return updatedUserList.size() + updatedMessageList.size() + updatedSenderMap.size();
    }

    public String findMessage(Date start, Date end, int K) throws Exception{

        List<Message> messageList = new ArrayList<>();
        for(Group group : groupUserMap.keySet()){
            messageList.addAll(groupMessageMap.get(group));
        }

        List<Message> filteredMessageList = new ArrayList<>();
        for(Message message : messageList){
            if(message.getTimestamp().after(start) && message.getTimestamp().before(end)){
                filteredMessageList.add(message);
            }
        }

        if(filteredMessageList.size()< K){
            throw new Exception("K is greater than the number of messages");
        }

        Collections.sort(filteredMessageList, new Comparator<Message>() {
            @Override
            public int compare(Message m1, Message m2) {
                return m2.getTimestamp().compareTo(m1.getTimestamp());
            }
        });

        return filteredMessageList.get(K-1).getContent();
    }

    public String deleteGroup(Group group, User user){

        if(!groupUserMap.containsKey(group)) return "not possible";

        if(!adminMap.get(group).equals(user)) return "not possible";

        groupUserMap.remove(group);
        groupMessageMap.remove(group);
        adminMap.remove(group);

        return "SUCCESS";
    }

    public String deleteMultipleMessages(Date start, Date end){
        List<Message> messageList = new ArrayList<>();
        for(Group group : groupUserMap.keySet()){
            messageList.addAll(groupMessageMap.get(group));
        }

        List<Message> updatedList = new ArrayList<>();
        for(Message message : messageList){
            if(message.getTimestamp().after(start) && message.getTimestamp().before(end)){
                continue;
            }
            updatedList.add(message);
        }

        for(Group group : groupUserMap.keySet()){
            List<Message> list = new ArrayList<>();
            list = groupMessageMap.get(group);
            List<Message> newList = new ArrayList<>();
            for(Message message : list){
                if(!updatedList.contains(message)){
                    continue;
                }
                newList.add(message);
            }
            groupMessageMap.put(group, newList);
        }

        return "Message Deleted Successfully";
    }

    // Multiple admin -> Created new HashMap -> HashMap<Group, List<User>> multipleAdminMap
    public String addAdmin(User approver, User user, Group group){

        if(!adminMap.get(group).equals(approver)){
            return "approver has no rights";
        }

        boolean isMember = false;
        List<User> userList  = groupUserMap.get(group);
        for(User user1 : userList){
            if(user1.equals(user)){
                isMember = true;
                break;
            }
        }

        if(isMember==false){
            return "user is not present in group";
        }

        List<User> adminList = new ArrayList<>();
        if(multipleAdminMap.containsKey(group)){
            adminList = multipleAdminMap.get(adminList);
        }

        adminList.add(user);
        adminList.add(approver);

        return "User is now admin";
    }
}